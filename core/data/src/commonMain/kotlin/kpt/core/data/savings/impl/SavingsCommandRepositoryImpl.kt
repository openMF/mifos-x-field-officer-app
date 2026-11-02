/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.savings.impl

import kotlinx.coroutines.CancellationException
import kpt.core.base.data.annotation.FromStore
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.mutation.CommandSpec
import kpt.core.base.store.mutation.MutationGateway
import kpt.core.base.store.mutation.MutationPolicy
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.savings.SavingsCommandRepository
import kpt.core.model.client.ClientAccounts
import kpt.core.model.group.GroupAccounts
import kpt.core.model.objects.account.loan.SavingsApproval
import kpt.core.model.objects.account.saving.SavingsActivationInput
import kpt.core.model.objects.account.saving.SavingsApplicationInput
import kpt.core.model.objects.account.saving.SavingsApprovalInput
import kpt.core.model.objects.account.saving.SavingsTransactionInput
import kpt.core.model.objects.payloads.ChargesPayload
import kpt.core.model.objects.payloads.SavingsPayload
import kpt.core.model.savings.FixedDepositPayload
import kpt.core.model.savings.SavingsAccountTransactionRequest
import kpt.core.model.savings.SavingsAccountWithAssociations
import kpt.core.model.savings.ShareAccountPayload
import kpt.core.network.mifos.savings.api.FixedDepositApi
import kpt.core.network.mifos.savings.api.SavingsAccountApi
import kpt.core.network.mifos.savings.api.ShareAccountApi
import kpt.core.store.config.AppStoreIds
import kpt.core.store.savings.SavingsAccountKey
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.impl.extensions.fresh

/**
 * Savings, fixed-deposit and share writes, through the single write door ([MutationGateway.command]).
 *
 * Every one of them changes state the SERVER computes — a balance and its transaction list, a
 * status cascade, a new account's number and summary row — none of which can be derived from the
 * request and its response. So [CommandSpec.localApply] ingests the authoritative record by reading
 * it through the store that owns it (`fresh`: network → source-of-truth writer → database), and the
 * read streams over those rows — the account screen, the client's / group's accounts list — re-emit
 * on their own.
 *
 * They used to end at the endpoint with `payload = Unit` and no local side: a deposit left the account
 * screen on its pre-deposit balance, and a new application was missing from the client's accounts until
 * something happened to refetch them.
 *
 * `OnlineRequired` — none of these is queued offline (offline capture is
 * [kpt.core.data.sync.OfflineQueueRepository]'s job) — so `localApply` runs only with the server's
 * result and an offline call is `Blocked` with nothing written.
 */
@RepositoryBinding(binds = SavingsCommandRepository::class)
internal class SavingsCommandRepositoryImpl(
    private val fixedDepositApi: FixedDepositApi,
    private val savingsAccountApi: SavingsAccountApi,
    private val shareAccountApi: ShareAccountApi,
    private val gateway: MutationGateway,
    @FromStore(AppStoreIds.SavingsAccounts)
    private val accountStore: Store<SavingsAccountKey, SavingsAccountWithAssociations>,
    @FromStore(AppStoreIds.ClientGetClientAccounts)
    private val clientAccountsStore: Store<Int, ClientAccounts>,
    @FromStore(AppStoreIds.GroupGetGroupAccounts)
    private val groupAccountsStore: Store<Int, GroupAccounts>,
) : SavingsCommandRepository {

    override suspend fun createFixedDepositAccount(fixedDepositPayload: FixedDepositPayload) =
        gateway.command(
            CommandSpec(
                payload = fixedDepositPayload,
                endpoint = { fixedDepositApi.createFixedDepositAccount(fixedDepositPayload = it) },
                localApply = { response ->
                    val clientId = fixedDepositPayload.clientId
                    if (response != null && clientId != null) ingest { clientAccountsStore.fresh(clientId) }
                },
            ),
            policy = MutationPolicy.OnlineRequired,
        )

    override suspend fun processTransaction(
        savingsAccountType: String,
        savingsAccountId: Int,
        transactionType: String,
        input: SavingsTransactionInput,
    ) = gateway.command(
        CommandSpec(
            payload = input,
            endpoint = {
                savingsAccountApi.processTransaction(
                    savingsAccountType = savingsAccountType,
                    savingsAccountId = savingsAccountId,
                    transactionType = transactionType,
                    savingsAccountTransactionRequest = it.toRequest(),
                )
            },
            // The response carries the transaction's id, not the running balance or the interest
            // the server re-posts — the account is read back whole.
            localApply = { response -> if (response != null) ingestAccount(savingsAccountType, savingsAccountId) },
        ),
        policy = MutationPolicy.OnlineRequired,
    )

    override suspend fun activateSavings(
        savingsAccountId: Int,
        input: SavingsActivationInput,
    ) = gateway.command(
        CommandSpec(
            payload = input,
            endpoint = {
                savingsAccountApi.activateSavings(
                    savingsAccountId = savingsAccountId,
                    // The endpoint's body is a string map, so the map is built HERE from a typed
                    // input rather than at the call site — which is what guarantees all three keys
                    // are present and spelled right.
                    genericRequest = hashMapOf(
                        "activatedOnDate" to it.activatedOnDate,
                        "locale" to it.locale,
                        "dateFormat" to it.dateFormat,
                    ),
                )
            },
            // Activation can post the minimum opening balance and charges, so status alone is not
            // the whole change. The endpoint is `savingsaccounts/...`, hence the fixed type.
            localApply = { response -> if (response != null) ingestAccount(SAVINGS_ACCOUNTS, savingsAccountId) },
        ),
        policy = MutationPolicy.OnlineRequired,
    )

    override suspend fun approveSavingsApplication(
        savingsAccountId: Int,
        input: SavingsApprovalInput,
    ) = gateway.command(
        CommandSpec(
            payload = input,
            endpoint = {
                savingsAccountApi.approveSavingsApplication(
                    savingsAccountId = savingsAccountId,
                    savingsApproval = SavingsApproval(
                        approvedOnDate = it.approvedOnDate,
                        locale = it.locale,
                        dateFormat = it.dateFormat,
                        note = it.note,
                    ),
                )
            },
            localApply = { response -> if (response != null) ingestAccount(SAVINGS_ACCOUNTS, savingsAccountId) },
        ),
        policy = MutationPolicy.OnlineRequired,
    )

    override suspend fun createSavingsAccount(
        input: SavingsApplicationInput,
    ): MutationResult<Unit> = gateway.command(
        CommandSpec(
            payload = input,
            endpoint = {
                savingsAccountApi.createSavingsAccount(savingsPayload = it.toPayload())
                Unit
            },
            // The owner's accounts list is where the new application appears, with the account
            // number Fineract assigns — so it is the list that is read back, under the owner the
            // payload was sent for.
            localApply = { response ->
                if (response != null) {
                    ingest {
                        if (input.isGroup) {
                            groupAccountsStore.fresh(input.ownerId)
                        } else {
                            clientAccountsStore.fresh(input.ownerId)
                        }
                    }
                }
            },
        ),
        policy = MutationPolicy.OnlineRequired,
    )

    override suspend fun createShareAccount(shareAccountPayload: ShareAccountPayload) =
        gateway.command(
            CommandSpec(
                payload = shareAccountPayload,
                endpoint = { shareAccountApi.createShareAccount(shareAccountPayload = it) },
                localApply = { response ->
                    if (response != null) ingest { clientAccountsStore.fresh(shareAccountPayload.clientId) }
                },
            ),
            policy = MutationPolicy.OnlineRequired,
        )

    /**
     * Read one account back through [accountStore], then its client's accounts list — the summary
     * row there carries the same balance and status.
     */
    private suspend fun ingestAccount(savingsAccountType: String, savingsAccountId: Int) = ingest {
        val account = accountStore.fresh(SavingsAccountKey(savingsAccountType, savingsAccountId))
        account.clientId?.let { clientAccountsStore.fresh(it) }
    }

    private companion object {
        const val SAVINGS_ACCOUNTS = "savingsaccounts"
    }
}

/**
 * Runs a read-back of a write the server has ALREADY accepted.
 *
 * A failure here must not reach the gateway: `localApply` throwing turns the command into `Failed`,
 * and an officer told a deposit failed takes the cash again. The rows keep their previous value and
 * the read store's own next fetch corrects them. Shared with the recurring-deposit create.
 */
internal suspend fun ingest(read: suspend () -> Unit) {
    runCatching { read() }.onFailure { if (it is CancellationException) throw it }
}

/**
 * The `core/model` input -> Room-entity request body the savings transaction endpoint takes.
 *
 * `SavingsAccountTransactionRequest` is the wire type the API declares, so the mapping lives
 * at this boundary. `id`/`savingAccountId`/`savingsAccountType`/`transactionType` are left unset:
 * they are STORAGE columns on that entity (it doubles as the offline-capture row), and the request
 * carries the account and the command in its path and query instead.
 *
 * `errorMessage` is likewise storage — a request body has no business carrying one.
 */
private fun SavingsTransactionInput.toRequest() = SavingsAccountTransactionRequest(
    transactionDate = transactionDate,
    transactionAmount = transactionAmount,
    locale = locale,
    dateFormat = dateFormat,
    paymentTypeId = paymentTypeId?.toString(),
    note = note,
    accountNumber = accountNumber,
    checkNumber = checkNumber,
    routingCode = routingCode,
    receiptNumber = receiptNumber,
    bankNumber = bankNumber,
)

/**
 * The `core/model` input -> network payload boundary for a savings application.
 *
 * The owner lands on EXACTLY ONE of `clientId` / `groupId`, chosen by [SavingsApplicationInput.isGroup].
 * The original set `clientId` unconditionally and never set `groupId`, so a group application
 * carried the wrong owner field — the route's `groupId` and `isGroupAccount` were both dropped in
 * the ViewModel.
 */
private fun SavingsApplicationInput.toPayload() = SavingsPayload().apply {
    productId = this@toPayload.productId
    clientId = this@toPayload.ownerId.takeUnless { isGroup }
    groupId = this@toPayload.ownerId.takeIf { isGroup }
    locale = this@toPayload.locale
    dateFormat = this@toPayload.dateFormat
    submittedOnDate = this@toPayload.submittedOnDate
    fieldOfficerId = this@toPayload.fieldOfficerId
    externalId = this@toPayload.externalId
    minRequiredOpeningBalance = this@toPayload.minRequiredOpeningBalance
    minRequiredBalance = this@toPayload.minRequiredBalance
    lockinPeriodFrequency = this@toPayload.lockinPeriodFrequency
    lockinPeriodFrequencyType = this@toPayload.lockinPeriodFrequencyType
    interestCompoundingPeriodType = this@toPayload.interestCompoundingPeriodType
    interestPostingPeriodType = this@toPayload.interestPostingPeriodType
    interestCalculationType = this@toPayload.interestCalculationType
    interestCalculationDaysInYearType = this@toPayload.interestCalculationDaysInYearType
    allowOverdraft = this@toPayload.allowOverdraft
    enforceMinRequiredBalance = this@toPayload.enforceMinRequiredBalance
    charges = this@toPayload.charges.map { charge ->
        // The charge's own date/format pair, from the same helper the application uses — the
        // original left both null on every charge while setting them on the application.
        ChargesPayload().apply {
            chargeId = charge.chargeId
            amount = charge.amount
            locale = this@toPayload.locale
            dateFormat = this@toPayload.dateFormat
        }
    }
}

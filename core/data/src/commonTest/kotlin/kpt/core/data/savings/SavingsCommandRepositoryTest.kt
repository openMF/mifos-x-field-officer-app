/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.savings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.fieldofficer.infra.OkHttpResponse
import kpt.core.data.infra.testMutationGateway
import kpt.core.data.savings.impl.SavingsCommandRepositoryImpl
import kpt.core.model.client.ClientAccounts
import kpt.core.model.group.GroupAccounts
import kpt.core.model.objects.account.loan.SavingsApproval
import kpt.core.model.objects.account.saving.SavingsAccountTransactionResponse
import kpt.core.model.objects.account.saving.SavingsActivationInput
import kpt.core.model.objects.account.saving.SavingsApplicationInput
import kpt.core.model.objects.account.saving.SavingsApprovalInput
import kpt.core.model.objects.account.saving.SavingsTransactionInput
import kpt.core.model.objects.payloads.SavingsPayload
import kpt.core.model.savings.FixedDepositPayload
import kpt.core.model.savings.SavingsAccount
import kpt.core.model.savings.SavingsAccountStatus
import kpt.core.model.savings.SavingsAccountSummary
import kpt.core.model.savings.SavingsAccountTransactionRequest
import kpt.core.model.savings.SavingsAccountWithAssociations
import kpt.core.model.savings.ShareAccountPayload
import kpt.core.model.shared.GenericResponse
import kpt.core.network.mifos.savings.api.FixedDepositApi
import kpt.core.network.mifos.savings.api.SavingsAccountApi
import kpt.core.network.mifos.savings.api.ShareAccountApi
import kpt.core.store.savings.SavingsAccountKey
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.StoreBuilder
import org.mobilenativefoundation.store.store5.impl.extensions.fresh
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kpt.core.model.client.ClientAccounts as ClientAccountsModel

/**
 * The savings commands over REAL Store5 read stores wired the way `core/store` wires them, each over
 * an in-memory "table". A command's local effect must land in the rows those stores' readers
 * observe — the account screen and the owner's accounts list — with nothing refreshed by a caller.
 */
class SavingsCommandRepositoryTest {

    /** The server. `readsFail` makes every GET throw, as a connection dropping after the POST does. */
    private var readsFail = false
    private val serverAccounts = mutableMapOf(1 to account(balance = 100.0, status = "Approved"))
    private val serverClientAccounts =
        mutableMapOf(CLIENT to ClientAccounts(savingsAccounts = listOf(SavingsAccount(id = 1))))
    private val serverGroupAccounts = mutableMapOf<Int, GroupAccounts>()
    private var posts = 0

    /** The tables: `SavingsAccountWithAssociations`, and the read cache rows of the two lists. */
    private val accounts = MutableStateFlow<Map<Int, SavingsAccountWithAssociations>>(emptyMap())
    private val clientAccounts = MutableStateFlow<Map<Int, ClientAccountsModel>>(emptyMap())
    private val groupAccounts = MutableStateFlow<Map<Int, GroupAccounts>>(emptyMap())

    /** A server-side record — the row type the API returns. */
    private fun account(balance: Double, status: String) = SavingsAccountWithAssociations(
        id = 1,
        clientId = CLIENT,
        status = SavingsAccountStatus(value = status),
        summary = SavingsAccountSummary(accountBalance = balance),
    )

    private fun <T> read(value: () -> T): T {
        check(!readsFail) { "connection dropped" }
        return value()
    }

    // ---- core/store, as provided in SavingsAccountStore.kt / the cachedRead list stores ----

    private val accountStore = StoreBuilder.from(
        fetcher = Fetcher.of { key: SavingsAccountKey -> read { serverAccounts.getValue(key.savingsAccountId) } },
        sourceOfTruth = SourceOfTruth.of(
            reader = { key: SavingsAccountKey -> accounts.map { it[key.savingsAccountId] } },
            writer = { _: SavingsAccountKey, a: SavingsAccountWithAssociations -> accounts.value += a.id!! to a },
        ),
    ).build()

    private val clientAccountsStore = StoreBuilder.from(
        fetcher = Fetcher.of { clientId: Int -> read { serverClientAccounts.getValue(clientId) } },
        sourceOfTruth = SourceOfTruth.of(
            reader = { clientId: Int -> clientAccounts.map { it[clientId] } },
            writer = { clientId: Int, list: ClientAccountsModel -> clientAccounts.value += clientId to list },
        ),
    ).build()

    private val groupAccountsStore = StoreBuilder.from(
        fetcher = Fetcher.of { groupId: Int -> read { serverGroupAccounts.getValue(groupId) } },
        sourceOfTruth = SourceOfTruth.of(
            reader = { groupId: Int -> groupAccounts.map { it[groupId] } },
            writer = { groupId: Int, list: GroupAccounts -> groupAccounts.value += groupId to list },
        ),
    ).build()

    // ---- core/network: each POST changes the server the way Fineract would ----

    /** A new application under [owner] — appended to the server's list for that owner. */
    private fun opened(owner: Int, id: Int) {
        posts++
        val current = serverClientAccounts[owner] ?: ClientAccounts()
        serverClientAccounts[owner] = current.copy(savingsAccounts = current.savingsAccounts + SavingsAccount(id = id))
    }

    private val savingsApi = object : SavingsAccountApi {
        override suspend fun getSavingsAccountWithAssociations(
            savingsAccountType: String,
            savingsAccountId: Int,
            association: String?,
        ) =
            error("reads go through the store")
        override suspend fun getSavingsAccountTransactionTemplate(
            savingsAccountType: String,
            savingsAccountId: Int,
            transactionType: String?,
        ) =
            error("reads go through the store")

        override suspend fun processTransaction(
            savingsAccountType: String,
            savingsAccountId: Int,
            transactionType: String?,
            savingsAccountTransactionRequest: SavingsAccountTransactionRequest?,
        ): SavingsAccountTransactionResponse {
            posts++
            val amount = savingsAccountTransactionRequest!!.transactionAmount!!.toDouble()
            val before = serverAccounts.getValue(savingsAccountId)
            serverAccounts[savingsAccountId] = before.copy(
                summary = before.summary!!.copy(accountBalance = before.summary!!.accountBalance!! + amount),
            )
            return SavingsAccountTransactionResponse(resourceId = 99)
        }

        override suspend fun activateSavings(
            savingsAccountId: Int,
            genericRequest: HashMap<String, String>,
        ): GenericResponse {
            posts++
            serverAccounts[savingsAccountId] = serverAccounts.getValue(savingsAccountId)
                .copy(status = SavingsAccountStatus(value = "Active"))
            return GenericResponse()
        }

        override suspend fun approveSavingsApplication(
            savingsAccountId: Int,
            savingsApproval: SavingsApproval?,
        ): GenericResponse {
            posts++
            serverAccounts[savingsAccountId] = serverAccounts.getValue(savingsAccountId)
                .copy(status = SavingsAccountStatus(value = "Approved"))
            return GenericResponse()
        }

        override suspend fun allSavingsAccounts() = error("reads go through the store")

        override suspend fun createSavingsAccount(savingsPayload: SavingsPayload?) = OkHttpResponse.also {
            val groupId = savingsPayload!!.groupId
            if (groupId != null) {
                posts++
                serverGroupAccounts[groupId] = GroupAccounts(savingsAccounts = listOf(SavingsAccount(id = 20)))
            } else {
                opened(savingsPayload.clientId!!, 20)
            }
        }

        override suspend fun savingsAccountTemplate() = error("reads go through the store")
        override suspend fun getClientSavingsAccountTemplateByProduct(
            clientId: Int,
            productId: Int,
        ) = error("reads go through the store")
        override suspend fun getGroupSavingsAccountTemplateByProduct(
            groupId: Int,
            productId: Int,
        ) = error("reads go through the store")
    }

    private val fixedDepositApi = object : FixedDepositApi {
        override suspend fun fixedDepositProductTemplate(
            clientId: Int,
            productId: Int?,
        ) = error("reads go through the store")
        override suspend fun createFixedDepositAccount(fixedDepositPayload: FixedDepositPayload) =
            OkHttpResponse.also { opened(fixedDepositPayload.clientId!!, 30) }
    }

    private val shareApi = object : ShareAccountApi {
        override suspend fun shareProductTemplate(clientId: Int, productId: Int?) = error("reads go through the store")
        override suspend fun createShareAccount(shareAccountPayload: ShareAccountPayload) =
            OkHttpResponse.also { opened(shareAccountPayload.clientId, 40) }
    }

    private fun commands(online: Boolean = true) = SavingsCommandRepositoryImpl(
        fixedDepositApi = fixedDepositApi,
        savingsAccountApi = savingsApi,
        shareAccountApi = shareApi,
        gateway = testMutationGateway(isOnline = online),
        accountStore = accountStore,
        clientAccountsStore = clientAccountsStore,
        groupAccountsStore = groupAccountsStore,
    )

    private val deposit = SavingsTransactionInput(
        transactionDate = "23 October 2026",
        transactionAmount = "50",
        locale = "en",
        dateFormat = "dd MMMM yyyy",
    )

    private fun application(isGroup: Boolean) = SavingsApplicationInput(
        ownerId = if (isGroup) GROUP else CLIENT,
        isGroup = isGroup,
        productId = 1,
        submittedOnDate = "23 October 2026",
        locale = "en",
        dateFormat = "dd MMMM yyyy",
    )

    private fun clientAccountIds() = clientAccounts.value[CLIENT]?.savingsAccounts?.map { it.id }

    // ---- writes land in the tables the reads observe ----

    @Test
    fun aDepositLandsTheServersBalanceInTheAccountRow() = runTest {
        val result = commands().processTransaction("savingsaccounts", 1, "deposit", deposit)

        assertEquals(99, assertIs<MutationResult.Applied<SavingsAccountTransactionResponse>>(result).value.resourceId)
        // The balance is the SERVER's — read back through the account store, never computed here.
        assertEquals(150.0, accounts.value[1]?.summary?.accountBalance)
        // And the owner's accounts list, whose summary row shows the same balance.
        assertEquals(listOf(1), clientAccountIds())
    }

    @Test
    fun activationLandsTheNewStatus() = runTest {
        assertIs<MutationResult.Applied<GenericResponse>>(
            commands().activateSavings(1, SavingsActivationInput("23 October 2026", "en", "dd MMMM yyyy")),
        )

        assertEquals("Active", accounts.value[1]?.status?.value)
    }

    @Test
    fun approvalLandsTheNewStatus() = runTest {
        serverAccounts[1] = account(balance = 0.0, status = "Submitted and pending approval")

        commands().approveSavingsApplication(1, SavingsApprovalInput("23 October 2026", "en", "dd MMMM yyyy"))

        assertEquals("Approved", accounts.value[1]?.status?.value)
    }

    @Test
    fun aClientApplicationAppearsInTheClientsAccounts() = runTest {
        assertIs<MutationResult.Applied<Unit>>(commands().createSavingsAccount(application(isGroup = false)))

        assertEquals(listOf(1, 20), clientAccountIds())
    }

    @Test
    fun aGroupApplicationAppearsInTheGroupsAccountsNotAClients() = runTest {
        commands().createSavingsAccount(application(isGroup = true))

        assertEquals(listOf(20), groupAccounts.value[GROUP]?.savingsAccounts?.map { it.id })
        assertTrue(clientAccounts.value.isEmpty())
    }

    @Test
    fun fixedDepositAndShareApplicationsAppearInTheClientsAccounts() = runTest {
        commands().createFixedDepositAccount(FixedDepositPayload(clientId = CLIENT))
        commands().createShareAccount(
            ShareAccountPayload(
                clientId = CLIENT,
                requestedShares = 1,
                submittedDate = "23 October 2026",
                applicationDate = "23 October 2026",
                allowDividendCalculationForInactiveClients = false,
                savingsAccountId = 1,
            ),
        )

        assertEquals(listOf(1, 30, 40), clientAccountIds())
    }

    // ---- the failure arms ----

    @Test
    fun offlineEveryWriteIsBlockedAndNothingIsWritten() = runTest {
        val offline = commands(online = false)

        assertIs<MutationResult.Blocked>(offline.processTransaction("savingsaccounts", 1, "deposit", deposit))
        assertIs<MutationResult.Blocked>(offline.createSavingsAccount(application(isGroup = false)))

        assertEquals(0, posts)
        assertTrue(accounts.value.isEmpty() && clientAccounts.value.isEmpty())
    }

    @Test
    fun aDepositTheServerAcceptedIsAppliedEvenWhenTheReadBackFails() = runTest {
        accountStore.fresh(SavingsAccountKey("savingsaccounts", 1))
        readsFail = true

        // Reporting Failed here would have the officer take the cash a second time.
        assertIs<MutationResult.Applied<SavingsAccountTransactionResponse>>(
            commands().processTransaction("savingsaccounts", 1, "deposit", deposit),
        )
        assertEquals(1, posts)
        // The row keeps its last-known value until the store's own next fetch.
        assertEquals(100.0, accounts.value[1]?.summary?.accountBalance)
    }

    private companion object {
        const val CLIENT = 7
        const val GROUP = 8
    }
}

/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.recurringdeposit.impl

import kpt.core.base.data.annotation.FromStore
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.mutation.CommandSpec
import kpt.core.base.store.mutation.MutationGateway
import kpt.core.base.store.mutation.MutationPolicy
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.recurringdeposit.RecurringDepositCommandRepository
import kpt.core.data.savings.impl.ingest
import kpt.core.model.client.ClientAccounts
import kpt.core.model.objects.payloads.ChargeItem
import kpt.core.model.objects.payloads.RecurringDepositAccountPayload
import kpt.core.model.recurringdeposit.RecurringDepositApplicationInput
import kpt.core.network.mifos.recurringdeposit.api.RecurringAccountApi
import kpt.core.store.config.AppStoreIds
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.impl.extensions.fresh

/**
 * The recurring-deposit application, through the single write door ([MutationGateway.command]).
 *
 * The new account shows up in the client's accounts list with a number and status Fineract
 * assigns, so [CommandSpec.localApply] ingests that list through its store rather than composing a
 * row it cannot know; the list's read stream re-emits on its own. It used to end at the endpoint
 * with `payload = Unit` and no local side, leaving the application missing from the client until
 * something refetched.
 *
 * `OnlineRequired`: offline it is `Blocked` and writes nothing.
 */
@RepositoryBinding(binds = RecurringDepositCommandRepository::class)
internal class RecurringDepositCommandRepositoryImpl(
    private val recurringAccountApi: RecurringAccountApi,
    private val gateway: MutationGateway,
    @FromStore(AppStoreIds.ClientGetClientAccounts)
    private val clientAccountsStore: Store<Int, ClientAccounts>,
) : RecurringDepositCommandRepository {

    override suspend fun createRecurringDepositAccount(
        input: RecurringDepositApplicationInput,
    ): MutationResult<Unit> = gateway.command(
        CommandSpec(
            payload = input,
            endpoint = {
                recurringAccountApi.createRecurringDepositAccount(recurringDepositAccountPayload = it.toPayload())
                Unit
            },
            localApply = { response -> if (response != null) ingest { clientAccountsStore.fresh(input.clientId) } },
        ),
        policy = MutationPolicy.OnlineRequired,
    )
}

/**
 * The `core/model` input -> network payload boundary.
 *
 * `monthDayFormat` and `expectedFirstDepositOnDate` are left unset: the form collects neither, and
 * the original hardcoded `expectedFirstDepositOnDate = null` for the same reason.
 *
 * Each charge's amount is parsed to the `Double` the payload declares. A row that does not parse is
 * dropped rather than sent as 0 — the form refuses to add one, so this is a type boundary rather
 * than a validation point.
 */
private fun RecurringDepositApplicationInput.toPayload() = RecurringDepositAccountPayload(
    clientId = clientId,
    productId = productId,
    submittedOnDate = submittedOnDate,
    locale = locale,
    dateFormat = dateFormat,
    mandatoryRecommendedDepositAmount = mandatoryRecommendedDepositAmount,
    recurringFrequency = recurringFrequency,
    recurringFrequencyType = recurringFrequencyType,
    depositPeriod = depositPeriod,
    depositPeriodFrequencyId = depositPeriodFrequencyId,
    fieldOfficerId = fieldOfficerId,
    externalId = externalId,
    isMandatoryDeposit = isMandatoryDeposit,
    adjustAdvanceTowardsFuturePayments = adjustAdvanceTowardsFuturePayments,
    allowWithdrawal = allowWithdrawal,
    interestCompoundingPeriodType = interestCompoundingPeriodType,
    interestPostingPeriodType = interestPostingPeriodType,
    interestCalculationType = interestCalculationType,
    interestCalculationDaysInYearType = interestCalculationDaysInYearType,
    lockinPeriodFrequency = lockinPeriodFrequency,
    lockinPeriodFrequencyType = lockinPeriodFrequencyType,
    preClosurePenalApplicable = preClosurePenalApplicable,
    preClosurePenalInterest = preClosurePenalInterest,
    preClosurePenalInterestOnTypeId = preClosurePenalInterestOnTypeId,
    charges = charges.mapNotNull { charge ->
        ChargeItem(chargeId = charge.chargeId, amount = charge.amount.toDoubleOrNull() ?: return@mapNotNull null)
    },
)

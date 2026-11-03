/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.draft

import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.submit.draft.FormDraft
import kpt.core.base.store.submit.draft.FormDraftJson.payloadAs
import kpt.core.data.center.CenterCommandRepository
import kpt.core.data.client.ClientCommandRepository
import kpt.core.data.group.GroupCommandRepository
import kpt.core.data.loan.LoanCommandRepository
import kpt.core.data.recurringdeposit.RecurringDepositCommandRepository
import kpt.core.data.savings.SavingsCommandRepository
import kpt.core.model.center.CenterPayload
import kpt.core.model.client.ClientPayload
import kpt.core.model.draft.AddressDraftPayload
import kpt.core.model.draft.CreateForm
import kpt.core.model.group.GroupPayload
import kpt.core.model.objects.account.loan.LoanApplicationInput
import kpt.core.model.objects.account.saving.SavingsApplicationInput
import kpt.core.model.objects.clients.CollateralPayload
import kpt.core.model.objects.clients.UpdateSavingsAccountRequest
import kpt.core.model.objects.error.serverErrorMessage
import kpt.core.model.objects.payloads.GroupLoanPayload
import kpt.core.model.recurringdeposit.RecurringDepositApplicationInput
import kpt.core.model.savings.FixedDepositPayload
import kpt.core.model.savings.ShareAccountPayload

/**
 * Sends a kept create the way its form would have: decodes [FormDraft.payload] with the form's own
 * payload type and calls the same command repository the form calls.
 *
 * THROWS unless the write was applied — the syncer marks a draft SUBMITTED when this returns, so a
 * swallowed refusal would delete a draft that never reached the server (the note replay's rule).
 * The client photo is not replayed: a draft keeps the record, not the image upload that follows it.
 */
class CreateDraftSubmitter(
    private val clients: ClientCommandRepository,
    private val groups: GroupCommandRepository,
    private val centers: CenterCommandRepository,
    private val loans: LoanCommandRepository,
    private val savings: SavingsCommandRepository,
    private val recurring: RecurringDepositCommandRepository,
) {
    /** Sends `draft` to the server through the command repository for its form. */
    suspend fun submit(draft: FormDraft) {
        val owner = draft.ownerId?.toInt()
        val result = send(draft, owner)
        when (result) {
            is MutationResult.Applied -> Unit
            is MutationResult.Failed -> throw IllegalStateException(
                result.cause.serverErrorMessage() ?: result.cause.message,
                result.cause,
            )
            else -> error("draft ${draft.form} not sent: $result")
        }
    }

    private suspend fun send(draft: FormDraft, owner: Int?): MutationResult<*> =
        when (CreateForm.fromKey(draft.form) ?: error("unknown draft form ${draft.form}")) {
            CreateForm.CLIENT -> clients.createClient(draft.payloadAs(ClientPayload.serializer()))
            CreateForm.GROUP -> groups.createGroup(draft.payloadAs(GroupPayload.serializer()))
            CreateForm.CENTER -> centers.createCenter(draft.payloadAs(CenterPayload.serializer()))
            CreateForm.LOAN -> loans.createLoanAccount(draft.payloadAs(LoanApplicationInput.serializer()))
            CreateForm.GROUP_LOAN -> loans.createGroupLoanAccount(draft.payloadAs(GroupLoanPayload.serializer()))
            CreateForm.SAVINGS -> savings.createSavingsAccount(draft.payloadAs(SavingsApplicationInput.serializer()))
            CreateForm.ADDRESS -> draft.payloadAs(AddressDraftPayload.serializer())
                .let { clients.createClientAddress(it.clientId, it.addressTypeId, it.request) }
            CreateForm.FIXED_DEPOSIT ->
                savings.createFixedDepositAccount(draft.payloadAs(FixedDepositPayload.serializer()))
            CreateForm.RECURRING_DEPOSIT ->
                recurring.createRecurringDepositAccount(draft.payloadAs(RecurringDepositApplicationInput.serializer()))
            CreateForm.SHARE -> savings.createShareAccount(draft.payloadAs(ShareAccountPayload.serializer()))
            CreateForm.COLLATERAL, CreateForm.DEFAULT_ACCOUNT -> sendClientOwned(draft, requireNotNull(owner))
        }

    /** The two forms that write under an existing client. */
    private suspend fun sendClientOwned(draft: FormDraft, clientId: Int): MutationResult<*> =
        if (CreateForm.fromKey(draft.form) == CreateForm.COLLATERAL) {
            clients.createCollateral(clientId, draft.payloadAs(CollateralPayload.serializer()))
        } else {
            clients.updateSavingsAccount(clientId, draft.payloadAs(UpdateSavingsAccountRequest.serializer()))
        }
}

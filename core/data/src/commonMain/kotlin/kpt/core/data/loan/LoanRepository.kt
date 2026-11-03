/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.loan

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.model.loan.LoanRepaymentTemplate
import kpt.core.model.loan.LoanTemplate
import kpt.core.model.objects.account.loan.loanDisburse.LoanDisburseTemplate
import kpt.core.model.objects.account.loan.loanWithAssociations.LoanWithAssociations
import kpt.core.model.objects.account.loan.reschedules.LoanRescheduleResponse
import kpt.core.model.objects.account.loan.reschedules.LoanRescheduleTemplate
import kpt.core.model.objects.account.loan.transfer.AccountTransferTemplate
import kpt.core.model.objects.organisations.LoanProducts
import kpt.core.model.objects.template.loan.GroupLoanTemplate

/** Reads loans, their schedules, transactions and templates, from the cache and the server. */
interface LoanRepository {
    /** One loan with its full associations — summary, timeline, schedule, transactions. */
    fun loanStream(loanId: Int, scope: CoroutineScope): ScreenDataStream<LoanWithAssociations>

    /**
     * The loan-application template for a (client, product).
     *
     * Cached: the officer fills this form in the field, so it has to render with no signal —
     * otherwise the offline capture queue has nothing to capture.
     */
    fun loanTemplateStream(clientId: Int, productId: Int?, scope: CoroutineScope): ScreenDataStream<LoanTemplate>

    /** Payment-type options a repayment form needs, per loan. */
    fun repaymentTemplateStream(loanId: Int, scope: CoroutineScope): ScreenDataStream<LoanRepaymentTemplate>

    /** Expected amounts, payment types and dates for a disbursement, per loan. */
    fun disburseTemplateStream(loanId: Int, scope: CoroutineScope): ScreenDataStream<LoanDisburseTemplate>

    /**
     * The account-transfer template: which offices, clients, account types and accounts the
     * officer may transfer TO, given what they have chosen so far.
     *
     * ## A suspend one-shot, not a `ScreenDataStream`
     *
     * This is a cascading QUERY, not a subscription. Each selection — office, then client, then
     * account type — narrows the next level, so the server is asked a DIFFERENT question every
     * time and the answer to the previous one is of no further interest. A `ScreenDataStream` is
     * keyed at construction, so modelling this as a stream would mean constructing a new one per
     * selection and leaking the collector of each.
     *
     * Returns null on failure; the caller decides what to say about it. The store still caches
     * each distinct question, so stepping back up the cascade is served from cache.
     */
    suspend fun accountTransferTemplate(
        fromClientId: Int,
        fromAccountId: Int,
        fromAccountType: Int,
        fromOfficeId: Int?,
        toOfficeId: Int? = null,
        toClientId: Int? = null,
        toAccountType: Int? = null,
    ): AccountTransferTemplate?

    /**
     * The tenant's loan PRODUCTS — a catalog, shared across every loan, hence no key.
     *
     * A STREAM rather than a one-shot, unlike the templates: the create-loan screens open on this
     * list, so it is the screen's subject and wants the substrate's Loading / NoNetwork / Error
     * states rather than a nullable return.
     */
    fun loanProductsStream(scope: CoroutineScope): ScreenDataStream<List<LoanProducts>>

    /**
     * The group-loan template for one (group, product) pair.
     *
     * A suspend one-shot: the create screen asks again each time the officer picks a different
     * product, which is the same cascading shape as [accountTransferTemplate].
     */
    suspend fun groupLoanTemplate(groupId: Int, productId: Int): GroupLoanTemplate?

    /**
     * The INDIVIDUAL loan-application template for one (client, product) pair.
     *
     * A one-shot rather than [loanTemplateStream], for the same reason `groupLoanTemplate` is: the
     * new-application wizard re-asks every time the officer changes product, and a
     * [ScreenDataStream] is keyed at CONSTRUCTION — cascading through it would leak a collector per
     * selection. `null` when the read fails, so the caller can offer retry without a crash.
     */
    suspend fun loanTemplate(clientId: Int, productId: Int): LoanTemplate?

    /**
     * Every reschedule proposed on one loan, with its status and timeline.
     *
     * A STREAM, unlike the two template reads: the list is the screen's subject and changes when
     * the officer approves or rejects a row, so it has to re-read.
     */
    fun loanReschedulesStream(loanId: Int, scope: CoroutineScope): ScreenDataStream<List<LoanRescheduleResponse>>

    /**
     * The tenant's reschedule REASONS — a catalog, shared across loans, hence no key.
     *
     * A suspend one-shot like the other two templates: the propose form asks once when it opens.
     */
    suspend fun loanRescheduleReasons(): LoanRescheduleTemplate?
}

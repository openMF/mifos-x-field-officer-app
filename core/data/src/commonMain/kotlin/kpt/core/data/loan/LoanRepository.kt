/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.loan

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.database.loan.entity.LoanRepaymentTemplateEntity
import kpt.core.database.loan.entity.LoanTemplate
import kpt.core.model.objects.account.loan.loanDisburse.LoanDisburseTemplate
import kpt.core.model.objects.account.loan.loanWithAssociations.LoanWithAssociations

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
    fun repaymentTemplateStream(loanId: Int, scope: CoroutineScope): ScreenDataStream<LoanRepaymentTemplateEntity>

    /** Expected amounts, payment types and dates for a disbursement, per loan. */
    fun disburseTemplateStream(loanId: Int, scope: CoroutineScope): ScreenDataStream<LoanDisburseTemplate>
}

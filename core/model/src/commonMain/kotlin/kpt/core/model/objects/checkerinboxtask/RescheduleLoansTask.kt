/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.checkerinboxtask

import kotlinx.serialization.Serializable
@Serializable
// Defaults on every field: this decodes Fineract's `LoanRescheduleRequestData` for
// `rescheduleloans?command=pending`, which carries no `actionName` and a null reason when none was
// given — one such request must not fail the whole pending list.
/** A reschedule loans task. */
data class RescheduleLoansTask(
    /** The server id of this record. */
    var id: Int,
    /** The loan the request reschedules — what opens its reschedule screen (approve / reject live there). */
    var loanId: Int? = null,
    /** The client's display name. */
    var clientName: String = "",
    /** The account number of the loan. */
    var loanAccountNumber: String = "",
    /** The installment date the reschedule starts from. */
    var rescheduleFromDate: List<Int> = emptyList(),
    /** The action the task or permission covers (create, approve, …). */
    var actionName: String = "",
    /** The reason given for the reschedule. */
    var rescheduleReasonCodeValue: RescheduleReasonCodeValue? = null,
)

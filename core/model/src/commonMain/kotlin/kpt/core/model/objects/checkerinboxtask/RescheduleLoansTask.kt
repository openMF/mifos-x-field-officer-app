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
data class RescheduleLoansTask(
    var id: Int,
    /** The loan the request reschedules — what opens its reschedule screen (approve / reject live there). */
    var loanId: Int? = null,
    var clientName: String = "",
    var loanAccountNumber: String = "",
    var rescheduleFromDate: List<Int> = emptyList(),
    var actionName: String = "",
    var rescheduleReasonCodeValue: RescheduleReasonCodeValue? = null,
)

/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.account.loan.loanWithAssociations

/** An actual disbursement date. */
data class ActualDisbursementDate(
    /** The server id of the loan. */
    val loanId: Int? = null,
    /** The year. */
    val year: Int? = null,
    /** The month (1–12). */
    val month: Int? = null,
    /** The day of the month. */
    val date: Int? = null,
)

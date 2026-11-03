/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.loan
import kotlinx.serialization.Serializable

/** Server response to a reject loan request. */
@Serializable
data class RejectLoanResponseDto(
    /** The server id of the office. */
    val officeId: Int,
    /** The server id of the client. */
    val clientId: Int,
    /** The server id of the loan. */
    val loanId: Int,
    /** The server id of the resource the request created or changed. */
    val resourceId: Int,
)

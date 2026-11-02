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

@Serializable
data class RejectLoanResponseDto(
    val officeId: Int,
    val clientId: Int,
    val loanId: Int,
    val resourceId: Int,
)

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

/** Request body for a loan charge off. */
@Serializable
data class LoanChargeOffRequestDto(
    /** The server id of the charge-off reason. */
    val chargeOffReasonId: Int,
    /** The date of the transaction. */
    val transactionDate: String,
    /** The id an external system uses for this record, if one was set. */
    val externalId: String? = null,
    /** A free-text note. */
    val note: String? = null,
    /** The locale Fineract parses the dates and numbers in this request with (e.g. `en`). */
    val locale: String,
    /** The pattern Fineract parses this request's dates with (e.g. `dd MMMM yyyy`). */
    val dateFormat: String,
)

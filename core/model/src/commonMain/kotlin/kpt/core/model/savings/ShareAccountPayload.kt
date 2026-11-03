/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.savings

import kotlinx.serialization.Serializable
import kpt.core.model.objects.payloads.ChargeItem

/** Request body for a share account. */
@Serializable
data class ShareAccountPayload(
    /** The server id of the client. */
    val clientId: Int,

    /** The server id of the product. */
    val productId: Int? = null,

    /** The number of shares requested. */
    val requestedShares: Int,

    /** The id an external system uses for this record, if one was set. */
    val externalId: String? = null,

    /** The date of submission. */
    val submittedDate: String,

    /** The shortest period the account must stay active, in [minimumActivePeriodFrequencyType] units. */
    val minimumActivePeriod: Int? = null,

    /** The unit of the minimum active period. */
    val minimumActivePeriodFrequencyType: Int? = null,

    /** The length of the lock-in period, in [lockinPeriodFrequencyType] units. */
    val lockinPeriodFrequency: Int? = null,

    /** The unit of the lock-in period. */
    val lockinPeriodFrequencyType: Int? = null,

    /** The date of the application. */
    val applicationDate: String,

    /** True when inactive clients still earn dividends. */
    val allowDividendCalculationForInactiveClients: Boolean,

    /** The locale Fineract parses the dates and numbers in this request with (e.g. `en`). */
    val locale: String? = null,

    /** The pattern Fineract parses this request's dates with (e.g. `dd MMMM yyyy`). */
    val dateFormat: String? = null,

    /** The charges. */
    val charges: List<ChargeItem> = emptyList(),

    /** The server id of the savings account. */
    val savingsAccountId: Int,
)

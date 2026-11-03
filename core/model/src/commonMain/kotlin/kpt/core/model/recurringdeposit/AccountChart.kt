/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.recurringdeposit

import kotlinx.serialization.Serializable

/** An account chart. */
@Serializable
data class AccountChart(
    /** The server id of the account. */
    val accountId: Int? = null,
    /** The account number the payment was made from or to. */
    val accountNumber: String? = null,
    /** The rate slabs of the chart. */
    val chartSlabs: List<ChartSlab>? = null,
    /** The date this ends. */
    val endDate: List<Int>? = null,
    /** The date this starts from. */
    val fromDate: List<Int>? = null,
    /** The server id of this record. */
    val id: Int? = null,
    /** True when the chart groups its slabs by amount before period. */
    val isPrimaryGroupingByAmount: Boolean? = null,
    /** The display name. */
    val name: String? = null,
    /** The period units the chart may use. */
    val periodTypes: List<PeriodType>? = null,
)

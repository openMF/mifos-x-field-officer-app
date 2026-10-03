/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.recurringdeposit

import kotlinx.serialization.Serializable

@Serializable
data class AccountChart(
    val accountId: Int? = null,
    val accountNumber: String? = null,
    val chartSlabs: List<ChartSlab>? = null,
    val endDate: List<Int>? = null,
    val fromDate: List<Int>? = null,
    val id: Int? = null,
    val isPrimaryGroupingByAmount: Boolean? = null,
    val name: String? = null,
    val periodTypes: List<PeriodType>? = null,
)

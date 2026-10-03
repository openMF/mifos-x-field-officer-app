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
data class ChartSlab(
    val amountRangeFrom: Double? = null,
    val annualInterestRate: Double? = null,
    val currency: Currency? = null,
    val description: String? = null,
    val fromPeriod: Int? = null,
    val id: Int? = null,
    val incentives: List<Incentive>? = null,
    val periodType: PeriodType? = null,
)

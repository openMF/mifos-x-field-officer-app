/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.template.recurring

import kotlinx.serialization.Serializable
import kpt.core.model.objects.template.recurring.incentive.Incentive
import kpt.core.model.objects.template.recurring.period.PeriodType
/** A chart slab. */
@Serializable
data class ChartSlab(
    /** The lower bound of the amount range. */
    val amountRangeFrom: Double? = null,
    /** The nominal annual interest rate, in percent. */
    val annualInterestRate: Double? = null,
    /** The currency the amounts are in. */
    val currency: Currency? = null,
    /** A free-text description. */
    val description: String? = null,
    /** The lower bound of the period range. */
    val fromPeriod: Int? = null,
    /** The incentives of this rate slab. */
    val incentives: List<Incentive>? = null,
    /** The unit of the period range. */
    val periodType: PeriodType? = null,
)

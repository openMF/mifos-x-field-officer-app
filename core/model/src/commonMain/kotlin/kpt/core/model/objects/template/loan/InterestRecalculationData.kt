/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.template.loan

import kotlinx.serialization.Serializable

/**
 * Created by Rajan Maurya on 16/07/16.
 */
@Serializable
data class InterestRecalculationData(
    /** The server id of this record. */
    var id: Int? = null,

    /** The server id of the product. */
    var productId: Int? = null,

    /** What interest recalculation compounds (interest, fees or both). */
    var interestRecalculationCompoundingType: InterestRecalculationCompoundingType? = null,

    /** How the schedule is adjusted when repayments arrive early. */
    var rescheduleStrategyType: RescheduleStrategyType? = null,

    /** The unit of the recalculation rest frequency. */
    var recalculationRestFrequencyType: RecalculationRestFrequencyType? = null,

    /** How many rest-frequency units between recalculations. */
    var recalculationRestFrequencyInterval: Int? = null,

    /** True when arrears are measured against the original schedule. */
    var isArrearsBasedOnOriginalSchedule: Boolean? = null,

    /** True when compounded interest is posted as its own transaction. */
    var isCompoundingToBePostedAsTransaction: Boolean? = null,

    /** How interest is calculated when the loan is closed early. */
    var preClosureInterestCalculationStrategy: PreClosureInterestCalculationStrategy? = null,

    /** True when interest compounds at end of day. */
    var allowCompoundingOnEod: Boolean? = null,
)

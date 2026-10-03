/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.template.loan

import kotlinx.serialization.Serializable

/**
 * Created by Rajan Maurya on 16/07/16.
 */
@Serializable
data class InterestRecalculationData(
    var id: Int? = null,

    var productId: Int? = null,

    var interestRecalculationCompoundingType: InterestRecalculationCompoundingType? = null,

    var rescheduleStrategyType: RescheduleStrategyType? = null,

    var recalculationRestFrequencyType: RecalculationRestFrequencyType? = null,

    var recalculationRestFrequencyInterval: Int? = null,

    var isArrearsBasedOnOriginalSchedule: Boolean? = null,

    var isCompoundingToBePostedAsTransaction: Boolean? = null,

    var preClosureInterestCalculationStrategy: PreClosureInterestCalculationStrategy? = null,

    var allowCompoundingOnEod: Boolean? = null,
)

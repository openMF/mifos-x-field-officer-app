/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.organisations

import kotlinx.serialization.Serializable

/**
 * Created by Rajan Maurya on 15/07/16.
 */
@Serializable
data class AllowAttributeOverrides(
    var amortizationType: Boolean? = null,

    var interestType: Boolean? = null,

    var transactionProcessingStrategyId: Boolean? = null,

    var interestCalculationPeriodType: Boolean? = null,

    var inArrearsTolerance: Boolean? = null,

    var repaymentEvery: Boolean? = null,

    var graceOnPrincipalAndInterestPayment: Boolean? = null,

    var graceOnArrearsAgeing: Boolean? = null,
)

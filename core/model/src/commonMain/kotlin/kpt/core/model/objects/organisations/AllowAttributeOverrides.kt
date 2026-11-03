/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.organisations

import kotlinx.serialization.Serializable

/**
 * Created by Rajan Maurya on 15/07/16.
 */
@Serializable
data class AllowAttributeOverrides(
    /** How the principal is amortised (equal installments or equal principal). */
    var amortizationType: Boolean? = null,

    /** The interest method (declining balance or flat). */
    var interestType: Boolean? = null,

    /** The server id of the repayment strategy (the order principal, interest, fees and penalties are paid in). */
    var transactionProcessingStrategyId: Boolean? = null,

    /** How interest is calculated over a period (daily or same as the repayment period). */
    var interestCalculationPeriodType: Boolean? = null,

    /** True when the arrears tolerance may be overridden. */
    var inArrearsTolerance: Boolean? = null,

    /** How many repayment-frequency units fall between two installments. */
    var repaymentEvery: Boolean? = null,

    /** True when the grace on principal and interest may be overridden. */
    var graceOnPrincipalAndInterestPayment: Boolean? = null,

    /** The number of days an overdue installment is not yet counted as in arrears. */
    var graceOnArrearsAgeing: Boolean? = null,
)

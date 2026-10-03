/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.template.recurring.charge

import kotlinx.serialization.Serializable
import kpt.core.model.objects.template.recurring.Currency
@Serializable
data class ChargeOption(
    val active: Boolean? = null,
    val amount: Double? = null,
    val chargeAppliesTo: ChargeAppliesTo? = null,
    val chargeCalculationType: ChargeCalculationType? = null,
    val chargePaymentMode: ChargePaymentMode? = null,
    val chargeTimeType: ChargeTimeType? = null,
    val currency: Currency? = null,
    val freeWithdrawal: Boolean? = null,
    val freeWithdrawalChargeFrequency: Int? = null,
    val id: Int? = null,
    val isPaymentType: Boolean? = null,
    val name: String? = null,
    val penalty: Boolean? = null,
    val restartFrequency: Int? = null,
    val restartFrequencyEnum: Int? = null,
)

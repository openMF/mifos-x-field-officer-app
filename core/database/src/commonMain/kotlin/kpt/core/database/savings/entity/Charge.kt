/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.savings.entity

import kotlinx.serialization.Serializable
import kpt.core.database.charge.entity.ChargeCalculationTypeEntity
import kpt.core.database.charge.entity.ChargeTimeTypeEntity
@Serializable
data class Charge(
    val id: Int? = null,

    val chargeId: Int? = null,

    val accountId: Int? = null,

    val name: String? = null,
    val chargeTimeType: ChargeTimeTypeEntity? = null,
    val chargeCalculationType: ChargeCalculationTypeEntity? = null,

    val percentage: Int? = null,

    val amountPercentageAppliedTo: Int? = null,

    val currency: SavingAccountCurrencyEntity? = null,

    val amount: Double? = null,

    val amountPaid: Double? = null,

    val amountWaived: Double? = null,

    val amountWrittenOff: Double? = null,

    val amountOutstanding: Double? = null,

    val amountOrPercentage: Double? = null,

    val penalty: Boolean? = null,

    val additionalProperties: MutableMap<String, String> = HashMap(),
) {

    fun setAdditionalProperty(name: String, value: String) {
        additionalProperties[name] = value
    }
}

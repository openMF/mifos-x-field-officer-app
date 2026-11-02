/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.savings

import kotlinx.serialization.Serializable
import kpt.core.model.charge.ChargeCalculationType
import kpt.core.model.charge.ChargeTimeType

/**
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `Charge`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class Charge(
    val id: Int? = null,
    val chargeId: Int? = null,
    val accountId: Int? = null,
    val name: String? = null,
    val chargeTimeType: ChargeTimeType? = null,
    val chargeCalculationType: ChargeCalculationType? = null,
    val percentage: Int? = null,
    val amountPercentageAppliedTo: Int? = null,
    val currency: SavingAccountCurrency? = null,
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

/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.savings.entity

import kotlinx.serialization.Serializable
import kpt.core.database.charge.entity.ChargeCalculationTypeEntity
import kpt.core.database.charge.entity.ChargeTimeTypeEntity
/** A charge. */
@Serializable
data class Charge(
    /** The server id of this record. */
    val id: Int? = null,

    /** The server id of the charge. */
    val chargeId: Int? = null,

    /** The server id of the account. */
    val accountId: Int? = null,

    /** The display name. */
    val name: String? = null,
    /** When the charge is applied (disbursement, specified due date, …). */
    val chargeTimeType: ChargeTimeTypeEntity? = null,
    /** How the charge amount is calculated (flat, percent of amount, …). */
    val chargeCalculationType: ChargeCalculationTypeEntity? = null,

    /** The charge percentage. */
    val percentage: Int? = null,

    /** What a percentage charge is calculated on. */
    val amountPercentageAppliedTo: Int? = null,

    /** The currency the amounts are in. */
    val currency: SavingAccountCurrencyEntity? = null,

    /** The amount, in the record's currency. */
    val amount: Double? = null,

    /** The amount already paid. */
    val amountPaid: Double? = null,

    /** The amount waived. */
    val amountWaived: Double? = null,

    /** The amount written off. */
    val amountWrittenOff: Double? = null,

    /** The amount still outstanding. */
    val amountOutstanding: Double? = null,

    /** The charge amount, or its percentage for a percentage charge. */
    val amountOrPercentage: Double? = null,

    /** True when this charge is a penalty rather than a fee. */
    val penalty: Boolean? = null,

    /** Extra properties Fineract sent that have no field of their own. */
    val additionalProperties: MutableMap<String, String> = HashMap(),
)

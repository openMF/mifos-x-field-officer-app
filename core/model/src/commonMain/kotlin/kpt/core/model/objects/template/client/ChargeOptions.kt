/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.template.client

import kotlinx.serialization.Serializable

/**
 * Created by mayankjindal on 13/12/16.
 */

@Serializable
data class ChargeOptions(
    /** The server id of this record. */
    val id: Int? = null,
    /** The display name. */
    val name: String? = null,
    /** True when the record is active. */
    val active: Boolean? = null,
    /** True when this charge is a penalty rather than a fee. */
    val penalty: Boolean? = null,
    /** The currency the amounts are in. */
    val currency: Currency? = null,
    /** The amount, in the record's currency. */
    val amount: Double? = null,
    /** When the charge is applied (disbursement, specified due date, …). */
    val chargeTimeType: ChargeTimeType? = null,
    /** What the charge applies to (loan, savings, client, …). */
    val chargeAppliesTo: ChargeAppliesTo? = null,
    /** How the charge amount is calculated (flat, percent of amount, …). */
    val chargeCalculationType: ChargeCalculationType? = null,
    /** How the charge is paid (regular or from a linked savings account). */
    val chargePaymentMode: ChargePaymentMode? = null,
    /** The GL account the charge is booked to. */
    val incomeOrLiabilityAccount: IncomeOrLiabilityAccount? = null,
)

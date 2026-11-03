/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.template.recurring.charge

import kotlinx.serialization.Serializable
import kpt.core.model.objects.template.recurring.Currency
/** One charge choice offered by a template. */
@Serializable
data class ChargeOption(
    /** True when the record is active. */
    val active: Boolean? = null,
    /** The amount, in the record's currency. */
    val amount: Double? = null,
    /** What the charge applies to (loan, savings, client, …). */
    val chargeAppliesTo: ChargeAppliesTo? = null,
    /** How the charge amount is calculated (flat, percent of amount, …). */
    val chargeCalculationType: ChargeCalculationType? = null,
    /** How the charge is paid (regular or from a linked savings account). */
    val chargePaymentMode: ChargePaymentMode? = null,
    /** When the charge is applied (disbursement, specified due date, …). */
    val chargeTimeType: ChargeTimeType? = null,
    /** The currency the amounts are in. */
    val currency: Currency? = null,
    /** True when some withdrawals are free of the charge. */
    val freeWithdrawal: Boolean? = null,
    /** How many withdrawals per period are free. */
    val freeWithdrawalChargeFrequency: Int? = null,
    /** The server id of this record. */
    val id: Int? = null,
    /** True when the charge applies to a payment type. */
    val isPaymentType: Boolean? = null,
    /** The display name. */
    val name: String? = null,
    /** True when this charge is a penalty rather than a fee. */
    val penalty: Boolean? = null,
    /** How often the free-withdrawal count restarts. */
    val restartFrequency: Int? = null,
    /** The unit of the restart frequency. */
    val restartFrequencyEnum: Int? = null,
)

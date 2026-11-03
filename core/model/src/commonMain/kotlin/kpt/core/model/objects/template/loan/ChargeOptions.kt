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
data class ChargeOptions(
    /** The server id of this record. */
    var id: Int? = null,

    /** The display name. */
    var name: String? = null,

    /** True when the record is active. */
    var active: Boolean? = null,

    /** True when this charge is a penalty rather than a fee. */
    var penalty: Boolean? = null,

    /** The currency the amounts are in. */
    var currency: Currency? = null,

    /** The amount, in the record's currency. */
    var amount: Double? = null,

    /** When the charge is applied (disbursement, specified due date, …). */
    var chargeTimeType: ChargeTimeType? = null,

    /** What the charge applies to (loan, savings, client, …). */
    var chargeAppliesTo: ChargeAppliesTo? = null,

    /** How the charge amount is calculated (flat, percent of amount, …). */
    var chargeCalculationType: ChargeCalculationType? = null,

    /** How the charge is paid (regular or from a linked savings account). */
    var chargePaymentMode: ChargePaymentMode? = null,

    /** The tax group applied to the charge. */
    var taxGroup: TaxGroup? = null,
)

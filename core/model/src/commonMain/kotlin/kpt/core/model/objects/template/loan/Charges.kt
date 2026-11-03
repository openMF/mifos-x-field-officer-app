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
 * Created by mayankjindal on 02/10/16.
 */
@Serializable
data class Charges(
    /** The server id of the charge. */
    var chargeId: Int? = null,

    /** The display name. */
    var name: String? = null,

    /** When the charge is applied (disbursement, specified due date, …). */
    var chargeTimeType: ChargeTimeType? = null,

    /** How the charge amount is calculated (flat, percent of amount, …). */
    var chargeCalculationType: ChargeCalculationType? = null,

    /** The currency the amounts are in. */
    var currency: Currency? = null,

    /** The amount, in the record's currency. */
    var amount: Double? = null,

    /** The amount already paid. */
    var amountPaid: Double? = null,

    /** The amount waived. */
    var amountWaived: Double? = null,

    /** The amount written off. */
    var amountWrittenOff: Double? = null,

    /** The amount still outstanding. */
    var amountOutstanding: Double? = null,

    /** The charge amount, or its percentage for a percentage charge. */
    var amountOrPercentage: Double? = null,

    /** True when this charge is a penalty rather than a fee. */
    var penalty: Boolean? = null,

    /** How the charge is paid (regular or from a linked savings account). */
    var chargePaymentMode: ChargePaymentMode? = null,

    /** True when it has been paid. */
    var paid: Boolean? = null,

    /** True when it has been waived. */
    var waived: Boolean? = null,

    /** True when the charge is payable now. */
    var chargePayable: Boolean? = null,
)

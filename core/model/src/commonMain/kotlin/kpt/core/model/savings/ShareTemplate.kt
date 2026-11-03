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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kpt.core.model.objects.template.client.ChargeAppliesTo
import kpt.core.model.objects.template.client.ChargeCalculationType
import kpt.core.model.objects.template.client.ChargePaymentMode
import kpt.core.model.objects.template.client.ChargeTimeType
import kpt.core.model.objects.template.client.Currency
import kpt.core.model.shared.ProductOption

/** Server template for a share account: the options its form offers. */
@Serializable
data class ShareTemplate(
    /** The server id of the client. */
    val clientId: Int,

    /** The client's display name. */
    val clientName: String,

    /** The currency the amounts are in. */
    val currency: Currency? = null,

    /** The current market price of one share. */
    val currentMarketPrice: Double? = null,

    /** The products that may be chosen. */
    val productOptions: List<ProductOption> = emptyList(),

    /** The charges that may be added. */
    val chargeOptions: List<ChargeOptions> = emptyList(),

    /** The savings accounts that may be linked. */
    @SerialName("clientSavingsAccounts")
    val savingsAccountOptions: List<SavingsAccountOption>? = emptyList(),

    /** The units the lock-in period may be set in. */
    val lockinPeriodFrequencyTypeOptions: List<FrequencyTypeOption>? = emptyList(),

    /** The units the minimum active period may be set in. */
    val minimumActivePeriodFrequencyTypeOptions: List<FrequencyTypeOption>? = emptyList(),
)

/** One savings account choice offered by a template. */
@Serializable
data class SavingsAccountOption(
    /** The server id of this record. */
    val id: Int,

    /** The account number Fineract assigned. */
    val accountNo: String,

    /** The name of the savings product. */
    val savingsProductName: String? = null,

    /** The server id of the savings product. */
    val savingsProductId: Int? = null,
)

/** One frequency type choice offered by a template. */
@Serializable
data class FrequencyTypeOption(
    /** The server id of this record. */
    val id: Int,

    /**
     * The Fineract code that identifies this value (an enum code such as `loanStatusType.active`, or an ISO
     * currency code).
     */
    val code: String,

    /** The human-readable label Fineract sends for this value. */
    val value: String,
)

/** One charge choice offered by a template. */
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
)

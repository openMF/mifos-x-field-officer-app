/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.account.saving

import kotlinx.serialization.Serializable

/**
 * Typed inputs for the three savings writes an officer performs in the field.
 *
 * ## Why these exist
 *
 * Each replaces something that could not express a correct request:
 *
 *  * **Activation** took a `HashMap<String, String>` — the officer's date, locale and format
 *    assembled by string key at the call site, with nothing to check that all three were present
 *    or spelled right.
 *  * **Approval** took `SavingsApproval?`, whose `locale` and `dateFormat` were DEFAULTED, so a
 *    caller could set a date without ever deciding how it would be parsed.
 *  * **A transaction** took `SavingsAccountTransactionRequestEntity?` — a nullable ROOM ENTITY,
 *    with every field nullable and an `errorMessage` column, used as an outbound request body.
 *
 * ## [locale] and [dateFormat] are REQUIRED on all three
 *
 * This is the structural fix for a defect that has now recurred **seven** times across this
 * migration, and the savings writes are its worst variant: the original sent
 * `approvalDate.toString()` where `approvalDate` was **epoch milliseconds**, so the payload carried
 * `"1761000000000"` as `approvedOnDate` while declaring `dateFormat = "dd MMMM yyyy"`. The screen
 * displayed the date correctly via `DateHelper.getDateAsStringFromLong`, so the bug was invisible
 * on screen — only the request was wrong. The activation screen did the same thing.
 *
 * Fineract parses with the format you declare, so the date string and the format are ONE decision.
 * Making both required means a caller cannot make half of it. Build them with
 * [kpt.core.model.utils.ApiDateFormatter].
 */
data class SavingsApprovalInput(
    /** The date the record was approved. */
    val approvedOnDate: String,
    /** The locale Fineract parses the dates and numbers in this request with (e.g. `en`). */
    val locale: String,
    /** The pattern Fineract parses this request's dates with (e.g. `dd MMMM yyyy`). */
    val dateFormat: String,
    /** A free-text note. */
    val note: String? = null,
)

/** Activate an approved savings account. See [SavingsApprovalInput] for the date-pairing rule. */
data class SavingsActivationInput(
    /** The date the record was activated. */
    val activatedOnDate: String,
    /** The locale Fineract parses the dates and numbers in this request with (e.g. `en`). */
    val locale: String,
    /** The pattern Fineract parses this request's dates with (e.g. `dd MMMM yyyy`). */
    val dateFormat: String,
)

/**
 * A deposit or a withdrawal.
 *
 * [transactionAmount] is a String because Fineract's savings transaction endpoint takes it as one
 * (unlike the loan endpoints, which take a number) — the caller validates and formats it, and
 * keeping the wire type here stops a Double round-trip from changing the value the officer typed.
 */
data class SavingsTransactionInput(
    /** The date of the transaction. */
    val transactionDate: String,
    /** The amount, as the officer typed it. */
    val transactionAmount: String,
    /** The locale Fineract parses the dates and numbers in this request with (e.g. `en`). */
    val locale: String,
    /** The pattern Fineract parses this request's dates with (e.g. `dd MMMM yyyy`). */
    val dateFormat: String,
    /** Null when the tenant configures no payment types, never `0` — `0` names no row. */
    val paymentTypeId: Int? = null,
    /** A free-text note. */
    val note: String? = null,
    /** The account number the payment was made from or to. */
    val accountNumber: String? = null,
    /** The cheque number of the payment. */
    val checkNumber: String? = null,
    /** The routing code of the payment. */
    val routingCode: String? = null,
    /** The receipt number of the payment. */
    val receiptNumber: String? = null,
    /** The bank number of the payment. */
    val bankNumber: String? = null,
)

/**
 * A new savings application, for a CLIENT or a GROUP.
 *
 * [ownerId] + [isGroup] replace the original route's `(groupId, clientId, isGroupAccount)` triple —
 * of which the wizard's ViewModel read only `clientId`, dropping the other two. So opening it from
 * group detail produced an application with no `groupId` at all: the group path was accepted by the
 * route and discarded by the ViewModel, which is the same accepted-then-dropped class as the group
 * loan's External Id, but on the IDENTITY of the account being created.
 *
 * One owner and one discriminator make that unexpressible — there is no second id to forget, and
 * the discriminator also selects which Fineract template endpoint the form reads.
 */
@Serializable
data class SavingsApplicationInput(
    /** The client or group the account is opened for. */
    val ownerId: Int,
    /** True when [ownerId] is a group. */
    val isGroup: Boolean,
    /** The server id of the product. */
    val productId: Int,
    /** The date the record was submitted. */
    val submittedOnDate: String,
    /** The locale Fineract parses the dates and numbers in this request with (e.g. `en`). */
    val locale: String,
    /** The pattern Fineract parses this request's dates with (e.g. `dd MMMM yyyy`). */
    val dateFormat: String,

    /** The server id of the field officer. */
    val fieldOfficerId: Int? = null,
    /** The id an external system uses for this record, if one was set. */
    val externalId: String? = null,
    /** The minimum balance required to open the account. */
    val minRequiredOpeningBalance: String? = null,
    /** The minimum balance the account must keep. */
    val minRequiredBalance: String? = null,
    /** The length of the lock-in period, in [lockinPeriodFrequencyType] units. */
    val lockinPeriodFrequency: Int? = null,
    /** The unit of the lock-in period. */
    val lockinPeriodFrequencyType: Int? = null,
    /** How often interest is compounded. */
    val interestCompoundingPeriodType: Int? = null,
    /** How often interest is posted to the account. */
    val interestPostingPeriodType: Int? = null,
    /** How interest is calculated on the balance (daily or average daily balance). */
    val interestCalculationType: Int? = null,
    /** How many days a year has for interest calculation (360, 365, actual). */
    val interestCalculationDaysInYearType: Int? = null,
    /** True when the account may be overdrawn. */
    val allowOverdraft: Boolean = false,
    /** True when the minimum balance is enforced. */
    val enforceMinRequiredBalance: Boolean = false,

    /**
     * Charges attached to the application.
     *
     * Unlike the LOAN application — where `LoansPayload` has no charges member and the wizard's
     * whole Charges page was discarded on submit — `SavingsPayload` does carry them, so this page
     * is real and the list reaches the server.
     */
    val charges: List<SavingsChargeInput> = emptyList(),
)

/**
 * One charge on a savings application: which charge, and how much.
 *
 * @property amount the amount, in the record's currency
 * @property chargeId the server id of the charge
 */
@Serializable
data class SavingsChargeInput(val chargeId: Int, val amount: String)

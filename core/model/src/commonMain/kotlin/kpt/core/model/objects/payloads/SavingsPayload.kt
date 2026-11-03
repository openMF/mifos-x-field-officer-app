/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.payloads

import kotlinx.serialization.Serializable

/**
 * Created by nellyk on 2/10/2016.
 */
@Serializable
class SavingsPayload {
    /** The server id of the product. */
    var productId: Int? = null

    /** The server id of the client. */
    var clientId: Int? = null

    /** The server id of the group. */
    var groupId: Int? = null

    /** The server id of the field officer. */
    var fieldOfficerId: Int? = null

    /** The locale Fineract parses the dates and numbers in this request with (e.g. `en`). */
    var locale: String? = null

    /** The pattern Fineract parses this request's dates with (e.g. `dd MMMM yyyy`). */
    var dateFormat: String? = null

    /** The date the record was submitted. */
    var submittedOnDate: String? = null

    /** The id an external system uses for this record, if one was set. */
    var externalId: String? = null

    /** The nominal annual interest rate, in percent. */
    var nominalAnnualInterestRate: String? = null

    /** How often interest is compounded. */
    var interestCompoundingPeriodType: Int? = null

    /** How interest is calculated on the balance (daily or average daily balance). */
    var interestCalculationType: Int? = null

    /** How many days a year has for interest calculation (360, 365, actual). */
    var interestCalculationDaysInYearType: Int? = null

    /** How often interest is posted to the account. */
    var interestPostingPeriodType: Int? = null

    /** True when the account may be overdrawn. */
    var allowOverdraft: Boolean? = null

    /** True when the minimum balance is enforced. */
    var enforceMinRequiredBalance: Boolean? = null

    /** The minimum balance required to open the account. */
    var minRequiredOpeningBalance: String? = null

    /** The minimum balance the account must keep. */
    var minRequiredBalance: String? = null

    /** The length of the lock-in period, in [lockinPeriodFrequencyType] units. */
    var lockinPeriodFrequency: Int? = null

    /** The unit of the lock-in period. */
    var lockinPeriodFrequencyType: Int? = null

    /** The charges. */
    var charges: List<ChargesPayload>? = null

    /** The interest rate charged on an overdraft. */
    var nominalAnnualInterestRateOverdraft: String? = null

    /** The overdraft limit. */
    var overdraftLimit: String? = null

    /** The overdraft balance above which interest is charged. */
    var minOverdraftForInterestCalculation: String? = null
}

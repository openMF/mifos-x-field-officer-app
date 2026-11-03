/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.recurringdeposit

import kotlinx.serialization.Serializable
import kpt.core.model.recurringdeposit.deposit.DepositPeriodFrequency
import kpt.core.model.recurringdeposit.deposit.DepositType
import kpt.core.model.recurringdeposit.deposit.InMultiplesOfDepositTermType
import kpt.core.model.recurringdeposit.deposit.MaxDepositTermType
import kpt.core.model.recurringdeposit.deposit.MinDepositTermType
import kpt.core.model.recurringdeposit.interest.InterestCalculationDaysInYearType
import kpt.core.model.recurringdeposit.interest.InterestCalculationType
import kpt.core.model.recurringdeposit.interest.InterestCompoundingPeriodType
import kpt.core.model.recurringdeposit.interest.InterestPostingPeriodType

/** A recurring deposit. */
@Serializable
data class RecurringDeposit(
    /** The interest-rate chart of the deposit. */
    val accountChart: AccountChart? = null,
    /** The account number Fineract assigned. */
    val accountNo: String? = null,
    /** True when an advance deposit is applied to future installments. */
    val adjustAdvanceTowardsFuturePayments: Boolean? = null,
    /** True when withdrawals are allowed before maturity. */
    val allowWithdrawal: Boolean? = null,
    /** The server id of the client. */
    val clientId: Int? = null,
    /** The client's display name. */
    val clientName: String? = null,
    /** The currency the amounts are in. */
    val currency: Currency? = null,
    /** The deposit amount. */
    val depositAmount: Double? = null,
    /** The length of the deposit term, in [depositPeriodFrequencyId] units. */
    val depositPeriod: Int? = null,
    /** The unit of the deposit term. */
    val depositPeriodFrequency: DepositPeriodFrequency? = null,
    /** The server id of the deposit product. */
    val depositProductId: Int? = null,
    /** The name of the deposit product. */
    val depositProductName: String? = null,
    /** The deposit product type (savings, fixed deposit or recurring deposit). */
    val depositType: DepositType? = null,
    /** The date the first deposit is expected. */
    val expectedFirstDepositOnDate: List<Int>? = null,
    /** The id an external system uses for this record, if one was set. */
    val externalId: String? = null,
    /** The server id of the field officer. */
    val fieldOfficerId: Int? = null,
    /** The field officer's display name. */
    val fieldOfficerName: String? = null,
    /** The server id of this record. */
    val id: Int? = null,
    /** The unit the deposit term is counted in multiples of. */
    val inMultiplesOfDepositTermType: InMultiplesOfDepositTermType? = null,
    /** How many days a year has for interest calculation (360, 365, actual). */
    val interestCalculationDaysInYearType: InterestCalculationDaysInYearType? = null,
    /** How interest is calculated on the balance (daily or average daily balance). */
    val interestCalculationType: InterestCalculationType? = null,
    /** How often interest is compounded. */
    val interestCompoundingPeriodType: InterestCompoundingPeriodType? = null,
    /** How often interest is posted to the account. */
    val interestPostingPeriodType: InterestPostingPeriodType? = null,
    /** True when the deposit follows the group's meeting calendar. */
    val isCalendarInherited: Boolean? = null,
    /** True when the recurring deposit is mandatory. */
    val isMandatoryDeposit: Boolean? = null,
    /** The length of the lock-in period, in [lockinPeriodFrequencyType] units. */
    val lockinPeriodFrequency: Int? = null,
    /** The unit of the lock-in period. */
    val lockinPeriodFrequencyType: LockinPeriodFrequencyType? = null,
    /** The deposit amount each period, mandatory or recommended. */
    val mandatoryRecommendedDepositAmount: Double? = null,
    /** The amount at maturity. */
    val maturityAmount: Double? = null,
    /** The date the deposit matures. */
    val maturityDate: List<Int>? = null,
    /** The unit of the maximum deposit term. */
    val maxDepositTermType: MaxDepositTermType? = null,
    /** The shortest deposit term. */
    val minDepositTerm: Int? = null,
    /** The unit of the minimum deposit term. */
    val minDepositTermType: MinDepositTermType? = null,
    /** The nominal annual interest rate, in percent. */
    val nominalAnnualInterestRate: Double? = null,
    /** True when a penalty applies if the deposit is closed early. */
    val preClosurePenalApplicable: Boolean? = null,
    /** How many frequency units between deposits. */
    val recurringFrequency: Int? = null,
    /** The unit of the deposit frequency. */
    val recurringFrequencyType: RecurringFrequencyType? = null,
    /** The record's Fineract status. */
    val status: Status? = null,
    /** The account's running totals. */
    val summary: Summary? = null,
    /** The dates and users of the record's lifecycle events (submitted, approved, activated, …). */
    val timeline: Timeline? = null,
    /** True when tax is withheld on interest. */
    val withHoldTax: Boolean? = null,
    /** True when the withdrawal fee also applies to transfers. */
    val withdrawalFeeForTransfers: Boolean? = null,
)

/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.dashboard.dto

import kotlinx.serialization.Serializable

/** `GET users/{userId}` — the signed-in user's own record; `staff` is the link that scopes their book. */
@Serializable
data class AppUserDto(
    /** The server id of this record. */
    val id: Long = 0,
    /** The username. */
    val username: String? = null,
    /** The person's first name. */
    val firstname: String? = null,
    /** The person's last name. */
    val lastname: String? = null,
    /** The server id of the office. */
    val officeId: Long = 0,
    /** The name of the office. */
    val officeName: String? = null,
    /** The staff record linked to the user. */
    val staff: StaffRefDto? = null,
    /** The email address. */
    val email: String? = null,
    /** The user's roles. */
    val selectedRoles: List<RoleRefDto> = emptyList(),
)

/**
 * Reference to a role embedded in another payload.
 *
 * @property id the server id of this record
 * @property name the display name
 */
@Serializable
data class RoleRefDto(val id: Long = 0, val name: String? = null)

/**
 * Reference to a staff embedded in another payload.
 *
 * @property displayName the display name
 * @property id the server id of this record
 */
@Serializable
data class StaffRefDto(val id: Long = 0, val displayName: String? = null)

/**
 * `GET businessdate/BUSINESS_DATE` — `date` is Fineract's `[year, month, day]` array.
 *
 * @property date the business date, as Fineract's `[year, month, day]` array
 * @property type the business-date type (`BUSINESS_DATE` or `COB_DATE`)
 */
@Serializable
data class BusinessDateDto(val type: String? = null, val date: List<Int> = emptyList())

/**
 * Reference to a currency embedded in another payload.
 *
 * @property code the Fineract code that identifies this value (an enum code such as `loanStatusType.active`, or an ISO
 *   currency code)
 * @property displaySymbol the currency symbol to display (e.g. `$`)
 */
@Serializable
data class CurrencyRefDto(val code: String? = null, val displaySymbol: String? = null)

/** Reference to a loan summary embedded in another payload. */
@Serializable
data class LoanSummaryRefDto(
    /** The principal still outstanding. */
    val principalOutstanding: Double = 0.0,
    /** The principal overdue. */
    val principalOverdue: Double = 0.0,
    /** The total amount outstanding. */
    val totalOutstanding: Double = 0.0,
    /** The total amount overdue. */
    val totalOverdue: Double = 0.0,
)

/**
 * Reference to a loan timeline embedded in another payload.
 *
 * @property submittedByUsername the username of whoever submitted the record
 */
@Serializable
data class LoanTimelineRefDto(val submittedByUsername: String? = null)

/** Wire shape of a loan list item. */
@Serializable
data class LoanListItemDto(
    /** The server id of this record. */
    val id: Long = 0,
    /** The account number Fineract assigned. */
    val accountNo: String? = null,
    /** The server id of the client. */
    val clientId: Long? = null,
    /** The client's display name. */
    val clientName: String? = null,
    /** The group. */
    val group: GroupRefDto? = null,
    /** The server id of the loan officer. */
    val loanOfficerId: Long? = null,
    /** The name of the loan product. */
    val loanProductName: String? = null,
    /** The loan principal. */
    val principal: Double = 0.0,
    /** True when an installment is overdue. */
    val inArrears: Boolean = false,
    /** The dates and users of the record's lifecycle events (submitted, approved, activated, …). */
    val timeline: LoanTimelineRefDto? = null,
    /** The account's running totals. */
    val summary: LoanSummaryRefDto? = null,
    /** The currency the amounts are in. */
    val currency: CurrencyRefDto? = null,
)

/**
 * Reference to a group embedded in another payload.
 *
 * @property id the server id of this record
 * @property name the display name
 */
@Serializable
data class GroupRefDto(val id: Long = 0, val name: String? = null)

/**
 * One page of loan list results.
 *
 * @property pageItems the records on this page
 * @property totalFilteredRecords the number of records matching the query, across every page
 */
@Serializable
data class LoanListPageDto(val totalFilteredRecords: Int = 0, val pageItems: List<LoanListItemDto> = emptyList())

/**
 * Reference to a savings summary embedded in another payload.
 *
 * @property accountBalance the account balance
 */
@Serializable
data class SavingsSummaryRefDto(val accountBalance: Double = 0.0)

/** Wire shape of a savings list item. */
@Serializable
data class SavingsListItemDto(
    /** The server id of this record. */
    val id: Long = 0,
    /** The server id of the field officer. */
    val fieldOfficerId: Long? = null,
    /** The account's running totals. */
    val summary: SavingsSummaryRefDto? = null,
    /** The currency the amounts are in. */
    val currency: CurrencyRefDto? = null,
)

/**
 * One page of savings list results.
 *
 * @property pageItems the records on this page
 * @property totalFilteredRecords the number of records matching the query, across every page
 */
@Serializable
data class SavingsListPageDto(val totalFilteredRecords: Int = 0, val pageItems: List<SavingsListItemDto> = emptyList())

/** `GET loans/{id}?associations=repaymentSchedule` — only what Today's collections reads. */
@Serializable
data class LoanScheduleDto(
    /** The server id of this record. */
    val id: Long = 0,
    /** The server id of the client. */
    val clientId: Long? = null,
    /** The currency the amounts are in. */
    val currency: CurrencyRefDto? = null,
    /** The repayment schedule. */
    val repaymentSchedule: RepaymentScheduleDto? = null,
)

/**
 * Wire shape of a repayment schedule.
 *
 * @property periods the installments
 */
@Serializable
data class RepaymentScheduleDto(val periods: List<SchedulePeriodDto> = emptyList())

/** One installment; `period` is null on the disbursement row. `dueDate` is Fineract's `[y, m, d]`. */
@Serializable
data class SchedulePeriodDto(
    /** The installment number. */
    val period: Int? = null,
    /** The date the amount is due. */
    val dueDate: List<Int> = emptyList(),
    /** The total paid for the installment. */
    val totalPaidForPeriod: Double = 0.0,
    /** The total outstanding for the installment. */
    val totalOutstandingForPeriod: Double = 0.0,
)

/**
 * One request inside `POST batches`; [relativeUrl] is relative to the API root (`loans/7?…`).
 *
 * @property method the HTTP method of the batch request
 * @property requestId the id that ties this part of a batch to its request
 */
@Serializable
data class BatchRequestDto(val requestId: Long, val relativeUrl: String, val method: String = "GET")

/**
 * Its answer: [body] is the JSON the single request would have returned, as a string.
 *
 * @property requestId the id that ties this part of a batch to its request
 * @property statusCode the HTTP status of this part of the batch
 */
@Serializable
data class BatchResponseDto(val requestId: Long = 0, val statusCode: Int = 0, val body: String? = null)

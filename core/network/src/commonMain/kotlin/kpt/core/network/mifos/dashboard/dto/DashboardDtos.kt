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
    val id: Long = 0,
    val username: String? = null,
    val firstname: String? = null,
    val lastname: String? = null,
    val officeId: Long = 0,
    val officeName: String? = null,
    val staff: StaffRefDto? = null,
    val email: String? = null,
    val selectedRoles: List<RoleRefDto> = emptyList(),
)

@Serializable
data class RoleRefDto(val id: Long = 0, val name: String? = null)

@Serializable
data class StaffRefDto(val id: Long = 0, val displayName: String? = null)

/** `GET businessdate/BUSINESS_DATE` — `date` is Fineract's `[year, month, day]` array. */
@Serializable
data class BusinessDateDto(val type: String? = null, val date: List<Int> = emptyList())

@Serializable
data class CurrencyRefDto(val code: String? = null, val displaySymbol: String? = null)

@Serializable
data class LoanSummaryRefDto(
    val principalOutstanding: Double = 0.0,
    val principalOverdue: Double = 0.0,
    val totalOutstanding: Double = 0.0,
    val totalOverdue: Double = 0.0,
)

@Serializable
data class LoanTimelineRefDto(val submittedByUsername: String? = null)

@Serializable
data class LoanListItemDto(
    val id: Long = 0,
    val accountNo: String? = null,
    val clientId: Long? = null,
    val clientName: String? = null,
    val group: GroupRefDto? = null,
    val loanOfficerId: Long? = null,
    val loanProductName: String? = null,
    val principal: Double = 0.0,
    val inArrears: Boolean = false,
    val timeline: LoanTimelineRefDto? = null,
    val summary: LoanSummaryRefDto? = null,
    val currency: CurrencyRefDto? = null,
)

@Serializable
data class GroupRefDto(val id: Long = 0, val name: String? = null)

@Serializable
data class LoanListPageDto(val totalFilteredRecords: Int = 0, val pageItems: List<LoanListItemDto> = emptyList())

@Serializable
data class SavingsSummaryRefDto(val accountBalance: Double = 0.0)

@Serializable
data class SavingsListItemDto(
    val id: Long = 0,
    val fieldOfficerId: Long? = null,
    val summary: SavingsSummaryRefDto? = null,
    val currency: CurrencyRefDto? = null,
)

@Serializable
data class SavingsListPageDto(val totalFilteredRecords: Int = 0, val pageItems: List<SavingsListItemDto> = emptyList())

/** `GET loans/{id}?associations=repaymentSchedule` — only what Today's collections reads. */
@Serializable
data class LoanScheduleDto(
    val id: Long = 0,
    val clientId: Long? = null,
    val currency: CurrencyRefDto? = null,
    val repaymentSchedule: RepaymentScheduleDto? = null,
)

@Serializable
data class RepaymentScheduleDto(val periods: List<SchedulePeriodDto> = emptyList())

/** One installment; `period` is null on the disbursement row. `dueDate` is Fineract's `[y, m, d]`. */
@Serializable
data class SchedulePeriodDto(
    val period: Int? = null,
    val dueDate: List<Int> = emptyList(),
    val totalPaidForPeriod: Double = 0.0,
    val totalOutstandingForPeriod: Double = 0.0,
)

/** One request inside `POST batches`; [relativeUrl] is relative to the API root (`loans/7?…`). */
@Serializable
data class BatchRequestDto(val requestId: Long, val relativeUrl: String, val method: String = "GET")

/** Its answer: [body] is the JSON the single request would have returned, as a string. */
@Serializable
data class BatchResponseDto(val requestId: Long = 0, val statusCode: Int = 0, val body: String? = null)

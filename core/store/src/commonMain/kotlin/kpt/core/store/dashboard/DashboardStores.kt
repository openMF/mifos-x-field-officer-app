/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.dashboard

import kotlinx.serialization.json.Json
import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.database.cache.dao.ApiResponseCacheDao
import kpt.core.model.dashboard.DashboardLoan
import kpt.core.model.dashboard.DashboardSavings
import kpt.core.model.dashboard.DashboardUser
import kpt.core.model.dashboard.DueTodayLoan
import kpt.core.network.mifos.client.api.ClientApi
import kpt.core.network.mifos.dashboard.api.DashboardApi
import kpt.core.network.mifos.dashboard.dto.BatchRequestDto
import kpt.core.network.mifos.dashboard.dto.LoanListItemDto
import kpt.core.network.mifos.dashboard.dto.LoanScheduleDto
import kpt.core.store.cache.cachedRead
import kpt.core.store.config.AppCacheKeys
import org.mobilenativefoundation.store.store5.Store

/**
 * Home dashboard reads. Each card is its own store so one slow or failing source never blanks the
 * others, and each is offline-first through the shared read cache.
 */

data class ClientCountKey(val officeId: Long, val status: String)

/** The business date (`yyyy-MM-dd`) and the officer's active loans to read the schedules of. */
data class DueTodayKey(val date: String, val loanIds: List<Long>)

// Loans per batch call — bounds one response (~24 KB a schedule) without adding round-trips for a book.
private const val SCHEDULE_BATCH = 100
private const val HTTP_OK = 200

private val batchJson = Json { ignoreUnknownKeys = true }

private const val PAGE = 200

// ponytail: walks every page up to MAX_ROWS — fine for one officer's book, swap for a server-side
// officer filter if Fineract ever adds one to GET /loans.
private const val MAX_ROWS = 2000

@StoreProvider(id = "dashboardUser", ttl = "12h")
@CacheKey(fn = "forKey", key = "dashboardUser:{key}", params = ["key:String"])
fun provideDashboardUserStore(
    dashboardApi: DashboardApi,
    cache: ApiResponseCacheDao,
): Store<Long, DashboardUser> = cachedRead(
    dao = cache,
    keyOf = { userId -> AppCacheKeys.DashboardUser.forKey("$userId") },
    fetch = { userId -> dashboardApi.getUser(userId) },
    map = { dto ->
        DashboardUser(
            id = dto.id,
            firstname = dto.firstname,
            lastname = dto.lastname,
            officeId = dto.officeId,
            officeName = dto.officeName,
            staffId = dto.staff?.id?.takeIf { it > 0 },
            staffName = dto.staff?.displayName,
            email = dto.email?.takeIf { it.isNotBlank() },
            roles = dto.selectedRoles.mapNotNull { it.name?.takeIf(String::isNotBlank) },
        )
    },
)

@StoreProvider(id = "dashboardBusinessDate", ttl = "1h")
@CacheKey(name = "CURRENT", key = "dashboardBusinessDate")
fun provideDashboardBusinessDateStore(
    dashboardApi: DashboardApi,
    cache: ApiResponseCacheDao,
): Store<Unit, String> = cachedRead(
    dao = cache,
    keyOf = { AppCacheKeys.DashboardBusinessDate.CURRENT },
    fetch = { dashboardApi.getBusinessDate() },
    map = { dto ->
        val (y, m, d) = dto.date.takeIf { it.size >= 3 } ?: error("business date missing")
        "$y-${m.toString().padStart(2, '0')}-${d.toString().padStart(2, '0')}"
    },
)

@StoreProvider(id = "dashboardLoans", ttl = "15m")
@CacheKey(fn = "forKey", key = "dashboardLoans:{key}", params = ["key:String"])
fun provideDashboardLoansStore(
    dashboardApi: DashboardApi,
    cache: ApiResponseCacheDao,
): Store<Int, List<DashboardLoan>> = cachedRead(
    dao = cache,
    keyOf = { status -> AppCacheKeys.DashboardLoans.forKey("$status") },
    fetch = { status ->
        buildList {
            var offset = 0
            do {
                val page = dashboardApi.getLoans(status = status, offset = offset, limit = PAGE)
                addAll(page.pageItems.map(LoanListItemDto::toDashboardLoan))
                offset += PAGE
            } while (offset < page.totalFilteredRecords && offset < MAX_ROWS && page.pageItems.isNotEmpty())
        }
    },
)

@StoreProvider(id = "dashboardClientCount", ttl = "15m")
@CacheKey(fn = "forKey", key = "dashboardClientCount:{key}", params = ["key:String"])
fun provideDashboardClientCountStore(
    clientApi: ClientApi,
    cache: ApiResponseCacheDao,
): Store<ClientCountKey, Int> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.DashboardClientCount.forKey("${key.officeId}:${key.status}") },
    fetch = { key -> clientApi.retrieveAll21(officeId = key.officeId, status = key.status, limit = 1) },
    map = { response -> response.totalFilteredRecords ?: 0 },
)

/**
 * Today's installments of the officer's active loans, from each loan's repayment schedule. The
 * fallback for Today's collections when the role lacks READ_COLLECTIONSHEET but has READ_LOAN.
 */
// One `POST batches` per 100 loans instead of one request per loan. A loan the server cannot return
// (404, a status that changed) is skipped; if every one fails, the first failure surfaces as the card's error.
@StoreProvider(id = "dashboardDueToday", ttl = "15m")
@CacheKey(fn = "forKey", key = "dashboardDueToday:{key}", params = ["key:String"])
fun provideDashboardDueTodayStore(
    dashboardApi: DashboardApi,
    cache: ApiResponseCacheDao,
): Store<DueTodayKey, List<DueTodayLoan>> = cachedRead(
    dao = cache,
    keyOf = { key -> AppCacheKeys.DashboardDueToday.forKey("${key.date}:${key.loanIds.sorted().hashCode()}") },
    fetch = { key ->
        val answers = key.loanIds.chunked(SCHEDULE_BATCH).flatMap { ids ->
            dashboardApi.batch(
                ids.mapIndexed { i, id ->
                    BatchRequestDto(
                        requestId = i + 1L,
                        relativeUrl = "loans/$id?associations=repaymentSchedule" +
                            "&fields=id,clientId,currency,repaymentSchedule",
                    )
                },
            )
        }
        val ok = answers.filter { it.statusCode == HTTP_OK && it.body != null }
        if (ok.isEmpty() && answers.isNotEmpty()) {
            val first = answers.first()
            // Shaped like Ktor's own failure so ServerErrorParser reads Fineract's message out of it.
            error("Client request(POST batches) invalid: ${first.statusCode}. Text: \"${first.body.orEmpty()}\"")
        }
        ok.mapNotNull { batchJson.decodeFromString(LoanScheduleDto.serializer(), it.body!!).dueOn(key.date) }
    },
)

/** The installment(s) of this loan falling due on [date] (`yyyy-MM-dd`), or null when none does. */
internal fun LoanScheduleDto.dueOn(date: String): DueTodayLoan? {
    val today = repaymentSchedule?.periods.orEmpty().filter { p ->
        p.period != null && p.dueDate.size >= 3 &&
            "${p.dueDate[0]}-${p.dueDate[1].toString().padStart(2, '0')}-" +
            p.dueDate[2].toString().padStart(2, '0') == date
    }
    if (today.isEmpty()) return null
    return DueTodayLoan(
        loanId = id,
        clientId = clientId,
        currencyCode = currency?.code.orEmpty(),
        currencySymbol = currency?.displaySymbol.orEmpty(),
        due = today.sumOf { it.totalOutstandingForPeriod },
        paid = today.sumOf { it.totalPaidForPeriod },
    )
}

@StoreProvider(id = "dashboardSavings", ttl = "15m")
@CacheKey(name = "LIST", key = "dashboardSavings")
fun provideDashboardSavingsStore(
    dashboardApi: DashboardApi,
    cache: ApiResponseCacheDao,
): Store<Unit, List<DashboardSavings>> = cachedRead(
    dao = cache,
    keyOf = { AppCacheKeys.DashboardSavings.LIST },
    fetch = {
        buildList {
            var offset = 0
            do {
                val page = dashboardApi.getSavingsAccounts(offset = offset, limit = PAGE)
                addAll(
                    page.pageItems.map {
                        DashboardSavings(
                            fieldOfficerId = it.fieldOfficerId,
                            balance = it.summary?.accountBalance ?: 0.0,
                            currencyCode = it.currency?.code.orEmpty(),
                            currencySymbol = it.currency?.displaySymbol.orEmpty(),
                        )
                    },
                )
                offset += PAGE
            } while (offset < page.totalFilteredRecords && offset < MAX_ROWS && page.pageItems.isNotEmpty())
        }
    },
)

private fun LoanListItemDto.toDashboardLoan() = DashboardLoan(
    id = id,
    accountNo = accountNo.orEmpty(),
    // A group loan carries no clientName — name it by the group instead of a blank row.
    clientName = clientName ?: group?.name.orEmpty(),
    loanOfficerId = loanOfficerId,
    submittedByUsername = timeline?.submittedByUsername,
    inArrears = inArrears,
    principalOutstanding = summary?.principalOutstanding ?: 0.0,
    principalOverdue = summary?.principalOverdue ?: 0.0,
    totalOutstanding = summary?.totalOutstanding ?: 0.0,
    totalOverdue = summary?.totalOverdue ?: 0.0,
    currencyCode = currency?.code.orEmpty(),
    currencySymbol = currency?.displaySymbol.orEmpty(),
    productName = loanProductName,
    clientId = clientId,
    principal = principal,
)

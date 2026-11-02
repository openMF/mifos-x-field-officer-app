/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.dashboard

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.model.dashboard.DashboardLoan
import kpt.core.model.dashboard.DashboardSavings
import kpt.core.model.dashboard.DashboardUser
import kpt.core.model.dashboard.DueTodayLoan

/** Home dashboard reads — each its own stream, so one failing card never blanks the rest. */
interface DashboardRepository {

    /** The signed-in user's own record: first name and the staff link that scopes the book. */
    fun userStream(userId: Long, scope: CoroutineScope): ScreenDataStream<DashboardUser>

    /** Fineract's business date as `yyyy-MM-dd` — the "today" every card means. */
    fun businessDateStream(scope: CoroutineScope): ScreenDataStream<String>

    /** Loans in one status (100 submitted, 200 approved, 300 active), unfiltered by officer. */
    fun loansStream(status: Int, scope: CoroutineScope): ScreenDataStream<List<DashboardLoan>>

    /** How many clients of [officeId] are in [status] (`pending`, `active`). */
    fun clientCountStream(officeId: Long, status: String, scope: CoroutineScope): ScreenDataStream<Int>

    fun savingsStream(scope: CoroutineScope): ScreenDataStream<List<DashboardSavings>>

    /** Installments of [loanIds] due on [date] (`yyyy-MM-dd`) — Today's collections without the collection sheet. */
    fun dueTodayStream(date: String, loanIds: List<Long>, scope: CoroutineScope): ScreenDataStream<List<DueTodayLoan>>

    companion object {
        const val STATUS_SUBMITTED = 100
        const val STATUS_APPROVED = 200
        const val STATUS_ACTIVE = 300
    }
}

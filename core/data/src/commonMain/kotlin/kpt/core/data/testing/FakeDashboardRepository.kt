/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(kpt.core.base.store.screen.ExperimentalScreenDataStreamTestingApi::class)

package kpt.core.data.testing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.data.dashboard.DashboardRepository
import kpt.core.model.dashboard.DashboardLoan
import kpt.core.model.dashboard.DashboardSavings
import kpt.core.model.dashboard.DashboardUser
import kpt.core.model.dashboard.DueTodayLoan

/** Scripted dashboard sources; every stream is a [MutableStateFlow] a test can move. [requested] records each call. */
@ExperimentalDataTestingApi
class FakeDashboardRepository(
    user: DashboardUser = DashboardUser(),
    businessDate: String = "2026-10-26",
    loansByStatus: Map<Int, List<DashboardLoan>> = emptyMap(),
    clientCounts: Map<String, Int> = emptyMap(),
    savings: List<DashboardSavings> = emptyList(),
    dueToday: List<DueTodayLoan> = emptyList(),
) : DashboardRepository {

    /** The signed-in user the stream emits. */
    val user = MutableStateFlow<ScreenState<DashboardUser>>(ScreenState.Content(user))

    /** The business date the stream emits. */
    val businessDate = MutableStateFlow<ScreenState<String>>(ScreenState.Content(businessDate))

    /** The loans per status the streams emit. */
    val loans = loansByStatus
        .mapValues { MutableStateFlow<ScreenState<List<DashboardLoan>>>(ScreenState.Content(it.value)) }
        .toMutableMap()

    /** The client counts per status the streams emit. */
    val clientCounts = clientCounts
        .mapValues { MutableStateFlow<ScreenState<Int>>(ScreenState.Content(it.value)) }
        .toMutableMap()

    /** The savings accounts the stream emits. */
    val savings = MutableStateFlow<ScreenState<List<DashboardSavings>>>(ScreenState.Content(savings))

    /** The due-today loans the stream emits. */
    val dueToday = MutableStateFlow<ScreenState<List<DueTodayLoan>>>(ScreenState.Content(dueToday))

    /** The streams opened, in order (`user`, `loans:<status>`, …). */
    val requested = mutableListOf<String>()

    override fun userStream(
        userId: Long,
        scope: CoroutineScope,
    ) = screenDataStreamForTesting(user).also { requested += "user" }

    override fun businessDateStream(
        scope: CoroutineScope,
    ) = screenDataStreamForTesting(businessDate).also { requested += "businessDate" }

    override fun loansStream(status: Int, scope: CoroutineScope) =
        screenDataStreamForTesting(loans.getOrPut(status) { MutableStateFlow(ScreenState.Content(emptyList())) })
            .also { requested += "loans:$status" }

    override fun clientCountStream(officeId: Long, status: String, scope: CoroutineScope) =
        screenDataStreamForTesting(clientCounts.getOrPut(status) { MutableStateFlow(ScreenState.Content(0)) })
            .also { requested += "clients:$status" }

    override fun savingsStream(
        scope: CoroutineScope,
    ) = screenDataStreamForTesting(savings).also { requested += "savings" }

    override fun dueTodayStream(date: String, loanIds: List<Long>, scope: CoroutineScope) =
        screenDataStreamForTesting(dueToday)
            .also { requested += "dueToday:$date:${loanIds.sorted().joinToString(",")}" }
}

/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.home.dashboard

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import kpt.core.base.store.freshness.FreshnessSignal
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.dashboard.CurrencyTotals
import kpt.core.model.recent.RecentKind
import kpt.core.model.recent.RecentRecord
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.Clock
import kotlin.time.Instant
import kpt.core.base.designsystem.theme.KptTheme as KptBaseTheme

/**
 * One preview per Stitch state (`idea-layer/mockups/home/stitch/0{1..5}-home-*`), fed from
 * `idea-layer/screens/home/demo-data.yaml` so a preview and its mockup show the same day.
 */
private val demoHeader = HomeHeader(
    name = "Irving",
    officeName = "CENTRAL",
    businessDate = "2026-10-26",
    instanceName = "Mifos field officer gateway",
    ownPortfolio = true,
)

private val demoQuickActions = QuickAction.entries.toList()

private val demoCollections = HomeCard.Collections(
    clientsDue = 14,
    loansDue = 17,
    dueByCurrency = listOf(Money(18_450.0, "MXN", "$")),
    collectedByCurrency = listOf(Money(30_250.0, "MXN", "$")),
    overdueClients = 3,
)

private val demoQueue = HomeCard.WorkQueue(
    checkerTasks = 2,
    awaitingApproval = 4,
    awaitingDisbursal = 2,
    pendingClients = 9,
    pendingReschedules = 1,
)

private val demoMeetings = HomeCard.Meetings(
    listOf(
        MeetingToday(centerId = 10, name = "Centro Arin", groups = 3, time = "09:30"),
        MeetingToday(centerId = 18, name = "Arin Savings", groups = 2, time = "14:00"),
    ),
)

private val demoPortfolio = HomeCard.Portfolio(
    activeClients = 1_098,
    activeLoans = 60,
    inArrears = 7,
    loanTotals = listOf(
        CurrencyTotals(
            currencyCode = "MXN",
            currencySymbol = "$",
            outstanding = 1_284_500.0,
            overdue = 96_340.0,
            principalOutstanding = 1_200_000.0,
            principalOverdue = 90_000.0,
        ),
    ),
    savings = listOf(Money(412_900.0, "MXN", "$")),
    groups = 11,
    centers = 2,
)

/** Opened 12m / 45m / 2h / a day before the render, as in the mockups. */
private fun demoRecent(): List<RecentRecord> {
    val now = Clock.System.now().toEpochMilliseconds()
    val min = 60_000L
    return listOf(
        RecentRecord(
            RecentKind.CLIENT,
            192,
            "FOTest Asha",
            accountNo = "00192",
            detail = "Active client",
            openedAtMs = now - 12 * min,
        ),
        RecentRecord(
            RecentKind.GROUP,
            84,
            "MARIPOSAS MONARCAS",
            accountNo = "0084",
            detail = "Solidarity group",
            openedAtMs = now - 45 * min,
        ),
        RecentRecord(
            RecentKind.LOAN,
            883,
            "CREDITO A LA PALABRA",
            accountNo = "LN-2024-883",
            detail = "FOTest Asha",
            openedAtMs = now - 120 * min,
        ),
        RecentRecord(
            RecentKind.CENTER,
            12,
            "Arin",
            accountNo = "CN-0012",
            detail = "Centre · 4 groups",
            openedAtMs = now - 26 * 60 * min,
        ),
    )
}

/**
 * The gateway's refusal as the device receives it (2026-10-27, POST /collectionsheet for a role without
 * READ_COLLECTIONSHEET): Ktor's request line, then Fineract's JSON body.
 */
private val gateway403 = RuntimeException(
    "Client request(POST https://apis.mifos.community/1.0/field/v1/collectionsheet) invalid: 403. Text: \"" +
        """{"developerMessage":"The user associated with credentials passed on this request does not have """ +
        """sufficient privileges to perform this action.",""" +
        """"httpStatusCode":"403","defaultUserMessage":"Insufficient privileges to perform this action.",""" +
        """"userMessageGlobalisationCode":"error.msg.not.authorized",""" +
        """"errors":[{"developerMessage":"User has no authority to READ collectionsheets",""" +
        """"defaultUserMessage":"User has no authority to READ collectionsheets",""" +
        """"userMessageGlobalisationCode":"error.msg.not.authorized","parameterName":"id","args":[]}]}""" + "\"",
)

private fun content(card: HomeCard): ScreenState<HomeCard> = ScreenState.Content(card)

private fun state(
    collections: ScreenState<HomeCard> = content(
        demoCollections,
    ),
    all: ScreenState<HomeCard>? = null,
) = HomeUiState(
    header = demoHeader,
    cards = listOf(
        HomeCardSlot(HomeCardId.COLLECTIONS, all ?: collections),
        HomeCardSlot(HomeCardId.WORK_QUEUE, all ?: content(demoQueue)),
        HomeCardSlot(HomeCardId.MEETINGS, all ?: content(demoMeetings)),
        HomeCardSlot(HomeCardId.PORTFOLIO, all ?: content(demoPortfolio)),
    ),
    quickActions = demoQuickActions,
    draftCount = 1,
    recent = demoRecent(),
)

@Composable
private fun Render(state: HomeUiState) = KptTheme(darkTheme = false) {
    // Light, on the app background — the scheme every Stitch render is drawn in.
    Surface(color = KptBaseTheme.colorScheme.background) {
        HomeDashboardScreenContent(state = state, onRetry = {}, onRefresh = {}, onNavigate = {})
    }
}

/** 01-home-loading */
@Preview
@Composable
internal fun HomeDashboardLoadingPreview() = Render(state(all = ScreenState.Loading))

/** 02-home-content */
@Preview
@Composable
internal fun HomeDashboardContentPreview() = Render(state())

/** 03-home-partial_failure — one card failed, the rest keep rendering. */
@Preview
@Composable
internal fun HomeDashboardPartialFailurePreview() = Render(
    state(
        collections = ScreenState.Content(
            demoCollections,
            fetchedAt = Instant.parse("2026-10-26T08:30:00Z"),
            freshnessSignal = FreshnessSignal.initial().copy(lastError = gateway403),
        ),
    ),
)

/** 04-home-error / 05-home-offline — nothing remote loaded; quick actions still work. */
@Preview
@Composable
internal fun HomeDashboardErrorPreview() = Render(state(all = ScreenState.Error(IllegalStateException("preview"))))

/** 05-home-offline — nothing remote answered for want of a network; quick actions still work. */
@Preview
@Composable
internal fun HomeDashboardOfflinePreview() = Render(state(all = ScreenState.NoNetwork()))

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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.dashboard.IndependentCardLayout
import kpt.core.base.ui.dashboard.toDashboardProgressState
import kpt.core.ui.util.exposeTestTagsAsResourceIds
import kpt.feature.home.ui.TestTags
import org.koin.compose.viewmodel.koinViewModel

/**
 * The Home tab body, built from the Stitch markup — one composition per state, each from its own
 * `idea-layer/mockups/home/stitch/0N-home-<state>/code.html`:
 *
 *   01 loading · 02 content · 03 partial_failure · 04 error · 05 offline
 *
 * Measurements are the HTML's Tailwind units, read through [tw] (one unit = `spacing.xs` = 4 dp),
 * so `p-3.5` is `3.5f.tw` and `w-11` is `11.tw`; colours are the theme slots the Tailwind
 * names map to (indigo-50 → primaryContainer, emerald → secondary, amber → tertiary, rose → the
 * finance negative pair, slate-100 → surfaceContainer). Every tap leaves through [onNavigate].
 */
@Composable
fun HomeDashboardScreen(
    onNavigate: (HomeNavigation) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    HomeDashboardScreenContent(
        state = state,
        onRetry = viewModel::onRetry,
        onRefresh = viewModel::onRefresh,
        onNavigate = onNavigate,
        modifier = modifier,
    )
}

/**
 * The stateless dashboard: header, the four remote cards each in its own load state, quick actions and recent
 * records. Taps go to [onNavigate]; [onRetry] reloads one card and [onRefresh] (pull-to-refresh) reloads them all.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDashboardScreenContent(
    state: HomeUiState,
    onRetry: (HomeCardId) -> Unit,
    onRefresh: () -> Unit,
    onNavigate: (HomeNavigation) -> Unit,
    modifier: Modifier = Modifier,
) {
    val states = state.cards.map { it.state }
    val progress = states.toDashboardProgressState()
    val allLoading = states.isNotEmpty() && states.all { it == ScreenState.Loading }
    val allFailed = states.isNotEmpty() && states.all { it is ScreenState.Error || it is ScreenState.NoNetwork }
    val offline = states.any { it is ScreenState.NoNetwork }

    PullToRefreshBox(
        isRefreshing = progress.isAnyLoading && progress.oldestFetchedAt != null,
        onRefresh = onRefresh,
        // Expose testTags as Android resource ids so the Maestro flows (ui.yaml testTags) can find them.
        modifier = modifier.fillMaxSize().exposeTestTagsAsResourceIds().testTag(TestTags.Home.DASHBOARD),
    ) {
        // main: px-4 · 01 pt-3.5 space-y-4 · 02 pt-3 space-y-6 · 04 pt-4 gap-6 · 05 p-4 space-y-4
        val gap = if (allLoading || (allFailed && offline)) 4.tw else 6.tw
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 4.tw, end = 4.tw, top = 3.tw, bottom = 8.tw)
                .testTag(TestTags.Home.DASHBOARD_SCROLL),
            verticalArrangement = Arrangement.spacedBy(gap),
        ) {
            when {
                allLoading -> {
                    Header(state.header, HeaderStyle.Loading, onSettings = {
                        onNavigate(HomeNavigation.Settings)
                    }, onProfile = { onNavigate(HomeNavigation.Profile) })
                    DashboardSkeleton()
                }
                allFailed && offline -> {
                    Header(state.header, HeaderStyle.Offline, onSettings = {
                        onNavigate(HomeNavigation.Settings)
                    }, onProfile = { onNavigate(HomeNavigation.Profile) })
                    OfflineError(onRetryAll = onRefresh)
                    OfflineBanner()
                    QuickActions(state.quickActions, onNavigate, QuickStyle.Offline)
                    RecentCard(state.recent, onNavigate, RecentStyle.Offline)
                }
                allFailed -> {
                    Header(state.header, HeaderStyle.Error, onSettings = {
                        onNavigate(HomeNavigation.Settings)
                    }, onProfile = { onNavigate(HomeNavigation.Profile) })
                    QuickActions(state.quickActions, onNavigate, QuickStyle.Error)
                    RecentCard(state.recent, onNavigate, RecentStyle.Error)
                    ScreenError(onRetryAll = onRefresh)
                }
                else -> {
                    Header(state.header, HeaderStyle.Content, onSettings = {
                        onNavigate(HomeNavigation.Settings)
                    }, onProfile = { onNavigate(HomeNavigation.Profile) })
                    ContentState(state, states, onRetry, onNavigate)
                }
            }
        }
    }
}

/** 02 content · 03 partial_failure — every card owns its state, so one failure never blanks the rest. */
@Composable
internal fun ContentState(
    state: HomeUiState,
    states: List<ScreenState<HomeCard>>,
    onRetry: (HomeCardId) -> Unit,
    onNavigate: (HomeNavigation) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.tw)) {
        if (states.isNotEmpty()) {
            IndependentCardLayout(
                states = states,
                onRetry = { index -> onRetry(state.cards[index].id) },
                cardChrome = { index, card ->
                    Section(state.cards[index], state.draftCount, state.header.businessDate, onNavigate, {
                        onRetry(state.cards[index].id)
                    }, card)
                },
                loading = { index -> CardSkeleton(state.cards[index].id) },
                error = { index, error -> CardError(onRetry = { onRetry(state.cards[index].id) }, error = error) },
                noNetwork = { index, _ -> CardError(onRetry = { onRetry(state.cards[index].id) }, offline = true) },
            ) { _, data, _ ->
                when (data) {
                    is HomeCard.Collections -> CollectionsBody(data)
                    is HomeCard.WorkQueue -> WorkQueueBody(data, state.draftCount, onNavigate)
                    is HomeCard.Meetings -> MeetingsBody(data, onNavigate)
                    is HomeCard.Portfolio -> PortfolioBody(data)
                }
            }
        }
        QuickActions(state.quickActions, onNavigate, QuickStyle.Content)
        RecentCard(state.recent, onNavigate, RecentStyle.Content)
    }
}

// ── Header (#header) ───────────────────────────────────────────────────────────────────────────

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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runComposeUiTest
import kpt.core.base.store.screen.ScreenState
import kpt.core.data.dashboard.DashboardRepository
import kpt.core.designsystem.theme.KptTheme
import kpt.feature.home.ui.TestTags
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class HomeDashboardScreenUiTest {

    private val queue = HomeCard.WorkQueue(checkerTasks = 0, awaitingApproval = 12, awaitingDisbursal = 1, pendingClients = 9, pendingReschedules = 1)

    private fun state(
        queueState: ScreenState<HomeCard> = ScreenState.Content(queue),
        collections: ScreenState<HomeCard> = ScreenState.Content(HomeCard.Collections(clientsDue = 3, loansDue = 4, dueByCurrency = listOf(Money(900.0, "MXN", "$")))),
        draftCount: Int = 0,
    ) = HomeUiState(
        header = HomeHeader(name = "IRVING", officeName = "CENTRAL", businessDate = "2026-10-26"),
        cards = listOf(HomeCardSlot(HomeCardId.COLLECTIONS, collections), HomeCardSlot(HomeCardId.WORK_QUEUE, queueState)),
        quickActions = listOf(QuickAction.NEW_CLIENT, QuickAction.SEARCH, QuickAction.PATH_TRACKER),
        draftCount = draftCount,
    )

    @Test
    fun contentRendersEveryDeclaredSurface() = runComposeUiTest {
        setContent { KptTheme { HomeDashboardScreenContent(state(), onRetry = {}, onRefresh = {}, onNavigate = {}) } }
        listOf(TestTags.Home.HEADER, TestTags.Home.COLLECTIONS, TestTags.Home.OPEN_COLLECTION_SHEET, TestTags.Home.WORK_QUEUE, TestTags.Home.QUICK_ACTIONS)
            .forEach { onNodeWithTag(it).performScrollTo().assertIsDisplayed() }
    }

    @Test
    fun clientCountsUseTheSingularForOne() = runComposeUiTest {
        var due by mutableIntStateOf(1)
        setContent {
            KptTheme {
                HomeDashboardScreenContent(
                    state(collections = ScreenState.Content(HomeCard.Collections(clientsDue = due, loansDue = due, dueByCurrency = listOf(Money(900.0, "MXN", "$"))))),
                    onRetry = {},
                    onRefresh = {},
                    onNavigate = {},
                )
            }
        }
        // the summary line and the tile both read the count — "1 client", never "1 clients"
        onNode(hasText("1 client due", substring = true), useUnmergedTree = true).assertExists()
        onNode(hasText("1 clients", substring = true), useUnmergedTree = true).assertDoesNotExist()
        due = 3
        onNode(hasText("3 clients due", substring = true), useUnmergedTree = true).assertExists()
    }

    @Test
    fun queueTilesNavigateToTheirDeclaredTargets() = runComposeUiTest {
        val taps = mutableListOf<HomeNavigation>()
        setContent { KptTheme { HomeDashboardScreenContent(state(), onRetry = {}, onRefresh = {}, onNavigate = { taps += it }) } }
        onNodeWithTag(TestTags.Home.QUEUE_APPROVAL).performScrollTo().performClick()
        onNodeWithTag(TestTags.Home.QUEUE_PENDING_CLIENTS).performScrollTo().performClick()
        onNodeWithTag(TestTags.Home.OPEN_COLLECTION_SHEET).performScrollTo().performClick()
        onNodeWithTag(TestTags.Home.QA_SEARCH).performScrollTo().performClick()
        onNodeWithTag(TestTags.Home.QA_PATH_TRACKER).performScrollTo().performClick()
        onNodeWithTag(TestTags.Home.SETTINGS).performScrollTo().performClick()
        onNodeWithTag(TestTags.Home.PROFILE).performScrollTo().performClick()
        assertEquals(
            listOf(
                HomeNavigation.LoanQueue(DashboardRepository.STATUS_SUBMITTED),
                HomeNavigation.PendingClients,
                HomeNavigation.CollectionSheet,
                HomeNavigation.Quick(QuickAction.SEARCH),
                HomeNavigation.Quick(QuickAction.PATH_TRACKER),
                HomeNavigation.Settings,
                HomeNavigation.Profile,
            ),
            taps,
        )
    }

    @Test
    fun draftsTileAlwaysShowsItsCount() = runComposeUiTest {
        var drafts by mutableIntStateOf(0)
        setContent { KptTheme { HomeDashboardScreenContent(state(draftCount = drafts), onRetry = {}, onRefresh = {}, onNavigate = {}) } }
        // Stitch 02: the sixth queue tile is always there; "0" means nothing is waiting to sync.
        onNode(hasText("0") and hasAnyAncestor(hasTestTag(TestTags.Home.QUEUE_DRAFTS)), useUnmergedTree = true).performScrollTo().assertIsDisplayed()
        drafts = 2
        onNode(hasText("2") and hasAnyAncestor(hasTestTag(TestTags.Home.QUEUE_DRAFTS)), useUnmergedTree = true).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun aFailedCardRetriesOnlyItselfWhileTheOthersRender() = runComposeUiTest {
        val retried = mutableListOf<HomeCardId>()
        setContent {
            KptTheme {
                HomeDashboardScreenContent(
                    state(collections = ScreenState.Error(IllegalStateException("500"))),
                    onRetry = { retried += it },
                    onRefresh = {},
                    onNavigate = {},
                )
            }
        }
        onNodeWithTag(TestTags.Home.CARD_ERROR).performScrollTo().assertIsDisplayed()
        onNodeWithTag(TestTags.Home.QUEUE_APPROVAL).performScrollTo().assertIsDisplayed()
        onNodeWithTag(TestTags.Home.SCREEN_ERROR).assertDoesNotExist()
        onNode(hasText("Retry") and hasAnyAncestor(hasTestTag(TestTags.Home.CARD_ERROR))).performClick()
        assertEquals(listOf(HomeCardId.COLLECTIONS), retried)
    }

    @Test
    fun whenEverythingFailsOneScreenErrorReplacesTheCardsAndQuickActionsStay() = runComposeUiTest {
        var refreshed = false
        val failed = ScreenState.Error(IllegalStateException("offline"))
        setContent {
            KptTheme { HomeDashboardScreenContent(state(queueState = failed, collections = failed), onRetry = {}, onRefresh = { refreshed = true }, onNavigate = {}) }
        }
        onNodeWithTag(TestTags.Home.SCREEN_ERROR).performScrollTo().assertIsDisplayed()
        onNodeWithTag(TestTags.Home.WORK_QUEUE).assertDoesNotExist()
        onNodeWithTag(TestTags.Home.QUICK_ACTIONS).performScrollTo().assertIsDisplayed()
        onNodeWithText("Retry").performClick()
        assertTrue(refreshed)
    }
}

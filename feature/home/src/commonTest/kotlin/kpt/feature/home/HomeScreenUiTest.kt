/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.home

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import kpt.core.designsystem.theme.KptTheme
import kpt.feature.home.search.SearchBodyContent
import kpt.feature.home.search.SearchState
import kpt.feature.home.ui.TestTags
import kotlin.test.Test

/**
 * The Home scaffold renders with the fork's own body.
 *
 * REWRITTEN in S2. This test previously constructed a `HomeViewModel` from `EmptyLoanRepository`,
 * `EmptyBillReminderRepository`, `LoadingEconomicRatesRepository` and
 * `FakeDashboardCurrencyRepository`, then rendered `HomeDashboard` with six demo navigation
 * callbacks — every one of them kmp-project-template DEMO surface that `remove-demo.sh` stripped at
 * fork standup. The module's `commonTest` had not compiled since; found on the first run of
 * `:feature:home:desktopTest`.
 *
 * It now renders the scaffold around the body the fork actually supplies through
 * `BackboneRegistry.homeBody` — Fineract search — so it exercises the real composition rather than a
 * deleted demo.
 */
@OptIn(ExperimentalTestApi::class)
class HomeScreenUiTest {

    @Test
    fun screenScaffoldIsDisplayedAroundTheForkBody() = runComposeUiTest {
        setContent {
            KptTheme {
                HomeScreen(
                    onSettingsClick = {},
                    homeBody = { SearchBodyContent(state = SearchState(), onAction = {}) },
                )
            }
        }
        onNodeWithTag(TestTags.Home.SCREEN).assertIsDisplayed()
        // The body is part of the scaffold's contract: an empty homeBody is what rendered on device
        // before S2 filled the seam, and the scaffold alone cannot tell the difference.
        onNodeWithTag(TestTags.Home.SEARCH).assertIsDisplayed()
    }
}

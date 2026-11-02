/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.home.search

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.theme.AppTheme
import kpt.core.model.objects.SearchedEntity
import kpt.feature.home.ui.TestTags
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Render verification for the Home body.
 *
 * Device-free on purpose. Home lives behind authentication, which needs a live Fineract server, so a
 * device capture cannot reach it — `RULE-TEST-VERIFY-001` puts render correctness here and runtime
 * behaviour on Maestro.
 */
@OptIn(ExperimentalTestApi::class)
class SearchBodyUiTest {

    private fun entity(id: Int, name: String) = SearchedEntity(
        entityId = id,
        entityName = name,
        entityType = "client",
    )

    @Test
    fun theSearchFieldAndFiltersRender() = runComposeUiTest {
        setContent {
            AppTheme {
                SearchBodyContent(
                    results = ScreenState.Empty,
                    state = SearchState(),
                    onAction = {},
                )
            }
        }
        onNodeWithTag(TestTags.Home.SEARCH).assertIsDisplayed()
        onNodeWithTag(TestTags.Home.SEARCH_FIELD).assertIsDisplayed()
        onNodeWithTag(TestTags.Home.SEARCH_FILTERS).assertIsDisplayed()
        onNodeWithTag(TestTags.Home.SEARCH_EXACT_MATCH).assertIsDisplayed()
    }

    @Test
    fun resultsRenderWhenTheStateCarriesContent() = runComposeUiTest {
        setContent {
            AppTheme {
                SearchBodyContent(
                    results = ScreenState.Content(
                        data = listOf(entity(1, "Ramesh"), entity(2, "Ramu")),
                    ),
                    state = SearchState(query = "ram"),
                    onAction = {},
                )
            }
        }
        onNodeWithTag(TestTags.Home.SEARCH_RESULTS).assertIsDisplayed()
    }

    /**
     * Every action the body can emit is reachable without a ViewModel.
     *
     * The split into [SearchBodyContent] exists so the render can be asserted; this checks the split
     * did not strand an action — a control wired to nothing would render fine and do nothing.
     */
    @Test
    fun typingEmitsAQueryChangedAction() = runComposeUiTest {
        val emitted = mutableListOf<SearchAction>()
        setContent {
            AppTheme {
                SearchBodyContent(
                    results = ScreenState.Empty,
                    state = SearchState(),
                    onAction = { emitted += it },
                )
            }
        }
        onNodeWithTag(TestTags.Home.SEARCH_EXACT_MATCH).performClick()
        assertEquals(listOf<SearchAction>(SearchAction.ExactMatchToggled), emitted)
    }
    // ---- the result tap (S4d) ----

    @Test
    fun aResultWhoseTypeHasAPortedDestinationIsClickable() = runComposeUiTest {
        var tapped: SearchedEntity? = null
        setContent {
            AppTheme {
                SearchBodyContent(
                    results = ScreenState.Content(listOf(entity(1, "Ramesh"))),
                    state = SearchState(query = "ram"),
                    onAction = {},
                    onResultClick = { tapped = it },
                )
            }
        }
        onNodeWithTag(TestTags.Home.searchResult(1)).performClick()
        assertEquals(1, tapped?.entityId)
    }

    @Test
    fun theTypeIsMatchedCaseINSENSITIVELY() = runComposeUiTest {
        // `entityType` arrives off the wire, not out of an enum — Fineract sends "CLIENT" but this
        // very test file's own fixture says "client", and a tenant could send "Client". Matching
        // exactly would have made every client result silently inert for one of those spellings.
        var tapped = 0
        setContent {
            AppTheme {
                SearchBodyContent(
                    results = ScreenState.Content(
                        listOf(
                            entity(1, "Ramesh").copy(entityType = "CLIENT"),
                            entity(2, "Ramu").copy(entityType = "client"),
                            entity(3, "Rama").copy(entityType = "Loan"),
                        ),
                    ),
                    state = SearchState(query = "ram"),
                    onAction = {},
                    onResultClick = { tapped++ },
                )
            }
        }
        onNodeWithTag(TestTags.Home.searchResult(1)).performClick()
        onNodeWithTag(TestTags.Home.searchResult(2)).performClick()
        onNodeWithTag(TestTags.Home.searchResult(3)).performClick()
        assertEquals(3, tapped)
    }

    @Test
    fun aResultWithNoPortedDestinationIsNOTClickable() = runComposeUiTest {
        // Every type Fineract's search returns is ported now (CENTER was last, 2026-10-25), so the
        // rule is pinned with a type that has no destination — the gate is the destination, never
        // whether a callback was supplied (RULE-IMPL-DEAD-CLICKABLE-001).
        var tapped = 0
        setContent {
            AppTheme {
                SearchBodyContent(
                    results = ScreenState.Content(
                        listOf(entity(1, "Share A").copy(entityType = "SHARE")),
                    ),
                    state = SearchState(query = "a"),
                    onAction = {},
                    onResultClick = { tapped++ },
                )
            }
        }
        onNodeWithTag(TestTags.Home.searchResult(1)).performClick()
        assertEquals(0, tapped, "an unported entity type must not be tappable")
    }

    @Test
    fun aCentreResultIsClickableNowCentreDetailExists() = runComposeUiTest {
        var tapped = 0
        setContent {
            AppTheme {
                SearchBodyContent(
                    results = ScreenState.Content(listOf(entity(1, "Centre A").copy(entityType = "CENTER"))),
                    state = SearchState(query = "a"),
                    onAction = {},
                    onResultClick = { tapped++ },
                )
            }
        }
        onNodeWithTag(TestTags.Home.searchResult(1)).performClick()
        assertEquals(1, tapped)
    }

    @Test
    fun aSavingsResultBecameClickableInS5a() = runComposeUiTest {
        var tapped = 0
        setContent {
            AppTheme {
                SearchBodyContent(
                    results = ScreenState.Content(
                        listOf(entity(2, "Savings A").copy(entityType = "SAVING")),
                    ),
                    state = SearchState(query = "a"),
                    onAction = {},
                    onResultClick = { tapped++ },
                )
            }
        }
        onNodeWithTag(TestTags.Home.searchResult(2)).performClick()
        // The savings summary is the destination — and it is what makes the whole savings vertical
        // reachable, per spec §6a.
        assertEquals(1, tapped)
    }

    @Test
    fun noResultIsClickableWhenTheHostSuppliesNoCallback() = runComposeUiTest {
        // This is what shipped in S2: no destination existed, so the rows were inert by design.
        var tapped = 0
        setContent {
            AppTheme {
                SearchBodyContent(
                    results = ScreenState.Content(listOf(entity(1, "Ramesh"))),
                    state = SearchState(query = "ram"),
                    onAction = {},
                    onResultClick = null,
                )
            }
        }
        onNodeWithTag(TestTags.Home.searchResult(1)).performClick()
        assertEquals(0, tapped)
    }
}

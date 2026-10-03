/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.home.search

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.theme.MifosTheme
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
            MifosTheme {
                SearchBodyContent(state = SearchState(), onAction = {})
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
            MifosTheme {
                SearchBodyContent(
                    state = SearchState(
                        query = "ram",
                        results = ScreenState.Content(
                            data = listOf(entity(1, "Ramesh"), entity(2, "Ramu")),
                        ),
                    ),
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
            MifosTheme {
                SearchBodyContent(state = SearchState(), onAction = { emitted += it })
            }
        }
        onNodeWithTag(TestTags.Home.SEARCH_EXACT_MATCH).performClick()
        assertEquals(listOf<SearchAction>(SearchAction.ExactMatchToggled), emitted)
    }
}

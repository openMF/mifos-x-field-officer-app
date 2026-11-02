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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.common.utils.Constants
import kpt.core.designsystem.fieldofficer.component.AppOutlinedTextField
import kpt.core.designsystem.icon.AppIcons
import kpt.core.model.objects.SearchedEntity
import kpt.feature.home.generated.resources.Res
import kpt.feature.home.generated.resources.feature_home_search_blank_query
import kpt.feature.home.generated.resources.feature_home_search_exact_match
import kpt.feature.home.generated.resources.feature_home_search_filter_clients
import kpt.feature.home.generated.resources.feature_home_search_filter_groups
import kpt.feature.home.generated.resources.feature_home_search_filter_loans
import kpt.feature.home.generated.resources.feature_home_search_filter_savings
import kpt.feature.home.generated.resources.feature_home_search_hint
import kpt.feature.home.ui.TestTags
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * The Home tab's body: Fineract resource search.
 *
 * Supplied to the shell through `BackboneRegistry.homeBody`, which is the template's fork seam and
 * was EMPTY — which is why Home rendered as a blank screen on device before S2.
 *
 * Ported from `6b66e8a43:feature/search/`. Results render through [ScreenContent], so Loading,
 * Empty, NoNetwork, Error and stale-while-revalidate are handled by the substrate rather than by the
 * four-branch `DataState` fold the original hand-rolled.
 */
@Composable
fun SearchBody(
    modifier: Modifier = Modifier,
    /**
     * Opens the tapped result's detail. Null leaves every row un-clickable, which is what shipped
     * in S2 when no destination existed; the shell supplies it now that three of the five entity
     * types have one. See [SearchResultRow].
     */
    onResultClick: ((SearchedEntity) -> Unit)? = null,
    viewModel: SearchViewModel = koinViewModel(),
) {
    val results by viewModel.results.collectAsStateWithLifecycle()
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    SearchBodyContent(
        state = state,
        results = results,
        onAction = { viewModel.actionChannel.trySend(it) },
        onResultClick = onResultClick,
        modifier = modifier,
    )
}

/**
 * The stateless half, so render behaviour is testable without a Koin graph or a signed-in session.
 *
 * Home sits behind authentication, which needs a live Fineract server — so a device screenshot cannot
 * reach this screen. Splitting state out is what lets `SearchBodyUiTest` assert the render on desktop
 * (RULE-TEST-VERIFY-001: render correctness is verified device-free and deterministically).
 */
@Composable
internal fun SearchBodyContent(
    state: SearchState,
    results: ScreenState<List<SearchedEntity>>,
    onAction: (SearchAction) -> Unit,
    modifier: Modifier = Modifier,
    onResultClick: ((SearchedEntity) -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag(TestTags.Home.SEARCH),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AppOutlinedTextField(
            value = state.query,
            onValueChange = { onAction(SearchAction.QueryChanged(it)) },
            label = stringResource(Res.string.feature_home_search_hint),
            leadingIcon = AppIcons.Search,
            showClearIcon = state.query.isNotEmpty(),
            onClickClearIcon = { onAction(SearchAction.Clear) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = { onAction(SearchAction.Submit) },
            ),
            isError = state.blankQuery,
            errorText = if (state.blankQuery) stringResource(Res.string.feature_home_search_blank_query) else null,
            modifier = Modifier.fillMaxWidth().testTag(TestTags.Home.SEARCH_FIELD),
        )

        // Scrolls rather than squeezing: four chips do not fit a phone's width, and the last one
        // wrapped mid-word ("Savin / gs", device 2026-10-25) — a longer translation is worse.
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).testTag(TestTags.Home.SEARCH_FILTERS),
        ) {
            SearchFilter.entries.forEach { filter ->
                FilterChip(
                    selected = state.filter == filter,
                    // Tapping the selected chip clears it — the original's filter was nullable and
                    // there was no other way back to "all resource types".
                    onClick = {
                        val next = if (state.filter == filter) null else filter
                        onAction(SearchAction.FilterChanged(next))
                    },
                    label = { Text(stringResource(filter.labelRes()), maxLines = 1, softWrap = false) },
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = state.exactMatch,
                onCheckedChange = { onAction(SearchAction.ExactMatchToggled) },
                modifier = Modifier.testTag(TestTags.Home.SEARCH_EXACT_MATCH),
            )
            Text(
                text = stringResource(Res.string.feature_home_search_exact_match),
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        ScreenContent(
            state = results,
            onRetry = { onAction(SearchAction.Submit) },
            modifier = Modifier.fillMaxSize(),
        ) { results, _ ->
            LazyColumn(modifier = Modifier.testTag(TestTags.Home.SEARCH_RESULTS)) {
                items(results) { entity ->
                    SearchResultRow(
                        entity = entity,
                        // Null for an entity type with no ported destination, so that row is not
                        // clickable while its siblings are — see [SearchResultRow].
                        onClick = onResultClick?.takeIf { entity.hasPortedDestination() }
                            ?.let { click -> { click(entity) } },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

/**
 * A result row, clickable only when its entity type has a ported destination.
 *
 * ## It took until S4 for this tap to be honest
 *
 * The row shipped deliberately inert in S2 — the original routed a tap to the client, group, centre,
 * loan or savings detail by `entityType`, and none of those existed, so a tap would have been the
 * dead-clickable defect (RULE-IMPL-DEAD-CLICKABLE-001).
 *
 * Three of the five now exist: `CLIENT` → the client profile, `GROUP` → group detail, `LOAN` → the
 * loan account SUMMARY (which is the screen the original's search opened — not the loan profile;
 * see `LoanSummaryViewModel` for why those are different). `SAVING` (S5a) and `CENTER` (2026-10-25)
 * followed, so all five are live — each became clickable the slice its destination landed. That is
 * also what makes `feature/loan` reachable at all: it is registered but, until this tap, nothing
 * in the running app navigated to any loan screen.
 */
@Composable
private fun SearchResultRow(entity: SearchedEntity, onClick: (() -> Unit)?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 12.dp)
            .testTag(TestTags.Home.searchResult(entity.entityId)),
    ) {
        Text(text = entity.description, style = MaterialTheme.typography.bodyLarge)
        entity.entityType?.let {
            Text(text = it, style = MaterialTheme.typography.bodySmall)
        }
    }
}

/**
 * Whether this result's type has somewhere to go.
 *
 * Fineract's `entityType` is upper-case (`"CLIENT"`, `"LOAN"`, …) — matched case-INSENSITIVELY,
 * because this value reaches the app from a server response rather than from a closed enum, and a
 * tenant returning `"Client"` would otherwise silently make every client result inert.
 */
private fun SearchedEntity.hasPortedDestination(): Boolean =
    entityType?.uppercase() in PORTED_SEARCH_ENTITIES

private val PORTED_SEARCH_ENTITIES = setOf(
    Constants.SEARCH_ENTITY_CLIENT,
    Constants.SEARCH_ENTITY_GROUP,
    Constants.SEARCH_ENTITY_LOAN,
    // S5a — the savings summary landed, so a SAVING hit now has somewhere to go.
    Constants.SEARCH_ENTITY_SAVING,
    // Centre detail landed (feature/center) — the original's fifth search destination.
    Constants.SEARCH_ENTITY_CENTER,
)

private fun SearchFilter.labelRes() = when (this) {
    SearchFilter.Clients -> Res.string.feature_home_search_filter_clients
    SearchFilter.Groups -> Res.string.feature_home_search_filter_groups
    SearchFilter.LoanAccounts -> Res.string.feature_home_search_filter_loans
    SearchFilter.SavingsAccounts -> Res.string.feature_home_search_filter_savings
}

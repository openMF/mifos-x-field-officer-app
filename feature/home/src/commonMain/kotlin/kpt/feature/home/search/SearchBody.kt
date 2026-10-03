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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.designsystem.component.MifosOutlinedTextField
import kpt.core.designsystem.icon.MifosIcons
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
    viewModel: SearchViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    SearchBodyContent(
        state = state,
        onAction = { viewModel.actionChannel.trySend(it) },
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
    onAction: (SearchAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag(TestTags.Home.SEARCH),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MifosOutlinedTextField(
            value = state.query,
            onValueChange = { onAction(SearchAction.QueryChanged(it)) },
            label = stringResource(Res.string.feature_home_search_hint),
            leadingIcon = MifosIcons.Search,
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

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.testTag(TestTags.Home.SEARCH_FILTERS),
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
                    label = { Text(stringResource(filter.labelRes())) },
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
            state = state.results,
            onRetry = { onAction(SearchAction.Submit) },
            modifier = Modifier.fillMaxSize(),
        ) { results, _ ->
            LazyColumn(modifier = Modifier.testTag(TestTags.Home.SEARCH_RESULTS)) {
                items(results) { entity ->
                    SearchResultRow(entity)
                    HorizontalDivider()
                }
            }
        }
    }
}

/**
 * A result row.
 *
 * Deliberately NOT clickable in S2. The original opened the client, group, loan or savings detail
 * for the tapped result — none of which exists yet; `client` arrives in S3, `loan` in S4, `savings`
 * in S5, `groups` in S6. Wiring a tap now would mean an `onClick` that goes nowhere, which is the
 * dead-clickable defect (RULE-IMPL-DEAD-CLICKABLE-001). The row gains its tap in S3, with the first
 * destination that can receive it.
 */
@Composable
private fun SearchResultRow(entity: SearchedEntity) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
    ) {
        Text(text = entity.description, style = MaterialTheme.typography.bodyLarge)
        entity.entityType?.let {
            Text(text = it, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun SearchFilter.labelRes() = when (this) {
    SearchFilter.Clients -> Res.string.feature_home_search_filter_clients
    SearchFilter.Groups -> Res.string.feature_home_search_filter_groups
    SearchFilter.LoanAccounts -> Res.string.feature_home_search_filter_loans
    SearchFilter.SavingsAccounts -> Res.string.feature_home_search_filter_savings
}

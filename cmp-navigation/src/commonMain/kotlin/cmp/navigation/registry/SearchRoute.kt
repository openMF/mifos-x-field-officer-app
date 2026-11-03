/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.registry

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import cmp.navigation.generated.resources.Res
import cmp.navigation.generated.resources.cd_navigate_back
import cmp.navigation.generated.resources.search_title
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.composableWithPushTransitions
import kpt.core.common.utils.Constants
import kpt.core.designsystem.icon.AppIcons
import kpt.core.model.objects.SearchedEntity
import kpt.core.ui.components.FabButton
import kpt.core.ui.components.FabButtonState
import kpt.core.ui.components.FabType
import kpt.core.ui.components.MultiFloatingActionButton
import kpt.feature.center.navigation.navigateToCenterDetail
import kpt.feature.center.navigation.navigateToCreateCenter
import kpt.feature.client.navigation.navigateToClientProfile
import kpt.feature.client.navigation.navigateToCreateNewClient
import kpt.feature.groups.navigation.navigateToCreateGroup
import kpt.feature.groups.navigation.navigateToGroupsDetail
import kpt.feature.home.search.SearchBody
import kpt.feature.loan.navigation.navigateToLoanSummary
import kpt.feature.savings.navigation.navigateToSavingsSummary
import org.jetbrains.compose.resources.stringResource

/**
 * Fineract resource search, as its OWN destination reached from the dashboard top bar's magnifier.
 *
 * ## Why it moved here
 *
 * The original app made Search its FIRST TAB (`6b66e8a43` declares SearchTab · ClientTab ·
 * CentersTab · GroupsTab — there is no Home tab), and this port initially matched that by pointing
 * the first tab at `feature/home`, whose body was `SearchBody`. The product decision is now Home
 * first, search on the toolbar — so search needs a destination of its own rather than a tab's body.
 *
 * `SearchBody` itself is unchanged: it is the ported `feature/search` content and keeps owning its
 * own query state, filter and results. This file only gives it a route and a bar to sit under.
 *
 * Registered on the INNER navbar host, so the bottom bar stays visible while searching — the search
 * results lead to Clients, which is a tab, and losing the bar on the way there would be a worse
 * trade than the extra chrome.
 */
@Serializable
data object SearchRoute

internal const val SEARCH_SCREEN_TAG: String = "shell:search"
internal const val SEARCH_CREATE_FAB_TAG: String = "shell:search:create"

/** Opens the global search screen. */
fun NavController.navigateToSearch() = navigate(SearchRoute)

internal fun NavGraphBuilder.searchDestination(
    onBackClick: () -> Unit,
    onResultClick: (SearchedEntity) -> Unit,
    onCreate: ((FabType) -> Unit)? = null,
) {
    composableWithPushTransitions<SearchRoute> {
        SearchScreen(onBackClick = onBackClick, onResultClick = onResultClick, onCreate = onCreate)
    }
}

/** The search FAB's three creates — the original's `onFabClick` on its search screen. */
internal fun NavController.navigateToCreate(type: FabType) {
    when (type) {
        FabType.CLIENT -> navigateToCreateNewClient()
        FabType.CENTER -> navigateToCreateCenter()
        FabType.GROUP -> navigateToCreateGroup()
    }
}

/**
 * Route a tapped search result to its detail, by Fineract's `entityType`.
 *
 * This is the original's `SearchNavigation.onSearchOptionClick` `when`, which lived in the search
 * feature and so made `feature/search` import `feature/client`, `feature/loan` and the rest. It
 * lives in the SHELL here for the reason every other cross-feature route does: the shell is the
 * only layer allowed to know about more than one feature.
 *
 * All five types have a destination (`CENTER` since 2026-10-25). `SearchBody` asks the same
 * question before drawing the tap, so a row that reaches this function always has somewhere to go.
 * Comparison is case-insensitive: the value comes
 * off the wire, not out of an enum.
 */
internal fun NavController.navigateToSearchResult(entity: SearchedEntity) {
    when (entity.entityType?.uppercase()) {
        Constants.SEARCH_ENTITY_CLIENT -> navigateToClientProfile(entity.entityId)
        Constants.SEARCH_ENTITY_GROUP -> navigateToGroupsDetail(entity.entityId)
        // The SUMMARY, not the loan profile — that is the screen the original's search opened, and
        // the two are different screens over the same loan (see `LoanSummaryViewModel`).
        Constants.SEARCH_ENTITY_LOAN -> navigateToLoanSummary(entity.entityId)
        // S5a. The search result carries no deposit TYPE, so this takes the plain-savings default;
        // a recurring-deposit account reached this way would address the wrong endpoint, which is
        // why the client's savings list (S5d) passes `depositType.endpoint` explicitly.
        Constants.SEARCH_ENTITY_SAVING -> navigateToSavingsSummary(entity.entityId)
        Constants.SEARCH_ENTITY_CENTER -> navigateToCenterDetail(entity.entityId)
        else -> Unit
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchScreen(
    onBackClick: () -> Unit,
    onResultClick: (SearchedEntity) -> Unit,
    modifier: Modifier = Modifier,
    onCreate: ((FabType) -> Unit)? = null,
) {
    var fabState by remember { mutableStateOf<FabButtonState>(FabButtonState.Collapsed) }
    Scaffold(
        modifier = modifier.testTag(SEARCH_SCREEN_TAG),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(Res.string.search_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = AppIcons.ArrowBack,
                            contentDescription = stringResource(Res.string.cd_navigate_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        // The original's `+` FAB on search: create client / centre / group. It came back with the
        // three create flows; absent (not inert) when the shell supplies no destination.
        floatingActionButton = {
            if (onCreate != null) {
                MultiFloatingActionButton(
                    fabButtons = listOf(
                        FabButton.VectorFab(type = FabType.CLIENT, iconRes = AppIcons.Person),
                        FabButton.VectorFab(type = FabType.CENTER, iconRes = AppIcons.buildingIcon),
                        FabButton.VectorFab(type = FabType.GROUP, iconRes = AppIcons.People),
                    ),
                    fabButtonState = fabState,
                    onFabButtonStateChange = { fabState = it },
                    modifier = Modifier.testTag(SEARCH_CREATE_FAB_TAG),
                    onFabClick = { type ->
                        fabState = FabButtonState.Collapsed
                        onCreate(type)
                    },
                )
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            SearchBody(onResultClick = onResultClick)
        }
    }
}

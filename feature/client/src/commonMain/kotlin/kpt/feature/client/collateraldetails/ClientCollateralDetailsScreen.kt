/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.collateraldetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.theme.AppTypography
import kpt.core.model.shared.CollateralItemResult
import kpt.core.ui.components.AppActionsCollateralDataListingComponent
import kpt.feature.client.generated.resources.Res
import kpt.feature.client.generated.resources.feature_client_general_collateral_title
import kpt.feature.client.generated.resources.feature_client_identifiers_item
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Carried over from
 * `6b66e8a43:feature/client/clientCollateralDetails/ClientCollateralDetailScreen.kt` — the header
 * with its count and one `AppActionsCollateralDataListingComponent` per pledged item, keyed by
 * `collateralId`, with the original's spacing.
 *
 * `menuList = emptyList()` is the original's: the rows are read-only, so the component renders no
 * action menu. Kept — an empty menu is not a dead control, it is the absence of one.
 */
@Composable
internal fun ClientCollateralDetailsScreen(
    // Added 2026-10-23 along with the AppScaffold below. This screen is read-only and had no
    // navigation callback of ANY kind, no app bar and no action — reached from the client detail's
    // "Collateral Data" row it was a complete dead end, escapable only by the system back gesture.
    // Its four scaffold-less siblings (closure / transfer / collateral / staff) at least had a
    // Back button in their populated branch; this one had nothing in any branch.
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ClientCollateralDetailsViewModel = koinViewModel(),
) {
    val collaterals by viewModel.collaterals.collectAsStateWithLifecycle()

    AppScaffold(
        title = stringResource(Res.string.feature_client_general_collateral_title),
        onBackPressed = onNavigateBack,
    ) { scaffoldPadding ->
        ClientCollateralDetailsContent(
            collaterals = collaterals,
            onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
            modifier = modifier.padding(scaffoldPadding),
        )
    }
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun ClientCollateralDetailsContent(
    collaterals: ScreenState<List<CollateralItemResult>>,
    onAction: (ClientCollateralDetailsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenContent(
        state = collaterals,
        onRetry = { onAction(ClientCollateralDetailsAction.OnRetry) },
        modifier = modifier.fillMaxSize().testTag(ClientCollateralDetailsTestTags.SCREEN),
    ) { items, _ ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = KptTheme.spacing.md),
        ) {
            Header(items.size.toString())

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(top = KptTheme.spacing.md),
                verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
            ) {
                items(items = items, key = { it.collateralId }) { item ->
                    val (total, totalCollateral) = collateralRowTotals(item)
                    AppActionsCollateralDataListingComponent(
                        name = item.name,
                        quantity = item.quantity.toString(),
                        totalValue = total.toString(),
                        totalCollateralValue = totalCollateral.toString(),
                        // Read-only rows, as in the original.
                        menuList = emptyList(),
                        onActionClicked = {},
                    )
                }
            }
        }
    }
}

@Composable
private fun Header(totalItem: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column {
            Text(
                text = stringResource(Res.string.feature_client_general_collateral_title),
                style = AppTypography.titleMedium,
            )
            Text(
                text = totalItem + " " + stringResource(Res.string.feature_client_identifiers_item),
                style = AppTypography.labelMedium,
                modifier = Modifier.testTag(ClientCollateralDetailsTestTags.COUNT),
            )
        }
    }
}

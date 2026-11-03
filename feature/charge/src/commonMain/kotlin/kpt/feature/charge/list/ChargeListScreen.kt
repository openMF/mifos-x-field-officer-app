/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.charge.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.base.ui.screen.RefreshableScreenContent
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.icon.AppIcons
import kpt.core.ui.components.Actions
import kpt.core.ui.components.AppActionsChargeListingComponent
import kpt.core.ui.components.AppEmptyUi
import kpt.core.ui.components.AppProgressIndicatorOverlay
import kpt.feature.charge.generated.resources.Res
import kpt.feature.charge.generated.resources.feature_charge_add_charge
import kpt.feature.charge.generated.resources.feature_charge_cancel
import kpt.feature.charge.generated.resources.feature_charge_charges
import kpt.feature.charge.generated.resources.feature_charge_delete
import kpt.feature.charge.generated.resources.feature_charge_delete_confirmation
import kpt.feature.charge.generated.resources.feature_charge_delete_title
import kpt.feature.charge.generated.resources.feature_charge_dismiss
import kpt.feature.charge.generated.resources.feature_charge_error_failed
import kpt.feature.charge.generated.resources.feature_charge_error_offline
import kpt.feature.charge.generated.resources.feature_charge_no_charges
import kpt.feature.charge.generated.resources.feature_charge_no_due_date
import kpt.feature.charge.generated.resources.feature_charge_outstanding
import kpt.feature.charge.ui.ChargeUi
import kpt.feature.charge.ui.TestTags
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Carried over from the original's charge list — `ShowClientCharge` / the `ShowChargeBottomSheet`
 * of `6b66e8a43:feature/client/charges/ChargesScreen.kt`: one `ChargeListingComponent` per charge
 * (name, calculation type, due date, amount) that expands to its actions.
 *
 * ## The list IS the screen, not a sheet behind a "N Charges — View" row
 *
 * The original opened on the add-charge FORM with a "3 Charges [View]" row under it, and the list
 * lived in a bottom sheet that row opened. Every caller here wants the list (the client's "Upcoming
 * Charges", the loan's "Charges", the savings summary), so the list is the destination and the form
 * is its "+" — the shape the loan screen already had.
 *
 * Row actions: View Details · Edit (only where the resource supports it) · Delete (confirmed).
 */
@Composable
internal fun ChargeListScreen(
    onBackPressed: () -> Unit,
    onAddCharge: (resourceType: String, resourceId: Int) -> Unit,
    onOpenCharge: (resourceType: String, resourceId: Int, chargeId: Int) -> Unit,
    onEditCharge: (resourceType: String, resourceId: Int, chargeId: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChargeListViewModel = koinViewModel(),
) {
    val charges by viewModel.charges.collectAsStateWithLifecycle()
    val ui by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            ChargeListEvent.NavigateBack -> onBackPressed()
            is ChargeListEvent.AddCharge -> onAddCharge(event.resourceType, event.resourceId)
            is ChargeListEvent.OpenCharge -> onOpenCharge(event.resourceType, event.resourceId, event.chargeId)
            is ChargeListEvent.EditCharge -> onEditCharge(event.resourceType, event.resourceId, event.chargeId)
        }
    }

    ChargeListContent(
        charges = charges,
        ui = ui,
        canEdit = viewModel.canEdit,
        onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
        modifier = modifier,
    )
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun ChargeListContent(
    charges: ScreenState<List<ChargeUi>>,
    ui: ChargeListState,
    canEdit: Boolean,
    onAction: (ChargeListAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppScaffold(
        title = stringResource(Res.string.feature_charge_charges),
        onBackPressed = { onAction(ChargeListAction.NavigateBack) },
        modifier = modifier.testTag(TestTags.ChargeList.SCREEN),
        actions = {
            IconButton(
                onClick = { onAction(ChargeListAction.OnAddCharge) },
                modifier = Modifier.testTag(TestTags.ChargeList.ADD),
            ) {
                Icon(
                    imageVector = AppIcons.Add,
                    contentDescription = stringResource(Res.string.feature_charge_add_charge),
                )
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            Column(Modifier.fillMaxSize()) {
                // A failed WRITE is shown ABOVE the list, never instead of it: the charges are still
                // correct. The original swapped the whole screen for an error component on a failed
                // delete.
                ui.writeError?.let { error ->
                    WriteErrorBanner(error = error, onDismiss = { onAction(ChargeListAction.DismissDialog) })
                }

                RefreshableScreenContent(
                    state = charges,
                    onRefresh = { onAction(ChargeListAction.OnRefresh) },
                    modifier = Modifier.fillMaxSize().testTag(TestTags.ChargeList.CONTENT),
                    empty = {
                        AppEmptyUi(
                            text = stringResource(Res.string.feature_charge_no_charges),
                            icon = AppIcons.FileTask,
                            modifier = Modifier.testTag(TestTags.ChargeList.EMPTY),
                        )
                    },
                ) { rows, _ ->
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().testTag(TestTags.ChargeList.LIST),
                        contentPadding = PaddingValues(KptTheme.spacing.md),
                        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
                    ) {
                        items(rows, key = { it.id }) { charge ->
                            ChargeRow(
                                charge = charge,
                                expanded = ui.expandedId == charge.id,
                                canEdit = canEdit,
                                onAction = onAction,
                            )
                        }
                    }
                }
            }
            // The original's `MifosProgressIndicatorOverlay` while the delete is in flight — over
            // the list, which stays put; on success the row leaves by itself (the command clears
            // it in the table the stream observes), so nothing re-reads.
            if (ui.isDeleting) {
                AppProgressIndicatorOverlay(Modifier.fillMaxSize().testTag(TestTags.ChargeList.DELETING))
            }
        }
    }

    if (ui.pendingDeleteId != null) {
        AlertDialog(
            onDismissRequest = { onAction(ChargeListAction.DismissDialog) },
            modifier = Modifier.testTag(TestTags.ChargeList.DELETE_DIALOG),
            title = { Text(stringResource(Res.string.feature_charge_delete_title)) },
            text = { Text(stringResource(Res.string.feature_charge_delete_confirmation)) },
            confirmButton = {
                TextButton(onClick = { onAction(ChargeListAction.ConfirmDelete) }) {
                    Text(stringResource(Res.string.feature_charge_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(ChargeListAction.DismissDialog) }) {
                    Text(stringResource(Res.string.feature_charge_cancel))
                }
            },
        )
    }
}

/**
 * One charge. Tapping it reveals its actions rather than navigating — the original's interaction,
 * kept because a destructive Delete one mis-tap away in a scrolling list is the alternative.
 *
 * The original passed `formattedDueDate` as BOTH the date and the "collected on" slot, so a row
 * printed its due date twice. The second slot carries what is still owed.
 */
@Composable
internal fun ChargeRow(
    charge: ChargeUi,
    expanded: Boolean,
    canEdit: Boolean,
    onAction: (ChargeListAction) -> Unit,
) {
    Box(Modifier.testTag(TestTags.ChargeList.row(charge.id))) {
        AppActionsChargeListingComponent(
            chargeTitle = charge.name,
            type = charge.calculation,
            date = charge.dueDate ?: stringResource(Res.string.feature_charge_no_due_date),
            collectedOn = stringResource(Res.string.feature_charge_outstanding, charge.outstanding),
            amount = charge.amount,
            menuList = buildList {
                add(Actions.ViewDetails())
                if (canEdit) add(Actions.Edit())
                add(Actions.Delete())
            },
            onActionClicked = { action ->
                when (action) {
                    is Actions.ViewDetails -> onAction(ChargeListAction.OnViewCharge(charge.id))
                    is Actions.Edit -> onAction(ChargeListAction.OnEditCharge(charge.id))
                    is Actions.Delete -> onAction(ChargeListAction.OnDeleteCharge(charge.id))
                    else -> Unit
                }
            },
            isExpanded = expanded,
            onExpandToggle = { onAction(ChargeListAction.ToggleExpanded(charge.id)) },
        )
    }
}

@Composable
private fun WriteErrorBanner(error: ChargeWriteError, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = KptTheme.spacing.md, vertical = KptTheme.spacing.sm)
            .testTag(TestTags.ChargeList.WRITE_ERROR),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = when (error) {
                ChargeWriteError.Offline -> stringResource(Res.string.feature_charge_error_offline)
                is ChargeWriteError.Failed ->
                    error.serverMessage ?: stringResource(Res.string.feature_charge_error_failed)
            },
            style = KptTheme.typography.bodySmall,
            color = KptTheme.colorScheme.error,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onDismiss) {
            Text(stringResource(Res.string.feature_charge_dismiss))
        }
    }
}

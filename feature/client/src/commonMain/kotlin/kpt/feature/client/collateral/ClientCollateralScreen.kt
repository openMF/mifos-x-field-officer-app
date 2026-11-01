/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.collateral

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.submit.draft.SavedFormDraft
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.base.ui.generated.resources.draft_save
import kpt.core.base.ui.generated.resources.draft_saved
import kpt.core.base.ui.generated.resources.draft_saved_offline
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.base.ui.submit.FormDraftBanner
import kpt.core.designsystem.fieldofficer.component.AppOutlinedTextField
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.fieldofficer.component.AppTextFieldDropdown
import kpt.core.designsystem.theme.AppTypography
import kpt.core.designsystem.theme.DesignToken
import kpt.core.model.shared.CollateralItem
import kpt.core.ui.components.AppProgressIndicatorOverlay
import kpt.core.ui.components.AppStatusDialog
import kpt.core.ui.components.AppTwoButtonRow
import kpt.core.ui.components.ResultStatus
import kpt.feature.client.generated.resources.Res
import kpt.feature.client.generated.resources.feature_client_btn_back
import kpt.feature.client.generated.resources.feature_client_btn_submit
import kpt.feature.client.generated.resources.feature_client_collateral_base_price
import kpt.feature.client.generated.resources.feature_client_collateral_choose_type
import kpt.feature.client.generated.resources.feature_client_collateral_failure_title
import kpt.feature.client.generated.resources.feature_client_collateral_name
import kpt.feature.client.generated.resources.feature_client_collateral_no_options
import kpt.feature.client.generated.resources.feature_client_collateral_pct_to_base
import kpt.feature.client.generated.resources.feature_client_collateral_quality
import kpt.feature.client.generated.resources.feature_client_collateral_quantity
import kpt.feature.client.generated.resources.feature_client_collateral_success_message
import kpt.feature.client.generated.resources.feature_client_collateral_success_title
import kpt.feature.client.generated.resources.feature_client_collateral_title
import kpt.feature.client.generated.resources.feature_client_collateral_total
import kpt.feature.client.generated.resources.feature_client_collateral_total_collateral
import kpt.feature.client.generated.resources.feature_client_collateral_unit_type
import kpt.feature.client.generated.resources.feature_client_dialog_continue
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kpt.core.base.ui.generated.resources.Res as BaseRes

/**
 * Carried over from `6b66e8a43:feature/client/clientCollateral/ClientCollateralScreen.kt` under
 * §6a — the dropdown, the five read-only mirror fields, the quantity field, the two computed
 * totals, `AppTwoButtonRow` and the overlay spinner are the original's, in its order.
 *
 * Beyond §6a's three adaptations, two call sites changed because the ViewModel's defects were
 * fixed (see its header):
 *
 * - the dropdown no longer re-fires `OnQuantityChange(state.quantity)` from `onValueChanged` to
 *   force the totals to recompute. They are derived by [collateralTotals] from the selected item,
 *   so selecting a type updates them by itself.
 * - `OnSave` carries the selected collateral id rather than letting the ViewModel re-index.
 */
@Composable
internal fun ClientCollateralScreen(
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ClientCollateralViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val offeredDraft by viewModel.offeredDraft.collectAsStateWithLifecycle()
    val collaterals by viewModel.collaterals.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            ClientCollateralEvent.NavigateBack -> onNavigateBack()
            ClientCollateralEvent.NavigateNext -> onNavigateNext()
        }
    }

    // Wrapped in AppScaffold 2026-10-23. This screen had NO app bar at all — no title and no
    // back arrow — and every one of its empty branches returns before the Back/Submit row, so an
    // officer who reached it on a tenant with nothing configured had no way out but the system
    // back gesture. Found on device: the client Closure screen rendered as the single line
    // "No closure reasons found" with zero clickable nodes in the whole hierarchy.
    //
    // Not a §6a deviation. The originals were Fragments and took their app bar from the hosting
    // Activity's toolbar; AppScaffold IS that toolbar in the ported world, which is why every
    // other ported screen has one. These five simply missed it.
    AppScaffold(
        title = stringResource(Res.string.feature_client_collateral_title),
        onBackPressed = { viewModel.trySendAction(ClientCollateralAction.NavigateBack) },
    ) { scaffoldPadding ->
        ClientCollateralContent(
            state = state,
            collaterals = collaterals,
            onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
            offeredDraft = offeredDraft,
            modifier = modifier.padding(scaffoldPadding),
        )
    }
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun ClientCollateralContent(
    state: ClientCollateralState,
    collaterals: ScreenState<List<CollateralItem>>,
    onAction: (ClientCollateralAction) -> Unit,
    modifier: Modifier = Modifier,
    offeredDraft: SavedFormDraft? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = KptTheme.spacing.md)
            .testTag(ClientCollateralTestTags.SCREEN),
    ) {
        Text(
            text = stringResource(Res.string.feature_client_collateral_title),
            style = AppTypography.labelLargeEmphasized,
            modifier = Modifier.padding(horizontal = KptTheme.spacing.md),
        )

        Spacer(modifier = Modifier.height(DesignToken.spacing.medium))

        ScreenContent(
            state = collaterals,
            onRetry = { onAction(ClientCollateralAction.OnRetry) },
            modifier = Modifier.fillMaxWidth().weight(1f),
        ) { items, _ ->
            if (items.isEmpty()) {
                Text(
                    text = stringResource(Res.string.feature_client_collateral_no_options),
                    modifier = Modifier.testTag(ClientCollateralTestTags.NO_OPTIONS),
                )
                return@ScreenContent
            }

            val selected = items[state.currentSelectedIndex.coerceIn(items.indices)]
            val (total, totalCollateral) = collateralTotals(selected, state.quantity)

            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = KptTheme.spacing.md),
            ) {
                offeredDraft?.let {
                    FormDraftBanner(
                        failureReason = it.errorMessage,
                        onResume = { onAction(ClientCollateralAction.ResumeDraft) },
                        onDiscard = { onAction(ClientCollateralAction.DiscardDraft) },
                    )
                }
                AppTextFieldDropdown(
                    value = selected.name,
                    onOptionSelected = { index, _ -> onAction(ClientCollateralAction.OptionChanged(index)) },
                    options = items.map { it.name },
                    label = stringResource(Res.string.feature_client_collateral_choose_type),
                    modifier = Modifier.fillMaxWidth().testTag(ClientCollateralTestTags.TYPE),
                )

                // Five read-only mirrors of the selected type. The original rendered all five and
                // they are kept verbatim — including `name`, which repeats the dropdown's value.
                ReadOnlyField(selected.name, stringResource(Res.string.feature_client_collateral_name))
                ReadOnlyField(selected.quality, stringResource(Res.string.feature_client_collateral_quality))
                ReadOnlyField(selected.unitType, stringResource(Res.string.feature_client_collateral_unit_type))
                ReadOnlyField("${selected.basePrice}", stringResource(Res.string.feature_client_collateral_base_price))
                ReadOnlyField(
                    selected.pctToBase.toString(),
                    stringResource(Res.string.feature_client_collateral_pct_to_base),
                )

                AppOutlinedTextField(
                    value = state.quantity,
                    onValueChange = { onAction(ClientCollateralAction.OnQuantityChange(it)) },
                    isError = state.quantityError != null,
                    errorText = state.quantityError?.let { stringResource(it) },
                    label = stringResource(Res.string.feature_client_collateral_quantity),
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.fillMaxWidth().testTag(ClientCollateralTestTags.QUANTITY),
                )

                // Derived, not stored — so they follow the selected TYPE as well as the quantity.
                ReadOnlyField(
                    total.toString(),
                    stringResource(Res.string.feature_client_collateral_total),
                    ClientCollateralTestTags.TOTAL,
                )
                ReadOnlyField(
                    totalCollateral.toString(),
                    stringResource(Res.string.feature_client_collateral_total_collateral),
                    ClientCollateralTestTags.TOTAL_COLLATERAL,
                )
            }
        }

        val items = (collaterals as? ScreenState.Content)?.data
        if (!items.isNullOrEmpty()) {
            val selected = items[state.currentSelectedIndex.coerceIn(items.indices)]
            AppTwoButtonRow(
                firstBtnText = stringResource(Res.string.feature_client_btn_back),
                secondBtnText = stringResource(Res.string.feature_client_btn_submit),
                onFirstBtnClick = { onAction(ClientCollateralAction.NavigateBack) },
                onSecondBtnClick = { onAction(ClientCollateralAction.OnSave(selected.id)) },
                isSecondButtonEnabled = !state.isOverlayLoading,
                modifier = Modifier
                    .padding(horizontal = KptTheme.spacing.md)
                    .testTag(ClientCollateralTestTags.BUTTONS),
            )
        }
    }

    if (state.isOverlayLoading) {
        AppProgressIndicatorOverlay()
    }

    when (val dialog = state.dialogState) {
        null -> Unit
        is ClientCollateralState.DialogState.ShowStatusDialog -> AppStatusDialog(
            status = dialog.status,
            btnText = stringResource(Res.string.feature_client_dialog_continue),
            onConfirm = {
                onAction(
                    if (dialog.status == ResultStatus.SUCCESS) {
                        ClientCollateralAction.OnNext
                    } else {
                        ClientCollateralAction.CloseDialog
                    },
                )
            },
            onDismissRequest = { onAction(ClientCollateralAction.CloseDialog) },
            successTitle = stringResource(Res.string.feature_client_collateral_success_title),
            successMessage = stringResource(Res.string.feature_client_collateral_success_message),
            failureTitle = stringResource(Res.string.feature_client_collateral_failure_title),
            failureMessage = dialog.msg,
            secondaryBtnText = stringResource(BaseRes.string.draft_save)
                .takeIf { dialog.status == ResultStatus.FAILURE && !dialog.draftSaved },
            onSecondary = { onAction(ClientCollateralAction.SaveDraft) },
            note = when {
                !dialog.draftSaved -> null
                dialog.offline -> stringResource(BaseRes.string.draft_saved_offline)
                else -> stringResource(BaseRes.string.draft_saved)
            },
            modifier = Modifier.fillMaxSize().testTag(ClientCollateralTestTags.STATUS_DIALOG),
        )
    }
}

/**
 * The original's read-only mirror field, extracted — it appeared seven times with identical
 * arguments except value and label. Same component, same `readOnly`/`enabled` pair.
 */
@Composable
private fun ReadOnlyField(value: String, label: String, testTag: String? = null) {
    AppOutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        enabled = false,
        label = label,
        modifier = if (testTag == null) Modifier else Modifier.testTag(testTag),
    )
}

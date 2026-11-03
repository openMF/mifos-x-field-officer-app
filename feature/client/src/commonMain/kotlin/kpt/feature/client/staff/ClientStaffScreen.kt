/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.staff

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.designsystem.fieldofficer.component.AppOutlinedButton
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.fieldofficer.component.AppTextButton
import kpt.core.designsystem.fieldofficer.component.AppTextFieldDropdown
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.AppTypography
import kpt.core.designsystem.theme.DesignToken
import kpt.core.model.client.StaffOption
import kpt.core.ui.components.AppStatusDialog
import kpt.feature.client.generated.resources.Res
import kpt.feature.client.generated.resources.feature_client_assign_staff
import kpt.feature.client.generated.resources.feature_client_btn_back
import kpt.feature.client.generated.resources.feature_client_btn_submit
import kpt.feature.client.generated.resources.feature_client_cannot_assign_staff
import kpt.feature.client.generated.resources.feature_client_choose_staff
import kpt.feature.client.generated.resources.feature_client_dialog_continue
import kpt.feature.client.generated.resources.feature_client_staff_assign_failure_title
import kpt.feature.client.generated.resources.feature_client_staff_assign_success_message
import kpt.feature.client.generated.resources.feature_client_staff_assign_success_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Carried over from `6b66e8a43:feature/client/clientStaff/ClientStaffScreen.kt` under §6a of the
 * migration spec — layout, components, spacing, button order and copy are the original's.
 *
 * The three permitted adaptations:
 *
 * 1. **State read** — the read's `DialogState.Loading` / `.Error` dialogs become [ScreenState]
 *    rendered through [ScreenContent], which also supplies retry. `DialogState.ShowStatusDialog`
 *    stays a dialog, because it is one.
 * 2. **Action dispatch** — unchanged in shape; the original was already on `trySendAction`.
 *    `OnSubmit` now carries the selected staff id rather than letting the ViewModel re-index.
 * 3. **Unported destinations** — none here. `onNavigateNext` is the client-detail return and is
 *    wired.
 *
 * `AppBreadcrumbNavBar(navController)` is DROPPED, and this is the one structural difference:
 * it takes a `NavController` and reads the live back stack, which makes the screen untestable
 * without a NavHost and is not available to the stateless half. Every ported screen in this
 * codebase omits it for the same reason; it returns with the Stitch pass.
 */
@Composable
internal fun ClientStaffScreen(
    onNavigateBack: () -> Unit,
    onNavigateNext: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ClientStaffViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val options by viewModel.staffOptions.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            ClientStaffEvent.NavigateBack -> onNavigateBack()
            ClientStaffEvent.NavigateNext -> onNavigateNext(state.id)
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
        title = stringResource(Res.string.feature_client_assign_staff),
        onBackPressed = { viewModel.trySendAction(ClientStaffAction.NavigateBack) },
    ) { scaffoldPadding ->
        ClientStaffContent(
            state = state,
            options = options,
            onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
            modifier = modifier.padding(scaffoldPadding),
        )
    }
}

/** Stateless half — rendered in tests and previews without Koin or a signed-in session. */
@Composable
internal fun ClientStaffContent(
    state: ClientStaffState,
    options: ScreenState<List<StaffOption>>,
    onAction: (ClientStaffAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenContent(
        state = options,
        onRetry = { onAction(ClientStaffAction.OnRetry) },
        modifier = modifier.fillMaxSize().testTag(ClientStaffTestTags.SCREEN),
    ) { staffOptions, _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = KptTheme.spacing.md),
        ) {
            if (staffOptions.isNotEmpty()) {
                // Clamped, unlike the original. A held index against a list that shrank on refresh
                // is an IndexOutOfBounds crash, and a crash is not UI fidelity.
                val selected = staffOptions[state.currentSelectedIndex.coerceIn(staffOptions.indices)]

                Text(
                    text = stringResource(Res.string.feature_client_assign_staff),
                    style = AppTypography.labelLargeEmphasized,
                )
                Spacer(Modifier.height(DesignToken.padding.largeIncreased))
                AppTextFieldDropdown(
                    value = selected.displayName,
                    onOptionSelected = { index, _ -> onAction(ClientStaffAction.OptionChanged(index)) },
                    options = staffOptions.map { it.displayName },
                    label = stringResource(Res.string.feature_client_choose_staff),
                    modifier = Modifier.fillMaxWidth().testTag(ClientStaffTestTags.DROPDOWN),
                )
                Spacer(Modifier.height(DesignToken.padding.largeIncreased))
                Row(modifier = Modifier.fillMaxWidth()) {
                    AppOutlinedButton(
                        onClick = { onAction(ClientStaffAction.NavigateBack) },
                        leadingIcon = {
                            Icon(
                                imageVector = AppIcons.ChevronLeft,
                                contentDescription = null,
                                modifier = Modifier.size(DesignToken.sizes.iconAverage),
                                tint = KptTheme.colorScheme.primary,
                            )
                        },
                        text = {
                            Text(
                                text = stringResource(Res.string.feature_client_btn_back),
                                color = KptTheme.colorScheme.primary,
                                style = AppTypography.labelLarge,
                            )
                        },
                        modifier = Modifier.weight(1f).testTag(ClientStaffTestTags.BACK),
                    )
                    Spacer(Modifier.padding(KptTheme.spacing.sm))
                    AppTextButton(
                        onClick = { onAction(ClientStaffAction.OnSubmit(selected.id)) },
                        enabled = !state.isSubmitting,
                        leadingIcon = {
                            Icon(
                                imageVector = AppIcons.Check,
                                contentDescription = null,
                                modifier = Modifier.size(DesignToken.sizes.iconAverage),
                            )
                        },
                        text = {
                            Text(
                                text = stringResource(Res.string.feature_client_btn_submit),
                                style = AppTypography.labelLarge,
                            )
                        },
                        modifier = Modifier.weight(1f).testTag(ClientStaffTestTags.SUBMIT),
                    )
                }
            } else {
                // The server returned a template with no assignable officers — a real answer, not
                // an empty state, which is why the stream does not mark it Empty.
                Text(
                    text = stringResource(Res.string.feature_client_cannot_assign_staff),
                    modifier = Modifier.testTag(ClientStaffTestTags.NO_STAFF),
                )
            }
        }
    }

    ClientStaffDialogs(state = state, onAction = onAction)
}

@Composable
private fun ClientStaffDialogs(
    state: ClientStaffState,
    onAction: (ClientStaffAction) -> Unit,
) {
    when (val dialog = state.dialogState) {
        null -> Unit
        is ClientStaffState.DialogState.ShowStatusDialog -> AppStatusDialog(
            status = dialog.status,
            btnText = stringResource(Res.string.feature_client_dialog_continue),
            // The original drove navigation from the SUCCESS dialog's only button and gave the
            // failure dialog the same one — so acknowledging a failure also navigated forward. The
            // button is wired per status here: forward on success, dismiss on failure.
            onConfirm = {
                onAction(
                    if (dialog.status == kpt.core.ui.components.ResultStatus.SUCCESS) {
                        ClientStaffAction.OnNext
                    } else {
                        ClientStaffAction.CloseDialog
                    },
                )
            },
            onDismissRequest = { onAction(ClientStaffAction.CloseDialog) },
            successTitle = stringResource(Res.string.feature_client_staff_assign_success_title),
            successMessage = stringResource(Res.string.feature_client_staff_assign_success_message),
            failureTitle = stringResource(Res.string.feature_client_staff_assign_failure_title),
            failureMessage = dialog.msg,
            modifier = Modifier.testTag(ClientStaffTestTags.STATUS_DIALOG),
        )
    }
}

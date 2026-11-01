/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.defaultaccount

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import kpt.core.base.store.submit.draft.SavedFormDraft
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.base.ui.generated.resources.draft_save
import kpt.core.base.ui.generated.resources.draft_saved
import kpt.core.base.ui.generated.resources.draft_saved_offline
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.base.ui.submit.FormDraftBanner
import kpt.core.designsystem.fieldofficer.component.AppOutlinedButton
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.fieldofficer.component.AppTextButton
import kpt.core.designsystem.fieldofficer.component.AppTextFieldDropdown
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.AppTypography
import kpt.core.designsystem.theme.DesignToken
import kpt.core.model.client.SavingAccountOption
import kpt.core.ui.components.AppStatusDialog
import kpt.core.ui.components.ResultStatus
import kpt.feature.client.generated.resources.Res
import kpt.feature.client.generated.resources.feature_client_btn_back
import kpt.feature.client.generated.resources.feature_client_btn_submit
import kpt.feature.client.generated.resources.feature_client_defaultaccount_choose
import kpt.feature.client.generated.resources.feature_client_defaultaccount_failure_title
import kpt.feature.client.generated.resources.feature_client_defaultaccount_no_savings_accounts
import kpt.feature.client.generated.resources.feature_client_defaultaccount_success_message
import kpt.feature.client.generated.resources.feature_client_defaultaccount_success_title
import kpt.feature.client.generated.resources.feature_client_dialog_continue
import kpt.feature.client.generated.resources.feature_client_update_default_account_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kpt.core.base.ui.generated.resources.Res as BaseRes

/**
 * Nominate one of a client's savings accounts as their default — carried over from
 * `6b66e8a43:feature/client/clientUpdateDefaultAccount/ClientUpdateDefaultAccountScreen.kt` under
 * §6a of the migration spec. Layout, components, spacing, button order, icons and copy are the
 * original's: a heading, one dropdown of account numbers, and a Back / Submit row.
 *
 * See [ClientDefaultAccountViewModel] for the state-plumbing changes. The screen-side ones:
 *
 * 1. **State read** — the read's `DialogState.Loading` spinner and `DialogState.Error` panel, which
 *    the original drew by hiding the whole form behind an `if`, become [ScreenState] rendered
 *    through [ScreenContent], which also supplies the retry the original's error component had.
 *    `DialogState.ShowStatusDialog` stays a dialog, because it is one.
 * 2. **Selection** — the dropdown's `onOptionSelected` index is resolved to the account's ID at the
 *    instant of the tap, against the very list being rendered, and the id is what travels. The
 *    original stored the index and re-indexed at submit time; [selectionFor] explains why that
 *    could write to the wrong account.
 * 3. **Unported destinations** — none. `onNavigateNext` is the return to client detail and is
 *    wired, so the success dialog's button is live.
 *
 * `MifosBreadcrumbNavBar(navController)` is DROPPED and the `NavController` parameter with it: it
 * reads the live back stack, which makes the screen untestable without a NavHost and unavailable
 * to the stateless half. Every ported screen in this codebase omits it for the same reason. The
 * [AppScaffold] top bar takes its place at the top of the screen, which is why the original's
 * heading [Text] is kept below it rather than folded into the bar — the form keeps the two-line
 * structure it had, and the title string is the one the client-detail row already uses for this
 * destination.
 */
@Composable
internal fun ClientDefaultAccountScreen(
    onNavigateBack: () -> Unit,
    /**
     * Return to the client detail the officer came from.
     *
     * Takes NO client id. It used to carry one, inherited from the original's forward navigation
     * (`navigateToClientDetails(clientId)`), which re-pushed a detail screen on TOP of the detail
     * already in the back stack. This destination pops instead — the live detail screen behind
     * re-reads and shows the new default — so the id had no reader, and a parameter no caller can
     * use is a signature that misleads the next one. Matches the `editdetails` sibling.
     */
    onNavigateNext: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ClientDefaultAccountViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val offeredDraft by viewModel.offeredDraft.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val defaultAccountId by viewModel.defaultAccountId.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            ClientDefaultAccountEvent.NavigateBack -> onNavigateBack()
            ClientDefaultAccountEvent.NavigateNext -> onNavigateNext()
        }
    }

    AppScaffold(
        title = stringResource(Res.string.feature_client_update_default_account_title),
        // Dispatched, not called directly — the ViewModel owns the event, so the hardware back
        // gesture and this button take the same path.
        onBackPressed = { viewModel.trySendAction(ClientDefaultAccountAction.NavigateBack) },
    ) { scaffoldPadding ->
        ClientDefaultAccountContent(
            state = state,
            accounts = accounts,
            defaultAccountId = defaultAccountId,
            onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
            offeredDraft = offeredDraft,
            modifier = modifier,
            scaffoldPadding = scaffoldPadding,
        )
    }
}

/** Stateless half — rendered in tests and previews without Koin or a signed-in session. */
@Composable
internal fun ClientDefaultAccountContent(
    state: ClientDefaultAccountState,
    accounts: ScreenState<List<SavingAccountOption>>,
    onAction: (ClientDefaultAccountAction) -> Unit,
    modifier: Modifier = Modifier,
    /** The client's CURRENT default, pre-selected until the officer picks something else. */
    defaultAccountId: Long? = null,
    /**
     * Insets from the host [AppScaffold]'s top bar.
     *
     * Defaulted so tests and previews can render this half alone; the production caller always
     * passes the scaffold's own padding.
     */
    scaffoldPadding: PaddingValues = PaddingValues(),
    offeredDraft: SavedFormDraft? = null,
) {
    ScreenContent(
        state = accounts,
        onRetry = { onAction(ClientDefaultAccountAction.OnRetry) },
        modifier = modifier
            .padding(scaffoldPadding)
            .fillMaxSize()
            .testTag(ClientDefaultAccountTestTags.SCREEN),
    ) { options, _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = KptTheme.spacing.md),
        ) {
            offeredDraft?.let {
                FormDraftBanner(
                    failureReason = it.errorMessage,
                    onResume = { onAction(ClientDefaultAccountAction.ResumeDraft) },
                    onDiscard = { onAction(ClientDefaultAccountAction.DiscardDraft) },
                )
            }
            val selected = options.selectionFor(state.selectedAccountId, defaultAccountId)

            if (selected != null) {
                Text(
                    text = stringResource(Res.string.feature_client_update_default_account_title),
                    style = AppTypography.labelLargeEmphasized,
                )
                Spacer(Modifier.height(DesignToken.padding.largeIncreased))

                AppTextFieldDropdown(
                    value = selected.accountNo,
                    // The index is resolved to an id HERE, against `options` as rendered, so the
                    // position never outlives the list it indexed.
                    onOptionSelected = { index, _ ->
                        options.getOrNull(index)?.let {
                            onAction(ClientDefaultAccountAction.OptionChanged(it.id))
                        }
                    },
                    options = options.map { it.accountNo },
                    label = stringResource(Res.string.feature_client_defaultaccount_choose),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(ClientDefaultAccountTestTags.DROPDOWN),
                )

                Spacer(Modifier.height(DesignToken.padding.largeIncreased))

                Row(modifier = Modifier.fillMaxWidth()) {
                    AppOutlinedButton(
                        onClick = { onAction(ClientDefaultAccountAction.NavigateBack) },
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
                        modifier = Modifier
                            .weight(1f)
                            .testTag(ClientDefaultAccountTestTags.BACK),
                    )
                    Spacer(Modifier.padding(KptTheme.spacing.sm))
                    AppTextButton(
                        // The ACCOUNT shown as selected is the account written. The original read
                        // `state.accounts[state.currentSelectedIndex]` again in the ViewModel at
                        // this point — see [selectionFor].
                        onClick = { onAction(ClientDefaultAccountAction.OnSave(selected.id)) },
                        // The original left the button live during the write; its Loading dialog
                        // happened to hide the form, which is not the same thing as a guard.
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
                        modifier = Modifier
                            .weight(1f)
                            .testTag(ClientDefaultAccountTestTags.SUBMIT),
                    )
                }
            } else {
                // The server returned the client's template with no savings accounts — a real
                // answer, not an absence of data, which is why the stream does not mark it Empty
                // and why this is the original's own inline message rather than an empty state.
                // There is nothing to choose from, so no dropdown and no Submit are drawn.
                Text(
                    text = stringResource(Res.string.feature_client_defaultaccount_no_savings_accounts),
                    modifier = Modifier.testTag(ClientDefaultAccountTestTags.NO_ACCOUNTS),
                )
            }
        }
    }

    ClientDefaultAccountDialogs(state = state, onAction = onAction)
}

@Composable
private fun ClientDefaultAccountDialogs(
    state: ClientDefaultAccountState,
    onAction: (ClientDefaultAccountAction) -> Unit,
) {
    when (val dialog = state.dialogState) {
        null -> Unit
        is ClientDefaultAccountState.DialogState.ShowStatusDialog -> AppStatusDialog(
            status = dialog.status,
            btnText = stringResource(Res.string.feature_client_dialog_continue),
            // Per status, as the original already had it here: forward on success (its success
            // copy promises exactly that — "Please Continue to get back to Profile Details
            // Screen"), dismiss back onto the form on failure so the officer can retry.
            onConfirm = {
                onAction(
                    if (dialog.status == ResultStatus.SUCCESS) {
                        ClientDefaultAccountAction.OnNext
                    } else {
                        ClientDefaultAccountAction.Dismiss
                    },
                )
            },
            onDismissRequest = { onAction(ClientDefaultAccountAction.Dismiss) },
            successTitle = stringResource(Res.string.feature_client_defaultaccount_success_title),
            successMessage = stringResource(Res.string.feature_client_defaultaccount_success_message),
            failureTitle = stringResource(Res.string.feature_client_defaultaccount_failure_title),
            failureMessage = dialog.msg,
            secondaryBtnText = stringResource(BaseRes.string.draft_save)
                .takeIf { dialog.status == ResultStatus.FAILURE && !dialog.draftSaved },
            onSecondary = { onAction(ClientDefaultAccountAction.SaveDraft) },
            note = when {
                !dialog.draftSaved -> null
                dialog.offline -> stringResource(BaseRes.string.draft_saved_offline)
                else -> stringResource(BaseRes.string.draft_saved)
            },
            modifier = Modifier.testTag(ClientDefaultAccountTestTags.STATUS_DIALOG),
        )
    }
}

/** Stable handles for UI tests and Maestro flows — see RULE-KMP-COMPOSE-UITEST-001. */
internal object ClientDefaultAccountTestTags {
    const val SCREEN = "clientDefaultAccount:screen"
    const val DROPDOWN = "clientDefaultAccount:dropdown"
    const val BACK = "clientDefaultAccount:back"
    const val SUBMIT = "clientDefaultAccount:submit"
    const val NO_ACCOUNTS = "clientDefaultAccount:noAccounts"
    const val STATUS_DIALOG = "clientDefaultAccount:statusDialog"
}

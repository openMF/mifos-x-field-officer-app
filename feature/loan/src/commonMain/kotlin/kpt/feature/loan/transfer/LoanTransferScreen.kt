/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loan.transfer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.designsystem.fieldofficer.component.AppButton
import kpt.core.designsystem.fieldofficer.component.AppCard
import kpt.core.designsystem.fieldofficer.component.AppOutlinedTextField
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.fieldofficer.component.AppSweetError
import kpt.core.designsystem.fieldofficer.component.AppTextFieldDropdown
import kpt.core.designsystem.theme.AppTypography
import kpt.core.designsystem.theme.DesignToken
import kpt.core.ui.components.AppProgressIndicatorMini
import kpt.core.ui.components.AppStatusDialog
import kpt.core.ui.components.ResultStatus
import kpt.feature.loan.generated.resources.Res
import kpt.feature.loan.generated.resources.feature_loan_account_number
import kpt.feature.loan.generated.resources.feature_loan_dialog_continue
import kpt.feature.loan.generated.resources.feature_loan_retry
import kpt.feature.loan.generated.resources.feature_loan_submit
import kpt.feature.loan.generated.resources.feature_loan_transfer_account
import kpt.feature.loan.generated.resources.feature_loan_transfer_account_required
import kpt.feature.loan.generated.resources.feature_loan_transfer_account_type
import kpt.feature.loan.generated.resources.feature_loan_transfer_account_type_required
import kpt.feature.loan.generated.resources.feature_loan_transfer_amount
import kpt.feature.loan.generated.resources.feature_loan_transfer_amount_invalid
import kpt.feature.loan.generated.resources.feature_loan_transfer_amount_not_positive
import kpt.feature.loan.generated.resources.feature_loan_transfer_client
import kpt.feature.loan.generated.resources.feature_loan_transfer_client_required
import kpt.feature.loan.generated.resources.feature_loan_transfer_description
import kpt.feature.loan.generated.resources.feature_loan_transfer_description_required
import kpt.feature.loan.generated.resources.feature_loan_transfer_failure_title
import kpt.feature.loan.generated.resources.feature_loan_transfer_from
import kpt.feature.loan.generated.resources.feature_loan_transfer_funds
import kpt.feature.loan.generated.resources.feature_loan_transfer_office
import kpt.feature.loan.generated.resources.feature_loan_transfer_office_required
import kpt.feature.loan.generated.resources.feature_loan_transfer_success_message
import kpt.feature.loan.generated.resources.feature_loan_transfer_success_title
import kpt.feature.loan.generated.resources.feature_loan_transfer_to
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Carried over from `6b66e8a43:feature/loan/amountTransfer/AmountTransferScreen.kt` — the FROM
 * card, the four cascading TO dropdowns, amount, description, and submit.
 *
 * §6a adaptations: the three-case `DialogState` keeps its shape (this screen's write genuinely has
 * a loading state the officer waits on, because each cascade step blocks the next), the field
 * errors become a closed enum rather than six `StringResource`s in state, and all four selections
 * carry their OPTION rather than two of them carrying a list index.
 */
@Composable
internal fun LoanTransferScreen(
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoanTransferViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            LoanTransferEvent.NavigateBack -> onBackPressed()
        }
    }

    LoanTransferContent(
        state = state,
        onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
        modifier = modifier,
    )
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun LoanTransferContent(
    state: LoanTransferState,
    onAction: (LoanTransferAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppScaffold(
        title = stringResource(Res.string.feature_loan_transfer_funds),
        onBackPressed = { onAction(LoanTransferAction.NavigateBack) },
        modifier = modifier.testTag(LoanTransferTestTags.SCREEN),
    ) { padding ->
        when {
            // The FIRST template read failing leaves nothing to fill in, so it replaces the form.
            // A later one failing does not — see `templateError`'s use below.
            state.templateError && state.offices.isEmpty() -> AppSweetError(
                message = stringResource(Res.string.feature_loan_transfer_failure_title),
                onclick = { onAction(LoanTransferAction.OnRetryFetching) },
                modifier = Modifier.fillMaxSize().padding(padding)
                    .testTag(LoanTransferTestTags.TEMPLATE_ERROR),
            )

            else -> TransferForm(
                state = state,
                onAction = onAction,
                modifier = Modifier.fillMaxSize().padding(padding),
            )
        }
    }

    when (val dialog = state.dialogState) {
        null -> Unit
        is LoanTransferState.DialogState.Result -> AppStatusDialog(
            status = dialog.status,
            // The dialog's single action IS the retry when the failure is retryable. `retryable`
            // was already set by the ViewModel and a comment here claimed retry was "offered
            // separately" — it never was: `AppStatusDialog` has one action slot, nothing read the
            // flag, and `LoanTransferAction.OnRetrySubmit` was handled but undispatched. Surfaced
            // by the action-dispatch sweep.
            btnText = stringResource(
                when {
                    dialog.status != ResultStatus.SUCCESS && dialog.retryable ->
                        Res.string.feature_loan_retry
                    else -> Res.string.feature_loan_dialog_continue
                },
            ),
            onConfirm = {
                onAction(
                    when {
                        dialog.status == ResultStatus.SUCCESS -> LoanTransferAction.NavigateBack
                        // Re-sends the SAME proven draft rather than re-validating the form, which
                        // is what the original's retry could not do safely.
                        dialog.retryable -> LoanTransferAction.OnRetrySubmit
                        // Not retryable: dismiss, keeping the form, so the officer can correct
                        // whatever Fineract objected to.
                        else -> LoanTransferAction.CloseDialog
                    },
                )
            },
            onDismissRequest = { onAction(LoanTransferAction.CloseDialog) },
            successTitle = stringResource(Res.string.feature_loan_transfer_success_title),
            successMessage = stringResource(Res.string.feature_loan_transfer_success_message),
            failureTitle = stringResource(Res.string.feature_loan_transfer_failure_title),
            failureMessage = dialog.msg,
            modifier = Modifier.fillMaxSize().testTag(LoanTransferTestTags.STATUS_DIALOG),
        )
    }
}

@Composable
private fun TransferForm(
    state: LoanTransferState,
    onAction: (LoanTransferAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(horizontal = KptTheme.spacing.md)
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.height(KptTheme.spacing.md))

        FromCard(state)

        Spacer(Modifier.height(KptTheme.spacing.md))

        TransferToHeader(isLoading = state.isLoadingTemplate)

        Spacer(Modifier.height(KptTheme.spacing.sm))

        TransferCascade(state, onAction)

        Spacer(Modifier.height(KptTheme.spacing.md))

        AppOutlinedTextField(
            value = state.amount,
            onValueChange = { onAction(LoanTransferAction.OnAmountChange(it)) },
            label = stringResource(Res.string.feature_loan_transfer_amount),
            keyboardType = KeyboardType.Number,
            isError = state.amountError != null,
            errorText = state.amountError?.let { stringResource(it.messageRes()) },
            modifier = Modifier.fillMaxWidth().testTag(LoanTransferTestTags.AMOUNT),
        )

        Spacer(Modifier.height(KptTheme.spacing.md))

        AppOutlinedTextField(
            value = state.description,
            onValueChange = { onAction(LoanTransferAction.OnDescriptionChange(it)) },
            label = stringResource(Res.string.feature_loan_transfer_description),
            isError = state.descriptionError != null,
            errorText = state.descriptionError?.let { stringResource(it.messageRes()) },
            modifier = Modifier.fillMaxWidth().testTag(LoanTransferTestTags.DESCRIPTION),
        )

        Spacer(Modifier.height(KptTheme.spacing.md))

        AppButton(
            onClick = { onAction(LoanTransferAction.OnSubmit) },
            enabled = !state.isSubmitting && !state.isLoadingTemplate,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(DesignToken.spacing.dp44)
                .testTag(LoanTransferTestTags.SUBMIT),
        ) {
            Text(text = stringResource(Res.string.feature_loan_submit))
        }

        Spacer(Modifier.height(KptTheme.spacing.md))
    }
}

@Composable
private fun TransferToHeader(isLoading: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(Res.string.feature_loan_transfer_to),
            style = AppTypography.labelMediumEmphasized,
        )
        // Beside the section heading, NOT at the bottom of the form.
        //
        // It started out after the submit button, which in a scrolling column six fields long
        // put it off-screen — so selecting an office showed no sign of anything happening
        // while the next level reloaded. A render test caught it (the node was composed but
        // never displayed), which is exactly the class of defect only a render test finds.
        //
        // Inline rather than a blocking overlay: every cascade step reloads the template, so
        // an overlay would flash over the form four times on the way to one transfer.
        if (isLoading) {
            AppProgressIndicatorMini(
                modifier = Modifier.testTag(LoanTransferTestTags.LOADING),
            )
        }
    }
}

/** The cascade of dependent dropdowns, office down to account. */
@Composable
private fun TransferCascade(
    state: LoanTransferState,
    onAction: (LoanTransferAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        // The cascade. Each level is disabled until the level above it has been chosen, which the
        // original expressed only by leaving the list empty — so an un-chooseable dropdown looked
        // identical to one whose office genuinely had no clients.
        AppTextFieldDropdown(
            value = state.office?.name.orEmpty(),
            onOptionSelected = { index, _ ->
                state.offices.getOrNull(index)?.let {
                    onAction(LoanTransferAction.OnOfficeSelected(it))
                }
            },
            options = state.offices.map { it.name.orEmpty() },
            label = stringResource(Res.string.feature_loan_transfer_office),
            readOnly = true,
            enabled = !state.isLoadingTemplate,
            errorMessage = state.officeError?.let { stringResource(it.messageRes()) },
            modifier = Modifier.fillMaxWidth().testTag(LoanTransferTestTags.OFFICE),
        )

        Spacer(Modifier.height(KptTheme.spacing.md))

        AppTextFieldDropdown(
            value = state.client?.displayName.orEmpty(),
            onOptionSelected = { index, _ ->
                state.clients.getOrNull(index)?.let {
                    onAction(LoanTransferAction.OnClientSelected(it))
                }
            },
            options = state.clients.map { it.displayName.orEmpty() },
            label = stringResource(Res.string.feature_loan_transfer_client),
            readOnly = true,
            enabled = state.office != null && !state.isLoadingTemplate,
            errorMessage = state.clientError?.let { stringResource(it.messageRes()) },
            modifier = Modifier.fillMaxWidth().testTag(LoanTransferTestTags.CLIENT),
        )

        Spacer(Modifier.height(KptTheme.spacing.md))

        AppTextFieldDropdown(
            value = state.accountType?.value.orEmpty(),
            onOptionSelected = { index, _ ->
                state.accountTypes.getOrNull(index)?.let {
                    onAction(LoanTransferAction.OnAccountTypeSelected(it))
                }
            },
            options = state.accountTypes.map { it.value.orEmpty() },
            label = stringResource(Res.string.feature_loan_transfer_account_type),
            readOnly = true,
            enabled = state.client != null && !state.isLoadingTemplate,
            errorMessage = state.accountTypeError?.let { stringResource(it.messageRes()) },
            modifier = Modifier.fillMaxWidth().testTag(LoanTransferTestTags.ACCOUNT_TYPE),
        )

        Spacer(Modifier.height(KptTheme.spacing.md))

        AppTextFieldDropdown(
            value = state.account?.accountNo.orEmpty(),
            onOptionSelected = { index, _ ->
                state.accounts.getOrNull(index)?.let {
                    onAction(LoanTransferAction.OnAccountSelected(it))
                }
            },
            options = state.accounts.map { it.accountNo.orEmpty() },
            label = stringResource(Res.string.feature_loan_transfer_account),
            readOnly = true,
            enabled = state.accountType != null && !state.isLoadingTemplate,
            errorMessage = state.accountError?.let { stringResource(it.messageRes()) },
            modifier = Modifier.fillMaxWidth().testTag(LoanTransferTestTags.ACCOUNT),
        )
    }
}

/** The account the money is leaving — read-only, from the loan the caller had already rendered. */
@Composable
private fun FromCard(state: LoanTransferState) {
    AppCard(modifier = Modifier.fillMaxWidth().testTag(LoanTransferTestTags.FROM_CARD)) {
        Column(
            modifier = Modifier.padding(KptTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
        ) {
            Text(
                text = stringResource(Res.string.feature_loan_transfer_from),
                style = AppTypography.labelMediumEmphasized,
            )
            LabelledRow(
                stringResource(Res.string.feature_loan_account_number),
                state.fromAccountNumber,
            )
        }
    }
}

@Composable
private fun LabelledRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = AppTypography.bodySmall,
            color = KptTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = value, style = AppTypography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

/**
 * The six field errors' copy.
 *
 * A closed enum mapped here, rather than the original's six `StringResource?` fields threaded
 * through state — so the ViewModel names WHAT failed and the screen owns how it reads.
 */
internal fun TransferFieldError.messageRes() = when (this) {
    TransferFieldError.AMOUNT_INVALID -> Res.string.feature_loan_transfer_amount_invalid
    TransferFieldError.AMOUNT_NOT_POSITIVE -> Res.string.feature_loan_transfer_amount_not_positive
    TransferFieldError.DESCRIPTION_REQUIRED -> Res.string.feature_loan_transfer_description_required
    TransferFieldError.OFFICE_REQUIRED -> Res.string.feature_loan_transfer_office_required
    TransferFieldError.CLIENT_REQUIRED -> Res.string.feature_loan_transfer_client_required
    TransferFieldError.ACCOUNT_TYPE_REQUIRED -> Res.string.feature_loan_transfer_account_type_required
    TransferFieldError.ACCOUNT_REQUIRED -> Res.string.feature_loan_transfer_account_required
}

/** Stable handles for UI tests and Maestro flows — see RULE-KMP-COMPOSE-UITEST-001. */
internal object LoanTransferTestTags {
    const val SCREEN = "loanTransfer:screen"
    const val FROM_CARD = "loanTransfer:fromCard"
    const val OFFICE = "loanTransfer:office"
    const val CLIENT = "loanTransfer:client"
    const val ACCOUNT_TYPE = "loanTransfer:accountType"
    const val ACCOUNT = "loanTransfer:account"
    const val AMOUNT = "loanTransfer:amount"
    const val DESCRIPTION = "loanTransfer:description"
    const val SUBMIT = "loanTransfer:submit"
    const val LOADING = "loanTransfer:loading"
    const val TEMPLATE_ERROR = "loanTransfer:templateError"
    const val STATUS_DIALOG = "loanTransfer:statusDialog"
}

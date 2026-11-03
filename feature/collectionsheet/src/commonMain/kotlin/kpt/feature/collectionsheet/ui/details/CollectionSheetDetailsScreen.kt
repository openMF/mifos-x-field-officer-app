/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.collectionsheet.ui.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.common.format.formatGrouped
import kpt.core.designsystem.fieldofficer.component.AppBottomSheet
import kpt.core.designsystem.fieldofficer.component.AppButton
import kpt.core.designsystem.fieldofficer.component.AppOutlinedTextField
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.fieldofficer.component.AppTextFieldDropdown
import kpt.core.designsystem.icon.AppIcons
import kpt.core.ui.components.AppEmptyCard
import kpt.feature.collectionsheet.generated.resources.Res
import kpt.feature.collectionsheet.generated.resources.feature_collection_sheet_account_number
import kpt.feature.collectionsheet.generated.resources.feature_collection_sheet_amount
import kpt.feature.collectionsheet.generated.resources.feature_collection_sheet_amount_invalid
import kpt.feature.collectionsheet.generated.resources.feature_collection_sheet_bank_number
import kpt.feature.collectionsheet.generated.resources.feature_collection_sheet_cancel
import kpt.feature.collectionsheet.generated.resources.feature_collection_sheet_cheque_number
import kpt.feature.collectionsheet.generated.resources.feature_collection_sheet_collecting
import kpt.feature.collectionsheet.generated.resources.feature_collection_sheet_failed_to_save_collection_sheet
import kpt.feature.collectionsheet.generated.resources.feature_collection_sheet_individual_collection_details
import kpt.feature.collectionsheet.generated.resources.feature_collection_sheet_loan_account
import kpt.feature.collectionsheet.generated.resources.feature_collection_sheet_no_payment_added
import kpt.feature.collectionsheet.generated.resources.feature_collection_sheet_nothing_due
import kpt.feature.collectionsheet.generated.resources.feature_collection_sheet_offline
import kpt.feature.collectionsheet.generated.resources.feature_collection_sheet_ok
import kpt.feature.collectionsheet.generated.resources.feature_collection_sheet_payment_added
import kpt.feature.collectionsheet.generated.resources.feature_collection_sheet_payment_type
import kpt.feature.collectionsheet.generated.resources.feature_collection_sheet_receipt_number
import kpt.feature.collectionsheet.generated.resources.feature_collection_sheet_routing_code
import kpt.feature.collectionsheet.generated.resources.feature_collection_sheet_save
import kpt.feature.collectionsheet.generated.resources.feature_collection_sheet_save_collection_sheet_success
import kpt.feature.collectionsheet.generated.resources.feature_collection_sheet_submit
import kpt.feature.collectionsheet.generated.resources.feature_collection_sheet_summary
import kpt.feature.collectionsheet.generated.resources.feature_collection_sheet_total_charges
import kpt.feature.collectionsheet.generated.resources.feature_collection_sheet_total_due
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Ported from `6b66e8a43:feature/collectionSheet/.../IndividualCollectionSheetDetailsScreen.kt` and
 * `PaymentDetailsScreen.kt` — the payment screen is now a sheet over this one, sharing its ViewModel,
 * so what the officer enters is what Submit sends (see [CollectionSheetDetailsViewModel]).
 */
@Composable
internal fun CollectionSheetDetailsScreen(
    onNavigateBack: () -> Unit,
    viewModel: CollectionSheetDetailsViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val sheet by viewModel.sheet.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            CollectionSheetDetailsEvent.NavigateBack -> onNavigateBack()
        }
    }

    CollectionSheetDetailsContent(
        state = state,
        sheet = sheet,
        onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
        onRetry = viewModel::onRetry,
    )
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun CollectionSheetDetailsContent(
    state: CollectionSheetDetailsState,
    sheet: ScreenState<CollectionSheetView>,
    onAction: (CollectionSheetDetailsAction) -> Unit,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {},
) {
    AppScaffold(
        title = stringResource(Res.string.feature_collection_sheet_individual_collection_details),
        onBackPressed = { onAction(CollectionSheetDetailsAction.NavigateBack) },
        modifier = modifier,
    ) { padding ->
        ScreenContent(
            state = sheet,
            onRetry = onRetry,
            modifier = Modifier.fillMaxSize().padding(padding).testTag(CollectionSheetDetailsTestTags.SCREEN),
            // Nothing due is not an error: say so plainly. The original opened an empty list.
            empty = {
                AppEmptyCard(
                    msg = stringResource(Res.string.feature_collection_sheet_nothing_due),
                    modifier = Modifier.padding(KptTheme.spacing.md).testTag(CollectionSheetDetailsTestTags.EMPTY),
                )
            },
        ) { view, _ ->
            Column(Modifier.fillMaxSize()) {
                Summary(view)
                LazyColumn(
                    modifier = Modifier.weight(1f).testTag(CollectionSheetDetailsTestTags.LIST),
                    verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
                ) {
                    items(view.rows, key = { it.loanId }) { row ->
                        CollectionRowCard(row) { onAction(CollectionSheetDetailsAction.EditPayment(row.loanId)) }
                    }
                }
                AppButton(
                    onClick = { onAction(CollectionSheetDetailsAction.Submit) },
                    enabled = !state.isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(KptTheme.spacing.md)
                        .testTag(CollectionSheetDetailsTestTags.SUBMIT),
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(strokeWidth = KptTheme.spacing.xs / 2)
                    } else {
                        Text(stringResource(Res.string.feature_collection_sheet_submit))
                    }
                }
            }
            state.editor?.let { PaymentEditorSheet(it, view, onAction) }
        }
    }
    state.dialog?.let { ResultDialog(it) { onAction(CollectionSheetDetailsAction.DismissDialog) } }
}

/** What the original's "Found Collection Sheet" sheet told the officer — now the list's header. */
@Composable
private fun Summary(view: CollectionSheetView) {
    Column(Modifier.fillMaxWidth().padding(KptTheme.spacing.md)) {
        Text(text = view.transactionDate, style = KptTheme.typography.titleMedium)
        Text(
            text = stringResource(
                Res.string.feature_collection_sheet_summary,
                view.rows.size,
                view.memberCount,
                view.total.formatGrouped(2),
            ),
            style = KptTheme.typography.bodyMedium,
            color = KptTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.testTag(CollectionSheetDetailsTestTags.SUMMARY),
        )
    }
}

@Composable
private fun CollectionRowCard(row: CollectionRow, onClick: () -> Unit) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = KptTheme.spacing.md)
            .clickable(onClick = onClick)
            .testTag(CollectionSheetDetailsTestTags.row(row.loanId)),
    ) {
        Row(Modifier.padding(KptTheme.spacing.md), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(text = row.clientName, style = KptTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(
                    text = stringResource(
                        Res.string.feature_collection_sheet_loan_account,
                        row.productName,
                        row.accountNo,
                    ),
                    style = KptTheme.typography.bodyMedium,
                    color = KptTheme.colorScheme.onSurfaceVariant,
                )
                LabelledAmount(Res.string.feature_collection_sheet_total_due, row.totalDue)
                LabelledAmount(Res.string.feature_collection_sheet_total_charges, row.chargesDue)
                Text(
                    text = stringResource(
                        Res.string.feature_collection_sheet_collecting,
                        row.collect.transactionAmount.formatGrouped(2),
                    ),
                    style = KptTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (row.edited) KptTheme.colorScheme.primary else KptTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(
                        if (row.hasPaymentDetail) {
                            Res.string.feature_collection_sheet_payment_added
                        } else {
                            Res.string.feature_collection_sheet_no_payment_added
                        },
                    ),
                    style = KptTheme.typography.labelMedium,
                    color = KptTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(imageVector = AppIcons.ChevronRight, contentDescription = null)
        }
    }
}

@Composable
private fun LabelledAmount(label: org.jetbrains.compose.resources.StringResource, amount: Double) {
    Row(horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm)) {
        Text(text = stringResource(label), style = KptTheme.typography.bodyMedium)
        Text(text = amount.formatGrouped(2), style = KptTheme.typography.bodyMedium)
    }
}

/** The original's payment screen: amount, payment type and the optional payment details. */
@Composable
private fun PaymentEditorSheet(
    editor: PaymentEditor,
    view: CollectionSheetView,
    onAction: (CollectionSheetDetailsAction) -> Unit,
) {
    fun change(f: (PaymentEditor) -> PaymentEditor) = onAction(CollectionSheetDetailsAction.EditorChanged(f))

    AppBottomSheet(onDismiss = { onAction(CollectionSheetDetailsAction.CloseEditor) }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(KptTheme.spacing.md)
                .testTag(CollectionSheetDetailsTestTags.EDITOR),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
        ) {
            Text(text = editor.clientName, style = KptTheme.typography.titleLarge)
            Text(
                text = stringResource(
                    Res.string.feature_collection_sheet_loan_account,
                    editor.productName,
                    editor.accountNo,
                ),
                style = KptTheme.typography.titleSmall,
                color = KptTheme.colorScheme.onSurfaceVariant,
            )
            AppOutlinedTextField(
                value = editor.amount,
                onValueChange = { v -> change { it.copy(amount = v) } },
                label = stringResource(Res.string.feature_collection_sheet_amount),
                keyboardType = KeyboardType.Decimal,
                isError = editor.amountError,
                errorText = if (editor.amountError) {
                    stringResource(Res.string.feature_collection_sheet_amount_invalid)
                } else {
                    null
                },
                modifier = Modifier.fillMaxWidth().testTag(CollectionSheetDetailsTestTags.AMOUNT),
            )
            LabelledAmount(Res.string.feature_collection_sheet_total_charges, editor.chargesDue)

            val types = view.paymentTypes
            AppTextFieldDropdown(
                value = types.firstOrNull { it.id == editor.paymentTypeId }?.name.orEmpty(),
                onOptionSelected = { index, _ ->
                    types.getOrNull(index)?.let { type -> change { it.copy(paymentTypeId = type.id) } }
                },
                options = types.map { it.name.orEmpty() },
                label = stringResource(Res.string.feature_collection_sheet_payment_type),
                modifier = Modifier.fillMaxWidth().testTag(CollectionSheetDetailsTestTags.PAYMENT_TYPE),
            )
            // The detail fields travel only WITH a payment type (see PaymentEditor.toTransaction),
            // so they are shown only once one is chosen — not typed and then silently dropped.
            if (editor.paymentTypeId != null) {
                DetailField(Res.string.feature_collection_sheet_account_number, editor.accountNumber) { v ->
                    change { it.copy(accountNumber = v) }
                }
                DetailField(Res.string.feature_collection_sheet_cheque_number, editor.checkNumber) { v ->
                    change { it.copy(checkNumber = v) }
                }
                DetailField(Res.string.feature_collection_sheet_routing_code, editor.routingCode) { v ->
                    change { it.copy(routingCode = v) }
                }
                DetailField(Res.string.feature_collection_sheet_receipt_number, editor.receiptNumber) { v ->
                    change { it.copy(receiptNumber = v) }
                }
                DetailField(Res.string.feature_collection_sheet_bank_number, editor.bankNumber) { v ->
                    change { it.copy(bankNumber = v) }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.md)) {
                AppButton(
                    onClick = { onAction(CollectionSheetDetailsAction.CloseEditor) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(Res.string.feature_collection_sheet_cancel))
                }
                AppButton(
                    onClick = { onAction(CollectionSheetDetailsAction.SaveEditor) },
                    modifier = Modifier.weight(1f).testTag(CollectionSheetDetailsTestTags.SAVE_PAYMENT),
                ) { Text(stringResource(Res.string.feature_collection_sheet_save)) }
            }
        }
    }
}

@Composable
private fun DetailField(
    label: org.jetbrains.compose.resources.StringResource,
    value: String,
    onChange: (String) -> Unit,
) {
    AppOutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = stringResource(label),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ResultDialog(dialog: CollectionSheetDetailsState.Dialog, onDismiss: () -> Unit) {
    val failed = stringResource(Res.string.feature_collection_sheet_failed_to_save_collection_sheet)
    val text = when (dialog) {
        CollectionSheetDetailsState.Dialog.Submitted ->
            stringResource(Res.string.feature_collection_sheet_save_collection_sheet_success)
        CollectionSheetDetailsState.Dialog.Offline -> stringResource(Res.string.feature_collection_sheet_offline)
        is CollectionSheetDetailsState.Dialog.Failed -> dialog.serverMessage?.let { "$failed\n$it" } ?: failed
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.feature_collection_sheet_ok)) }
        },
        text = { Text(text) },
        modifier = Modifier.testTag(CollectionSheetDetailsTestTags.DIALOG),
    )
}

/** Stable handles for UI tests and device flows. */
internal object CollectionSheetDetailsTestTags {
    const val SCREEN = "collectionSheetDetails:screen"
    const val LIST = "collectionSheetDetails:list"
    const val EMPTY = "collectionSheetDetails:empty"
    const val SUMMARY = "collectionSheetDetails:summary"
    const val SUBMIT = "collectionSheetDetails:submit"
    const val EDITOR = "collectionSheetDetails:editor"
    const val AMOUNT = "collectionSheetDetails:amount"
    const val PAYMENT_TYPE = "collectionSheetDetails:paymentType"
    const val SAVE_PAYMENT = "collectionSheetDetails:savePayment"
    const val DIALOG = "collectionSheetDetails:dialog"

    fun row(loanId: Int) = "collectionSheetDetails:row:$loanId"
}

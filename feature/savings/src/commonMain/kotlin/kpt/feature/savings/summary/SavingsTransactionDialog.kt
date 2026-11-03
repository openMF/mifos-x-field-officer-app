/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.savings.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.theme.AppTypography
import kpt.feature.savings.generated.resources.Res
import kpt.feature.savings.generated.resources.feature_savings_amount
import kpt.feature.savings.generated.resources.feature_savings_close
import kpt.feature.savings.generated.resources.feature_savings_date
import kpt.feature.savings.generated.resources.feature_savings_reversed
import kpt.feature.savings.generated.resources.feature_savings_running_balance
import kpt.feature.savings.generated.resources.feature_savings_transaction_detail
import kpt.feature.savings.generated.resources.feature_savings_transaction_id
import kpt.feature.savings.generated.resources.feature_savings_transaction_type
import org.jetbrains.compose.resources.stringResource

/**
 * One transaction's detail — the original's `DialogBoxRowItem` sheet
 * (`6b66e8a43:.../SavingsAccountSummaryScreen.kt:460`).
 *
 * `AlertDialog`, not the original's hand-built `MifosCard` inside a `Dialog`: the dialog needs a
 * dismiss affordance and the original's card had none visible — it relied on tapping outside,
 * which is not discoverable and is not reachable by a screen reader.
 *
 * The original's rows were drawn in `DarkGray` on a `White` card regardless of scheme (the same
 * literal-colour defect as the repayment header); these use theme roles.
 */
@Composable
internal fun SavingsTransactionDialog(
    row: SavingsTransactionRow,
    onDismiss: () -> Unit,
    summary: SavingsSummary? = null,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag(SavingsSummaryTestTags.DETAIL_DIALOG),
        title = { Text(stringResource(Res.string.feature_savings_transaction_detail)) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.feature_savings_close))
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs)) {
                DetailRow(
                    stringResource(Res.string.feature_savings_transaction_id),
                    row.id?.toString().orEmpty(),
                )
                DetailRow(stringResource(Res.string.feature_savings_date), row.date)
                DetailRow(
                    stringResource(Res.string.feature_savings_transaction_type),
                    row.typeLabel.orEmpty(),
                )
                DetailRow(
                    stringResource(Res.string.feature_savings_amount),
                    // The dialog may be shown without the summary in hand (a test, a preview), so
                    // the raw figure is the fallback rather than a blank.
                    summary?.money(row.amount) ?: row.amount?.toString().orEmpty(),
                )
                DetailRow(
                    stringResource(Res.string.feature_savings_running_balance),
                    summary?.money(row.runningBalance) ?: row.runningBalance?.toString().orEmpty(),
                )
                if (row.isReversed) {
                    Text(
                        text = stringResource(Res.string.feature_savings_reversed),
                        style = AppTypography.labelLarge,
                    )
                }
            }
        },
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = AppTypography.bodyMedium)
        Text(text = value, style = AppTypography.labelLarge)
    }
}

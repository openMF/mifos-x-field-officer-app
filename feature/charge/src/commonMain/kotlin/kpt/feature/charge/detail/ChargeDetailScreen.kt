/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.charge.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.base.ui.screen.RefreshableScreenContent
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.DesignToken
import kpt.core.ui.components.AppEmptyUi
import kpt.feature.charge.generated.resources.Res
import kpt.feature.charge.generated.resources.feature_charge_amount
import kpt.feature.charge.generated.resources.feature_charge_amount_outstanding
import kpt.feature.charge.generated.resources.feature_charge_amount_paid
import kpt.feature.charge.generated.resources.feature_charge_amount_waived
import kpt.feature.charge.generated.resources.feature_charge_calculation
import kpt.feature.charge.generated.resources.feature_charge_details
import kpt.feature.charge.generated.resources.feature_charge_due_date
import kpt.feature.charge.generated.resources.feature_charge_edit
import kpt.feature.charge.generated.resources.feature_charge_id
import kpt.feature.charge.generated.resources.feature_charge_name
import kpt.feature.charge.generated.resources.feature_charge_no
import kpt.feature.charge.generated.resources.feature_charge_no_due_date
import kpt.feature.charge.generated.resources.feature_charge_not_found
import kpt.feature.charge.generated.resources.feature_charge_penalty
import kpt.feature.charge.generated.resources.feature_charge_time
import kpt.feature.charge.generated.resources.feature_charge_yes
import kpt.feature.charge.ui.ChargeUi
import kpt.feature.charge.ui.TestTags
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * One charge, labelled row by row — the original loan card's label-left / value-right rows
 * (`6b66e8a43:feature/loan/loanCharge/LoanChargeScreen.kt`), extended with the paid / waived /
 * outstanding split. Theme colours and a typography token, not the original's `Color.Black` /
 * `Color.DarkGray` and inline `16.sp`, which were near-black on near-black in the dark scheme.
 */
@Composable
internal fun ChargeDetailScreen(
    onBackPressed: () -> Unit,
    onEditCharge: (resourceType: String, resourceId: Int, chargeId: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChargeDetailViewModel = koinViewModel(),
) {
    val charge by viewModel.charge.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            ChargeDetailEvent.NavigateBack -> onBackPressed()
            is ChargeDetailEvent.EditCharge -> onEditCharge(event.resourceType, event.resourceId, event.chargeId)
        }
    }

    ChargeDetailContent(
        charge = charge,
        canEdit = viewModel.canEdit,
        onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
        modifier = modifier,
    )
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun ChargeDetailContent(
    charge: ScreenState<ChargeUi>,
    canEdit: Boolean,
    onAction: (ChargeDetailAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppScaffold(
        title = stringResource(Res.string.feature_charge_details),
        onBackPressed = { onAction(ChargeDetailAction.NavigateBack) },
        modifier = modifier.testTag(TestTags.ChargeDetail.SCREEN),
        actions = {
            // Only with something to edit, and only where Fineract can update it.
            if (canEdit && charge is ScreenState.Content) {
                IconButton(
                    onClick = { onAction(ChargeDetailAction.OnEdit) },
                    modifier = Modifier.testTag(TestTags.ChargeDetail.EDIT),
                ) {
                    Icon(AppIcons.Edit, contentDescription = stringResource(Res.string.feature_charge_edit))
                }
            }
        },
    ) { padding ->
        RefreshableScreenContent(
            state = charge,
            onRefresh = { onAction(ChargeDetailAction.OnRefresh) },
            modifier = Modifier.fillMaxSize().padding(padding),
            // Deleted elsewhere, or a link to a charge the account no longer carries.
            empty = {
                AppEmptyUi(
                    text = stringResource(Res.string.feature_charge_not_found),
                    icon = AppIcons.FileTask,
                    modifier = Modifier.testTag(TestTags.ChargeDetail.NOT_FOUND),
                )
            },
        ) { row, _ ->
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    .testTag(TestTags.ChargeDetail.CONTENT),
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(KptTheme.spacing.md),
                    elevation = CardDefaults.cardElevation(defaultElevation = DesignToken.elevation.dp2),
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = KptTheme.spacing.md),
                        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
                    ) {
                        DetailRow(stringResource(Res.string.feature_charge_name), row.name)
                        DetailRow(stringResource(Res.string.feature_charge_id), row.definitionId)
                        DetailRow(stringResource(Res.string.feature_charge_time), row.time)
                        DetailRow(stringResource(Res.string.feature_charge_calculation), row.calculation)
                        DetailRow(
                            stringResource(Res.string.feature_charge_due_date),
                            row.dueDate ?: stringResource(Res.string.feature_charge_no_due_date),
                        )
                        DetailRow(stringResource(Res.string.feature_charge_amount), row.amount)
                        DetailRow(stringResource(Res.string.feature_charge_amount_paid), row.amountPaid)
                        DetailRow(stringResource(Res.string.feature_charge_amount_waived), row.amountWaived)
                        DetailRow(stringResource(Res.string.feature_charge_amount_outstanding), row.outstanding)
                        DetailRow(
                            stringResource(Res.string.feature_charge_penalty),
                            stringResource(
                                if (row.penalty) Res.string.feature_charge_yes else Res.string.feature_charge_no,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(field: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = KptTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = field,
            style = KptTheme.typography.bodyLarge,
            color = KptTheme.colorScheme.onSurface,
            textAlign = TextAlign.Start,
        )
        Text(
            modifier = Modifier.weight(1f),
            text = value,
            style = KptTheme.typography.bodyLarge,
            color = KptTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Start,
        )
    }
}

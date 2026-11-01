/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.general

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.theme.AppColors
import kpt.core.designsystem.theme.AppTypography
import kpt.core.designsystem.theme.DesignToken
import kpt.core.ui.components.AppRowCard
import kpt.core.ui.util.TextUtil
import kpt.feature.client.general.components.ClientGeneralActionItem
import kpt.feature.client.general.components.clientGeneralActions
import kpt.feature.client.generated.resources.Res
import kpt.feature.client.generated.resources.feature_client_general_header_actions
import kpt.feature.client.generated.resources.feature_client_general_header_performance_history
import kpt.feature.client.generated.resources.feature_client_not_available
import kpt.feature.client.generated.resources.feature_client_perf_active_loans
import kpt.feature.client.generated.resources.feature_client_perf_active_savings
import kpt.feature.client.generated.resources.feature_client_perf_last_loan_amount
import kpt.feature.client.generated.resources.feature_client_perf_loan_cycles
import kpt.feature.client.generated.resources.feature_client_perf_total_savings
import kpt.feature.client.generated.resources.feature_client_screen_general
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * The client's GENERAL view — ported from
 * `6b66e8a43:feature/client/clientGeneral/ClientProfileGeneralScreen.kt`.
 *
 * "Performance History" label → the primary-coloured card with its five `SpaceBetween` rows →
 * "Actions" label → one `AppRowCard` per entry of [clientGeneralActions]. Same order, same
 * tokens, same typography.
 *
 * §6a's three adaptations: the read's Loading/Error dialogs become [ScreenState] through
 * [ScreenContent]; the action EVENT carries the `clientId` instead of the screen reading
 * `state.client?.id ?: -1` at seven call sites; unported destinations are `null` so their row is
 * absent rather than inert. `AppBreadcrumbNavBar` dropped, as everywhere.
 *
 * The empty `rightValues` TextUtil is the original's, comment and all: it reserved the slot for
 * per-action counts that have no API behind them yet. Kept as an empty string rather than removed,
 * so the row's layout is unchanged.
 */
@Composable
internal fun ClientGeneralScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onUpcomingCharges: ((Int) -> Unit)? = null,
    onLoanAccounts: ((Int) -> Unit)? = null,
    onSavingAccounts: ((Int) -> Unit)? = null,
    onFixedDepositAccounts: ((Int) -> Unit)? = null,
    onRecurringDepositAccounts: ((Int) -> Unit)? = null,
    onSharesAccounts: ((Int) -> Unit)? = null,
    onCollateralData: ((Int) -> Unit)? = null,
    viewModel: ClientGeneralViewModel = koinViewModel(),
) {
    val performance by viewModel.performance.collectAsStateWithLifecycle()
    // The wired destination of each action row; an action with none is absent rather than inert.
    val actionHandlers = mapOf<ClientGeneralActionItem, ((Int) -> Unit)?>(
        ClientGeneralActionItem.UpcomingCharges to onUpcomingCharges,
        ClientGeneralActionItem.LoanAccount to onLoanAccounts,
        ClientGeneralActionItem.SavingAccounts to onSavingAccounts,
        ClientGeneralActionItem.FixedDepositAccounts to onFixedDepositAccounts,
        ClientGeneralActionItem.RecurringDepositAccounts to onRecurringDepositAccounts,
        ClientGeneralActionItem.SharesAccounts to onSharesAccounts,
        ClientGeneralActionItem.CollateralData to onCollateralData,
    )

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            ClientGeneralEvent.NavigateBack -> onNavigateBack()
            is ClientGeneralEvent.OnActionClick -> actionHandlers[event.action]?.invoke(event.clientId)
        }
    }

    AppScaffold(
        title = stringResource(Res.string.feature_client_screen_general),
        // Dispatched, not called directly: the ViewModel owns the event, which is the
        // path `ClientGeneralAction.NavigateBack` already declared and nothing reached.
        onBackPressed = { viewModel.trySendAction(ClientGeneralAction.NavigateBack) },
    ) { scaffoldPadding ->
        ClientGeneralContent(
            performance = performance,
            onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
            availableActions = remember(actionHandlers) {
                clientGeneralActions.filter { actionHandlers[it] != null }
            },
            modifier = modifier,
            scaffoldPadding = scaffoldPadding,
        )
    }
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun ClientGeneralContent(
    performance: ScreenState<PerformanceHistory>,
    onAction: (ClientGeneralAction) -> Unit,
    availableActions: List<ClientGeneralActionItem>,
    modifier: Modifier = Modifier,
    /**
     * Insets from the host [AppScaffold]'s top bar.
     *
     * Defaulted so tests and previews can render this half alone — the production caller always
     * passes the scaffold's own padding. These screens had NO top bar at all before S5d: the shell
     * draws one only for tab roots, so every client push destination rendered with no title and no
     * back affordance (which is why their `NavigateBack` action had no dispatcher).
     */
    scaffoldPadding: PaddingValues = PaddingValues(),
) {
    // The host scaffold's insets, folded into the modifier this half already receives so
    // the screen's own root layout places them.
    @Suppress("NAME_SHADOWING")
    val modifier = modifier.padding(scaffoldPadding)

    ScreenContent(
        state = performance,
        onRetry = { onAction(ClientGeneralAction.OnRetry) },
        modifier = modifier.fillMaxSize().testTag(ClientGeneralTestTags.SCREEN),
    ) { history, _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = KptTheme.spacing.md),
        ) {
            Text(
                stringResource(Res.string.feature_client_general_header_performance_history),
                style = KptTheme.typography.labelLarge,
            )

            Spacer(Modifier.height(DesignToken.spacing.medium))

            PerformanceHistoryCard(history)

            Spacer(Modifier.height(DesignToken.spacing.largeIncreased))

            Text(
                stringResource(Res.string.feature_client_general_header_actions),
                style = KptTheme.typography.labelLarge,
            )

            Spacer(Modifier.height(KptTheme.spacing.sm))

            availableActions.forEach { item ->
                AppRowCard(
                    title = stringResource(item.title),
                    imageVector = item.icon,
                    leftValues = listOf(
                        TextUtil(
                            text = stringResource(item.subTitle),
                            style = AppTypography.bodySmall,
                            color = KptTheme.colorScheme.secondary,
                        ),
                    ),
                    // Empty, as in the original — the slot is reserved for a per-action count that
                    // no endpoint supplies yet. Removing it would change the row's layout.
                    rightValues = listOf(
                        TextUtil(
                            text = "",
                            style = AppTypography.bodySmall,
                            color = AppColors.customEnable,
                        ),
                    ),
                    modifier = Modifier
                        .padding(vertical = DesignToken.padding.medium)
                        .clickable { onAction(ClientGeneralAction.OnActionClick(item)) }
                        .testTag(ClientGeneralTestTags.actionRow(item)),
                )
            }
        }
    }
}

@Composable
private fun PerformanceHistoryCard(history: PerformanceHistory) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(KptTheme.shapes.medium)
            .background(KptTheme.colorScheme.primary)
            .padding(KptTheme.spacing.lg)
            .testTag(ClientGeneralTestTags.PERFORMANCE_CARD),
        contentAlignment = Alignment.Center,
    ) {
        val notAvailable = stringResource(Res.string.feature_client_not_available)

        Column(
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PerformanceHistoryRow(
                stringResource(Res.string.feature_client_perf_loan_cycles),
                history.loanCyclesCount.toString(),
            )
            PerformanceHistoryRow(
                stringResource(Res.string.feature_client_perf_active_loans),
                history.activeLoans.toString(),
            )
            PerformanceHistoryRow(
                stringResource(Res.string.feature_client_perf_last_loan_amount),
                // Always "Not Available" — nothing computes it. See PerformanceHistory.
                history.lastLoanAmount?.let { "${history.currency} $it" } ?: notAvailable,
            )
            PerformanceHistoryRow(
                stringResource(Res.string.feature_client_perf_active_savings),
                history.activeSavingsCount.toString(),
            )
            PerformanceHistoryRow(
                stringResource(Res.string.feature_client_perf_total_savings),
                history.totalSaving?.let { "${history.currency} $it" } ?: notAvailable,
            )
        }
    }
}

@Composable
private fun PerformanceHistoryRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val textStyle = TextStyle(
        color = KptTheme.colorScheme.onPrimary,
        fontStyle = KptTheme.typography.labelMedium.fontStyle,
    )
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = textStyle)
        Text(value, style = textStyle)
    }
}

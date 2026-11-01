/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.applynew

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.fieldofficer.component.AppSweetError
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.AppTypography
import kpt.core.model.client.Client
import kpt.feature.client.generated.resources.Res
import kpt.feature.client.generated.resources.feature_client_apply_new_fixed
import kpt.feature.client.generated.resources.feature_client_apply_new_fixed_subtitle
import kpt.feature.client.generated.resources.feature_client_apply_new_inactive
import kpt.feature.client.generated.resources.feature_client_apply_new_loan
import kpt.feature.client.generated.resources.feature_client_apply_new_loan_subtitle
import kpt.feature.client.generated.resources.feature_client_apply_new_none_available
import kpt.feature.client.generated.resources.feature_client_apply_new_recurring
import kpt.feature.client.generated.resources.feature_client_apply_new_recurring_subtitle
import kpt.feature.client.generated.resources.feature_client_apply_new_savings
import kpt.feature.client.generated.resources.feature_client_apply_new_savings_subtitle
import kpt.feature.client.generated.resources.feature_client_apply_new_share
import kpt.feature.client.generated.resources.feature_client_apply_new_share_subtitle
import kpt.feature.client.generated.resources.feature_client_apply_new_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * "Apply for a new account" — see [ClientApplyNewViewModel] for the gating and the status rule.
 *
 * ## §6a adaptations
 *
 * The original took a `NavController` straight into its stateless content to render
 * `MifosBreadcrumbNavBar(navController)`. That breadcrumb is not ported (see
 * `loan-summary-breadcrumb-not-ported` in CAPABILITY_GAPS — `AppBreadcrumbNavBar` exists in
 * `core/ui` with zero callers), and threading a nav controller into a stateless composable is what
 * made that screen untestable without one. A scaffold title stands in.
 */
@Composable
internal fun ClientApplyNewScreen(
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    onApplyLoan: ((Int) -> Unit)? = null,
    onApplySavings: ((Int) -> Unit)? = null,
    onApplyRecurringDeposit: ((Int) -> Unit)? = null,
    onApplyShare: ((Int) -> Unit)? = null,
    onApplyFixedDeposit: ((Int) -> Unit)? = null,
    viewModel: ClientApplyNewViewModel = koinViewModel(),
) {
    val client by viewModel.client.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            ClientApplyNewEvent.NavigateBack -> onBackPressed()
            is ClientApplyNewEvent.OpenApplication -> when (event.item) {
                ApplyNewItem.LOAN -> onApplyLoan?.invoke(event.clientId)
                ApplyNewItem.SAVINGS -> onApplySavings?.invoke(event.clientId)
                ApplyNewItem.RECURRING_DEPOSIT -> onApplyRecurringDeposit?.invoke(event.clientId)
                ApplyNewItem.SHARE -> onApplyShare?.invoke(event.clientId)
                ApplyNewItem.FIXED_DEPOSIT -> onApplyFixedDeposit?.invoke(event.clientId)
            }
        }
    }

    ClientApplyNewContent(
        client = client,
        // A row is drawn only when its application exists — absent, never inert. The original drew
        // all five, so in this fork three of them would open nothing.
        available = remember(
            onApplyLoan,
            onApplySavings,
            onApplyRecurringDeposit,
            onApplyShare,
            onApplyFixedDeposit,
        ) {
            ApplyNewItem.entries.filter { item ->
                when (item) {
                    ApplyNewItem.LOAN -> onApplyLoan != null
                    ApplyNewItem.SAVINGS -> onApplySavings != null
                    ApplyNewItem.RECURRING_DEPOSIT -> onApplyRecurringDeposit != null
                    ApplyNewItem.SHARE -> onApplyShare != null
                    ApplyNewItem.FIXED_DEPOSIT -> onApplyFixedDeposit != null
                }
            }
        },
        onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
        modifier = modifier,
    )
}

/** Stateless half — rendered in tests and previews without Koin or a nav controller. */
@Composable
internal fun ClientApplyNewContent(
    client: ScreenState<Client>,
    available: List<ApplyNewItem>,
    onAction: (ClientApplyNewAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppScaffold(
        title = stringResource(Res.string.feature_client_apply_new_title),
        onBackPressed = { onAction(ClientApplyNewAction.NavigateBack) },
        modifier = modifier.testTag(ClientApplyNewTestTags.SCREEN),
    ) { padding ->
        ScreenContent(
            state = client,
            onRetry = { onAction(ClientApplyNewAction.OnRetry) },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = KptTheme.spacing.md)
                .testTag(ClientApplyNewTestTags.CONTENT),
        ) { clientEntity, _ ->
            Column {
                // Fineract refuses an application for a client that is not active, so this is a real
                // precondition rather than a cosmetic one — and it is derived from the CLIENT, not from
                // a nav argument that goes stale (see the ViewModel).
                if (!clientEntity.canApplyForNewAccount()) {
                    AppSweetError(
                        message = stringResource(Res.string.feature_client_apply_new_inactive),
                        isRetryEnabled = false,
                        modifier = Modifier.fillMaxWidth()
                            .testTag(ClientApplyNewTestTags.INACTIVE),
                    )
                    return@Column
                }

                Spacer(Modifier.height(KptTheme.spacing.md))

                if (available.isEmpty()) {
                    // Reachable only if no application type is ported, which was true before S4.
                    // Stated rather than left blank.
                    Text(
                        text = stringResource(Res.string.feature_client_apply_new_none_available),
                        style = AppTypography.bodyMedium,
                        modifier = Modifier.testTag(ClientApplyNewTestTags.NONE_AVAILABLE),
                    )
                    return@Column
                }

                available.forEach { item ->
                    ApplyRow(
                        item = item,
                        onClick = { onAction(ClientApplyNewAction.OnItemClick(item)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ApplyRow(item: ApplyNewItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = KptTheme.spacing.md)
            .testTag(ClientApplyNewTestTags.row(item)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Column {
            Text(text = stringResource(item.title), style = AppTypography.labelLarge)
            Text(
                text = stringResource(item.subtitle),
                style = AppTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private val ApplyNewItem.title: StringResource get() = when (this) {
    ApplyNewItem.LOAN -> Res.string.feature_client_apply_new_loan
    ApplyNewItem.SAVINGS -> Res.string.feature_client_apply_new_savings
    ApplyNewItem.SHARE -> Res.string.feature_client_apply_new_share
    ApplyNewItem.RECURRING_DEPOSIT -> Res.string.feature_client_apply_new_recurring
    ApplyNewItem.FIXED_DEPOSIT -> Res.string.feature_client_apply_new_fixed
}

private val ApplyNewItem.subtitle: StringResource get() = when (this) {
    ApplyNewItem.LOAN -> Res.string.feature_client_apply_new_loan_subtitle
    ApplyNewItem.SAVINGS -> Res.string.feature_client_apply_new_savings_subtitle
    // The original reused the LOAN subtitle here — "Apply for a loan account" under the Share
    // row — so the share option described the wrong product.
    ApplyNewItem.SHARE -> Res.string.feature_client_apply_new_share_subtitle
    ApplyNewItem.RECURRING_DEPOSIT -> Res.string.feature_client_apply_new_recurring_subtitle
    ApplyNewItem.FIXED_DEPOSIT -> Res.string.feature_client_apply_new_fixed_subtitle
}

/**
 * One icon per type, from `AppIcons`.
 *
 * The original's five were DRAWABLES in its own module (`account_balance_wallet`, `savings`,
 * `stacked_bar_chart`, `calendar_month`, `shield`); these are the nearest `AppIcons` vectors, so
 * the rows theme with the rest of the app instead of shipping five more PNGs.
 */
private val ApplyNewItem.icon get() = when (this) {
    ApplyNewItem.LOAN -> AppIcons.AccountBalanceWallet
    ApplyNewItem.SAVINGS -> AppIcons.PiggyBank
    ApplyNewItem.SHARE -> AppIcons.GrowthChart
    ApplyNewItem.RECURRING_DEPOSIT -> AppIcons.EventRepeat
    ApplyNewItem.FIXED_DEPOSIT -> AppIcons.Wallet
}

/** Stable handles for UI tests and Maestro flows — see RULE-KMP-COMPOSE-UITEST-001. */
internal object ClientApplyNewTestTags {
    const val SCREEN = "clientApplyNew:screen"
    const val CONTENT = "clientApplyNew:content"
    const val INACTIVE = "clientApplyNew:inactive"
    const val NONE_AVAILABLE = "clientApplyNew:noneAvailable"
    fun row(item: ApplyNewItem): String = "clientApplyNew:row:${item.name}"
}

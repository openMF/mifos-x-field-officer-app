/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loan.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.common.utils.CurrencyFormatter
import kpt.core.designsystem.fieldofficer.component.AppButton
import kpt.core.designsystem.fieldofficer.component.AppCard
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.theme.AppColors
import kpt.core.designsystem.theme.AppTypography
import kpt.core.designsystem.theme.DesignToken
import kpt.core.model.objects.account.loan.loanWithAssociations.LoanWithAssociations
import kpt.core.ui.components.AppRowCard
import kpt.core.ui.util.TextUtil
import kpt.feature.loan.generated.resources.Res
import kpt.feature.loan.generated.resources.feature_loan_profile_account
import kpt.feature.loan.generated.resources.feature_loan_profile_action_approve
import kpt.feature.loan.generated.resources.feature_loan_profile_action_repayment
import kpt.feature.loan.generated.resources.feature_loan_profile_action_transfer
import kpt.feature.loan.generated.resources.feature_loan_profile_action_view
import kpt.feature.loan.generated.resources.feature_loan_profile_label_arrears
import kpt.feature.loan.generated.resources.feature_loan_profile_label_balance
import kpt.feature.loan.generated.resources.feature_loan_profile_label_client_name_placeholder
import kpt.feature.loan.generated.resources.feature_loan_profile_section_account_overview
import kpt.feature.loan.generated.resources.feature_loan_profile_section_actions_details
import kpt.feature.loan.generated.resources.feature_loan_profile_status_active
import kpt.feature.loan.generated.resources.feature_loan_profile_status_overpaid
import kpt.feature.loan.generated.resources.feature_loan_profile_status_pending
import kpt.feature.loan.generated.resources.feature_loan_profile_status_unknown
import kpt.feature.loan.profile.components.LoanProfileActionItem
import kpt.feature.loan.profile.components.loanProfileActionItems
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Carried over from `6b66e8a43:feature/loan/loanAccountProfile/LoanAccountProfileScreen.kt` — the
 * account card with its status chip and overview rows, the status-driven primary button, then the
 * "ACTIONS & DETAILS" section with all thirteen `AppRowCard` rows.
 *
 * §6a's three adaptations: the read's Loading/Error dialogs become [ScreenState] through
 * [ScreenContent]; the primary button's action carries the STATUS the screen rendered (the
 * ViewModel used to re-read it and silently do nothing if the load had not landed); and both the
 * detail rows and the primary button are gated on whether their destination is PORTED.
 *
 * ## Why the gating is a list and a set rather than nullable callbacks
 *
 * The caller passes [availableDetails] (which of the thirteen rows to draw) and [availableActions]
 * (which statuses the primary button is live for). Two rows and one action exist after S4b.
 *
 * A row with no destination is not drawn at all — absent, never inert. The primary button is a
 * different case: it is the screen's one call to action and is always drawn, so for an unported
 * status it renders DISABLED with its real label rather than silently doing nothing when tapped.
 * A greyed "Make Repayment" says truthfully that this is the action for this loan and it is not
 * available here; a live one that does nothing is the defect this rule exists to prevent.
 *
 * The section HEADER is drawn regardless, as the original draws it. Tracked in CAPABILITY_GAPS.
 */
@Composable
internal fun LoanProfileScreen(
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    onDetail: ((LoanProfileActionItem, Int) -> Unit)? = null,
    onPrimaryAction: ((LoanPrimaryAction, Int) -> Unit)? = null,
    onAccountDetails: ((Int) -> Unit)? = null,
    availableDetails: List<LoanProfileActionItem> = emptyList(),
    availableActions: Set<LoanPrimaryAction> = emptySet(),
    viewModel: LoanProfileViewModel = koinViewModel(),
) {
    val loan by viewModel.loan.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            LoanProfileEvent.NavigateBack -> onBackPressed()
            is LoanProfileEvent.OpenDetail -> onDetail?.invoke(event.item, event.loanId)
            is LoanProfileEvent.OpenAction -> onPrimaryAction?.invoke(event.action, event.loanId)
            is LoanProfileEvent.OpenAccountDetails -> onAccountDetails?.invoke(event.loanId)
        }
    }

    LoanProfileContent(
        loan = loan,
        status = status,
        onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
        // Intersected with the canonical list so the ORDER is always the profile's own, whatever
        // order the caller happened to list them in.
        availableDetails = remember(onDetail, availableDetails) {
            if (onDetail == null) emptyList() else loanProfileActionItems.filter { it in availableDetails }
        },
        availableActions = remember(onPrimaryAction, availableActions) {
            if (onPrimaryAction == null) emptySet() else availableActions
        },
        accountDetailsAvailable = onAccountDetails != null,
        modifier = modifier,
    )
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun LoanProfileContent(
    loan: ScreenState<LoanWithAssociations>,
    status: ScreenState<LoanProfileStatus>,
    onAction: (LoanProfileAction) -> Unit,
    modifier: Modifier = Modifier,
    availableDetails: List<LoanProfileActionItem> = emptyList(),
    availableActions: Set<LoanPrimaryAction> = emptySet(),
    accountDetailsAvailable: Boolean = false,
) {
    AppScaffold(
        title = stringResource(Res.string.feature_loan_profile_account),
        onBackPressed = { onAction(LoanProfileAction.NavigateBack) },
        modifier = modifier.testTag(LoanProfileTestTags.SCREEN),
    ) { padding ->
        ScreenContent(
            state = loan,
            onRetry = { onAction(LoanProfileAction.OnRetry) },
            modifier = Modifier.fillMaxSize().padding(padding).testTag(LoanProfileTestTags.CONTENT),
        ) { account, _ ->
            val profileStatus = (status as? ScreenState.Content)?.data ?: LoanProfileStatus.UNKNOWN

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = KptTheme.spacing.md),
            ) {
                AccountCard(
                    account = account,
                    status = profileStatus,
                    // Null, not a no-op lambda: AppCard then draws a plain card with no ripple,
                    // so the card does not advertise a tap that goes nowhere.
                    onClick = if (accountDetailsAvailable) {
                        { onAction(LoanProfileAction.OnAccountClick) }
                    } else {
                        null
                    },
                )

                Spacer(Modifier.height(KptTheme.spacing.md))

                AppButton(
                    // Carries the status the card just rendered, so the button and the chip cannot
                    // describe different loans.
                    onClick = { onAction(LoanProfileAction.OnPrimaryActionClick(profileStatus)) },
                    // UNKNOWN has no action of its own — it falls through to account details, so
                    // it is live only when THAT is wired. See this file's header.
                    enabled = profileStatus.primaryAction()
                        ?.let { it in availableActions }
                        ?: accountDetailsAvailable,
                    modifier = Modifier.fillMaxWidth().testTag(LoanProfileTestTags.PRIMARY_ACTION),
                ) {
                    Text(
                        text = stringResource(profileStatus.primaryActionLabel()),
                        style = AppTypography.labelMediumEmphasized,
                        color = KptTheme.colorScheme.onPrimary,
                    )
                }

                Spacer(Modifier.height(KptTheme.spacing.lg))

                Text(
                    text = stringResource(Res.string.feature_loan_profile_section_actions_details),
                    style = AppTypography.labelMediumEmphasized,
                )

                Spacer(Modifier.height(DesignToken.padding.medium))

                // Only the ported rows — see this file's header.
                availableDetails.forEach { item ->
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
                        rightValues = emptyList(),
                        modifier = Modifier
                            .clickable { onAction(LoanProfileAction.OnDetailItemClick(item)) }
                            .padding(vertical = DesignToken.padding.medium),
                    )
                }

                Spacer(Modifier.height(KptTheme.spacing.md))
            }
        }
    }
}

@Composable
private fun AccountCard(
    account: LoanWithAssociations,
    status: LoanProfileStatus,
    onClick: (() -> Unit)?,
) {
    val currencyCode = account.currency?.code
    val decimalPlaces = account.currency?.decimalPlaces
    val balance = CurrencyFormatter.format(account.summary?.totalOutstanding, currencyCode, decimalPlaces)
    val arrears = CurrencyFormatter.format(account.summary?.totalOverdue, currencyCode, decimalPlaces)

    // `onClick` is AppCard's own parameter (the fork's card, formerly MifosCard) — not a
    // `clickable` modifier, which would not get the card's ripple.
    AppCard(
        modifier = Modifier.fillMaxWidth().testTag(LoanProfileTestTags.CARD),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.padding(KptTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs),
        ) {
            Text(
                text = account.clientName
                    ?: stringResource(Res.string.feature_loan_profile_label_client_name_placeholder),
                style = AppTypography.titleMediumEmphasized,
            )
            account.loanProductName?.let {
                Text(text = it, style = AppTypography.bodySmall, color = KptTheme.colorScheme.secondary)
            }

            // The status chip. Label and colour come from the SAME derived status the button uses —
            // except that UNKNOWN names the server's own status: the original's "Unknown" covered
            // Approved / waiting-for-disbursal and read as broken on a live loan (device, 2026-10-25).
            Text(
                text = account.status?.value?.takeIf { status == LoanProfileStatus.UNKNOWN }
                    ?: stringResource(status.labelRes()),
                style = AppTypography.labelMedium,
                color = status.color(),
                modifier = Modifier.testTag(LoanProfileTestTags.STATUS),
            )

            Spacer(Modifier.height(KptTheme.spacing.sm))

            Text(
                text = stringResource(Res.string.feature_loan_profile_section_account_overview),
                style = AppTypography.labelMediumEmphasized,
            )
            OverviewRow(stringResource(Res.string.feature_loan_profile_label_balance), balance)
            OverviewRow(stringResource(Res.string.feature_loan_profile_label_arrears), arrears)
        }
    }
}

@Composable
private fun OverviewRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = AppTypography.bodySmall, color = KptTheme.colorScheme.secondary)
        Text(value, style = AppTypography.bodySmall)
    }
}

/** The original's status → label mapping. */
internal fun LoanProfileStatus.labelRes() = when (this) {
    LoanProfileStatus.ACTIVE -> Res.string.feature_loan_profile_status_active
    LoanProfileStatus.PENDING -> Res.string.feature_loan_profile_status_pending
    LoanProfileStatus.OVERPAID -> Res.string.feature_loan_profile_status_overpaid
    LoanProfileStatus.UNKNOWN -> Res.string.feature_loan_profile_status_unknown
}

/** The original's status → colour mapping, from `AppColors`' four loan-status colours. */
internal fun LoanProfileStatus.color(): Color = when (this) {
    LoanProfileStatus.ACTIVE -> AppColors.loanActiveStatus
    LoanProfileStatus.PENDING -> AppColors.loanPendingStatus
    LoanProfileStatus.OVERPAID -> AppColors.loanOverpaidStatus
    LoanProfileStatus.UNKNOWN -> AppColors.loanUnknownStatus
}

/** The original's status → primary-button label mapping. */
internal fun LoanProfileStatus.primaryActionLabel() = when (this) {
    LoanProfileStatus.PENDING -> Res.string.feature_loan_profile_action_approve
    LoanProfileStatus.OVERPAID -> Res.string.feature_loan_profile_action_transfer
    LoanProfileStatus.ACTIVE -> Res.string.feature_loan_profile_action_repayment
    LoanProfileStatus.UNKNOWN -> Res.string.feature_loan_profile_action_view
}

/** Stable handles for UI tests and Maestro flows — see RULE-KMP-COMPOSE-UITEST-001. */
internal object LoanProfileTestTags {
    const val SCREEN = "loanProfile:screen"
    const val CONTENT = "loanProfile:content"
    const val CARD = "loanProfile:card"
    const val STATUS = "loanProfile:status"
    const val PRIMARY_ACTION = "loanProfile:primaryAction"
}

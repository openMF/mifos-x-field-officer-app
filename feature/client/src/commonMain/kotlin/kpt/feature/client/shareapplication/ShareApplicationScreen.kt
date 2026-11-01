/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.shareapplication

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.submit.draft.SavedFormDraft
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.base.ui.submit.FormDraftBanner
import kpt.core.base.ui.submit.FormDraftFailure
import kpt.core.designsystem.fieldofficer.component.AppOutlinedTextField
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.fieldofficer.component.AppTextFieldDropdown
import kpt.core.designsystem.theme.AppTypography
import kpt.core.model.savings.ShareTemplate
import kpt.core.ui.components.AppTwoButtonRow
import kpt.feature.client.generated.resources.Res
import kpt.feature.client.generated.resources.feature_client_btn_back
import kpt.feature.client.generated.resources.feature_client_btn_submit
import kpt.feature.client.generated.resources.feature_client_shareapp_dividend_allowed
import kpt.feature.client.generated.resources.feature_client_shareapp_error_failed
import kpt.feature.client.generated.resources.feature_client_shareapp_error_offline
import kpt.feature.client.generated.resources.feature_client_shareapp_error_product_required
import kpt.feature.client.generated.resources.feature_client_shareapp_error_savings_required
import kpt.feature.client.generated.resources.feature_client_shareapp_error_shares_required
import kpt.feature.client.generated.resources.feature_client_shareapp_external_id
import kpt.feature.client.generated.resources.feature_client_shareapp_lock_in_period
import kpt.feature.client.generated.resources.feature_client_shareapp_min_active_period
import kpt.feature.client.generated.resources.feature_client_shareapp_next
import kpt.feature.client.generated.resources.feature_client_shareapp_no_products
import kpt.feature.client.generated.resources.feature_client_shareapp_product
import kpt.feature.client.generated.resources.feature_client_shareapp_savings_account
import kpt.feature.client.generated.resources.feature_client_shareapp_step_of
import kpt.feature.client.generated.resources.feature_client_shareapp_title
import kpt.feature.client.generated.resources.feature_client_shareapp_total_shares
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Open a share account for a client. See [ShareApplicationViewModel] for the porting decisions.
 *
 * Carried over from `6b66e8a43:feature/client/createShareAccount/` — the four pages (Details,
 * Terms, Charges, Preview), the step header with its progress bar, and the Back/Next footer that
 * becomes Back/Submit on the last page.
 *
 * ## §6a adaptations
 *
 * The template read's `ScreenState.Error` / overlay-loading pair becomes [ScreenState] through
 * [ScreenContent]. The original drove its own `isOverLayLoadingActive` flag alongside a
 * `DialogState`, set in different branches, for one condition.
 *
 * The CHARGES page is rendered read-only. The original lets an officer add charges through an
 * `AddNewCharge` dialog, which it builds from `template.chargeOptions`; that dialog is a separate
 * composable this port does not have, so the page lists what the product already carries rather
 * than offering an add control that would do nothing. Recorded as
 * `share-application-charges-not-editable`.
 */
@Composable
internal fun ShareApplicationScreen(
    onNavigateBack: () -> Unit,
    onCreated: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ShareApplicationViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val offeredDraft by viewModel.offeredDraft.collectAsStateWithLifecycle()
    val template by viewModel.template.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            ShareApplicationEvent.NavigateBack -> onNavigateBack()
            ShareApplicationEvent.Created -> onCreated()
        }
    }

    AppScaffold(
        title = stringResource(Res.string.feature_client_shareapp_title),
        onBackPressed = { viewModel.trySendAction(ShareApplicationAction.NavigateBack) },
    ) { scaffoldPadding ->
        ShareApplicationContent(
            state = state,
            template = template,
            onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
            modifier = modifier,
            scaffoldPadding = scaffoldPadding,
            offeredDraft = offeredDraft,
        )
    }
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun ShareApplicationContent(
    state: ShareApplicationState,
    template: ScreenState<ShareTemplate>,
    onAction: (ShareApplicationAction) -> Unit,
    modifier: Modifier = Modifier,
    scaffoldPadding: PaddingValues = PaddingValues(),
    offeredDraft: SavedFormDraft? = null,
) {
    Column(
        modifier = modifier
            .padding(scaffoldPadding)
            .fillMaxSize()
            .testTag(ShareApplicationTestTags.SCREEN),
    ) {
        StepHeader(state.step)

        ScreenContent(
            state = template,
            onRetry = { },
            modifier = Modifier.weight(1f),
        ) { options, _ ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = KptTheme.spacing.md),
            ) {
                offeredDraft?.let {
                    FormDraftBanner(
                        failureReason = it.errorMessage,
                        onResume = { onAction(ShareApplicationAction.ResumeDraft) },
                        onDiscard = { onAction(ShareApplicationAction.DiscardDraft) },
                    )
                }
                when (state.step) {
                    ShareStep.DETAILS -> DetailsPage(state, options, onAction)
                    ShareStep.TERMS -> TermsPage(state, onAction)
                    ShareStep.CHARGES -> ChargesPage(options)
                    ShareStep.PREVIEW -> PreviewPage(state)
                }

                state.error?.let { error ->
                    Spacer(Modifier.height(KptTheme.spacing.sm))
                    FormDraftFailure(
                        message = stringResource(error.message),
                        serverReason = state.serverMessage,
                        draftSaved = state.draftSaved,
                        offline = error == ShareApplicationError.Offline,
                        onSaveDraft = { onAction(ShareApplicationAction.SaveDraft) },
                        modifier = Modifier.testTag(ShareApplicationTestTags.ERROR),
                    )
                }

                Spacer(Modifier.height(KptTheme.spacing.lg))
            }
        }

        StepFooter(state, onAction)
    }
}

@Composable
private fun DetailsPage(
    state: ShareApplicationState,
    template: ShareTemplate,
    onAction: (ShareApplicationAction) -> Unit,
) {
    Column {
        val products = template.productOptions
        // Required: say so when the tenant has none (gateway, 2026-10-26), as the deposit wizards do.
        if (products.isEmpty()) {
            Text(
                text = stringResource(Res.string.feature_client_shareapp_no_products),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth().testTag(ShareApplicationTestTags.NO_PRODUCTS),
            )
        }
        AppTextFieldDropdown(
            value = state.product?.label.orEmpty(),
            onOptionSelected = { index, label ->
                products.getOrNull(index)?.id?.let {
                    onAction(ShareApplicationAction.UpdateProduct(ShareOption(it, label)))
                }
            },
            options = products.mapNotNull { it.name },
            label = stringResource(Res.string.feature_client_shareapp_product),
            errorMessage = state.productError
                .errorOr(Res.string.feature_client_shareapp_error_product_required),
            modifier = Modifier.fillMaxWidth().testTag(ShareApplicationTestTags.PRODUCT),
        )

        val savings = template.savingsAccountOptions.orEmpty()
        AppTextFieldDropdown(
            value = state.savingsAccount?.label.orEmpty(),
            onOptionSelected = { index, label ->
                savings.getOrNull(index)?.id?.let {
                    onAction(ShareApplicationAction.UpdateSavingsAccount(ShareOption(it, label)))
                }
            },
            options = savings.mapNotNull { it.accountNo },
            label = stringResource(Res.string.feature_client_shareapp_savings_account),
            errorMessage = state.savingsAccountError
                .errorOr(Res.string.feature_client_shareapp_error_savings_required),
            modifier = Modifier.fillMaxWidth().testTag(ShareApplicationTestTags.SAVINGS_ACCOUNT),
        )

        AppOutlinedTextField(
            value = state.totalShares,
            onValueChange = { onAction(ShareApplicationAction.UpdateTotalShares(it)) },
            label = stringResource(Res.string.feature_client_shareapp_total_shares),
            isError = state.totalSharesError,
            errorText = state.totalSharesError
                .errorOr(Res.string.feature_client_shareapp_error_shares_required),
            keyboardType = KeyboardType.Number,
            modifier = Modifier.fillMaxWidth().testTag(ShareApplicationTestTags.TOTAL_SHARES),
        )

        AppOutlinedTextField(
            value = state.externalId,
            onValueChange = { onAction(ShareApplicationAction.UpdateExternalId(it)) },
            label = stringResource(Res.string.feature_client_shareapp_external_id),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun TermsPage(
    state: ShareApplicationState,
    onAction: (ShareApplicationAction) -> Unit,
) {
    Column {
        AppOutlinedTextField(
            value = state.minimumActivePeriod,
            onValueChange = { onAction(ShareApplicationAction.UpdateMinimumActivePeriod(it)) },
            label = stringResource(Res.string.feature_client_shareapp_min_active_period),
            keyboardType = KeyboardType.Number,
            modifier = Modifier.fillMaxWidth(),
        )

        AppOutlinedTextField(
            value = state.lockInPeriod,
            onValueChange = { onAction(ShareApplicationAction.UpdateLockInPeriod(it)) },
            label = stringResource(Res.string.feature_client_shareapp_lock_in_period),
            keyboardType = KeyboardType.Number,
            modifier = Modifier.fillMaxWidth(),
        )

        androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = state.isDividendAllowed,
                onCheckedChange = { onAction(ShareApplicationAction.UpdateDividendAllowed(it)) },
                modifier = Modifier.testTag(ShareApplicationTestTags.DIVIDEND),
            )
            Text(
                text = stringResource(Res.string.feature_client_shareapp_dividend_allowed),
                style = AppTypography.bodyMedium,
            )
        }
    }
}

/**
 * The product's charges, read-only.
 *
 * The original offered an add/edit dialog here; see the screen KDoc for why it is a list in this
 * port rather than an add control that could not work.
 */
@Composable
private fun ChargesPage(template: ShareTemplate) {
    template.chargeOptions.forEach { charge ->
        Text(
            text = charge.name.orEmpty(),
            style = AppTypography.bodyMedium,
            modifier = Modifier.fillMaxWidth().padding(vertical = KptTheme.spacing.xs),
        )
    }
}

/** What will be sent, from the SAME state the payload is built from. */
@Composable
private fun PreviewPage(state: ShareApplicationState) {
    Column {
        PreviewRow(stringResource(Res.string.feature_client_shareapp_product), state.product?.label.orEmpty())
        PreviewRow(
            stringResource(Res.string.feature_client_shareapp_savings_account),
            state.savingsAccount?.label.orEmpty(),
        )
        PreviewRow(stringResource(Res.string.feature_client_shareapp_total_shares), state.totalShares)
        PreviewRow(stringResource(Res.string.feature_client_shareapp_external_id), state.externalId)
    }
}

@Composable
private fun PreviewRow(label: String, value: String) {
    Text(
        text = "$label: $value",
        style = AppTypography.bodyMedium,
        modifier = Modifier.fillMaxWidth().padding(vertical = KptTheme.spacing.xs),
    )
}

@Composable
private fun StepHeader(step: ShareStep) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = KptTheme.spacing.md, vertical = KptTheme.spacing.sm)
            .testTag(ShareApplicationTestTags.STEP_HEADER),
    ) {
        Text(
            text = stringResource(
                Res.string.feature_client_shareapp_step_of,
                step.ordinal + 1,
                ShareStep.entries.size,
            ),
            style = AppTypography.labelLarge,
        )
        LinearProgressIndicator(
            progress = { (step.ordinal + 1).toFloat() / ShareStep.entries.size },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun StepFooter(
    state: ShareApplicationState,
    onAction: (ShareApplicationAction) -> Unit,
) {
    val isLast = state.step == ShareStep.entries.last()
    AppTwoButtonRow(
        firstBtnText = stringResource(Res.string.feature_client_btn_back),
        secondBtnText = stringResource(
            if (isLast) Res.string.feature_client_btn_submit else Res.string.feature_client_shareapp_next,
        ),
        onFirstBtnClick = {
            // On the first page Back LEAVES; after that it steps back one page.
            onAction(
                if (state.step == ShareStep.DETAILS) {
                    ShareApplicationAction.NavigateBack
                } else {
                    ShareApplicationAction.PreviousStep
                },
            )
        },
        onSecondBtnClick = {
            onAction(if (isLast) ShareApplicationAction.Submit else ShareApplicationAction.NextStep)
        },
        isButtonIconVisible = false,
        modifier = Modifier.testTag(ShareApplicationTestTags.STEP_FOOTER),
    )
}

@Composable
private fun Boolean.errorOr(resource: StringResource): String? =
    if (this) stringResource(resource) else null

private val ShareApplicationError.message: StringResource
    get() = when (this) {
        ShareApplicationError.Offline -> Res.string.feature_client_shareapp_error_offline
        ShareApplicationError.Failed -> Res.string.feature_client_shareapp_error_failed
    }

internal object ShareApplicationTestTags {
    const val NO_PRODUCTS = "share-application:no-products"
    const val SCREEN = "ShareApplicationScreen"
    const val STEP_HEADER = "ShareApplicationStepHeader"
    const val STEP_FOOTER = "ShareApplicationStepFooter"
    const val PRODUCT = "ShareApplicationProduct"
    const val SAVINGS_ACCOUNT = "ShareApplicationSavingsAccount"
    const val TOTAL_SHARES = "ShareApplicationTotalShares"
    const val DIVIDEND = "ShareApplicationDividend"
    const val ERROR = "ShareApplicationError"
}

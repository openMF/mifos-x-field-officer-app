/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.designsystem.fieldofficer.component.AppBasicDialog
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.fieldofficer.component.AppTextButton
import kpt.core.designsystem.fieldofficer.component.BasicDialogState
import kpt.core.designsystem.theme.AppTypography
import kpt.core.designsystem.theme.DesignToken
import kpt.core.model.client.Client
import kpt.core.ui.components.AppDefaultListingComponentFromStringResources
import kpt.core.ui.components.AppRowCard
import kpt.core.ui.components.AppStatusDialog
import kpt.core.ui.components.ResultStatus
import kpt.core.ui.util.TextUtil
import kpt.core.ui.util.toDateString
import kpt.feature.client.details.components.ClientDetailsActionItem
import kpt.feature.client.details.components.ClientDetailsHeader
import kpt.feature.client.details.components.clientDetailsActionItems
import kpt.feature.client.generated.resources.Res
import kpt.feature.client.generated.resources.arrow_downward
import kpt.feature.client.generated.resources.arrow_up
import kpt.feature.client.generated.resources.feature_client_activate_client
import kpt.feature.client.generated.resources.feature_client_confirm_text
import kpt.feature.client.generated.resources.feature_client_details_account_no
import kpt.feature.client.generated.resources.feature_client_details_activation_date
import kpt.feature.client.generated.resources.feature_client_details_client_classification
import kpt.feature.client.generated.resources.feature_client_details_client_type
import kpt.feature.client.generated.resources.feature_client_details_date_of_birth
import kpt.feature.client.generated.resources.feature_client_details_external_id
import kpt.feature.client.generated.resources.feature_client_details_gender
import kpt.feature.client.generated.resources.feature_client_details_legal_form
import kpt.feature.client.generated.resources.feature_client_details_office
import kpt.feature.client.generated.resources.feature_client_details_staff
import kpt.feature.client.generated.resources.feature_client_details_submission_date
import kpt.feature.client.generated.resources.feature_client_dialog_continue
import kpt.feature.client.generated.resources.feature_client_dialog_unassign_message
import kpt.feature.client.generated.resources.feature_client_dismiss_text
import kpt.feature.client.generated.resources.feature_client_profile_actions
import kpt.feature.client.generated.resources.feature_client_screen_details
import kpt.feature.client.generated.resources.feature_client_scroll_for_more_options
import kpt.feature.client.generated.resources.feature_client_staff_unassign_failure_title
import kpt.feature.client.generated.resources.feature_client_staff_unassign_success_message
import kpt.feature.client.generated.resources.feature_client_staff_unassign_success_title
import kpt.feature.client.generated.resources.feature_client_string_not_available
import kpt.feature.client.generated.resources.feature_client_title_unassign_staff
import kpt.feature.client.generated.resources.feature_client_update_details
import kpt.feature.client.generated.resources.feature_client_update_photo
import kpt.feature.client.generated.resources.feature_client_update_signature
import kpt.feature.client.generated.resources.pen_icon
import kpt.feature.client.generated.resources.update_signature
import kpt.feature.client.generated.resources.warning_amber
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * The client's detail fields and its eight actions — ported from
 * `6b66e8a43:feature/client/clientDetailsProfile/ClientProfileDetailsScreen.kt`.
 *
 * Order, components and copy are the original's: the avatar header, the Update Photo / Update
 * Signature button pair, three detail cards, the "Scroll for More Options" hint beside Update
 * Details, an "Actions" label, then one `AppRowCard` per entry of [clientDetailsActionItems].
 *
 * §6a's three adaptations: read states through [ScreenContent]; actions carry what the ViewModel
 * would otherwise have had to read back off the client record; unported destinations are `null`
 * so their row is absent. `AppBreadcrumbNavBar` dropped, as everywhere.
 */
@Composable
internal fun ClientDetailsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onApplyNewApplication: ((Int) -> Unit)? = null,
    onClosureApplication: ((Int) -> Unit)? = null,
    onTransferClient: ((Int) -> Unit)? = null,
    onAssignStaff: ((Int) -> Unit)? = null,
    onAddCharge: ((Int) -> Unit)? = null,
    onCreateCollateral: ((Int) -> Unit)? = null,
    onUpdateDefaultAccount: ((Int) -> Unit)? = null,
    onCreateStandingInstructions: ((Int) -> Unit)? = null,
    onUpdateDetails: ((Int) -> Unit)? = null,
    onUpdatePhoto: ((Int, String, String) -> Unit)? = null,
    onUpdateSignature: ((Int, String, String) -> Unit)? = null,
    onActivate: ((Int) -> Unit)? = null,
    viewModel: ClientDetailsViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val client by viewModel.client.collectAsStateWithLifecycle()
    // The wired destination of each action row; an action with none (or not in the rendered list) is absent.
    val actionHandlers = mapOf<ClientDetailsActionItem, ((Int) -> Unit)?>(
        ClientDetailsActionItem.ApplyNewApplication to onApplyNewApplication,
        ClientDetailsActionItem.ClosureApplication to onClosureApplication,
        ClientDetailsActionItem.TransferClient to onTransferClient,
        ClientDetailsActionItem.AssignStaff to onAssignStaff,
        ClientDetailsActionItem.AddCharge to onAddCharge,
        ClientDetailsActionItem.CreateCollateral to onCreateCollateral,
        ClientDetailsActionItem.UpdateDefaultAccount to onUpdateDefaultAccount,
        ClientDetailsActionItem.CreateStandingInstructions to onCreateStandingInstructions,
    )

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            ClientDetailsEvent.NavigateBack -> onNavigateBack()
            is ClientDetailsEvent.NavigateToUpdateDetails -> onUpdateDetails?.invoke(event.clientId)
            is ClientDetailsEvent.NavigateToUpdatePhoto ->
                onUpdatePhoto?.invoke(event.clientId, event.name, event.accountNo)
            is ClientDetailsEvent.NavigateToUpdateSignature ->
                onUpdateSignature?.invoke(event.clientId, event.name, event.accountNo)
            is ClientDetailsEvent.NavigateToActivate -> onActivate?.invoke(event.clientId)

            // The original's three empty `{}` branches (reports, view standing instructions, self-service
            // users) have no handler: unreachable, as they are not in the rendered list.
            is ClientDetailsEvent.OnActionClick -> actionHandlers[event.action]?.invoke(event.clientId)
        }
    }

    AppScaffold(
        title = stringResource(Res.string.feature_client_screen_details),
        // Dispatched, not called directly: the ViewModel owns the event, which is the
        // path `ClientDetailsAction.NavigateBack` already declared and nothing reached.
        onBackPressed = { viewModel.trySendAction(ClientDetailsAction.NavigateBack) },
        // The original's bottom bar: a client that is not active (pending) gets "Activate Client".
        // Without it a client created un-activated could never be activated — the activate route
        // existed with no caller (device, 2026-10-26).
        bottomBar = {
            val pending = (client as? ScreenState.Content)?.data?.active == false
            if (pending && onActivate != null) {
                ActivateClientButton(onClick = { viewModel.trySendAction(ClientDetailsAction.OnActivateClick) })
            }
        },
    ) { scaffoldPadding ->
        ClientDetailsContent(
            state = state,
            client = client,
            onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
            availableActions = remember(actionHandlers) {
                clientDetailsActionItems.filter { actionHandlers[it] != null }
            },
            canUpdateDetails = onUpdateDetails != null,
            canUpdatePhoto = onUpdatePhoto != null,
            canUpdateSignature = onUpdateSignature != null,
            modifier = modifier,
            scaffoldPadding = scaffoldPadding,
        )
    }
}

@Composable
private fun ActivateClientButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = KptTheme.spacing.md, vertical = KptTheme.spacing.sm)
            .testTag(ClientDetailsTestTags.ACTIVATE),
    ) {
        Text(stringResource(Res.string.feature_client_activate_client))
    }
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun ClientDetailsContent(
    state: ClientDetailsState,
    client: ScreenState<Client>,
    onAction: (ClientDetailsAction) -> Unit,
    availableActions: List<ClientDetailsActionItem>,
    modifier: Modifier = Modifier,
    canUpdateDetails: Boolean = true,
    canUpdatePhoto: Boolean = true,
    canUpdateSignature: Boolean = true,
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
        state = client,
        onRetry = { onAction(ClientDetailsAction.OnRetry) },
        modifier = modifier.fillMaxSize().testTag(ClientDetailsTestTags.SCREEN),
    ) { entity, _ ->
        val notAvailable = stringResource(Res.string.feature_client_string_not_available)
        val name = entity.displayName.orEmpty()
        val accountNo = entity.accountNo.orEmpty()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = KptTheme.spacing.md),
        ) {
            ClientDetailsHeader(
                image = state.profileImage,
                name = entity.displayName,
                mobile = entity.mobileNo,
                email = entity.emailAddress,
                modifier = Modifier.testTag(ClientDetailsTestTags.HEADER),
            )

            Spacer(Modifier.height(DesignToken.padding.medium))

            Row(modifier = Modifier.fillMaxWidth()) {
                if (canUpdatePhoto) {
                    AppTextButton(
                        onClick = { onAction(ClientDetailsAction.OnUpdatePhotoClick(name, accountNo)) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(Res.drawable.arrow_up),
                                contentDescription = null,
                                modifier = Modifier.size(DesignToken.sizes.iconAverage),
                            )
                        },
                        text = {
                            Text(
                                text = stringResource(Res.string.feature_client_update_photo),
                                style = AppTypography.labelMediumEmphasized,
                            )
                        },
                        modifier = Modifier.weight(1f).testTag(ClientDetailsTestTags.UPDATE_PHOTO),
                    )
                    Spacer(Modifier.width(KptTheme.spacing.sm))
                }
                if (canUpdateSignature) {
                    AppTextButton(
                        onClick = { onAction(ClientDetailsAction.OnUpdateSignatureClick(name, accountNo)) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(Res.drawable.update_signature),
                                contentDescription = null,
                                modifier = Modifier.size(DesignToken.sizes.iconAverage),
                            )
                        },
                        text = {
                            Text(
                                text = stringResource(Res.string.feature_client_update_signature),
                                style = AppTypography.labelMediumEmphasized,
                            )
                        },
                        modifier = Modifier.weight(1f).testTag(ClientDetailsTestTags.UPDATE_SIGNATURE),
                    )
                }
            }

            Spacer(Modifier.height(KptTheme.spacing.md))

            // Three cards: personal / account / other — the original's grouping and field order.
            // Derived from the client rather than written into the state on load.
            detailCards(entity, notAvailable).forEach { card ->
                AppDefaultListingComponentFromStringResources(data = card)
                Spacer(Modifier.height(KptTheme.spacing.md))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painterResource(Res.drawable.arrow_downward),
                        contentDescription = null,
                        modifier = Modifier.size(DesignToken.sizes.iconAverage),
                        tint = KptTheme.colorScheme.secondary.copy(alpha = 0.5f),
                    )
                    Spacer(Modifier.width(KptTheme.spacing.sm))
                    Text(
                        text = stringResource(Res.string.feature_client_scroll_for_more_options),
                        style = AppTypography.tag,
                        color = KptTheme.colorScheme.secondary.copy(alpha = 0.5f),
                    )
                }
                Spacer(Modifier.width(KptTheme.spacing.sm))
                if (canUpdateDetails) {
                    AppTextButton(
                        onClick = { onAction(ClientDetailsAction.OnUpdateDetailsClick) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(Res.drawable.pen_icon),
                                contentDescription = null,
                                modifier = Modifier.size(DesignToken.sizes.iconAverage),
                            )
                        },
                        text = {
                            Text(
                                text = stringResource(Res.string.feature_client_update_details),
                                style = AppTypography.labelMediumEmphasized,
                            )
                        },
                        modifier = Modifier.weight(1f).testTag(ClientDetailsTestTags.UPDATE_DETAILS),
                    )
                }
            }

            Spacer(Modifier.height(KptTheme.spacing.md))
            Text(
                text = stringResource(Res.string.feature_client_profile_actions),
                style = AppTypography.labelLargeEmphasized,
            )
            Spacer(Modifier.height(KptTheme.spacing.md))

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
                    rightValues = emptyList(),
                    modifier = Modifier
                        .clickable {
                            onAction(
                                ClientDetailsAction.OnActionClick(
                                    action = item,
                                    // Read HERE, from the client the stream emitted — the ViewModel
                                    // used `state.client!!.staffId` on a record it did not own.
                                    hasStaff = entity.staffName != null,
                                    staffId = entity.staffId,
                                ),
                            )
                        }
                        .padding(vertical = DesignToken.padding.medium)
                        .testTag(ClientDetailsTestTags.actionRow(item)),
                )
            }
        }
    }

    Dialogs(state = state, onAction = onAction)
}

/**
 * The three detail cards, derived from the client.
 *
 * Field order and grouping are the original's `buildClientDetails`, and so is the single "N/A"
 * fallback for every blank value.
 */
@Composable
private fun detailCards(
    client: Client,
    notAvailable: String,
): List<Map<org.jetbrains.compose.resources.StringResource, String>> {
    fun String?.orNa() = this?.takeIf { it.isNotBlank() } ?: notAvailable

    val personal = mapOf(
        Res.string.feature_client_details_gender to client.gender?.name.orNa(),
        Res.string.feature_client_details_date_of_birth to client.dateOfBirth.toDateString().orNa(),
    )
    val account = mapOf(
        Res.string.feature_client_details_account_no to client.accountNo.orNa(),
        Res.string.feature_client_details_office to client.officeName.orNa(),
        Res.string.feature_client_details_external_id to client.externalId.orNa(),
    )
    val other = mapOf(
        Res.string.feature_client_details_legal_form to client.legalForm?.value.orNa(),
        Res.string.feature_client_details_client_type to client.clientType?.name.orNa(),
        Res.string.feature_client_details_client_classification to client.clientClassification?.name.orNa(),
        Res.string.feature_client_details_submission_date to client.timeline?.submittedOnDate?.toDateString().orNa(),
        Res.string.feature_client_details_activation_date to client.activationDate.toDateString().orNa(),
        Res.string.feature_client_details_staff to client.staffName.orNa(),
    )
    return listOf(personal, account, other)
}

@Composable
private fun Dialogs(
    state: ClientDetailsState,
    onAction: (ClientDetailsAction) -> Unit,
) {
    when (val dialog = state.dialogState) {
        null -> Unit

        is ClientDetailsState.DialogState.UnAssignStaff -> AppBasicDialog(
            visibilityState = BasicDialogState.Shown(
                message = stringResource(Res.string.feature_client_dialog_unassign_message),
                title = stringResource(Res.string.feature_client_title_unassign_staff),
            ),
            onConfirm = { onAction(ClientDetailsAction.ConfirmUnAssignStaff(dialog.staffId)) },
            onDismissRequest = { onAction(ClientDetailsAction.DismissDialog) },
            confirmText = stringResource(Res.string.feature_client_confirm_text),
            dismissText = stringResource(Res.string.feature_client_dismiss_text),
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.warning_amber),
                    contentDescription = null,
                    modifier = Modifier.size(DesignToken.sizes.iconMedium),
                )
            },
        )

        is ClientDetailsState.DialogState.ShowStatusDialog -> AppStatusDialog(
            status = dialog.status,
            btnText = stringResource(Res.string.feature_client_dialog_continue),
            // `OnNext` on success re-reads the client so the Staff row updates; on failure it
            // dismisses. The original wired both to the same re-load.
            onConfirm = {
                onAction(
                    if (dialog.status == ResultStatus.SUCCESS) {
                        ClientDetailsAction.OnNext
                    } else {
                        ClientDetailsAction.DismissDialog
                    },
                )
            },
            onDismissRequest = { onAction(ClientDetailsAction.DismissDialog) },
            successTitle = stringResource(Res.string.feature_client_staff_unassign_success_title),
            successMessage = stringResource(Res.string.feature_client_staff_unassign_success_message),
            failureTitle = stringResource(Res.string.feature_client_staff_unassign_failure_title),
            failureMessage = dialog.msg,
            modifier = Modifier.testTag(ClientDetailsTestTags.STATUS_DIALOG),
        )
    }
}

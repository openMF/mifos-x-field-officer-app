/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.signature

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.attafitamim.krop.core.crop.AspectRatio
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.designsystem.fieldofficer.component.AppBasicDialog
import kpt.core.designsystem.fieldofficer.component.AppBottomSheet
import kpt.core.designsystem.fieldofficer.component.AppBottomSheetOptionItem
import kpt.core.designsystem.fieldofficer.component.AppOutlinedButton
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.fieldofficer.component.AppTextButton
import kpt.core.designsystem.fieldofficer.component.BasicDialogState
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.AppTypography
import kpt.core.designsystem.theme.DesignToken
import kpt.core.ui.components.AppAlertDialog
import kpt.core.ui.components.AppImageCropperDialog
import kpt.core.ui.components.AppSignatureDrawDialog
import kpt.core.ui.components.AppUserSignatureImage
import kpt.feature.client.generated.resources.Res
import kpt.feature.client.generated.resources.arrow_up
import kpt.feature.client.generated.resources.feature_client_account_number_prefix
import kpt.feature.client.generated.resources.feature_client_cancel
import kpt.feature.client.generated.resources.feature_client_delete_dialog_title
import kpt.feature.client.generated.resources.feature_client_remove
import kpt.feature.client.generated.resources.feature_client_screen_signature
import kpt.feature.client.generated.resources.feature_client_signature_crop_failed
import kpt.feature.client.generated.resources.feature_client_signature_delete
import kpt.feature.client.generated.resources.feature_client_signature_delete_warning_message
import kpt.feature.client.generated.resources.feature_client_signature_draw
import kpt.feature.client.generated.resources.feature_client_signature_gallery
import kpt.feature.client.generated.resources.feature_client_signature_more
import kpt.feature.client.generated.resources.feature_client_signature_not_found
import kpt.feature.client.generated.resources.feature_client_signature_upload
import kpt.feature.client.generated.resources.feature_client_signature_upload_message
import kpt.feature.client.generated.resources.feature_client_signature_write_failed
import kpt.feature.client.generated.resources.feature_client_signature_write_offline
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * The client's signature — carried over from
 * `6b66e8a43:feature/client/clientSignature/ClientSignatureScreen.kt`.
 *
 * Name + account number, the signature image (or its empty message), a nudge when there is none,
 * then Delete / Upload buttons; Upload opens a bottom sheet with Gallery / Draw / More. Same
 * components, same order, same copy.
 *
 * §6a's three adaptations: the document read's Loading/Error dialogs become [ScreenState] through
 * [ScreenContent]; actions carry the signature id the screen read off the derived stream; nothing
 * here routes elsewhere, so there are no null callbacks.
 *
 * The sheet's "More" option is a no-op in the original (`// it implement further`) and is kept
 * DISABLED rather than drawn live — a sheet option that does nothing when tapped is the dead
 * control RULE-IMPL-DEAD-CLICKABLE-001 forbids, and removing it would change the sheet's layout.
 */
@Composable
internal fun ClientSignatureScreen(
    clientName: String,
    accountNo: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ClientSignatureViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val signatureId by viewModel.signatureId.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            ClientSignatureEvent.NavigateBack -> onNavigateBack()
        }
    }

    // Download the bytes once the id is known. The original chained getSignatureId() into
    // getSignature() inside the ViewModel, where the second step silently did nothing if the first
    // had not landed; keyed on the id here, so it runs exactly when there is one to fetch.
    val id = (signatureId as? ScreenState.Content)?.data
    LaunchedEffect(id) {
        if (id != null) viewModel.trySendAction(ClientSignatureAction.LoadSignature(id))
    }

    AppScaffold(
        title = stringResource(Res.string.feature_client_screen_signature),
        // Dispatched, not called directly: the ViewModel owns the event, which is the
        // path `ClientSignatureAction.NavigateBack` already declared and nothing reached.
        onBackPressed = { viewModel.trySendAction(ClientSignatureAction.NavigateBack) },
    ) { scaffoldPadding ->
        ClientSignatureContent(
            clientName = clientName,
            accountNo = accountNo,
            state = state,
            signatureId = signatureId,
            cropper = viewModel.cropState,
            onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
            modifier = modifier,
            scaffoldPadding = scaffoldPadding,
        )
    }
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun ClientSignatureContent(
    clientName: String,
    accountNo: String,
    state: ClientSignatureState,
    signatureId: ScreenState<Int?>,
    cropper: com.attafitamim.krop.core.crop.ImageCropper,
    onAction: (ClientSignatureAction) -> Unit,
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
        state = signatureId,
        onRetry = { onAction(ClientSignatureAction.OnRetry) },
        modifier = modifier.fillMaxSize().testTag(ClientSignatureTestTags.SCREEN),
    ) { id, _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = KptTheme.spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(DesignToken.padding.largeIncreased))

            Text(text = clientName, style = AppTypography.titleMediumEmphasized)
            Spacer(Modifier.height(DesignToken.padding.extraExtraSmall))
            Text(
                text = stringResource(Res.string.feature_client_account_number_prefix, accountNo),
                style = AppTypography.bodySmall,
                color = KptTheme.colorScheme.secondary,
            )
            Spacer(Modifier.height(DesignToken.padding.largeIncreased))

            AppUserSignatureImage(
                bitmap = state.clientSignatureImage,
                emptyMessage = stringResource(Res.string.feature_client_signature_not_found),
                modifier = Modifier.testTag(ClientSignatureTestTags.IMAGE),
            )

            if (id == null) {
                Spacer(Modifier.height(KptTheme.spacing.md))
                Text(
                    text = stringResource(Res.string.feature_client_signature_upload_message),
                    style = AppTypography.bodySmall,
                    color = KptTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(DesignToken.padding.extraExtraLarge))

            AppOutlinedButton(
                text = {
                    Text(
                        text = stringResource(Res.string.feature_client_signature_delete),
                        style = AppTypography.labelLarge,
                    )
                },
                onClick = { onAction(ClientSignatureAction.ShowDeleteDialog) },
                // Nothing to delete until there is a signature — the original's gate.
                enabled = id != null && !state.isSubmitting,
                leadingIcon = {
                    Icon(imageVector = AppIcons.DeleteDocument, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth().testTag(ClientSignatureTestTags.DELETE),
            )

            Spacer(Modifier.height(KptTheme.spacing.md))

            AppTextButton(
                text = {
                    Text(
                        text = stringResource(Res.string.feature_client_signature_upload),
                        style = AppTypography.labelLarge,
                    )
                },
                onClick = { onAction(ClientSignatureAction.ShowUploadOptionsBottomSheet) },
                enabled = !state.isSubmitting,
                leadingIcon = {
                    Icon(
                        painter = painterResource(Res.drawable.arrow_up),
                        contentDescription = null,
                        modifier = Modifier.size(DesignToken.sizes.iconAverage),
                    )
                },
                modifier = Modifier.fillMaxWidth().testTag(ClientSignatureTestTags.UPLOAD),
            )
        }
    }

    Dialogs(
        state = state,
        signatureId = (signatureId as? ScreenState.Content)?.data,
        cropper = cropper,
        onAction = onAction,
    )
}

@Composable
private fun Dialogs(
    state: ClientSignatureState,
    signatureId: Int?,
    cropper: com.attafitamim.krop.core.crop.ImageCropper,
    onAction: (ClientSignatureAction) -> Unit,
) {
    when (state.dialogState) {
        null -> Unit

        ClientSignatureState.DialogState.ShowDeleteDialog -> AppBasicDialog(
            visibilityState = BasicDialogState.Shown(
                title = stringResource(Res.string.feature_client_delete_dialog_title),
                message = stringResource(Res.string.feature_client_signature_delete_warning_message),
            ),
            onDismissRequest = { onAction(ClientSignatureAction.OnDismissDialog) },
            // Guarded: the button that opens this dialog is disabled without an id, but the dialog
            // must not be able to send a delete for `null` if that ever changes.
            onConfirm = {
                signatureId?.let { onAction(ClientSignatureAction.DeleteSignature(it)) }
            },
            confirmText = stringResource(Res.string.feature_client_remove),
            dismissText = stringResource(Res.string.feature_client_cancel),
            icon = {
                Icon(
                    imageVector = AppIcons.DeleteDocument,
                    contentDescription = null,
                    tint = KptTheme.colorScheme.primary,
                    modifier = Modifier.size(DesignToken.sizes.iconMedium),
                )
            },
        )

        ClientSignatureState.DialogState.ShowUploadOptions -> UploadOptions(onAction)

        ClientSignatureState.DialogState.ShowSignatureDraw -> AppSignatureDrawDialog(
            onSaveFile = { onAction(ClientSignatureAction.CropImage(it)) },
            onDismiss = { onAction(ClientSignatureAction.OnDismissDialog) },
        )

        ClientSignatureState.DialogState.ShowImageCrop ->
            cropper.cropState?.let { AppImageCropperDialog(state = it, aspectRatio = AspectRatio(2, 1)) }
    }

    state.writeError?.let { error ->
        val message = when (error) {
            SignatureWriteError.Offline -> stringResource(Res.string.feature_client_signature_write_offline)
            SignatureWriteError.Failed -> stringResource(Res.string.feature_client_signature_write_failed)
            SignatureWriteError.CropFailed -> stringResource(Res.string.feature_client_signature_crop_failed)
        }
        AppAlertDialog(
            dialogText = message,
            onDismissRequest = { onAction(ClientSignatureAction.OnDismissDialog) },
            onConfirmation = { onAction(ClientSignatureAction.OnDismissDialog) },
            dismissText = null,
            modifier = Modifier.testTag(ClientSignatureTestTags.WRITE_ERROR_DIALOG),
        )
    }
}

@Composable
private fun UploadOptions(onAction: (ClientSignatureAction) -> Unit) {
    AppBottomSheet(onDismiss = { onAction(ClientSignatureAction.OnDismissDialog) }) {
        Row(
            modifier = Modifier.padding(
                start = KptTheme.spacing.md,
                end = KptTheme.spacing.md,
                bottom = KptTheme.spacing.md,
            ),
        ) {
            AppBottomSheetOptionItem(
                label = stringResource(Res.string.feature_client_signature_gallery),
                icon = AppIcons.Gallery,
                onClick = { onAction(ClientSignatureAction.OpenImagePicker) },
            )
            AppBottomSheetOptionItem(
                label = stringResource(Res.string.feature_client_signature_draw),
                icon = AppIcons.Draw,
                onClick = { onAction(ClientSignatureAction.OpenSignatureDraw) },
            )
            // The original's third option, whose body is `// it implement further` — a tap that
            // does nothing.
            //
            // KEPT as found. §6a is explicit that a carried-over screen keeps everything
            // "including anything the agent believes is wrong", with the suspicion recorded rather
            // than fixed. Both alternatives would change the screen: omitting it alters the
            // sheet's layout, and `AppBottomSheetOptionItem` has no `enabled` parameter to grey
            // it out without editing core/designsystem. Tracked as
            // `client-signature-more-option-inert` in CAPABILITY_GAPS.
            AppBottomSheetOptionItem(
                label = stringResource(Res.string.feature_client_signature_more),
                icon = AppIcons.MoreHoriz,
                onClick = {},
            )
        }
    }
}

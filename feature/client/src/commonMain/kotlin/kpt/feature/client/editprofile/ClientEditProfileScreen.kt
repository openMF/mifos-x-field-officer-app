/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.editprofile

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
import com.attafitamim.krop.core.crop.ImageCropper
import kotlinx.coroutines.delay
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.designsystem.fieldofficer.component.BasicDialogState
import kpt.core.designsystem.fieldofficer.component.AppBasicDialog
import kpt.core.designsystem.fieldofficer.component.AppBottomSheet
import kpt.core.designsystem.fieldofficer.component.AppBottomSheetOptionItem
import kpt.core.designsystem.fieldofficer.component.AppOutlinedButton
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.fieldofficer.component.AppTextButton
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.AppTypography
import kpt.core.designsystem.theme.DesignToken
import kpt.core.ui.components.AppAlertDialog
import kpt.core.ui.components.AppImageCropperDialog
import kpt.core.ui.components.AppStatusDialog
import kpt.core.ui.components.AppUserImage
import kpt.core.ui.components.ResultStatus
import kpt.feature.client.generated.resources.Res
import kpt.feature.client.generated.resources.arrow_up
import kpt.feature.client.generated.resources.feature_client_account_number_prefix
import kpt.feature.client.generated.resources.feature_client_cancel
import kpt.feature.client.generated.resources.feature_client_delete_dialog_title
import kpt.feature.client.generated.resources.feature_client_dialog_continue
import kpt.feature.client.generated.resources.feature_client_editprofile_camera
import kpt.feature.client.generated.resources.feature_client_editprofile_camera_unavailable
import kpt.feature.client.generated.resources.feature_client_editprofile_crop_failed
import kpt.feature.client.generated.resources.feature_client_editprofile_delete_dialog_message
import kpt.feature.client.generated.resources.feature_client_editprofile_delete_photo
import kpt.feature.client.generated.resources.feature_client_editprofile_gallery
import kpt.feature.client.generated.resources.feature_client_editprofile_more
import kpt.feature.client.generated.resources.feature_client_editprofile_photo_updated_success_message
import kpt.feature.client.generated.resources.feature_client_editprofile_photo_updated_success_title
import kpt.feature.client.generated.resources.feature_client_editprofile_update_photo_message
import kpt.feature.client.generated.resources.feature_client_editprofile_upload_new_photo
import kpt.feature.client.generated.resources.feature_client_editprofile_write_failed
import kpt.feature.client.generated.resources.feature_client_editprofile_write_offline
import kpt.feature.client.generated.resources.feature_client_remove
import kpt.feature.client.generated.resources.feature_client_update_photo
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * The client's profile PHOTO — carried over from
 * `6b66e8a43:feature/client/clientEditProfile/ClientProfileEditScreen.kt`.
 *
 * Name + account number, the avatar (or its placeholder), a nudge when there is no photo, then
 * Delete / Upload buttons; Upload opens a bottom sheet. Same components, same order, same copy as
 * the signature sibling, because the original pair were the same screen over two different blobs.
 *
 * ## What the substrate changes
 *
 * The original drew a `MifosBreadcrumbNavBar(navController)` at the top and took a `NavController`
 * for it alone. Push destinations here wear [AppScaffold] with a title and a back arrow (the same
 * S5d change the signature port records), so the `NavController` parameter is gone — which also
 * means this screen is renderable in a test without a nav graph.
 *
 * The original gated the entire content on `dialogState != DialogState.Loading`, i.e. the whole
 * screen was blank while the photo loaded. There is no photo-read loading state here (see the
 * ViewModel): the avatar placeholder stands in until the bytes arrive, so the name, the account
 * number and both buttons are usable immediately.
 */
@Composable
internal fun ClientEditProfileScreen(
    clientName: String,
    accountNo: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ClientEditProfileViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            ClientEditProfileEvent.NavigateBack -> onNavigateBack()
        }
    }

    AppScaffold(
        title = stringResource(Res.string.feature_client_update_photo),
        // Dispatched, not called directly: the ViewModel owns the event, which is the path
        // `ClientEditProfileAction.NavigateBack` already declared and nothing reached.
        onBackPressed = { viewModel.trySendAction(ClientEditProfileAction.NavigateBack) },
    ) { scaffoldPadding ->
        ClientEditProfileContent(
            clientName = clientName,
            accountNo = accountNo,
            state = state,
            cropper = viewModel.cropState,
            onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
            modifier = modifier,
            scaffoldPadding = scaffoldPadding,
        )
    }
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun ClientEditProfileContent(
    clientName: String,
    accountNo: String,
    state: ClientEditProfileState,
    cropper: ImageCropper,
    onAction: (ClientEditProfileAction) -> Unit,
    modifier: Modifier = Modifier,
    /**
     * Insets from the host [AppScaffold]'s top bar. Defaulted so this half renders alone in a test
     * or a preview; the production caller always passes the scaffold's own padding.
     */
    scaffoldPadding: PaddingValues = PaddingValues(),
) {
    Column(
        modifier = modifier
            .padding(scaffoldPadding)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = KptTheme.spacing.md)
            .testTag(ClientEditProfileTestTags.SCREEN),
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

        AppUserImage(
            bitmap = state.profileImage,
            modifier = Modifier
                .size(DesignToken.sizes.avatarLargeLarge)
                .testTag(ClientEditProfileTestTags.IMAGE),
            hasBorder = true,
        )

        if (state.profileImage == null) {
            Spacer(Modifier.height(KptTheme.spacing.md))
            Text(
                text = stringResource(Res.string.feature_client_editprofile_update_photo_message),
                style = AppTypography.bodySmall,
                color = KptTheme.colorScheme.secondary,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(DesignToken.padding.extraExtraLarge))

        AppOutlinedButton(
            text = {
                Text(
                    text = stringResource(Res.string.feature_client_editprofile_delete_photo),
                    style = AppTypography.labelLarge,
                )
            },
            onClick = { onAction(ClientEditProfileAction.ShowDeleteDialog) },
            // Nothing to delete until there is a photo — the original's gate, plus the in-flight
            // one it lacked: both writes were re-sendable while the first was still running.
            enabled = state.profileImage != null && !state.isSubmitting,
            leadingIcon = {
                Icon(imageVector = AppIcons.DeleteDocument, contentDescription = null)
            },
            modifier = Modifier.fillMaxWidth().testTag(ClientEditProfileTestTags.DELETE),
        )

        Spacer(Modifier.height(KptTheme.spacing.md))

        AppTextButton(
            text = {
                Text(
                    text = stringResource(Res.string.feature_client_editprofile_upload_new_photo),
                    style = AppTypography.labelLarge,
                )
            },
            onClick = { onAction(ClientEditProfileAction.ShowUploadOptionsBottomSheet) },
            enabled = !state.isSubmitting,
            leadingIcon = {
                Icon(
                    painter = painterResource(Res.drawable.arrow_up),
                    contentDescription = null,
                    modifier = Modifier.size(DesignToken.sizes.iconAverage),
                )
            },
            modifier = Modifier.fillMaxWidth().testTag(ClientEditProfileTestTags.UPLOAD),
        )
    }

    Dialogs(state = state, cropper = cropper, onAction = onAction)
}

@Composable
private fun Dialogs(
    state: ClientEditProfileState,
    cropper: ImageCropper,
    onAction: (ClientEditProfileAction) -> Unit,
) {
    when (state.dialogState) {
        null -> Unit

        ClientEditProfileState.DialogState.ShowDeleteDialog -> AppBasicDialog(
            visibilityState = BasicDialogState.Shown(
                title = stringResource(Res.string.feature_client_delete_dialog_title),
                message = stringResource(Res.string.feature_client_editprofile_delete_dialog_message),
            ),
            onDismissRequest = { onAction(ClientEditProfileAction.OnDismissDialog) },
            onConfirm = { onAction(ClientEditProfileAction.OnConfirmDeleteClick) },
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

        ClientEditProfileState.DialogState.ShowUploadOptions -> UploadOptions(onAction)

        ClientEditProfileState.DialogState.ShowImageCrop ->
            // Square, because the avatar is a circle — the original's `AspectRatio(1, 1)`.
            cropper.cropState?.let { AppImageCropperDialog(state = it, aspectRatio = AspectRatio(1, 1)) }

        ClientEditProfileState.DialogState.UploadSucceeded -> {
            // The original's auto-dismiss, kept: `delay(1500)` then `OnNext`, which leaves the
            // screen. It is why the dialog carries no button.
            LaunchedEffect(Unit) {
                delay(SUCCESS_DIALOG_DWELL_MS)
                onAction(ClientEditProfileAction.OnSuccessContinue)
            }
            AppStatusDialog(
                status = ResultStatus.SUCCESS,
                onConfirm = { onAction(ClientEditProfileAction.OnSuccessContinue) },
                onDismissRequest = { onAction(ClientEditProfileAction.OnSuccessContinue) },
                btnText = stringResource(Res.string.feature_client_dialog_continue),
                successTitle = stringResource(Res.string.feature_client_editprofile_photo_updated_success_title),
                successMessage = stringResource(Res.string.feature_client_editprofile_photo_updated_success_message),
                // The FAILURE arm is unreachable: this dialog is only ever constructed for a
                // SUCCESS, and a failed write reaches `state.writeError` below. The original had
                // the same single-valued status and so the same dead arm — it just also declared
                // two unused strings for it. Empty rather than invented copy nothing can show.
                failureTitle = "",
                failureMessage = "",
                // The original showed no button and relied on the timer alone.
                showButton = false,
                showAsDialog = true,
                modifier = Modifier.testTag(ClientEditProfileTestTags.SUCCESS_DIALOG),
            )
        }
    }

    state.writeError?.let { error ->
        val message = when (error) {
            PhotoWriteError.Offline -> stringResource(Res.string.feature_client_editprofile_write_offline)
            PhotoWriteError.Failed -> stringResource(Res.string.feature_client_editprofile_write_failed)
            PhotoWriteError.CropFailed -> stringResource(Res.string.feature_client_editprofile_crop_failed)
            PhotoWriteError.CameraUnavailable ->
                stringResource(Res.string.feature_client_editprofile_camera_unavailable)
        }
        AppAlertDialog(
            dialogText = message,
            onDismissRequest = { onAction(ClientEditProfileAction.OnDismissDialog) },
            onConfirmation = { onAction(ClientEditProfileAction.OnDismissDialog) },
            dismissText = null,
            modifier = Modifier.testTag(ClientEditProfileTestTags.WRITE_ERROR_DIALOG),
        )
    }
}

/**
 * Gallery, Camera and More — the original's three options, in its order.
 *
 * "More" does nothing: its body in the original is the comment `// it implement further`. It is
 * KEPT as found, matching the identical option on the signature sibling.
 *
 * This port first omitted it, reading §6a's "absent, not inert". That reading is wrong, and the
 * distinction is worth stating because it recurs: adaptation 3 governs **callbacks to UNPORTED
 * DESTINATIONS** — affordances this port cannot yet reach — and passes them `null` so nothing is
 * drawn. "More" is not one of those. It has no destination in the ORIGINAL either, which makes it
 * a pre-existing UI defect, and §6a is explicit about those: everything else is verbatim
 * "including anything the agent believes is wrong", recorded in CAPABILITY_GAPS and LEFT IN PLACE,
 * because Stitch is where the UI gets fixed. Omitting it would also change the sheet's layout,
 * which is exactly the redesign the rule exists to prevent.
 *
 * Tracked as `client-signature-more-option-inert`, which now covers both sheets.
 */
@Composable
private fun UploadOptions(onAction: (ClientEditProfileAction) -> Unit) {
    AppBottomSheet(onDismiss = { onAction(ClientEditProfileAction.OnDismissDialog) }) {
        Row(
            modifier = Modifier.padding(
                start = KptTheme.spacing.md,
                end = KptTheme.spacing.md,
                bottom = KptTheme.spacing.md,
            ),
        ) {
            AppBottomSheetOptionItem(
                label = stringResource(Res.string.feature_client_editprofile_gallery),
                icon = AppIcons.Gallery,
                onClick = { onAction(ClientEditProfileAction.OpenImagePicker) },
            )
            AppBottomSheetOptionItem(
                label = stringResource(Res.string.feature_client_editprofile_camera),
                icon = AppIcons.Camera,
                onClick = { onAction(ClientEditProfileAction.OpenCamera) },
            )
            // Dead in the original (`// it implement further`). Kept verbatim — see the KDoc.
            AppBottomSheetOptionItem(
                label = stringResource(Res.string.feature_client_editprofile_more),
                icon = AppIcons.MoreHoriz,
                onClick = {},
            )
        }
    }
}

/** How long the success dialog stands before it leaves the screen. The original's 1500ms. */
private const val SUCCESS_DIALOG_DWELL_MS = 1_500L

/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.adddocument

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.designsystem.fieldofficer.component.AppOutlinedButton
import kpt.core.designsystem.fieldofficer.component.AppOutlinedTextField
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.theme.AppTypography
import kpt.core.ui.components.AppFilePickerBottomSheet
import kpt.core.ui.components.AppRowWithTextAndButton
import kpt.feature.client.generated.resources.Res
import kpt.feature.client.generated.resources.feature_client_adddocument_description
import kpt.feature.client.generated.resources.feature_client_adddocument_description_hint
import kpt.feature.client.generated.resources.feature_client_adddocument_error_failed
import kpt.feature.client.generated.resources.feature_client_adddocument_error_name_required
import kpt.feature.client.generated.resources.feature_client_adddocument_error_offline
import kpt.feature.client.generated.resources.feature_client_adddocument_error_pick_failed
import kpt.feature.client.generated.resources.feature_client_adddocument_error_too_large
import kpt.feature.client.generated.resources.feature_client_adddocument_name
import kpt.feature.client.generated.resources.feature_client_adddocument_name_hint
import kpt.feature.client.generated.resources.feature_client_adddocument_no_file
import kpt.feature.client.generated.resources.feature_client_adddocument_pick
import kpt.feature.client.generated.resources.feature_client_adddocument_title
import kpt.feature.client.generated.resources.feature_client_btn_back
import kpt.feature.client.generated.resources.feature_client_btn_submit
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Attach a document to a client. See [ClientAddDocumentViewModel] for the porting decisions.
 *
 * Carried over from `6b66e8a43:feature/client/clientAddDocuments/ClientAddDocumentsScreen.kt` — the
 * title, the name and description fields, the read-only file row with its action button, the
 * Back/Submit pair, and the Gallery / Files / More bottom sheet.
 *
 * ## §6a adaptations
 *
 * The `dialogState` sealed class (Loading / Error) that REPLACED the whole form becomes inline
 * state: a submit in flight disables the button, and an error is a message beside the form rather
 * than instead of it. The original's error dialog offered only "Go back", which left the screen and
 * discarded whatever had been typed.
 *
 * The breadcrumb bar is not ported, per `loan-summary-breadcrumb-not-ported`.
 *
 * ## The file row's button no longer toggles to "View"
 *
 * In the original it read "Add" before a file was chosen and "View" afterwards, with View opening
 * the document PREVIEW screen (and, for a PDF, an external app). That preview screen is NOT
 * ported, so under §6a adaptation 3 the destination is unreachable and the affordance is ABSENT:
 * the button keeps offering a re-pick, which is the one thing it can still do. This is the
 * genuine unported-destination case — unlike the sheet's "More" below.
 *
 * ## "More" is kept, and does nothing
 *
 * `AppFilePickerBottomSheet` draws three options and `onMoreClick` is required. The original's
 * handler is `ClientAddDocumentScreenAction.UseMoreOptions -> {}` — an empty branch, i.e. dead in
 * the ORIGINAL, which makes it a pre-existing UI defect rather than an unported destination. §6a
 * keeps those verbatim and records them; see `client-signature-more-option-inert`, which now
 * covers all three sheets that carry this option.
 */
@Composable
internal fun ClientAddDocumentScreen(
    onNavigateBack: () -> Unit,
    onUploaded: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ClientAddDocumentViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            ClientAddDocumentEvent.NavigateBack -> onNavigateBack()
            ClientAddDocumentEvent.Uploaded -> onUploaded()
        }
    }

    AppScaffold(
        title = stringResource(Res.string.feature_client_adddocument_title),
        onBackPressed = { viewModel.trySendAction(ClientAddDocumentAction.NavigateBack) },
    ) { scaffoldPadding ->
        ClientAddDocumentContent(
            state = state,
            onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
            modifier = modifier,
            scaffoldPadding = scaffoldPadding,
        )
    }
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun ClientAddDocumentContent(
    state: ClientAddDocumentState,
    onAction: (ClientAddDocumentAction) -> Unit,
    modifier: Modifier = Modifier,
    scaffoldPadding: PaddingValues = PaddingValues(),
) {
    Column(
        modifier = modifier
            .padding(scaffoldPadding)
            .fillMaxSize()
            .padding(horizontal = KptTheme.spacing.md)
            .testTag(ClientAddDocumentTestTags.SCREEN),
    ) {
        AppOutlinedTextField(
            value = state.name,
            onValueChange = { onAction(ClientAddDocumentAction.UpdateName(it)) },
            label = stringResource(Res.string.feature_client_adddocument_name),
            placeholder = stringResource(Res.string.feature_client_adddocument_name_hint),
            maxLines = 1,
            isError = state.nameError,
            errorText = if (state.nameError) {
                stringResource(Res.string.feature_client_adddocument_error_name_required)
            } else {
                null
            },
            modifier = Modifier.fillMaxWidth().testTag(ClientAddDocumentTestTags.NAME),
        )

        AppOutlinedTextField(
            value = state.description,
            onValueChange = { onAction(ClientAddDocumentAction.UpdateDescription(it)) },
            label = stringResource(Res.string.feature_client_adddocument_description),
            placeholder = stringResource(Res.string.feature_client_adddocument_description_hint),
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = KptTheme.spacing.sm)
                .testTag(ClientAddDocumentTestTags.DESCRIPTION),
        )

        AppRowWithTextAndButton(
            // The picked file's name, or the original's "No file selected".
            text = state.fileName ?: stringResource(Res.string.feature_client_adddocument_no_file),
            btnText = stringResource(Res.string.feature_client_adddocument_pick),
            onBtnClick = { onAction(ClientAddDocumentAction.OpenFileSource) },
            modifier = Modifier.testTag(ClientAddDocumentTestTags.FILE_ROW),
        )

        state.error?.let { error ->
            Spacer(Modifier.height(KptTheme.spacing.sm))
            Text(
                text = stringResource(error.message),
                style = AppTypography.bodySmall,
                color = KptTheme.colorScheme.error,
                modifier = Modifier.testTag(ClientAddDocumentTestTags.ERROR),
            )
        }

        Spacer(Modifier.height(KptTheme.spacing.lg))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            AppOutlinedButton(
                onClick = { onAction(ClientAddDocumentAction.NavigateBack) },
                modifier = Modifier.weight(1f).testTag(ClientAddDocumentTestTags.BACK),
            ) {
                Text(stringResource(Res.string.feature_client_btn_back))
            }

            Spacer(Modifier.width(KptTheme.spacing.sm))

            AppOutlinedButton(
                onClick = { onAction(ClientAddDocumentAction.Submit) },
                // The original's `enabled = step == VIEW` — i.e. a file has been chosen. Also off
                // while a submit is in flight, which the original did not guard: its progress
                // dialog merely covered the button.
                enabled = state.canSubmit,
                modifier = Modifier.weight(1f).testTag(ClientAddDocumentTestTags.SUBMIT),
            ) {
                Text(stringResource(Res.string.feature_client_btn_submit))
            }
        }

        if (state.isSourceSheetOpen) {
            AppFilePickerBottomSheet(
                onDismiss = { onAction(ClientAddDocumentAction.DismissFileSource) },
                onGalleryClick = { onAction(ClientAddDocumentAction.PickFromGallery) },
                onFilesClick = { onAction(ClientAddDocumentAction.PickFromFiles) },
                // Dead in the original (`UseMoreOptions -> {}`). Kept — see the KDoc.
                onMoreClick = {},
            )
        }
    }
}

private val AddDocumentError.message: StringResource
    get() = when (this) {
        AddDocumentError.TooLarge -> Res.string.feature_client_adddocument_error_too_large
        AddDocumentError.PickFailed -> Res.string.feature_client_adddocument_error_pick_failed
        AddDocumentError.Offline -> Res.string.feature_client_adddocument_error_offline
        AddDocumentError.Failed -> Res.string.feature_client_adddocument_error_failed
    }

internal object ClientAddDocumentTestTags {
    const val SCREEN = "ClientAddDocumentScreen"
    const val NAME = "ClientAddDocumentName"
    const val DESCRIPTION = "ClientAddDocumentDescription"
    const val FILE_ROW = "ClientAddDocumentFileRow"
    const val ERROR = "ClientAddDocumentError"
    const val BACK = "ClientAddDocumentBack"
    const val SUBMIT = "ClientAddDocumentSubmit"
}

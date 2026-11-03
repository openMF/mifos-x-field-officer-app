/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.document.upload

import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.designsystem.fieldofficer.component.AppButton
import kpt.core.designsystem.fieldofficer.component.AppOutlinedTextField
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.ui.components.AppAlertDialog
import kpt.feature.document.generated.resources.Res
import kpt.feature.document.generated.resources.feature_document_browse
import kpt.feature.document.generated.resources.feature_document_description
import kpt.feature.document.generated.resources.feature_document_message_field_required
import kpt.feature.document.generated.resources.feature_document_message_file_required
import kpt.feature.document.generated.resources.feature_document_name
import kpt.feature.document.generated.resources.feature_document_offline
import kpt.feature.document.generated.resources.feature_document_selected_file
import kpt.feature.document.generated.resources.feature_document_update_document
import kpt.feature.document.generated.resources.feature_document_upload
import kpt.feature.document.generated.resources.feature_document_upload_document
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Upload or replace a document — carried over from
 * `6b66e8a43:feature/document/documentDialog/DocumentDialogScreen.kt`.
 *
 * Name, description, a read-only file field with Browse, and the submit button; the title switches
 * on whether a `documentId` was supplied, as the original's did.
 *
 * §6a's adaptations: the three fields and their required-field errors live in the ViewModel rather
 * than in screen-local `remember` (so they survive rotation and are testable — see its header), and
 * the progress state disables the button instead of replacing the form.
 *
 * The original rendered this as a DIALOG over the list. It is a full screen here because its
 * route is a navigation destination like every other form in this port; the content, fields and
 * copy are unchanged.
 */
@Composable
internal fun DocumentUploadScreen(
    onBackPressed: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DocumentUploadViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            DocumentUploadEvent.NavigateBack -> onBackPressed()
            DocumentUploadEvent.Saved -> onSaved()
        }
    }

    DocumentUploadContent(
        state = state,
        onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
        modifier = modifier,
    )
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun DocumentUploadContent(
    state: DocumentUploadState,
    onAction: (DocumentUploadAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val required = stringResource(Res.string.feature_document_message_field_required)

    AppScaffold(
        // The original switched this on whether it was editing; kept.
        title = stringResource(
            if (state.isEditing) {
                Res.string.feature_document_update_document
            } else {
                Res.string.feature_document_upload_document
            },
        ),
        onBackPressed = { onAction(DocumentUploadAction.NavigateBack) },
        modifier = modifier.testTag(DocumentUploadTestTags.SCREEN),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = KptTheme.spacing.md)
                .verticalScroll(rememberScrollState()),
        ) {
            AppOutlinedTextField(
                value = state.name,
                onValueChange = { onAction(DocumentUploadAction.NameChanged(it)) },
                label = stringResource(Res.string.feature_document_name),
                isError = state.nameError,
                errorText = if (state.nameError) required else null,
                modifier = Modifier.fillMaxWidth().testTag(DocumentUploadTestTags.NAME),
            )

            Spacer(Modifier.height(KptTheme.spacing.sm))

            AppOutlinedTextField(
                value = state.description,
                onValueChange = { onAction(DocumentUploadAction.DescriptionChanged(it)) },
                label = stringResource(Res.string.feature_document_description),
                isError = state.descriptionError,
                errorText = if (state.descriptionError) required else null,
                modifier = Modifier.fillMaxWidth().testTag(DocumentUploadTestTags.DESCRIPTION),
            )

            Spacer(Modifier.height(KptTheme.spacing.sm))

            // Read-only: the value comes from the picker, never the keyboard.
            AppOutlinedTextField(
                value = state.fileName.orEmpty(),
                onValueChange = {},
                readOnly = true,
                label = stringResource(Res.string.feature_document_selected_file),
                isError = state.fileError,
                errorText = if (state.fileError) {
                    stringResource(Res.string.feature_document_message_file_required)
                } else {
                    null
                },
                modifier = Modifier.fillMaxWidth().testTag(DocumentUploadTestTags.FILE),
            )

            Spacer(Modifier.height(KptTheme.spacing.sm))

            AppButton(
                onClick = { onAction(DocumentUploadAction.PickFile) },
                enabled = !state.isSubmitting,
                modifier = Modifier.fillMaxWidth().testTag(DocumentUploadTestTags.BROWSE),
            ) {
                Text(stringResource(Res.string.feature_document_browse))
            }

            Spacer(Modifier.height(KptTheme.spacing.md))

            AppButton(
                onClick = { onAction(DocumentUploadAction.Submit) },
                enabled = !state.isSubmitting,
                modifier = Modifier.fillMaxWidth().testTag(DocumentUploadTestTags.SUBMIT),
            ) {
                Text(stringResource(Res.string.feature_document_upload))
            }
        }
    }

    state.error?.let { error ->
        AppAlertDialog(
            dialogText = when (error) {
                UploadError.Offline -> stringResource(Res.string.feature_document_offline)
                UploadError.Failed -> required
            },
            onConfirmation = { onAction(DocumentUploadAction.DismissError) },
            onDismissRequest = { onAction(DocumentUploadAction.DismissError) },
            dismissText = null,
            modifier = Modifier.testTag(DocumentUploadTestTags.ERROR_DIALOG),
        )
    }
}

/** Stable handles for UI tests and Maestro flows — see RULE-KMP-COMPOSE-UITEST-001. */
internal object DocumentUploadTestTags {
    const val SCREEN = "documentUpload:screen"
    const val NAME = "documentUpload:name"
    const val DESCRIPTION = "documentUpload:description"
    const val FILE = "documentUpload:file"
    const val BROWSE = "documentUpload:browse"
    const val SUBMIT = "documentUpload:submit"
    const val ERROR_DIALOG = "documentUpload:error"
}

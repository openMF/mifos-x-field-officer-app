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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.launch
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.document.DocumentCommandRepository
import kpt.core.ui.util.documentUpload

/**
 * Upload or replace a document — ported from
 * `6b66e8a43:feature/document/documentDialog/DocumentDialogViewModel.kt`.
 *
 * Serves both: a `documentId` on the route means REPLACE, its absence means CREATE. The original
 * decided the same way, and its title string switched on it.
 *
 * ## Validation moved off the screen
 *
 * The original held `documentName` / `description` / the picked file and three `*Error` booleans in
 * the SCREEN's `remember`, validated them in the submit lambda, and passed the values down to the
 * ViewModel. So the form's state died on rotation and nothing about it was testable. The state and
 * the validation are here; the screen renders them.
 *
 * ## The picker's accepted extensions are the original's
 *
 * `xls, xlsx, pdf, doc, docx, png, jpeg, jpg` — kept verbatim. Fineract accepts more, but widening
 * the list is a product decision.
 */
internal class DocumentUploadViewModel(
    private val commands: DocumentCommandRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<DocumentUploadState, DocumentUploadEvent, DocumentUploadAction>(
    DocumentUploadState(
        entityType = savedStateHandle.get<String>(ENTITY_TYPE_KEY).orEmpty(),
        entityId = savedStateHandle.get<Int>(ENTITY_ID_KEY) ?: -1,
        // -1 rather than null on the route, because a nav argument cannot be absent — the route
        // carries it and this is what "no document, so create" looks like on the wire.
        documentId = savedStateHandle.get<Int>(DOCUMENT_ID_KEY)?.takeIf { it > 0 },
    ),
) {

    override fun handleAction(action: DocumentUploadAction) {
        when (action) {
            DocumentUploadAction.NavigateBack -> sendEvent(DocumentUploadEvent.NavigateBack)

            is DocumentUploadAction.NameChanged ->
                mutableStateFlow.value = state.copy(name = action.value, nameError = false)

            is DocumentUploadAction.DescriptionChanged ->
                mutableStateFlow.value =
                    state.copy(description = action.value, descriptionError = false)

            DocumentUploadAction.PickFile -> pickFile()
            DocumentUploadAction.DismissError -> mutableStateFlow.value = state.copy(error = null)
            DocumentUploadAction.Submit -> submit()
        }
    }

    private fun pickFile() {
        viewModelScope.launch {
            val file = runCatching {
                FileKit.openFilePicker(type = FileKitType.File(extensions = ACCEPTED_EXTENSIONS))
            }.getOrElse {
                // The original caught, printed the stack trace and handed back null — so a picker
                // that failed looked identical to one the user cancelled. Logged and distinguished.
                Logger.e(tag = TAG, throwable = it) { "file picker failed" }
                null
            }
            // A null here is also a CANCEL, which must not clear a file already chosen.
            if (file != null) {
                mutableStateFlow.value = state.copy(file = file, fileError = false)
            }
        }
    }

    private fun submit() {
        val validated = state.validated()
        mutableStateFlow.value = validated
        val file = validated.file
        if (!validated.isValid || file == null) return
        if (validated.isSubmitting) return

        mutableStateFlow.value = validated.copy(isSubmitting = true, error = null)

        viewModelScope.launch {
            val body = documentUpload(
                file = file.readBytes(),
                name = validated.name,
                extension = file.extension,
                description = validated.description,
            )
            val existingId = validated.documentId
            val result = if (existingId == null) {
                commands.createDocument(validated.entityType, validated.entityId, body)
            } else {
                commands.updateDocument(validated.entityType, validated.entityId, existingId, body)
            }

            when (result) {
                is MutationResult.Applied -> {
                    mutableStateFlow.value = state.copy(isSubmitting = false)
                    sendEvent(DocumentUploadEvent.Saved)
                }

                is MutationResult.Blocked -> mutableStateFlow.value = state.copy(
                    isSubmitting = false,
                    error = if (result.reason == BlockReason.OFFLINE) {
                        UploadError.Offline
                    } else {
                        UploadError.Failed
                    },
                )

                is MutationResult.Failed -> {
                    Logger.e(tag = TAG, throwable = result.cause) { "document upload failed" }
                    mutableStateFlow.value = state.copy(isSubmitting = false, error = UploadError.Failed)
                }

                is MutationResult.Conflicted ->
                    mutableStateFlow.value = state.copy(isSubmitting = false, error = UploadError.Failed)
            }
        }
    }

    private companion object {
        const val TAG = "DocumentUpload"
        const val ENTITY_TYPE_KEY = "entityType"
        const val ENTITY_ID_KEY = "entityId"
        const val DOCUMENT_ID_KEY = "documentId"

        /** The original's list, verbatim. */
        val ACCEPTED_EXTENSIONS = listOf("xls", "xlsx", "pdf", "doc", "docx", "png", "jpeg", "jpg")
    }
}

internal data class DocumentUploadState(
    val entityType: String = "",
    val entityId: Int = -1,
    /** `null` means CREATE; a value means REPLACE that document. */
    val documentId: Int? = null,
    val name: String = "",
    val description: String = "",
    val file: PlatformFile? = null,
    val nameError: Boolean = false,
    val descriptionError: Boolean = false,
    val fileError: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: UploadError? = null,
) {
    /** The chosen file's name, for the read-only field the form shows. */
    val fileName: String? get() = file?.name

    val isEditing: Boolean get() = documentId != null

    val isValid: Boolean get() = !nameError && !descriptionError && !fileError

    /**
     * The original's three required-field checks, in one place.
     *
     * All three are required — `name`, `description` AND a file — which is the original's rule even
     * though Fineract only requires the file and a name.
     */
    fun validated(): DocumentUploadState = copy(
        nameError = name.isBlank(),
        descriptionError = description.isBlank(),
        fileError = file == null,
    )
}

internal enum class UploadError { Offline, Failed }

internal sealed interface DocumentUploadEvent {
    data object NavigateBack : DocumentUploadEvent
    data object Saved : DocumentUploadEvent
}

internal sealed interface DocumentUploadAction {
    data object NavigateBack : DocumentUploadAction
    data object PickFile : DocumentUploadAction
    data object Submit : DocumentUploadAction
    data object DismissError : DocumentUploadAction
    data class NameChanged(val value: String) : DocumentUploadAction
    data class DescriptionChanged(val value: String) : DocumentUploadAction
}

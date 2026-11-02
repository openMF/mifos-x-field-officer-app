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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.size
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.common.utils.FileKitUtil
import kpt.core.data.document.DocumentCommandRepository
import kpt.core.ui.util.documentUpload

/**
 * Attach a document to a client — ported from
 * `6b66e8a43:feature/client/clientAddDocuments/{ClientAddDocumentsScreen,ClientAddDocumentScreenViewmodel}.kt`.
 *
 * Reached from the client DOCUMENTS screen's "+", which was `null`-gated (so not drawn) until this
 * landed. That is the screen the original's graph points at —
 * `clientDocumentsDestination(navigateToAddDocuments = navController::navigateToClientAddDocumentRoute)`.
 *
 * ## Why this is not the document module's upload screen
 *
 * `feature/document` already ships `DocumentUploadScreen`, and both end in
 * `DocumentCommandRepository.createDocument`. They are NOT the same screen: that one is ported from
 * `feature/document/documentDialog/DocumentDialogScreen.kt` and serves the seven other entity types
 * that reach `navigateToDocumentList`, with a single Browse field. This one is the CLIENT's, and it
 * has its own file-source bottom sheet and its own size limit. Sharing the repository call is the
 * substrate idiom, not duplication — what `G-IMPL-DUP` forbids is duplicate feature scaffolding,
 * which is why the three deposit-account screens collapsed into one and these two do not.
 *
 * ## The shared step machine is gone
 *
 * The original routed everything through a feature-local `DocumentSelectAndUploadRepository` whose
 * `EntityDocumentState` carried a `Step` (ADD / VIEW / UPDATE_PREVIEW) and a `SubmitMode` between
 * this screen and the document PREVIEW screen. That preview screen is not ported, so the only
 * reader of the shared state is this screen — and a shared mutable singleton read by one consumer
 * is just state with extra steps, and one that outlives the screen: the original never reset it, so
 * opening the form a second time found the previous document still selected.
 *
 * State lives here instead. [ClientAddDocumentState.file] being non-null IS the original's
 * `Step.VIEW`, which is also what gates the submit button.
 *
 * ## The size limit is enforced BEFORE the picker closes the sheet
 *
 * The original checked `platformFile.size() > 1048576L` and raised an error dialog whose only
 * button was "Go back" — which dismissed the whole SCREEN, losing the name and description the
 * officer had already typed, for a mistake as recoverable as picking the wrong file. Here an
 * oversized file is a field-level message and the form stays as it was.
 */
internal class ClientAddDocumentViewModel(
    private val commands: DocumentCommandRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<ClientAddDocumentState, ClientAddDocumentEvent, ClientAddDocumentAction>(
    ClientAddDocumentState(clientId = savedStateHandle.get<Int>(CLIENT_ID_KEY) ?: -1),
) {

    override fun handleAction(action: ClientAddDocumentAction) {
        when (action) {
            ClientAddDocumentAction.NavigateBack -> sendEvent(ClientAddDocumentEvent.NavigateBack)

            ClientAddDocumentAction.OpenFileSource ->
                mutableStateFlow.value = state.copy(isSourceSheetOpen = true)

            ClientAddDocumentAction.DismissFileSource ->
                mutableStateFlow.value = state.copy(isSourceSheetOpen = false)

            is ClientAddDocumentAction.UpdateName ->
                mutableStateFlow.value = state.copy(name = action.value, nameError = false)

            is ClientAddDocumentAction.UpdateDescription ->
                mutableStateFlow.value = state.copy(description = action.value)

            ClientAddDocumentAction.PickFromGallery -> pick(fromGallery = true)
            ClientAddDocumentAction.PickFromFiles -> pick(fromGallery = false)

            ClientAddDocumentAction.Submit -> submit()

            ClientAddDocumentAction.DismissError ->
                mutableStateFlow.value = state.copy(error = null)
        }
    }

    private fun pick(fromGallery: Boolean) {
        viewModelScope.launch {
            mutableStateFlow.value = state.copy(isSourceSheetOpen = false)
            val picker = if (fromGallery) FileKitUtil.pickImage() else FileKitUtil.pickFile()
            val file = picker.first().getOrElse {
                Logger.e(tag = TAG, throwable = it) { "document picker failed" }
                mutableStateFlow.value = state.copy(error = AddDocumentError.PickFailed)
                return@launch
            }
            // A null is a CANCEL, not a failure — it must not clear a file already chosen.
            if (file == null) return@launch

            if (file.size() > MAX_DOCUMENT_BYTES) {
                // Field-level, so the typed name and description survive. The original raised a
                // dialog whose only button left the screen.
                mutableStateFlow.value = state.copy(error = AddDocumentError.TooLarge)
                return@launch
            }
            mutableStateFlow.value = state.copy(file = file, error = null)
        }
    }

    private fun submit() {
        val file = state.file
        if (state.name.isBlank()) {
            mutableStateFlow.value = state.copy(nameError = true)
            return
        }
        // The button is disabled without a file, so this is a guard rather than a path.
        if (file == null || state.isSubmitting) return

        mutableStateFlow.value = state.copy(isSubmitting = true, error = null)
        viewModelScope.launch {
            val body = documentUpload(
                file = file.readBytes(),
                name = state.name.trim(),
                extension = file.extension,
                description = state.description.trim(),
            )
            when (val result = commands.createDocument(ENTITY_TYPE, state.clientId, body)) {
                is MutationResult.Applied -> {
                    mutableStateFlow.value = state.copy(isSubmitting = false)
                    sendEvent(ClientAddDocumentEvent.Uploaded)
                }

                is MutationResult.Blocked -> mutableStateFlow.value = state.copy(
                    isSubmitting = false,
                    // Offline is told apart from refused. The original surfaced a raw exception
                    // message for both, so "we could not reach the server" and "the server said no"
                    // read identically.
                    error = if (result.reason == BlockReason.OFFLINE) {
                        AddDocumentError.Offline
                    } else {
                        AddDocumentError.Failed
                    },
                )

                is MutationResult.Failed -> {
                    Logger.e(tag = TAG, throwable = result.cause) { "client document upload failed" }
                    mutableStateFlow.value =
                        state.copy(isSubmitting = false, error = AddDocumentError.Failed)
                }

                is MutationResult.Conflicted ->
                    mutableStateFlow.value =
                        state.copy(isSubmitting = false, error = AddDocumentError.Failed)
            }
        }
    }

    internal companion object {
        const val CLIENT_ID_KEY = "clientId"

        /** Fineract's entity path segment for a client document. */
        const val ENTITY_TYPE = "clients"

        /** The original's limit, to the byte: `platformFile.size() > 1048576L`. */
        const val MAX_DOCUMENT_BYTES = 1_048_576L

        private const val TAG = "ClientAddDocument"
    }
}

/** What went wrong, as a value the SCREEN renders — never a string built in the ViewModel. */
internal enum class AddDocumentError {
    /** The chosen file is over [ClientAddDocumentViewModel.MAX_DOCUMENT_BYTES]. */
    TooLarge,

    /** The platform picker itself failed. */
    PickFailed,

    /** No connection — nothing was uploaded. */
    Offline,

    /** The server refused, or the request failed. */
    Failed,
}

internal data class ClientAddDocumentState(
    val clientId: Int = -1,
    val name: String = "",
    val description: String = "",
    val file: PlatformFile? = null,
    val nameError: Boolean = false,
    val isSourceSheetOpen: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: AddDocumentError? = null,
) {
    /** The picked file's name, for the read-only file row. */
    val fileName: String? get() = file?.name

    /**
     * Submit is live only once a file is chosen — the original's
     * `enabled = state.step == EntityDocumentState.Step.VIEW`, which is the same condition
     * expressed through the shared step machine.
     */
    val canSubmit: Boolean get() = file != null && !isSubmitting
}

internal sealed interface ClientAddDocumentEvent {
    data object NavigateBack : ClientAddDocumentEvent
    data object Uploaded : ClientAddDocumentEvent
}

internal sealed interface ClientAddDocumentAction {
    data object NavigateBack : ClientAddDocumentAction
    data object OpenFileSource : ClientAddDocumentAction
    data object DismissFileSource : ClientAddDocumentAction
    data object PickFromGallery : ClientAddDocumentAction
    data object PickFromFiles : ClientAddDocumentAction
    data object Submit : ClientAddDocumentAction
    data object DismissError : ClientAddDocumentAction
    data class UpdateName(val value: String) : ClientAddDocumentAction
    data class UpdateDescription(val value: String) : ClientAddDocumentAction
}

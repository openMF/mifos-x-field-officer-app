/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.addidentifier

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.common.utils.Constants
import kpt.core.common.utils.FileKitUtil
import kpt.core.data.client.ClientCommandRepository
import kpt.core.data.client.ClientRepository
import kpt.core.data.document.DocumentCommandRepository
import kpt.core.model.objects.error.serverErrorMessage
import kpt.core.model.objects.noncoreobjects.IdentifierPayload
import kpt.core.model.objects.noncoreobjects.IdentifierTemplate
import kpt.core.ui.fieldofficer.utils.getClientIdentifierStatus
import kpt.core.ui.util.documentUpload
import kpt.feature.client.identifiers.identifierDocumentKey

/**
 * Add an identity document to a client — ported from
 * `6b66e8a43:feature/client/clientIdentifiersAddUpdate/{ClientIdentifiersAddUpdateViewModel,
 * ClientIdentitiesAddUpdateScreen}.kt` (657 + 413 lines).
 *
 * Reached from the client IDENTIFIERS screen's "+", which was `null`-gated (so not drawn) until
 * this landed.
 *
 * ## The original's three phases
 *
 * The original is a phase machine over `Feature`: `ADD_IDENTIFIER` creates the identifier, then
 * `ADD_UPDATE_DOCUMENT` attaches a scan to it, and `VIEW_DOCUMENT` previews an existing one.
 *
 * The first two are here. `VIEW_DOCUMENT` was only ever opened from the identifiers LIST, so it
 * landed as that list's "View document" → the shared `ClientDocumentPreviewScreen` (2026-10-23),
 * not as a third phase of this form.
 *
 * ## The phases are SEQUENTIAL, and the first one is already committed when the second runs
 *
 * This matters for what the officer sees on a failure. Creating the identifier is its own write;
 * once Fineract has accepted it the identifier EXISTS, whatever happens to the document upload
 * that follows. The original obscured this — a failed document upload surfaced an error over a
 * screen that gave no sign the identifier had been created, so the officer's reasonable move was
 * to go back and create it again.
 *
 * Here [ClientAddIdentifierState.identifierCreated] is set the moment the first write lands, the
 * document step says so, and a failure there offers a retry of the UPLOAD rather than of the whole
 * form.
 *
 * ## The server's duplicate message is no longer rewritten
 *
 * The original reshaped Fineract's error text by hand:
 *
 * ```kotlin
 * dataState.message.replace("unique", "document").replace("under", " under")
 * ```
 *
 * — a string substitution on a server message, applied only when the exception happened to be an
 * `IllegalStateException`. It turns "must be unique" into "must be document", which is not what
 * the server said and not what the officer needs. The message is shown as received.
 */
internal class ClientAddIdentifierViewModel(
    repository: ClientRepository,
    private val commands: ClientCommandRepository,
    private val documents: DocumentCommandRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<ClientAddIdentifierState, ClientAddIdentifierEvent, ClientAddIdentifierAction>(
    ClientAddIdentifierState(clientId = savedStateHandle.get<Long>(CLIENT_ID_KEY) ?: -1L),
) {

    /** The document types this tenant allows as identification. */
    val template: StateFlow<ScreenState<IdentifierTemplate>> =
        repository.clientIdentifierTemplateStream(state.clientId, viewModelScope).state
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    override fun handleAction(action: ClientAddIdentifierAction) {
        when (action) {
            ClientAddIdentifierAction.NavigateBack ->
                sendEvent(ClientAddIdentifierEvent.NavigateBack)

            is ClientAddIdentifierAction.UpdateDocumentType ->
                mutableStateFlow.value =
                    state.copy(
                        documentTypeId = action.id,
                        documentTypeLabel = action.label,
                        documentTypeError = false,
                    )

            is ClientAddIdentifierAction.UpdateDocumentKey ->
                mutableStateFlow.value =
                    state.copy(documentKey = action.value, documentKeyError = false)

            is ClientAddIdentifierAction.UpdateStatus ->
                mutableStateFlow.value = state.copy(status = action.value, statusError = false)

            is ClientAddIdentifierAction.UpdateDescription ->
                mutableStateFlow.value = state.copy(description = action.value)

            ClientAddIdentifierAction.CreateIdentifier -> createIdentifier()

            ClientAddIdentifierAction.OpenFileSource ->
                mutableStateFlow.value = state.copy(isSourceSheetOpen = true)

            ClientAddIdentifierAction.DismissFileSource ->
                mutableStateFlow.value = state.copy(isSourceSheetOpen = false)

            ClientAddIdentifierAction.PickFromGallery -> pick(fromGallery = true)
            ClientAddIdentifierAction.PickFromFiles -> pick(fromGallery = false)

            ClientAddIdentifierAction.UploadDocument -> uploadDocument()

            // Finishing WITHOUT a document is a legitimate end to the flow: the identifier is
            // already created, and an identity document is optional in Fineract.
            ClientAddIdentifierAction.Finish -> sendEvent(ClientAddIdentifierEvent.Finished)

            ClientAddIdentifierAction.DismissError ->
                mutableStateFlow.value = state.copy(error = null, serverMessage = null)
        }
    }

    private fun createIdentifier() {
        val typeMissing = state.documentTypeId <= 0
        val keyMissing = state.documentKey.isBlank()
        val statusMissing = state.status !in IdentifierStatus.values
        if (typeMissing || keyMissing || statusMissing) {
            mutableStateFlow.value = state.copy(
                documentTypeError = typeMissing,
                documentKeyError = keyMissing,
                statusError = statusMissing,
            )
            return
        }
        if (state.isSubmitting) return

        mutableStateFlow.value = state.copy(isSubmitting = true, error = null, serverMessage = null)
        viewModelScope.launch {
            val payload = IdentifierPayload(
                documentTypeId = state.documentTypeId,
                documentKey = state.documentKey.trim(),
                status = state.status,
                description = state.description.trim().ifEmpty { null },
            )
            when (val result = commands.createClientIdentifier(state.clientId, payload)) {
                is MutationResult.Applied -> mutableStateFlow.value = state.copy(
                    isSubmitting = false,
                    // The identifier now EXISTS. Everything after this point is the optional
                    // document step, and the screen says so.
                    identifierCreated = true,
                )

                is MutationResult.Blocked -> mutableStateFlow.value = state.copy(
                    isSubmitting = false,
                    error = if (result.reason == BlockReason.OFFLINE) {
                        IdentifierError.Offline
                    } else {
                        IdentifierError.CreateFailed
                    },
                )

                is MutationResult.Failed -> {
                    Logger.e(tag = TAG, throwable = result.cause) { "create identifier failed" }
                    mutableStateFlow.value = state.copy(
                        isSubmitting = false,
                        error = IdentifierError.CreateFailed,
                        // Shown AS RECEIVED — see the class KDoc on the original's rewriting.
                        serverMessage = result.cause.serverErrorMessage(),
                    )
                }

                is MutationResult.Conflicted ->
                    mutableStateFlow.value =
                        state.copy(isSubmitting = false, error = IdentifierError.CreateFailed)
            }
        }
    }

    private fun pick(fromGallery: Boolean) {
        viewModelScope.launch {
            mutableStateFlow.value = state.copy(isSourceSheetOpen = false)
            val picker = if (fromGallery) FileKitUtil.pickImage() else FileKitUtil.pickFile()
            val file = picker.first().getOrElse {
                Logger.e(tag = TAG, throwable = it) { "identifier document picker failed" }
                mutableStateFlow.value = state.copy(error = IdentifierError.PickFailed)
                return@launch
            }
            // A null is a CANCEL — it must not clear a file already chosen.
            if (file == null) return@launch
            mutableStateFlow.value = state.copy(file = file, error = null)
        }
    }

    private fun uploadDocument() {
        val file = state.file ?: return
        if (state.isSubmitting) return

        mutableStateFlow.value = state.copy(isSubmitting = true, error = null, serverMessage = null)
        viewModelScope.launch {
            val body = documentUpload(
                file = file.readBytes(),
                name = state.documentKey.trim().ifEmpty { file.name },
                extension = file.extension,
                // NOT the officer's description — that already went on the identifier itself in
                // step one. The document's description is how the identifiers list FINDS this scan
                // again (`identifierDocumentKey`); writing anything else here stranded it.
                description = identifierDocumentKey(
                    documentTypeName = state.documentTypeLabel,
                    documentKey = state.documentKey.trim(),
                    status = getClientIdentifierStatus(state.status.trim().ifEmpty { null }),
                ),
            )
            val result = documents.createDocument(
                entityType = Constants.ENTITY_TYPE_CLIENT_IDENTIFIERS,
                // The IDENTIFIER's entity id is the client's here, which is what the original
                // passed (`createDocument(ENTITY_TYPE_CLIENT_IDENTIFIERS, route.clientId)`).
                entityId = state.clientId.toInt(),
                upload = body,
            )
            when (result) {
                is MutationResult.Applied -> {
                    mutableStateFlow.value = state.copy(isSubmitting = false)
                    sendEvent(ClientAddIdentifierEvent.Finished)
                }

                is MutationResult.Blocked -> mutableStateFlow.value = state.copy(
                    isSubmitting = false,
                    error = if (result.reason == BlockReason.OFFLINE) {
                        IdentifierError.Offline
                    } else {
                        IdentifierError.UploadFailed
                    },
                )

                is MutationResult.Failed -> {
                    Logger.e(tag = TAG, throwable = result.cause) { "identifier document failed" }
                    mutableStateFlow.value =
                        state.copy(isSubmitting = false, error = IdentifierError.UploadFailed)
                }

                is MutationResult.Conflicted ->
                    mutableStateFlow.value =
                        state.copy(isSubmitting = false, error = IdentifierError.UploadFailed)
            }
        }
    }

    internal companion object {
        const val CLIENT_ID_KEY = "clientId"
        private const val STATE_TIMEOUT_MS = 5_000L
        private const val TAG = "ClientAddIdentifier"
    }
}

/** What went wrong, as a value the screen renders. */
internal enum class IdentifierError {
    /** The identifier itself could not be created — nothing exists yet. */
    CreateFailed,

    /** The identifier EXISTS; only its document failed. The two are not interchangeable. */
    UploadFailed,

    PickFailed,
    Offline,
}

internal data class ClientAddIdentifierState(
    val clientId: Long = -1L,
    val documentTypeId: Int = 0,
    val documentTypeLabel: String = "",
    val documentKey: String = "",
    val status: String = "",
    val description: String = "",
    val documentTypeError: Boolean = false,
    val statusError: Boolean = false,
    val documentKeyError: Boolean = false,
    /** True once the first write lands. From here the identifier exists and only the doc is left. */
    val identifierCreated: Boolean = false,
    val file: PlatformFile? = null,
    val isSourceSheetOpen: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: IdentifierError? = null,
    /** Fineract's own words, when it gave any. Never rewritten. */
    val serverMessage: String? = null,
) {
    val fileName: String? get() = file?.name

    /** The create button: a type and a key, and no write already running. */
    val canCreate: Boolean get() =
        documentTypeId > 0 && documentKey.isNotBlank() && status in IdentifierStatus.values && !isSubmitting

    /** The upload button: a chosen file, and no write already running. */
    val canUpload: Boolean get() = file != null && !isSubmitting
}

internal sealed interface ClientAddIdentifierEvent {
    data object NavigateBack : ClientAddIdentifierEvent

    /** The flow is done — with a document or without one. */
    data object Finished : ClientAddIdentifierEvent
}

internal sealed interface ClientAddIdentifierAction {
    data object NavigateBack : ClientAddIdentifierAction
    data object CreateIdentifier : ClientAddIdentifierAction
    data object OpenFileSource : ClientAddIdentifierAction
    data object DismissFileSource : ClientAddIdentifierAction
    data object PickFromGallery : ClientAddIdentifierAction
    data object PickFromFiles : ClientAddIdentifierAction
    data object UploadDocument : ClientAddIdentifierAction
    data object Finish : ClientAddIdentifierAction
    data object DismissError : ClientAddIdentifierAction
    data class UpdateDocumentType(val id: Int, val label: String) : ClientAddIdentifierAction
    data class UpdateDocumentKey(val value: String) : ClientAddIdentifierAction

    /** [value] is one of [IdentifierStatus.values] — the wire value, not the displayed label. */
    data class UpdateStatus(val value: String) : ClientAddIdentifierAction
    data class UpdateDescription(val value: String) : ClientAddIdentifierAction
}

/**
 * Fineract's identifier statuses, as sent. REQUIRED, as in the original's dropdown
 * (`statusList = listOf(Inactive, Active)`, Submit disabled until one was chosen): the server
 * dereferences `status` on create, so leaving it out is a 500 rather than a validation error
 * (measured on the sandbox 2026-10-24). The port had made it an optional free-text field.
 */
internal object IdentifierStatus {
    const val ACTIVE = "Active"
    const val INACTIVE = "Inactive"
    val values = listOf(INACTIVE, ACTIVE)
}

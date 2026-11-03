/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.documentpreview

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.document.DocumentRepository
import kpt.core.model.objects.noncoreobjects.Document

/**
 * Shows one saved document in the app — ported from
 * `6b66e8a43:feature/client/documentPreviewScreen/` (view half) and the `VIEW_DOCUMENT` phase of
 * `clientIdentifiersAddUpdate/`.
 *
 * ## What the original actually did, and what changed
 *
 * The original had NO single viewer. A document-list "View" downloaded the file and then forked on
 * the extension: a PDF went to an EXTERNAL app (`openPdfWithDefaultExternalApp`), an image went to
 * `DocumentPreviewScreen`, which shared a feature-local `DocumentSelectAndUploadRepository` singleton
 * with the add-document form. The identifier list's "View document" went somewhere else again — a
 * phase of the add/update form that drew its own `PdfPreview`.
 *
 * Both callers land here now, and both kinds of file render in place (`AppDocumentViewer`). The
 * operator chose in-app over external (2026-10-23): documents need the auth token to download, so
 * the app holds the bytes anyway; officers are often offline and on devices with no PDF app; and an
 * ID scan stays out of other apps. Recorded as a §6a deviation in `MIGRATION.md`.
 *
 * The upload half of `DocumentPreviewScreen` (pick, Submit / Reject) is NOT here — the ported
 * add-document form already owns its picked file (see `ClientAddDocumentViewModel`).
 *
 * ## Two ways to name the document
 *
 * - **By id** — the documents list knows the row it was tapped on.
 * - **By match key** — an identifier does not know its document's id. The original stored a key in
 *   the uploaded document's `description` and searched the entity's documents for it. See
 *   [findIdentifierDocument] for the lookup and why it is looser than the original's.
 */
internal class ClientDocumentPreviewViewModel(
    private val repository: DocumentRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<ClientDocumentPreviewState, ClientDocumentPreviewEvent, ClientDocumentPreviewAction>(
    ClientDocumentPreviewState(
        title = savedStateHandle.get<String>(TITLE_KEY).orEmpty(),
    ),
) {

    private val entityType: String = savedStateHandle.get<String>(ENTITY_TYPE_KEY).orEmpty()
    private val entityId: Int = savedStateHandle.get<Int>(ENTITY_ID_KEY) ?: -1
    private val documentId: Int = savedStateHandle.get<Int>(DOCUMENT_ID_KEY) ?: NO_DOCUMENT_ID
    private val matchKey: String = savedStateHandle.get<String>(MATCH_KEY_KEY).orEmpty()
    private val matchPrefix: String = savedStateHandle.get<String>(MATCH_PREFIX_KEY).orEmpty()

    init {
        load()
    }

    override fun handleAction(action: ClientDocumentPreviewAction) {
        when (action) {
            ClientDocumentPreviewAction.NavigateBack -> sendEvent(ClientDocumentPreviewEvent.NavigateBack)
            ClientDocumentPreviewAction.Retry -> load()
        }
    }

    private fun load() {
        mutableStateFlow.value = state.copy(content = PreviewContent.Loading)
        viewModelScope.launch {
            val document: Document? = if (documentId != NO_DOCUMENT_ID) {
                Document(id = documentId)
            } else {
                // The server's list when online (the scan may have been uploaded seconds ago), the
                // database's when not — see `DocumentRepository.fetchDocuments`.
                when (val listed = repository.fetchDocuments(entityType, entityId)) {
                    null -> {
                        mutableStateFlow.value = state.copy(content = PreviewContent.Failed)
                        return@launch
                    }
                    else -> findIdentifierDocument(listed, matchKey, matchPrefix)
                }
            }

            if (document == null) {
                mutableStateFlow.value = state.copy(content = PreviewContent.NoDocument)
                return@launch
            }

            val bytes = repository.downloadDocument(entityType, entityId, document.id)
            mutableStateFlow.value = state.copy(
                content = if (bytes == null) {
                    PreviewContent.Failed
                } else {
                    PreviewContent.Loaded(bytes = bytes, fileName = document.fileName, mimeType = document.type)
                },
            )
        }
    }

    private companion object {
        const val ENTITY_TYPE_KEY = "entityType"
        const val ENTITY_ID_KEY = "entityId"
        const val DOCUMENT_ID_KEY = "documentId"
        const val MATCH_KEY_KEY = "matchKey"
        const val MATCH_PREFIX_KEY = "matchPrefix"
        const val TITLE_KEY = "title"
    }
}

/** Route value meaning "find the document by its match key instead". */
internal const val NO_DOCUMENT_ID = -1

/**
 * Finds an identifier's document among its entity's documents.
 *
 * The original matched `description == documentType + documentKey + status` EXACTLY, so the moment
 * an identifier's status changed (Active ↔ Inactive) its scan could no longer be found — the key it
 * was uploaded under no longer matched the key the list computed. The exact match is still tried
 * first (it is what existing uploads carry), then the status-free `type + key` prefix: Fineract
 * keeps a document key unique per type for a client, so the prefix still names one identifier.
 */
internal fun findIdentifierDocument(documents: List<Document>, matchKey: String, matchPrefix: String): Document? =
    documents.firstOrNull { matchKey.isNotEmpty() && it.description == matchKey }
        ?: documents.firstOrNull { matchPrefix.isNotEmpty() && it.description?.startsWith(matchPrefix) == true }

internal data class ClientDocumentPreviewState(
    val title: String = "",
    val content: PreviewContent = PreviewContent.Loading,
)

internal sealed interface PreviewContent {
    data object Loading : PreviewContent

    /** The entity has no document under this key — an identifier uploaded without a scan. */
    data object NoDocument : PreviewContent

    /** The list or the file could not be fetched; offline lands here too, and Retry recovers. */
    data object Failed : PreviewContent

    class Loaded(val bytes: ByteArray, val fileName: String?, val mimeType: String?) : PreviewContent
}

internal sealed interface ClientDocumentPreviewEvent {
    data object NavigateBack : ClientDocumentPreviewEvent
}

internal sealed interface ClientDocumentPreviewAction {
    data object NavigateBack : ClientDocumentPreviewAction
    data object Retry : ClientDocumentPreviewAction
}

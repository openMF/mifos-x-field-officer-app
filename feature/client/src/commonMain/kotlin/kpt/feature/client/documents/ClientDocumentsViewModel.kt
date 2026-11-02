/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.documents

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.combineContent
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.common.utils.Constants
import kpt.core.data.document.DocumentCommandRepository
import kpt.core.data.document.DocumentRepository
import kpt.core.model.objects.noncoreobjects.Document

/**
 * The client's attached documents — ported from
 * `6b66e8a43:feature/client/clientDocuments/ClientDocumentsViewModel.kt`.
 *
 * ## The search filter is a STREAM combine, not a hand-managed list
 *
 * The original kept `clientDocuments` in the state and overwrote it with the filtered result on
 * `SearchDocument`, so the unfiltered list was LOST — clearing the query could not restore it
 * without re-fetching, and `Refresh` was the only way back. The rows stay the stream's here and
 * the query is combined over them, which is the same shape the client list uses.
 *
 * ## `pullDownRefresh` is gone
 *
 * It was a `Boolean` in the state that nothing ever set back to `false` on the error path. The
 * stream's own `refresh()` drives `PullToRefreshBox` from `ScreenState`'s freshness instead.
 *
 * ## View opens the in-app preview
 *
 * The original's `ViewDocument` downloaded the file HERE and then forked: a PDF went to an external
 * app (`OpenExternalPdfViewer`, five `expect`/`actual` pairs under `feature/client/utils`), an image
 * to `DocumentPreviewScreen`. Downloading is the preview screen's job now and both kinds render in
 * place, so this ViewModel only names the row — see `ClientDocumentPreviewViewModel`.
 */
internal class ClientDocumentsViewModel(
    repository: DocumentRepository,
    private val commands: DocumentCommandRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<ClientDocumentsState, ClientDocumentsEvent, ClientDocumentsAction>(
    ClientDocumentsState(),
) {

    private val clientId: Int = savedStateHandle.get<Int>(CLIENT_ID_KEY) ?: -1

    private val stream = repository.documentsStream(
        entityType = Constants.ENTITY_TYPE_CLIENTS,
        entityId = clientId,
        scope = viewModelScope,
    )

    /** The query actually applied to the list — committed on search, not on every keystroke. */
    private val appliedQuery = MutableStateFlow("")

    val documents: StateFlow<ScreenState<List<Document>>> =
        stream.state.applyQuery(appliedQuery)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    override fun handleAction(action: ClientDocumentsAction) {
        when (action) {
            ClientDocumentsAction.NavigateBack -> sendEvent(ClientDocumentsEvent.NavigateBack)
            ClientDocumentsAction.AddDocument -> sendEvent(ClientDocumentsEvent.AddDocument(clientId))

            is ClientDocumentsAction.ViewDocument -> sendEvent(
                ClientDocumentsEvent.ViewDocument(
                    clientId = clientId,
                    documentId = action.document.id,
                    title = action.document.name ?: action.document.fileName.orEmpty(),
                ),
            )
            ClientDocumentsAction.Refresh -> stream.refresh()

            ClientDocumentsAction.ToggleSearch -> {
                val active = !state.isSearchBarActive
                mutableStateFlow.value = state.copy(isSearchBarActive = active, searchText = "")
                // Closing the bar clears the FILTER too. The original left the filtered list in
                // place, so dismissing search left a list that looked like the whole set and was
                // not.
                if (!active) appliedQuery.value = ""
            }

            is ClientDocumentsAction.UpdateSearchQuery ->
                mutableStateFlow.value = state.copy(searchText = action.query)

            ClientDocumentsAction.SearchDocument -> appliedQuery.value = state.searchText

            is ClientDocumentsAction.DeleteDocument ->
                mutableStateFlow.value = state.copy(
                    dialogState = ClientDocumentsState.DialogState.ConfirmDeletion(
                        documentName = action.documentName,
                        documentId = action.documentId,
                    ),
                )

            ClientDocumentsAction.CloseDialog ->
                mutableStateFlow.value = state.copy(dialogState = null, writeError = null)

            is ClientDocumentsAction.ConfirmDeleteDocument -> delete(action.documentId)
        }
    }

    private fun delete(documentId: Int) {
        if (state.isDeleting) return
        mutableStateFlow.value = state.copy(isDeleting = true, dialogState = null, writeError = null)

        viewModelScope.launch {
            val result = commands.removeDocument(
                entityType = Constants.ENTITY_TYPE_CLIENTS,
                entityId = clientId,
                documentId = documentId,
            )
            when (result) {
                is MutationResult.Applied -> {
                    mutableStateFlow.value = state.copy(isDeleting = false)
                    // The deleted row is the stream's, and the command repository is cache-free —
                    // same shape as the note delete, which shipped broken for exactly this reason.
                    stream.refresh(forceFresh = true)
                }

                is MutationResult.Blocked -> mutableStateFlow.value = state.copy(
                    isDeleting = false,
                    writeError = if (result.reason == BlockReason.OFFLINE) {
                        DocumentWriteError.Offline
                    } else {
                        DocumentWriteError.Failed
                    },
                )

                is MutationResult.Failed -> {
                    Logger.e(tag = TAG, throwable = result.cause) { "document delete failed" }
                    mutableStateFlow.value =
                        state.copy(isDeleting = false, writeError = DocumentWriteError.Failed)
                }

                is MutationResult.Conflicted ->
                    mutableStateFlow.value =
                        state.copy(isDeleting = false, writeError = DocumentWriteError.Failed)
            }
        }
    }

    private companion object {
        const val TAG = "ClientDocuments"
        const val CLIENT_ID_KEY = "clientId"
        const val STATE_TIMEOUT_MS = 5_000L
    }
}

/**
 * Applies the committed search query to the rows.
 *
 * Built on `combineContent`, as `ClientListViewModel.applyFilters` is: a non-Content state passes
 * through untouched, so a filter never turns an Error into an apparently-empty list, and Content
 * keeps its `fetchedAt` / freshness signal without this file rebuilding it.
 */
internal fun Flow<ScreenState<List<Document>>>.applyQuery(query: Flow<String>): Flow<ScreenState<List<Document>>> =
    combineContent(query) { rows, q, _ -> rows.filter { it.matches(q) } }

/**
 * Matches a document against a search query.
 *
 * Name, file name and description — the original filtered on `name` alone, which missed a document
 * whose FILE name the officer remembers but whose display name they do not. Widened deliberately:
 * a search that cannot find a document the user is looking at is the defect.
 */
internal fun Document.matches(query: String): Boolean {
    if (query.isBlank()) return true
    return listOfNotNull(name, fileName, description).any { it.contains(query, ignoreCase = true) }
}

internal data class ClientDocumentsState(
    val searchText: String = "",
    val isSearchBarActive: Boolean = false,
    val isDeleting: Boolean = false,
    val dialogState: DialogState? = null,
    val writeError: DocumentWriteError? = null,
) {
    /** One case, where the original had three — the other two described the read. */
    sealed interface DialogState {
        data class ConfirmDeletion(val documentName: String, val documentId: Int) : DialogState
    }
}

internal enum class DocumentWriteError { Offline, Failed }

internal sealed interface ClientDocumentsEvent {
    data object NavigateBack : ClientDocumentsEvent
    data class AddDocument(val clientId: Int) : ClientDocumentsEvent
    data class ViewDocument(val clientId: Int, val documentId: Int, val title: String) : ClientDocumentsEvent
}

internal sealed interface ClientDocumentsAction {
    data object NavigateBack : ClientDocumentsAction
    data object AddDocument : ClientDocumentsAction
    data object Refresh : ClientDocumentsAction
    data object ToggleSearch : ClientDocumentsAction
    data object SearchDocument : ClientDocumentsAction
    data object CloseDialog : ClientDocumentsAction
    data class UpdateSearchQuery(val query: String) : ClientDocumentsAction
    data class DeleteDocument(val documentName: String, val documentId: Int) : ClientDocumentsAction
    data class ConfirmDeleteDocument(val documentId: Int) : ClientDocumentsAction
    data class ViewDocument(val document: Document) : ClientDocumentsAction
}

/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.document.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.data.document.DocumentCommandRepository
import kpt.core.data.document.DocumentRepository
import kpt.core.model.objects.noncoreobjects.Document

/**
 * Documents attached to ANY Fineract entity — ported from
 * `6b66e8a43:feature/document/documentList/DocumentListViewModel.kt`.
 *
 * Reached from group detail today (`groupNavGraph(loadDocumentList = …)`); loan and savings reach it
 * when those slices land. Distinct from `feature/client/documents`, which is the CLIENT-specific
 * list the client profile opens — the original ships both, with different callers, so both are
 * ported.
 *
 * ## Substrate changes
 *
 * Three use-cases (`GetDocumentsList`, `DownloadDocument`, `RemoveDocument`) collapse to
 * `documentsStream` + two repository calls. `core/domain` is deleted on this substrate (§2).
 *
 * `isRefreshing` was a `Boolean` the original set `true` then immediately `false` around a
 * launch — so it was never observably true and the pull-to-refresh spinner never showed. The
 * stream's own state drives it.
 *
 * ## A defect carried over, not fixed: Download reports success and saves nothing
 *
 * `downloadDocument` fetches the attachment's bytes and then DISCARDS them — the original sets a
 * flag, the screen shows "Download successful", and no file is ever written or opened. The bytes
 * are fetched here too, for the same reason: fixing it means choosing where a file lands on four
 * platforms, which is a product decision rather than a port. Tracked as
 * `document-download-saves-nothing` in CAPABILITY_GAPS; the affordance is left exactly as the
 * original draws it (§6a).
 */
internal class DocumentListViewModel(
    repository: DocumentRepository,
    private val documents: DocumentRepository,
    private val commands: DocumentCommandRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<DocumentListState, DocumentListEvent, DocumentListAction>(
    DocumentListState(
        entityType = savedStateHandle.get<String>(ENTITY_TYPE_KEY).orEmpty(),
        entityId = savedStateHandle.get<Int>(ENTITY_ID_KEY) ?: -1,
    ),
) {

    private val stream = repository.documentsStream(
        entityType = state.entityType,
        entityId = state.entityId,
        scope = viewModelScope,
    )

    val documentList: StateFlow<ScreenState<List<Document>>> = stream.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    override fun handleAction(action: DocumentListAction) {
        when (action) {
            DocumentListAction.NavigateBack -> sendEvent(DocumentListEvent.NavigateBack)
            DocumentListAction.Refresh -> stream.refresh()
            DocumentListAction.UploadClicked ->
                sendEvent(DocumentListEvent.Upload(state.entityType, state.entityId))

            DocumentListAction.DismissNotice ->
                mutableStateFlow.value = state.copy(notice = null)

            is DocumentListAction.ShowOptions ->
                mutableStateFlow.value = state.copy(selectedDocumentId = action.documentId)

            DocumentListAction.CloseOptions ->
                mutableStateFlow.value = state.copy(selectedDocumentId = null)

            is DocumentListAction.Download -> download(action.documentId)
            is DocumentListAction.Remove -> remove(action.documentId)
        }
    }

    private fun download(documentId: Int) {
        if (state.isWorking) return
        mutableStateFlow.value = state.copy(isWorking = true, selectedDocumentId = null, notice = null)

        viewModelScope.launch {
            val bytes = documents.downloadDocument(state.entityType, state.entityId, documentId)
            mutableStateFlow.value = state.copy(
                isWorking = false,
                // Reports what actually happened: bytes arrived, or they did not. The original
                // reported success unconditionally. Neither version SAVES the file — see the
                // class header.
                notice = if (bytes != null) DocumentNotice.Downloaded else DocumentNotice.DownloadFailed,
            )
        }
    }

    private fun remove(documentId: Int) {
        if (state.isWorking) return
        mutableStateFlow.value = state.copy(isWorking = true, selectedDocumentId = null, notice = null)

        viewModelScope.launch {
            val result = commands.removeDocument(state.entityType, state.entityId, documentId)
            mutableStateFlow.value = when (result) {
                is MutationResult.Applied -> {
                    // The removed row is the stream's, and the command repository is cache-free.
                    stream.refresh(forceFresh = true)
                    state.copy(isWorking = false, notice = DocumentNotice.Removed)
                }

                is MutationResult.Blocked -> state.copy(
                    isWorking = false,
                    notice = if (result.reason == BlockReason.OFFLINE) {
                        DocumentNotice.Offline
                    } else {
                        DocumentNotice.RemoveFailed
                    },
                )

                is MutationResult.Failed -> {
                    Logger.e(tag = TAG, throwable = result.cause) { "remove document failed" }
                    state.copy(isWorking = false, notice = DocumentNotice.RemoveFailed)
                }

                is MutationResult.Conflicted ->
                    state.copy(isWorking = false, notice = DocumentNotice.RemoveFailed)
            }
        }
    }

    private companion object {
        const val TAG = "DocumentList"
        const val ENTITY_TYPE_KEY = "entityType"
        const val ENTITY_ID_KEY = "entityId"
        const val STATE_TIMEOUT_MS = 5_000L
    }
}

internal data class DocumentListState(
    val entityType: String = "",
    val entityId: Int = -1,
    /** Which row's options sheet is open. The original held this in the screen. */
    val selectedDocumentId: Int? = null,
    val isWorking: Boolean = false,
    val notice: DocumentNotice? = null,
)

/**
 * What to tell the officer after a download or a remove.
 *
 * The original had two `Boolean`s (`downloadDocumentState`, `removeDocumentState`) that were set
 * true and never reset, so each snackbar could only ever fire once per screen visit.
 */
internal enum class DocumentNotice { Downloaded, DownloadFailed, Removed, RemoveFailed, Offline }

internal sealed interface DocumentListEvent {
    data object NavigateBack : DocumentListEvent
    data class Upload(val entityType: String, val entityId: Int) : DocumentListEvent
}

internal sealed interface DocumentListAction {
    data object NavigateBack : DocumentListAction
    data object Refresh : DocumentListAction
    data object UploadClicked : DocumentListAction
    data object CloseOptions : DocumentListAction
    data object DismissNotice : DocumentListAction
    data class ShowOptions(val documentId: Int) : DocumentListAction
    data class Download(val documentId: Int) : DocumentListAction
    data class Remove(val documentId: Int) : DocumentListAction
}

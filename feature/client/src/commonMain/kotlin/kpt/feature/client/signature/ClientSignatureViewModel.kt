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

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.attafitamim.krop.core.crop.CropError
import com.attafitamim.krop.core.crop.CropResult
import com.attafitamim.krop.core.crop.ImageCropper
import com.attafitamim.krop.core.crop.crop
import com.attafitamim.krop.core.crop.imageCropper
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.ImageFormat
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.util.encodeToByteArray
import io.github.vinceglb.filekit.dialogs.compose.util.toImageBitmap
import io.github.vinceglb.filekit.dialogs.openFilePicker
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.mapContent
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.common.utils.Constants
import kpt.core.data.document.DocumentCommandRepository
import kpt.core.data.document.DocumentRepository
import kpt.core.ui.util.documentUpload

/**
 * The client's signature — ported from
 * `6b66e8a43:feature/client/clientSignature/ClientSignatureViewModel.kt` (446 lines).
 *
 * Reached from client DETAILS' "Update Signature" button. (The original also wired it from
 * `clientDetailRoute`, but nothing navigates to that screen — see `ClientProfileRoute`'s KDoc.)
 *
 * ## A signature IS a document
 *
 * There is no signature endpoint. It is a document attached to the client under the reserved name
 * `clientSignature`, so the whole screen is: read the client's document list, find that one, and
 * download / create / replace / delete it. The original spread this across five use-cases
 * (`GetDocumentsListUseCase`, `DownloadDocumentUseCase`, `CreateSignatureUseCase`,
 * `UpdateSignatureUseCase`, `RemoveDocumentUseCase`) over two repositories; it is one stream plus
 * three commands here.
 *
 * ## `signatureId` is DERIVED, not stored
 *
 * The original wrote it into the state from `getSignatureId()`, then `getSignature()` read it back
 * off the state — a two-step sequence where the second step silently did nothing if the first had
 * not landed yet (`state.signatureId?.let { … }` with no else). It is derived from the document
 * stream here, so the id and the list cannot disagree.
 *
 * ## The image bytes are a one-shot blob
 *
 * Downloaded on demand into [ClientSignatureState.clientSignatureImage]. A client with no signature
 * is the common case, not an error — so it never reaches [ScreenState].
 */
internal class ClientSignatureViewModel(
    repository: DocumentRepository,
    private val documents: DocumentRepository,
    private val commands: DocumentCommandRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<ClientSignatureState, ClientSignatureEvent, ClientSignatureAction>(
    ClientSignatureState(),
) {

    private val clientId: Int = savedStateHandle.get<Int>(CLIENT_ID_KEY) ?: -1

    private val stream = repository.documentsStream(
        entityType = Constants.CLIENTS,
        entityId = clientId,
        scope = viewModelScope,
    )

    /**
     * The signature's document id, or `null` when the client has none.
     *
     * Derived from the document list, so "is there a signature" and "which document is it" are one
     * answer rather than two fields that can disagree.
     */
    val signatureId: StateFlow<ScreenState<Int?>> = stream.state
        .mapContent { docs, _ -> docs.firstOrNull { it.name == SIGNATURE_NAME }?.id }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    /** krop's cropper, held here because the crop is awaited across a suspend boundary. */
    val cropState: ImageCropper = imageCropper()

    override fun handleAction(action: ClientSignatureAction) {
        when (action) {
            ClientSignatureAction.NavigateBack -> sendEvent(ClientSignatureEvent.NavigateBack)
            ClientSignatureAction.OnRetry -> stream.retry()

            ClientSignatureAction.ShowDeleteDialog ->
                mutableStateFlow.value = state.copy(dialogState = ClientSignatureState.DialogState.ShowDeleteDialog)

            ClientSignatureAction.ShowUploadOptionsBottomSheet ->
                mutableStateFlow.value = state.copy(dialogState = ClientSignatureState.DialogState.ShowUploadOptions)

            ClientSignatureAction.OpenSignatureDraw ->
                mutableStateFlow.value = state.copy(dialogState = ClientSignatureState.DialogState.ShowSignatureDraw)

            ClientSignatureAction.OnDismissDialog ->
                mutableStateFlow.value = state.copy(dialogState = null, writeError = null)

            is ClientSignatureAction.LoadSignature -> loadImage(action.signatureId)

            is ClientSignatureAction.DeleteSignature -> delete(action.signatureId)

            ClientSignatureAction.OpenImagePicker -> viewModelScope.launch {
                val file = FileKit.openFilePicker(mode = FileKitMode.Single, type = FileKitType.Image)
                file?.let { crop(it.toImageBitmap()) }
            }

            // Fired by the draw dialog with the canvas it produced.
            is ClientSignatureAction.CropImage -> viewModelScope.launch { crop(action.file) }
        }
    }

    private fun loadImage(signatureId: Int) {
        viewModelScope.launch {
            val bytes = documents.downloadDocument(Constants.CLIENTS, clientId, signatureId)
            mutableStateFlow.value = state.copy(clientSignatureImage = bytes)
        }
    }

    /**
     * Crop, then create or replace.
     *
     * Whether this is a CREATE or an UPDATE is decided from the derived id at submit time, which is
     * the one place it can be known — the original read `state.signatureId`, set by an earlier
     * load that may not have completed.
     */
    private suspend fun crop(file: ImageBitmap) {
        mutableStateFlow.value = state.copy(dialogState = ClientSignatureState.DialogState.ShowImageCrop)

        when (val result = cropState.crop(bmp = file)) {
            is CropError -> mutableStateFlow.value = state.copy(
                dialogState = null,
                writeError = SignatureWriteError.CropFailed,
            )

            CropResult.Cancelled -> mutableStateFlow.value = state.copy(dialogState = null)

            is CropResult.Success -> submit(result.bitmap)
        }
    }

    private suspend fun submit(bitmap: ImageBitmap) {
        if (state.isSubmitting) return
        mutableStateFlow.value = state.copy(isSubmitting = true, dialogState = null, writeError = null)

        val body = documentUpload(
            file = bitmap.encodeToByteArray(ImageFormat.PNG),
            name = SIGNATURE_NAME,
            extension = ImageFormat.PNG.name,
            description = SIGNATURE_DESCRIPTION,
        )
        val existingId = (signatureId.value as? ScreenState.Content)?.data

        val result = if (existingId == null) {
            commands.createDocument(Constants.CLIENTS, clientId, body)
        } else {
            commands.updateDocument(Constants.CLIENTS, clientId, existingId, body)
        }
        finish(result, onApplied = { mutableStateFlow.value = state.copy(clientSignatureImage = null) })
    }

    private fun delete(signatureId: Int) {
        if (state.isSubmitting) return
        mutableStateFlow.value = state.copy(isSubmitting = true, dialogState = null, writeError = null)

        viewModelScope.launch {
            val result = commands.removeDocument(Constants.CLIENTS, clientId, signatureId)
            finish(result, onApplied = { mutableStateFlow.value = state.copy(clientSignatureImage = null) })
        }
    }

    /**
     * Shared tail for all three writes: clear the in-flight flag, surface the failure, and on
     * success re-read the document list so the derived id follows.
     */
    private fun <T> finish(result: MutationResult<T>, onApplied: () -> Unit) {
        when (result) {
            is MutationResult.Applied -> {
                mutableStateFlow.value = state.copy(isSubmitting = false)
                // Clears the cached bytes so the screen re-downloads the NEW signature rather than
                // redrawing the old one, and re-reads the list so a create gains its id.
                onApplied()
                stream.refresh(forceFresh = true)
            }

            is MutationResult.Blocked -> mutableStateFlow.value = state.copy(
                isSubmitting = false,
                writeError = if (result.reason == BlockReason.OFFLINE) {
                    SignatureWriteError.Offline
                } else {
                    SignatureWriteError.Failed
                },
            )

            is MutationResult.Failed -> {
                Logger.e(tag = TAG, throwable = result.cause) { "signature write failed" }
                mutableStateFlow.value =
                    state.copy(isSubmitting = false, writeError = SignatureWriteError.Failed)
            }

            is MutationResult.Conflicted ->
                mutableStateFlow.value =
                    state.copy(isSubmitting = false, writeError = SignatureWriteError.Failed)
        }
    }

    private companion object {
        const val TAG = "ClientSignature"
        const val CLIENT_ID_KEY = "clientId"
        const val STATE_TIMEOUT_MS = 5_000L

        /** The reserved document name a signature is stored under. The original's constants. */
        const val SIGNATURE_NAME = "clientSignature"
        const val SIGNATURE_DESCRIPTION = "Client Signature"
    }
}

internal data class ClientSignatureState(
    val clientSignatureImage: ByteArray? = null,
    val isSubmitting: Boolean = false,
    val dialogState: DialogState? = null,
    val writeError: SignatureWriteError? = null,
) {
    /**
     * Four cases, where the original had six. `Loading` and `Error` described the document READ and
     * are the content area's now; these four are all genuinely dialogs or sheets.
     */
    sealed interface DialogState {
        data object ShowDeleteDialog : DialogState
        data object ShowUploadOptions : DialogState
        data object ShowImageCrop : DialogState
        data object ShowSignatureDraw : DialogState
    }

    // ByteArray in a data class compares by identity — see ClientProfileState.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ClientSignatureState) return false
        return clientSignatureImage.contentEquals(other.clientSignatureImage) &&
            isSubmitting == other.isSubmitting &&
            dialogState == other.dialogState &&
            writeError == other.writeError
    }

    override fun hashCode(): Int {
        var result = clientSignatureImage?.contentHashCode() ?: 0
        result = 31 * result + isSubmitting.hashCode()
        result = 31 * result + (dialogState?.hashCode() ?: 0)
        result = 31 * result + (writeError?.hashCode() ?: 0)
        return result
    }
}

internal enum class SignatureWriteError { Offline, Failed, CropFailed }

internal sealed interface ClientSignatureEvent {
    data object NavigateBack : ClientSignatureEvent
}

internal sealed interface ClientSignatureAction {
    data object NavigateBack : ClientSignatureAction
    data object OnRetry : ClientSignatureAction
    data object ShowDeleteDialog : ClientSignatureAction
    data object ShowUploadOptionsBottomSheet : ClientSignatureAction
    data object OpenImagePicker : ClientSignatureAction
    data object OpenSignatureDraw : ClientSignatureAction
    data object OnDismissDialog : ClientSignatureAction

    /** Both carry the id the SCREEN read off the derived stream — see the ViewModel's header. */
    data class LoadSignature(val signatureId: Int) : ClientSignatureAction
    data class DeleteSignature(val signatureId: Int) : ClientSignatureAction

    data class CropImage(val file: ImageBitmap) : ClientSignatureAction
}

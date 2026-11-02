/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.editprofile

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
import io.github.vinceglb.filekit.name
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.common.utils.FileKitUtil
import kpt.core.data.client.ClientCommandRepository
import kpt.core.data.client.ClientRepository
import kpt.core.model.objects.noncoreobjects.DocumentUpload
import kpt.core.ui.util.getMimeTypeFromPlatformFile
import kpt.core.ui.util.imageToByteArray

/**
 * The client's PROFILE PHOTO — ported from `6b66e8a43:feature/client/clientEditProfile/`
 * (`ClientProfileEditScreen.kt` 336 lines, `ClientProfileEditViewModel.kt` 342,
 * `ClientEditProfileNavigation.kt` 48).
 *
 * Reached from client DETAILS' "Update Photo" row, which was passing `onUpdatePhoto = null` — so
 * the row was not drawn at all — until this screen existed. It is the photo twin of the signature
 * screen next door: show what is stored, replace it, or remove it.
 *
 * ## Named `ClientEditProfile*`, not `ClientProfileEdit*`
 *
 * The original disagreed with itself — the nav file declared `ClientEditProfileRoute` while the
 * screen and ViewModel were `ClientProfileEdit*`. One spelling, matching the package and the route.
 *
 * ## The read is a one-shot blob, not a stream
 *
 * `ClientDetailsRepository.getImage()` returned `Flow<DataState<String>>`, so the original carried
 * Loading and Error states for the PHOTO READ and gated the whole screen behind
 * `dialogState != Loading`. On this substrate the photo is
 * [ClientRepository.clientImage] — a suspend `String?` — for the reason its KDoc gives: a client
 * with no photo is the common case rather than an error, and the avatar placeholder IS the empty
 * state. This is the treatment the sibling `ClientProfileViewModel` already applies to the same
 * read, so the two screens cannot disagree about what "no photo" looks like.
 *
 * That also removes a leak. The original's `uploadImage` called `loadImage(route.id)` on success,
 * which launched ANOTHER collector on the same never-completing flow without cancelling the first —
 * so uploading N times left N live collectors on the screen. A suspend call cannot leak that way.
 *
 * ## Writes report an OUTCOME
 *
 * [ClientCommandRepository] returns [MutationResult], so offline is distinguishable from failed.
 * The original wrapped `deleteClientImage` in `try` / `catch (e: Exception)` and surfaced
 * `e.message`, which cannot tell a field officer "you are offline, nothing was deleted" from "the
 * server refused" — and on a dropped connection showed a Ktor exception message. Both writes route
 * through [finish], and the three distinguishable outcomes become [PhotoWriteError] values the
 * screen renders as copy.
 *
 * ## What the original declared and nothing reached
 *
 * Five dead members are NOT ported, because §6a requires an unreachable affordance to be absent
 * rather than inert. Each is listed with its evidence in the port report:
 *
 *  - `OnNameChanged` / `OnAccountNoChanged` — no dispatcher. They rewrote the displayed name and
 *    account number in state with no write anywhere, so a "profile edit" screen could edit its own
 *    labels and nothing else.
 *  - `DismissModalBottomSheet` — no dispatcher; a duplicate of `OnDismissDialog`, which is what the
 *    sheet actually sent.
 *  - `UpdateImagePicker` + the `openImagePicker: Boolean` it set — no dispatcher, and nothing read
 *    the field.
 *  - `OnSaveSuccess` — the screen handled this event; `handleAction` never sent it.
 *  - `OnRetry` — handled as `-> Unit`, a retry button wired to nothing (see [ClientEditProfileAction]).
 */
internal class ClientEditProfileViewModel(
    private val repository: ClientRepository,
    private val commands: ClientCommandRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<ClientEditProfileState, ClientEditProfileEvent, ClientEditProfileAction>(
    ClientEditProfileState(),
) {

    private val clientId: Int = savedStateHandle.get<Int>(CLIENT_ID_KEY) ?: -1

    /**
     * krop's cropper, held HERE rather than in the state.
     *
     * The original made it a default property value of the state data class
     * (`val cropState: ImageCropper = imageCropper()`). Two problems: `imageCropper()` builds a
     * Compose-state-backed object, so every default construction of the state — including the one
     * in a test or a preview — builds a new one; and a mutable identity-compared object inside a
     * data class makes `copy` equality meaningless. It survives a crop here because the crop is
     * awaited across a suspend boundary on ONE instance. Same placement as the signature sibling.
     */
    val cropState: ImageCropper = imageCropper()

    init {
        loadImage()
    }

    private fun loadImage() {
        viewModelScope.launch {
            mutableStateFlow.value =
                state.copy(profileImage = imageToByteArray(repository.clientImage(clientId)))
        }
    }

    override fun handleAction(action: ClientEditProfileAction) {
        when (action) {
            ClientEditProfileAction.NavigateBack ->
                sendEvent(ClientEditProfileEvent.NavigateBack)

            ClientEditProfileAction.ShowDeleteDialog ->
                mutableStateFlow.value =
                    state.copy(dialogState = ClientEditProfileState.DialogState.ShowDeleteDialog)

            ClientEditProfileAction.ShowUploadOptionsBottomSheet ->
                mutableStateFlow.value =
                    state.copy(dialogState = ClientEditProfileState.DialogState.ShowUploadOptions)

            ClientEditProfileAction.OnDismissDialog ->
                mutableStateFlow.value =
                    state.copy(dialogState = null, writeError = null)

            ClientEditProfileAction.OnConfirmDeleteClick -> delete()

            ClientEditProfileAction.OnSuccessContinue ->
                sendEvent(ClientEditProfileEvent.NavigateBack)

            ClientEditProfileAction.OpenImagePicker -> viewModelScope.launch {
                val file = FileKit.openFilePicker(mode = FileKitMode.Single, type = FileKitType.Image)
                // A cancelled picker closes the sheet rather than leaving it open over nothing.
                if (file == null) {
                    mutableStateFlow.value = state.copy(dialogState = null)
                } else {
                    crop(file.toImageBitmap(), file.name)
                }
            }

            ClientEditProfileAction.OpenCamera -> viewModelScope.launch { capture() }
        }
    }

    /**
     * Take a photo with the device camera.
     *
     * The original reached the camera through a `rememberPlatformCameraLauncher` composable in its
     * own `feature/client/utils` package, which does not exist on this substrate. The capability
     * does — `FileKitUtil.takePhoto()` is an EXISTING expect/actual in `core/common` with real
     * actuals on Android and iOS — so this uses it rather than introducing a second platform seam
     * for the same job.
     *
     * Desktop, JS and Wasm implement `platformTakePhoto` as a failed `Result`, and that failure is
     * reported as [PhotoWriteError.CameraUnavailable] instead of being swallowed. The option is
     * still OFFERED on those targets because nothing in the port exposes "does this platform have
     * a camera" as a value a composable could gate on, and inventing that flag is a wider change
     * than this screen; an option that explains why it cannot run is at least honest, where a
     * silent no-op would not be. Recorded in the port report.
     */
    private suspend fun capture() {
        FileKitUtil.takePhoto().first().fold(
            onSuccess = { file ->
                if (file == null) {
                    mutableStateFlow.value = state.copy(dialogState = null)
                } else {
                    crop(file.toImageBitmap(), file.name)
                }
            },
            onFailure = {
                Logger.e(tag = TAG, throwable = it) { "camera unavailable" }
                mutableStateFlow.value = state.copy(
                    dialogState = null,
                    writeError = PhotoWriteError.CameraUnavailable,
                )
            },
        )
    }

    /** Crop to the square the avatar renders, then upload. */
    private suspend fun crop(file: ImageBitmap, fileName: String) {
        mutableStateFlow.value =
            state.copy(dialogState = ClientEditProfileState.DialogState.ShowImageCrop)

        when (val result = cropState.crop(bmp = file)) {
            // The original put the literal "Unexpected error" into its error state from the
            // ViewModel. A user-facing string belongs in resources and is rendered by the screen.
            is CropError ->
                mutableStateFlow.value =
                    state.copy(dialogState = null, writeError = PhotoWriteError.CropFailed)

            CropResult.Cancelled -> mutableStateFlow.value = state.copy(dialogState = null)

            is CropResult.Success -> upload(result.bitmap, fileName)
        }
    }

    private suspend fun upload(bitmap: ImageBitmap, fileName: String) {
        if (state.isSubmitting) return
        mutableStateFlow.value = state.copy(isSubmitting = true, dialogState = null, writeError = null)

        val result = commands.uploadClientImage(
            clientId,
            // The same parts multipartRequestBody produced — the name, `<base>.png`, its MIME type and
            // the bytes — as a core/model value; core/data does the HTTP encoding.
            DocumentUpload(
                name = fileName,
                fileName = "${fileName.substringBeforeLast(".")}.${ImageFormat.PNG.name.lowercase()}",
                mimeType = getMimeTypeFromPlatformFile(ImageFormat.PNG.name),
                bytes = bitmap.encodeToByteArray(ImageFormat.PNG),
            ),
        )
        finish(result) {
            // Re-read rather than showing the bytes just uploaded: Fineract re-encodes what it
            // stores, so this is the photo every OTHER screen will render. The original re-read
            // too, for the same reason.
            mutableStateFlow.value = state.copy(
                profileImage = imageToByteArray(repository.clientImage(clientId)),
                dialogState = ClientEditProfileState.DialogState.UploadSucceeded,
            )
        }
    }

    private fun delete() {
        if (state.isSubmitting) return
        mutableStateFlow.value = state.copy(isSubmitting = true, dialogState = null, writeError = null)

        viewModelScope.launch {
            finish(commands.deleteClientImage(clientId)) {
                // No success dialog on delete — the avatar going back to its placeholder IS the
                // confirmation, and that is what the original did.
                mutableStateFlow.value = state.copy(profileImage = null)
            }
        }
    }

    /**
     * Shared tail for both writes: clear the in-flight flag, then either run the success step or
     * surface the failure as copy the screen can render.
     */
    private suspend fun finish(result: MutationResult<Unit>, onApplied: suspend () -> Unit) {
        when (result) {
            is MutationResult.Applied -> {
                mutableStateFlow.value = state.copy(isSubmitting = false)
                onApplied()
            }

            is MutationResult.Blocked -> mutableStateFlow.value = state.copy(
                isSubmitting = false,
                writeError = if (result.reason == BlockReason.OFFLINE) {
                    PhotoWriteError.Offline
                } else {
                    PhotoWriteError.Failed
                },
            )

            is MutationResult.Failed -> {
                Logger.e(tag = TAG, throwable = result.cause) { "client photo write failed" }
                mutableStateFlow.value =
                    state.copy(isSubmitting = false, writeError = PhotoWriteError.Failed)
            }

            is MutationResult.Conflicted ->
                mutableStateFlow.value =
                    state.copy(isSubmitting = false, writeError = PhotoWriteError.Failed)
        }
    }

    internal companion object {
        const val CLIENT_ID_KEY = "clientId"
        private const val TAG = "ClientEditProfile"
    }
}

internal data class ClientEditProfileState(
    val profileImage: ByteArray? = null,
    val isSubmitting: Boolean = false,
    val dialogState: DialogState? = null,
    val writeError: PhotoWriteError? = null,
) {
    /**
     * Four cases, where the original had six. Its `Loading` and `Error` described the photo READ
     * and are gone with the stream (see the ViewModel's header); these four are all genuinely a
     * dialog or a sheet.
     */
    sealed interface DialogState {
        data object ShowDeleteDialog : DialogState
        data object ShowUploadOptions : DialogState
        data object ShowImageCrop : DialogState

        /**
         * The post-upload confirmation.
         *
         * Carries no status and no message, unlike the original's
         * `ShowStatusDialog(status, msg)`. It was only ever constructed with
         * `ResultStatus.SUCCESS` and the one success string, so the status parameter had a single
         * value and the dialog's whole FAILURE arm — `failureTitle`, `failureMessage`,
         * `showButton = status == FAILURE` — was unreachable. Failures now reach [writeError].
         */
        data object UploadSucceeded : DialogState
    }

    // ByteArray in a data class compares by identity — see ClientProfileState. Content equality,
    // or every state copy looks like a change and the avatar recomposes on each one.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ClientEditProfileState) return false
        return profileImage.contentEquals(other.profileImage) &&
            isSubmitting == other.isSubmitting &&
            dialogState == other.dialogState &&
            writeError == other.writeError
    }

    override fun hashCode(): Int {
        var result = profileImage?.contentHashCode() ?: 0
        result = 31 * result + isSubmitting.hashCode()
        result = 31 * result + (dialogState?.hashCode() ?: 0)
        result = 31 * result + (writeError?.hashCode() ?: 0)
        return result
    }
}

/** Why the photo did not change. Each maps to one string the screen renders. */
internal enum class PhotoWriteError { Offline, Failed, CropFailed, CameraUnavailable }

internal sealed interface ClientEditProfileEvent {
    data object NavigateBack : ClientEditProfileEvent
}

/**
 * What the screen can ask for.
 *
 * `OnRetry` is NOT here. The original declared it, handled it as `-> Unit`, and drew the button
 * that sent it — `MifosErrorComponent(isRetryEnabled = true, onRetry = { onAction(OnRetry) })` —
 * so a visible Retry did nothing at all. That component rendered the photo READ's error state,
 * which this port does not have (a missing photo is not an error), so there is no surface left to
 * put a retry on and it is absent rather than re-wired to an invented affordance.
 */
internal sealed interface ClientEditProfileAction {
    data object NavigateBack : ClientEditProfileAction
    data object ShowDeleteDialog : ClientEditProfileAction
    data object ShowUploadOptionsBottomSheet : ClientEditProfileAction
    data object OnConfirmDeleteClick : ClientEditProfileAction
    data object OpenImagePicker : ClientEditProfileAction
    data object OpenCamera : ClientEditProfileAction
    data object OnDismissDialog : ClientEditProfileAction

    /** The original's `OnNext` — dismiss the success dialog, which means leave the screen. */
    data object OnSuccessContinue : ClientEditProfileAction
}

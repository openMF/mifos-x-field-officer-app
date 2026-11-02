/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.createnew

import androidx.compose.ui.graphics.ImageBitmap
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.serialization.Serializable
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.combineScreenStates
import kpt.core.base.store.screen.dataOrNull
import kpt.core.base.store.screen.mapContent
import kpt.core.base.store.submit.draft.FormDraftRepository
import kpt.core.base.store.submit.draft.FormDraftSlot
import kpt.core.base.store.submit.draft.SavedFormDraft
import kpt.core.base.store.submit.draft.offlineWhenUnsent
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.common.utils.FileKitUtil
import kpt.core.data.client.ClientCommandRepository
import kpt.core.data.client.ClientRepository
import kpt.core.data.office.OfficeRepository
import kpt.core.data.staff.StaffRepository
import kpt.core.model.client.ClientPayload
import kpt.core.model.client.Options
import kpt.core.model.datatable.DataTable
import kpt.core.model.draft.CreateForm
import kpt.core.model.objects.clients.Address
import kpt.core.model.objects.error.serverErrorMessage
import kpt.core.model.objects.noncoreobjects.DocumentUpload
import kpt.core.model.staff.Staff
import kpt.core.model.utils.ApiDateFormatter
import kpt.core.ui.util.getMimeTypeFromPlatformFile
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Create a new client — ported from `6b66e8a43:feature/client/createNewClient/`
 * (`CreateNewClientScreen.kt` 1352 lines, `CreateNewClientViewModel.kt` 206,
 * `CreateNewClientUiState.kt` 42).
 *
 * Reached from the client list's FAB, which until now passed `onCreateClient = null` so the FAB was
 * not drawn at all.
 *
 * The screen is one long form: photo, five identity text fields, five option dropdowns, an
 * activate-now toggle with its date, and — on tenants that enable the address module — a nine-field
 * address block. The write is `ClientCommandRepository.createClient`, optionally followed by
 * `uploadClientImage` for the photo.
 *
 * ## FIXED: four dropdowns stored the id of a DIFFERENT option than the one they showed
 *
 * The office, staff, client-type and classification dropdowns each displayed an alphabetically
 * SORTED list and then read the chosen id out of the UNSORTED one, by the sorted list's index:
 *
 * ```kotlin
 * MifosTextFieldDropdown(
 *     onOptionSelected = { index, value ->
 *         selectedOffice = value
 *         selectedOfficeId = officeList[index].id        // <- unsorted
 *     },
 *     options = officeList.sortedBy { it.name }.map { it.name.toString() },  // <- sorted
 * )
 * ```
 *
 * Whenever Fineract's own order differs from alphabetical — which is the normal case, offices come
 * back in hierarchy order — the officer picks "Bhopal Branch" and the payload carries the id of
 * whichever office happens to sit at that position unsorted. A client filed against the wrong
 * office, the wrong loan officer, or the wrong client type, with the right name on screen. The same
 * slip appears four times; only the gender dropdown escaped it, because that one does not sort.
 *
 * Fixed by carrying `(id, label)` pairs: [ClientOption] is sorted ONCE and the choice IS the
 * option, so there is no index to mismatch. This is the same treatment the savings application port
 * applied to its eight parallel index fields.
 *
 * ## FIXED: every message this screen produced was invisible
 *
 * The screen created a `SnackbarHostState`, threaded it through seven functions, and **never hosted
 * it** — there is no `Scaffold` and no `SnackbarHost` anywhere in the 1352 lines. So all four
 * validation messages, plus "no staff associated with office" and the creation confirmation, were
 * shown to a host that does not exist. Tapping Submit with an empty first name did, visibly,
 * nothing at all.
 *
 * Validation failures are now field-level [CreateClientFieldError] values the screen renders on the
 * offending input — which is also where an officer can act on them — and the outcome is a dialog in
 * state rather than a snackbar fired from a composition body.
 *
 * ## FIXED: on a tenant with the address module DISABLED, the form could never be submitted
 *
 * `isAllFieldsValid` ran the address-type check unconditionally:
 *
 * ```kotlin
 * !isAddressTypeIdValid(addressTypeId, scope, snackbarHostState) -> { false }
 * ```
 *
 * and `isAddressTypeIdValid` fails on `addressTypeId <= 0`. With the address block not enabled it
 * is never drawn, so the id stays `0`, so validation always failed — and per the defect above, with
 * no visible reason. Create-client was inoperable on every address-disabled tenant. The check now
 * applies only when the address block is actually on screen.
 *
 * ## FIXED: a picked photo was silently dropped, or the screen dead-ended
 *
 * The photo existed TWICE: `CreateNewClientContent` held it in a plain `remember`, and the
 * ViewModel held its own copy set through `onImageSelected`. `createClientWithImage` — the flag
 * that decides whether to upload after creating — was set from the COMPOSABLE's copy, which does
 * not survive a configuration change, while the flag itself was `rememberSaveable` and did. So:
 *
 *  - rotate after picking, then submit → the composable's copy is gone, the flag is not set, the
 *    client is created and the photo the officer chose is never uploaded; and
 *  - rotate with the flag already set → `uploadImage` starts with `if (selectedImage.value == null)
 *    return`, so it returns before setting any state, leaving the screen on `SetClientId`, a branch
 *    that renders nothing. Blank screen, client created, no navigation, no message.
 *
 * The photo is ViewModel state here and nowhere else, so there is one answer to "is there a photo".
 *
 * ## FIXED: offline was reported as "waiting for checker approval"
 *
 * `DataManagerClient.createClient` returned `null` for TWO different things — the user being in
 * offline mode (payload stashed in Room) and Fineract accepting the client into a maker-checker
 * queue — and the ViewModel rendered both as `ShowWaitingForCheckerApproval`. An officer working
 * offline was told their client was awaiting approval when it had not left the device.
 * [MutationResult] separates them: `Blocked(OFFLINE)` is offline, and `Applied` with no `clientId`
 * is genuinely the checker queue.
 *
 * ## FIXED: staff came from an office nobody had chosen
 *
 * `LaunchedEffect(Unit) { if (officeList.isNotEmpty()) loadStaffInOffice(officeList[0].id) }` loaded
 * the FIRST office's staff while leaving the office field blank and `selectedOfficeId = 0`. The
 * staff dropdown therefore offered office zero's loan officers against a payload that named no
 * office. [staffOptions] follows the CHOSEN office, and the dropdown is disabled until there is
 * one. The companion `LaunchedEffect(staffInOffices)` that announced "no staff associated with
 * office" also fired on first composition, before any office was picked — the screen shows that
 * note only once a chosen office has genuinely come back empty.
 *
 * ## FIXED: the activation date could not be set to today
 *
 * `isSelectableDate` returned `utcTimeMillis >= now`, i.e. FUTURE ONLY, so the one date an officer
 * activating a client today actually wants was unselectable. Occurrence nine of this inverted rule
 * in the migration; it is not in the future that an activation happened.
 *
 * ## FIXED: an invalid phone number vanished instead of being refused
 *
 * `createClientPayload` set `mobileNo` only `if (PhoneNumberUtil.isGlobalPhoneNumber(mobileNumber))`
 * and said nothing otherwise, so a typo dropped the number from the payload and the client was
 * created without it. A number that does not parse is now a field error; a BLANK number is still
 * simply omitted, which is the original's behaviour for the field being optional.
 *
 * That rule also behaved differently per platform. `PhoneNumberUtil` was an `expect object` whose
 * Android actual delegated to `android.telephony.PhoneNumberUtils.isGlobalPhoneNumber` while the
 * desktop, native, JS and Wasm actuals each carried the same stub under a
 * "TODO: Implement the logic" comment: `phoneNumber.all { it.isDigit() || it == '+' }`. The same
 * `+1 (555) 123-4567` was kept on Android and dropped everywhere else. [isGlobalPhoneNumber] states
 * the Android rule once in common code, so there is no platform seam and no stub.
 *
 * ## Datatables
 *
 * The original branched on `clientTemplate.dataTables` and handed the un-submitted payload to a
 * separate datatable screen — whose fields could not be typed into and whose values were never sent
 * (see ClientDataTables.kt). The tables are now sections of this form, validated with it, and sent in
 * `ClientPayload.datatables` with the create.
 */
@OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)
internal class CreateNewClientViewModel(
    clients: ClientRepository,
    offices: OfficeRepository,
    private val staff: StaffRepository,
    private val commands: ClientCommandRepository,
    drafts: FormDraftRepository,
) : BaseViewModel<CreateNewClientState, CreateNewClientEvent, CreateNewClientAction>(
    CreateNewClientState(),
) {

    /** A create that did not land is kept here — resumable on this form, listed in Sync & Drafts. */
    private val draft = FormDraftSlot(
        drafts,
        viewModelScope,
        CreateForm.CLIENT.key,
        ownerId = null,
        ClientFormSnapshot.serializer(),
        ClientPayload.serializer(),
    )

    val offeredDraft: StateFlow<SavedFormDraft?> = draft.offered

    /** The payload of the last submit — what "Save as draft" keeps. */
    private var lastPayload: ClientPayload? = null

    private fun keepDraft(payload: ClientPayload, failure: String?) {
        val name = listOf(state.text(CreateClientField.FIRST_NAME), state.text(CreateClientField.LAST_NAME))
            .filter { it.isNotBlank() }.joinToString(" ")
        draft.keep(name, ClientFormSnapshot.of(state), payload, failure)
    }

    private val templateStream = clients.clientsTemplateStream(viewModelScope)
    private val officesStream = offices.officesStream(viewModelScope)
    private val addressConfigStream = clients.addressConfigurationStream(viewModelScope)
    private val addressTemplateStream = clients.addressTemplateStream(viewModelScope)

    /**
     * krop's cropper, held here because the crop is awaited across a suspend boundary.
     *
     * Same placement as the photo and signature siblings — not a field of the state, where a
     * mutable identity-compared object would make `copy` equality meaningless.
     */
    val cropState: ImageCropper = imageCropper()

    /**
     * The form's option sets, which arrive from TWO reads: the client template carries gender,
     * client type and classification; the office list is its own catalog.
     *
     * Combined, so the form appears when it is complete. The original set `ShowClientTemplate` off
     * the template alone and filled the office dropdown from a second StateFlow as it landed, which
     * means an officer could fill in the whole form before the offices arrived and submit it with
     * `officeId = 0`. Worse, `loadOffices`' failure path wrote `ShowError` over the template state,
     * so a failed office fetch replaced a usable form — and a slower template fetch then wrote the
     * form back over the error. Office is mandatory on the payload, so it is a prerequisite here
     * rather than a race.
     */
    val form: StateFlow<ScreenState<CreateClientForm>> = combineScreenStates(
        templateStream.state,
        officesStream.state,
    ) { template, officeList ->
        CreateClientForm(
            genders = template.genderOptions.orEmpty().toOptions(sorted = false),
            clientTypes = template.clientTypeOptions.orEmpty().toOptions(),
            classifications = template.clientClassificationOptions.orEmpty().toOptions(),
            serverActivationDate = template.activationDate.toPickerMillis(),
            dataTables = template.dataTables.orEmpty().withEntryColumns(),
            offices = officeList
                .map { ClientOption(id = it.id, label = it.name.orEmpty()) }
                .sortedBy { it.label },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    /**
     * The address block's options, or `null` when the tenant has the address module off.
     *
     * Deliberately NOT part of [form]: a tenant with addresses disabled must still be able to
     * create a client, so neither of these two reads may gate the form. A failure of either is read
     * as "no address block" for the same reason — and that is also what the ORIGINAL effectively
     * did, by accident. `loadAddressConfiguration`'s catch assigned `ShowError`, and the line
     * directly after the call assigned `ShowClientTemplate` over it:
     *
     * ```kotlin
     * .collect {
     *     loadAddressConfiguration()
     *     _createNewClientUiState.value = CreateNewClientUiState.ShowClientTemplate(...)
     * }
     * ```
     *
     * so the error was overwritten before it could render and the address block was silently
     * dropped. The EFFECT is reasonable; it was just never a decision. It is one here.
     */
    val address: StateFlow<AddressFormOptions?> = combine(
        addressConfigStream.state,
        addressTemplateStream.state,
    ) { config, template ->
        val enabled = config.dataOrNull?.enabled == true
        val options = template.dataOrNull
        if (enabled && options != null) {
            AddressFormOptions(
                types = options.addressTypeIdOptions.toOptions(),
                states = options.stateProvinceIdOptions.toOptions(),
                countries = options.countryIdOptions.toOptions(),
            )
        } else {
            null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), null)

    /**
     * One [ScreenDataStream] per office id the officer has visited.
     *
     * `staffStream` is keyed by office, so a new stream is needed each time the office changes.
     * Memoised rather than rebuilt: every stream `stateIn`s on `viewModelScope`, so re-creating one
     * per selection would accumulate live collectors for the lifetime of the screen. Re-selecting
     * an office also then shows its staff immediately.
     */
    private val staffStreams = mutableMapOf<Int, ScreenDataStream<List<Staff>>>()

    private fun staffStreamFor(officeId: Int): ScreenDataStream<List<Staff>> =
        staffStreams.getOrPut(officeId) { staff.staffStream(officeId, viewModelScope) }

    /**
     * The chosen office's staff.
     *
     * `ScreenState.Empty` is meaningful here and the screen renders it as "this office has no loan
     * officers" — the message the original showed, but only when it is true. Before an office is
     * chosen there is nothing to load, so the flow sits at `Empty` and the screen keeps the
     * dropdown disabled on `state.choice(OFFICE) == null`.
     */
    val staffOptions: StateFlow<ScreenState<List<ClientOption>>> = stateFlow
        .map { it.choice(CreateClientField.OFFICE)?.id }
        .distinctUntilChanged()
        .flatMapLatest { officeId ->
            if (officeId == null) {
                flowOf<ScreenState<List<ClientOption>>>(ScreenState.Empty)
            } else {
                staffStreamFor(officeId).state.mapContent { list, _ ->
                    list
                        // A staff member with no id cannot be sent, so it is not offered — the
                        // same `mapNotNull` guard the savings application port applies to products.
                        .mapNotNull { member ->
                            ClientOption(
                                id = member.id ?: return@mapNotNull null,
                                label = member.displayName.orEmpty(),
                            )
                        }
                        .sortedBy { it.label }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Empty)

    override fun handleAction(action: CreateNewClientAction) {
        when (action) {
            CreateNewClientAction.NavigateBack -> sendEvent(CreateNewClientEvent.NavigateBack)

            CreateNewClientAction.ResumeDraft -> draft.resume()?.let { mutableStateFlow.value = it.applyTo(state) }
            CreateNewClientAction.DiscardDraft -> draft.discard()
            CreateNewClientAction.SaveDraft -> saveDraft()
            CreateNewClientAction.OnRetry -> retryAll()
            CreateNewClientAction.OnDismissDialog ->
                mutableStateFlow.value = state.copy(dialog = null)

            CreateNewClientAction.OnCreatedContinue -> onCreatedContinue()

            CreateNewClientAction.OnSubmit -> submit()
            else -> handlePhotoOrFormAction(action)
        }
    }

    /** The photo dialog actions; anything else is a form edit. */
    private fun handlePhotoOrFormAction(action: CreateNewClientAction) {
        when (action) {
            CreateNewClientAction.ShowPhotoOptions ->
                mutableStateFlow.value = state.copy(dialog = CreateClientDialog.PhotoOptions)

            CreateNewClientAction.OpenImagePicker -> openImagePicker()

            CreateNewClientAction.OpenCamera -> viewModelScope.launch { capture() }

            CreateNewClientAction.RemovePhoto ->
                mutableStateFlow.value = state.copy(dialog = null, photo = null)

            else -> handleFormAction(action)
        }
    }

    private fun handleFormAction(action: CreateNewClientAction) {
        when (action) {
            is CreateNewClientAction.OnTextChanged -> mutableStateFlow.value = state.copy(
                texts = state.texts + (action.field to action.value),
                errors = state.errors - action.field,
            )

            is CreateNewClientAction.OnOptionSelected -> onOptionSelected(action)

            is CreateNewClientAction.OnActiveToggled ->
                mutableStateFlow.value = state.copy(isActive = action.active)

            is CreateNewClientAction.OnAddressActiveToggled ->
                mutableStateFlow.value = state.copy(isAddressActive = action.active)

            is CreateNewClientAction.OpenDatePicker ->
                mutableStateFlow.value = state.copy(openDatePicker = action.field)

            CreateNewClientAction.CloseDatePicker ->
                mutableStateFlow.value = state.copy(openDatePicker = null)

            is CreateNewClientAction.OnDatePicked -> onDatePicked(action)
            is CreateNewClientAction.DataTableValueChanged -> mutableStateFlow.value = state.copy(
                dataTableValues = state.dataTableValues + (action.field to action.value),
                dataTableErrors = state.dataTableErrors - action.field,
            )

            else -> Unit
        }
    }

    private fun saveDraft() {
        lastPayload?.let { payload ->
            val failed = state.dialog as? CreateClientDialog.Failed
            keepDraft(payload, failure = failed?.message ?: NOT_ACCEPTED)
            mutableStateFlow.value = state.copy(dialog = failed?.copy(draftSaved = true))
        }
    }

    private fun retryAll() {
        templateStream.retry()
        officesStream.retry()
        // Retried too: a failed address read is silently a disabled address block, so this
        // is the only way back from one.
        addressConfigStream.retry()
        addressTemplateStream.retry()
        // And the chosen office's staff, if one has been chosen — the original's retry
        // reloaded the template and the offices and left the staff read with no way back.
        state.choice(CreateClientField.OFFICE)?.id?.let { staffStreamFor(it).retry() }
    }

    private fun openImagePicker() {
        viewModelScope.launch {
            val file = FileKit.openFilePicker(mode = FileKitMode.Single, type = FileKitType.Image)
            // A cancelled picker closes the sheet rather than leaving it open over nothing.
            if (file == null) {
                mutableStateFlow.value = state.copy(dialog = null)
            } else {
                crop(file.toImageBitmap(), file.name)
            }
        }
    }

    private fun onCreatedContinue() {
        val clientId = (state.dialog as? CreateClientDialog.Created)?.clientId
        mutableStateFlow.value = state.copy(dialog = null)
        clientId
            ?.let { sendEvent(CreateNewClientEvent.Created(it)) }
            ?: sendEvent(CreateNewClientEvent.NavigateBack)
    }

    /**
     * Choosing an office clears the staff choice.
     *
     * A loan officer belongs to one office, so a staff member selected under the previous office is
     * not merely stale — sending it files the client against a loan officer who does not work
     * there. The original left `selectedStaffId` untouched across an office change.
     */
    private fun onOptionSelected(action: CreateNewClientAction.OnOptionSelected) {
        val cleared = if (action.field == CreateClientField.OFFICE) {
            state.choices - CreateClientField.STAFF
        } else {
            state.choices
        }
        mutableStateFlow.value = state.copy(
            choices = cleared + (action.field to action.option),
            errors = state.errors - action.field,
        )
    }

    private fun onDatePicked(action: CreateNewClientAction.OnDatePicked) {
        mutableStateFlow.value = when (action.field) {
            ClientDateField.ACTIVATION -> state.copy(activationDate = action.millis)
            ClientDateField.DATE_OF_BIRTH -> state.copy(dateOfBirth = action.millis)
        }.copy(openDatePicker = null)
    }

    /**
     * Take a photo with the device camera.
     *
     * The original reached the camera through a `rememberPlatformCameraLauncher` composable in its
     * own `feature.client.utils` package, which does not exist on this substrate. The capability
     * does — `FileKitUtil.takePhoto()` is an existing expect/actual in `core/common` — so this uses
     * it rather than opening a second platform seam for the same job. Identical call to the
     * profile-photo sibling, deliberately: both screens pick a client photo.
     */
    private suspend fun capture() {
        FileKitUtil.takePhoto().first().fold(
            onSuccess = { file ->
                if (file == null) {
                    mutableStateFlow.value = state.copy(dialog = null)
                } else {
                    crop(file.toImageBitmap(), file.name)
                }
            },
            onFailure = {
                Logger.e(tag = TAG, throwable = it) { "camera unavailable" }
                mutableStateFlow.value = state.copy(dialog = CreateClientDialog.CameraUnavailable)
            },
        )
    }

    /**
     * Crop to the square the avatar renders, then hold the bytes until the client exists.
     *
     * Cropping replaces the original's `ImageUtil.compressImage(image.readBytes())`: krop already
     * re-encodes what it returns, so a second compression pass adds a platform call and another
     * lossy round for nothing. The bytes stay in state because there is no client to attach them to
     * until the create lands.
     */
    private suspend fun crop(image: ImageBitmap, fileName: String) {
        mutableStateFlow.value = state.copy(dialog = CreateClientDialog.ImageCrop)

        when (val result = cropState.crop(bmp = image)) {
            is CropError ->
                mutableStateFlow.value =
                    state.copy(dialog = CreateClientDialog.CropFailed)

            CropResult.Cancelled -> mutableStateFlow.value = state.copy(dialog = null)

            is CropResult.Success -> mutableStateFlow.value = state.copy(
                dialog = null,
                photo = PickedPhoto(
                    bytes = result.bitmap.encodeToByteArray(ImageFormat.PNG),
                    fileName = fileName,
                ),
            )
        }
    }

    private fun submit() {
        // Needs the options to decide which fields the tenant actually offers — the original's own
        // `clientTemplate.genderOptions?.isNotEmpty() == true` guards, kept.
        val formOptions = form.value.dataOrNull
        if (state.isSubmitting || formOptions == null) return

        val hasAddressBlock = address.value != null

        val errors = state.validate(hasAddressBlock = hasAddressBlock)
        val dataTableErrors = validateDataTables(formOptions.dataTables, state.dataTableValues)
        if (errors.isNotEmpty() || dataTableErrors.isNotEmpty()) {
            mutableStateFlow.value = state.copy(errors = errors, dataTableErrors = dataTableErrors)
            return
        }

        mutableStateFlow.value = state.copy(isSubmitting = true, dialog = null)

        viewModelScope.launch {
            val payload = state.toPayload(formOptions, hasAddressBlock)
            lastPayload = payload
            when (val result = commands.createClient(payload).offlineWhenUnsent()) {
                is MutationResult.Applied -> {
                    draft.clear()
                    onCreated(result.value.clientId)
                }

                is MutationResult.Blocked -> mutableStateFlow.value = state.copy(
                    isSubmitting = false,
                    dialog = if (result.reason == BlockReason.OFFLINE) {
                        // NOT "waiting for checker approval" — see the class KDoc. Kept as a draft
                        // automatically; it is sent on reconnect.
                        keepDraft(payload, failure = null)
                        CreateClientDialog.Failed(offline = true, message = null, draftSaved = true)
                    } else {
                        CreateClientDialog.Failed(offline = false, message = null)
                    },
                )

                is MutationResult.Failed -> {
                    Logger.e(tag = TAG, throwable = result.cause) { "create client failed" }
                    mutableStateFlow.value = state.copy(
                        isSubmitting = false,
                        // The server's own refusal, which is usually the useful half of a Fineract
                        // 400 — the original surfaced it through `MFErrorParser.errorMessage(e)`.
                        // It replaced the whole form with an error screen there, losing every typed
                        // field; it is a dismissable dialog here and the form is still behind it.
                        dialog = CreateClientDialog.Failed(
                            offline = false,
                            message = result.cause.serverErrorMessage(),
                        ),
                    )
                }

                is MutationResult.Conflicted -> mutableStateFlow.value = state.copy(
                    isSubmitting = false,
                    dialog = CreateClientDialog.Failed(offline = false, message = null),
                )
            }
        }
    }

    /**
     * The client landed. Upload the photo if there is one, then confirm.
     *
     * A null [clientId] is the maker-checker queue: Fineract accepted the client for approval and
     * returned no id, so there is nothing to attach a photo to and nothing to navigate to.
     *
     * A FAILED photo upload still confirms the client, with a note. The original surfaced it as
     * `ShowStringError`, which replaced the screen — leaving a client that had just been created
     * unreachable behind an error about its photo.
     */
    private suspend fun onCreated(clientId: Int?) {
        if (clientId == null) {
            mutableStateFlow.value =
                state.copy(isSubmitting = false, dialog = CreateClientDialog.AwaitingChecker)
            return
        }

        val photo = state.photo
        if (photo == null) {
            mutableStateFlow.value = state.copy(
                isSubmitting = false,
                dialog = CreateClientDialog.Created(clientId, photoFailed = false),
            )
            return
        }

        mutableStateFlow.value = state.copy(isUploadingPhoto = true)
        val upload = commands.uploadClientImage(
            clientId,
            DocumentUpload(
                name = photo.fileName,
                fileName = "${photo.fileName.substringBeforeLast(".")}.${ImageFormat.PNG.name.lowercase()}",
                mimeType = getMimeTypeFromPlatformFile(ImageFormat.PNG.name),
                bytes = photo.bytes,
            ),
        )
        if (upload !is MutationResult.Applied) {
            Logger.e(tag = TAG) { "client $clientId created, photo upload did not apply" }
        }
        mutableStateFlow.value = state.copy(
            isSubmitting = false,
            isUploadingPhoto = false,
            dialog = CreateClientDialog.Created(
                clientId = clientId,
                photoFailed = upload !is MutationResult.Applied,
            ),
        )
    }

    private companion object {
        const val TAG = "CreateNewClient"
        const val STATE_TIMEOUT_MS = 5_000L
    }
}

/** `{id, name}` option rows as selectable options, alphabetically unless told otherwise. */
private fun List<Options>.toOptions(sorted: Boolean = true): List<ClientOption> {
    val options = map { ClientOption(id = it.id, label = it.name) }
    return if (sorted) options.sortedBy { it.label } else options
}

/**
 * Android's `PhoneNumberUtils.isGlobalPhoneNumber` rule, stated once in common code.
 *
 * At least one digit, an optional leading `+`, and nothing but dialable characters otherwise. See
 * the class KDoc for the `expect`/`actual` this replaces and the per-platform divergence it had.
 */
internal fun String.isGlobalPhoneNumber(): Boolean {
    val body = removePrefix("+")
    return body.any { it.isDigit() } && body.all { it.isDigit() || it in DIALABLE }
}

private const val DIALABLE = "*#-. ()"

/**
 * Only letters and spaces, which is the originals' `"[^a-zA-Z ]".toRegex()` guard inverted.
 *
 * Kept as-is, including its limitation: it rejects any name with an accent, an apostrophe or a
 * hyphen, which excludes a great many real names. Widening it changes who the app will accept as a
 * client, which is a product decision rather than a port. Recorded as a gap.
 */
private val NON_ALPHABETIC = Regex("[^a-zA-Z ]")

/** The fields the form can hold a value, a choice, or an error for. */
internal enum class CreateClientField {
    FIRST_NAME,
    MIDDLE_NAME,
    LAST_NAME,
    MOBILE_NO,
    EXTERNAL_ID,
    GENDER,
    CLIENT_TYPE,
    CLASSIFICATION,
    OFFICE,
    STAFF,
    ADDRESS_TYPE,
    ADDRESS_LINE_1,
    ADDRESS_LINE_2,
    ADDRESS_LINE_3,
    CITY,
    POSTAL_CODE,
    STATE_PROVINCE,
    COUNTRY,
}

/** Why one field is not acceptable. The screen owns how each reads. */
internal enum class CreateClientFieldError { Required, AlphabetsOnly, InvalidPhoneNumber }

/** The two dates on the form. */
internal enum class ClientDateField { ACTIVATION, DATE_OF_BIRTH }

/** One dropdown choice — the id travels WITH the label, which is the dropdown fix. */
@Serializable
internal data class ClientOption(val id: Int, val label: String)

/** The option sets the form needs before it can be filled in. */
internal data class CreateClientForm(
    val genders: List<ClientOption> = emptyList(),
    val clientTypes: List<ClientOption> = emptyList(),
    val classifications: List<ClientOption> = emptyList(),
    val offices: List<ClientOption> = emptyList(),
    /**
     * The SERVER's business date from the template — the activation default. The device clock is
     * wrong for this: Fineract rejects a date ahead of its own ("Activation date cannot be in the
     * future"), and a device east of the server's timezone is a day ahead every evening (measured
     * 2026-10-24: IST device on the 24th, server on the 23rd — "Client Active" failed every time).
     */
    val serverActivationDate: Long? = null,
    /** The tenant's client datatables with something to fill in — see ClientDataTables.kt. */
    val dataTables: List<DataTable> = emptyList(),
)

/** The address block's option sets. Present only on tenants that enable addresses. */
internal data class AddressFormOptions(
    val types: List<ClientOption> = emptyList(),
    val states: List<ClientOption> = emptyList(),
    val countries: List<ClientOption> = emptyList(),
)

/** A cropped photo waiting for a client to attach itself to. */
internal data class PickedPhoto(val bytes: ByteArray, val fileName: String) {
    // ByteArray compares by identity in a data class, so without this every state copy looks like a
    // new photo and the avatar recomposes on each one. Same treatment as the profile-photo sibling.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PickedPhoto) return false
        return fileName == other.fileName && bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int = 31 * bytes.contentHashCode() + fileName.hashCode()
}

/** Everything the screen puts over the form. */
internal sealed interface CreateClientDialog {
    /** Gallery / Camera / Remove — the original's `MifosSelectImageDialog`. */
    data object PhotoOptions : CreateClientDialog
    data object ImageCrop : CreateClientDialog
    data object CropFailed : CreateClientDialog
    data object CameraUnavailable : CreateClientDialog

    /** Fineract took the client into its maker-checker queue and returned no id. */
    data object AwaitingChecker : CreateClientDialog

    /** [photoFailed] is true when the client exists but its photo did not upload. */
    data class Created(val clientId: Int, val photoFailed: Boolean) : CreateClientDialog

    /**
     * [message] is the server's own refusal when there is one. [draftSaved] = the create was kept
     * (offline automatically, or "Save as draft") and is in Settings › Sync & Drafts.
     */
    data class Failed(val offline: Boolean, val message: String?, val draftSaved: Boolean = false) : CreateClientDialog
}

@OptIn(ExperimentalTime::class)
internal data class CreateNewClientState(
    val texts: Map<CreateClientField, String> = emptyMap(),
    val choices: Map<CreateClientField, ClientOption> = emptyMap(),
    val errors: Map<CreateClientField, CreateClientFieldError> = emptyMap(),
    /** Datatable entries as typed (see [validateDataTables] for each type's encoding). */
    val dataTableValues: Map<DataTableField, String> = emptyMap(),
    val dataTableErrors: Map<DataTableField, DataTableFieldError> = emptyMap(),
    val isActive: Boolean = false,
    /** The officer's PICK; null until they choose one — read through [activationDateOr]. */
    val activationDate: Long? = null,
    val dateOfBirth: Long? = null,
    val isAddressActive: Boolean = false,
    val openDatePicker: ClientDateField? = null,
    val photo: PickedPhoto? = null,
    val isSubmitting: Boolean = false,
    val isUploadingPhoto: Boolean = false,
    val dialog: CreateClientDialog? = null,
) {
    fun text(field: CreateClientField): String = texts[field].orEmpty()

    fun choice(field: CreateClientField): ClientOption? = choices[field]

    /** Nothing may be re-sent while a create or its photo upload is still in flight. */
    val canSubmit: Boolean get() = !isSubmitting && !isUploadingPhoto
}

/**
 * Every field the form refuses, keyed by field.
 *
 * The four rules are the original's, in its order: first name required and alphabetic, middle name
 * alphabetic when filled, last name required and alphabetic, address type required. Two
 * differences, both covered in the ViewModel's KDoc — the address-type rule applies only when the
 * address block is on screen, and the whole set is reported at once instead of stopping at the
 * first failure, which is what lets the screen mark every bad field rather than one.
 *
 * Two rules are ADDED, each turning a silent loss into a visible refusal:
 *  - OFFICE, because `officeId` is mandatory on [ClientPayload] and the original defaulted it
 *    to `0`; submitting without touching the dropdown posted office zero and the server's refusal
 *    then replaced the form, losing every typed field.
 *  - MOBILE_NO, because a number failing the phone rule was dropped from the payload in silence.
 */
internal fun CreateNewClientState.validate(
    hasAddressBlock: Boolean,
): Map<CreateClientField, CreateClientFieldError> = buildMap {
    alphabetic(CreateClientField.FIRST_NAME, text(CreateClientField.FIRST_NAME), required = true)
    alphabetic(CreateClientField.MIDDLE_NAME, text(CreateClientField.MIDDLE_NAME), required = false)
    alphabetic(CreateClientField.LAST_NAME, text(CreateClientField.LAST_NAME), required = true)

    if (choice(CreateClientField.OFFICE) == null) {
        put(CreateClientField.OFFICE, CreateClientFieldError.Required)
    }

    val mobile = text(CreateClientField.MOBILE_NO).trim()
    if (mobile.isNotEmpty() && !mobile.isGlobalPhoneNumber()) {
        put(CreateClientField.MOBILE_NO, CreateClientFieldError.InvalidPhoneNumber)
    }

    if (hasAddressBlock && choice(CreateClientField.ADDRESS_TYPE) == null) {
        put(CreateClientField.ADDRESS_TYPE, CreateClientFieldError.Required)
    }
}

/**
 * One name field: present when required, and letters-and-spaces either way.
 *
 * Trimmed before the emptiness test, which the original was not — it tested `name.isEmpty()`, and a
 * name of nothing but spaces passes the alphabetic rule too, so `firstname = "   "` reached
 * Fineract. Trimming is the safer reading of the same intent, and [toPayload] trims as well so the
 * value validated is the value sent.
 */
private fun MutableMap<CreateClientField, CreateClientFieldError>.alphabetic(
    field: CreateClientField,
    value: String,
    required: Boolean,
) {
    val name = value.trim()
    when {
        name.isEmpty() -> if (required) put(field, CreateClientFieldError.Required)
        name.contains(NON_ALPHABETIC) -> put(field, CreateClientFieldError.AlphabetsOnly)
    }
}

/**
 * The form as a [ClientPayload].
 *
 * Field-for-field the original's `createClientPayload`, including the shape of its optional-field
 * guards: a value reaches the payload only when the tenant offers that option set AND something was
 * chosen, so an untouched dropdown sends nothing rather than `0`.
 *
 * `legalFormId = 1` stays hardcoded, as in the original. The template DOES carry
 * `clientLegalFormOptions` and neither version offers it — an Entity client needs a different
 * payload shape (a company name rather than a person's three names), so exposing the choice is a
 * feature, not a port. Recorded as a gap.
 */
internal fun CreateNewClientState.toPayload(
    form: CreateClientForm,
    hasAddressBlock: Boolean,
): ClientPayload {
    val activationDate = if (isActive) ApiDateFormatter.formatForApi(activationDateOr(form)) else null
    val dateOfBirth = dateOfBirth?.let { ApiDateFormatter.formatForApi(it) }

    return ClientPayload(
        firstname = text(CreateClientField.FIRST_NAME).trim(),
        lastname = text(CreateClientField.LAST_NAME).trim(),
        middlename = text(CreateClientField.MIDDLE_NAME).trim().takeIf { it.isNotEmpty() },
        officeId = choice(CreateClientField.OFFICE)?.id,
        legalFormId = PERSON_LEGAL_FORM_ID,

        active = isActive,
        activationDate = activationDate,
        dateOfBirth = dateOfBirth,
        // Declared only alongside a date, which is the original's guard: a format for no date tells
        // the server how to parse nothing.
        dateFormat = ApiDateFormatter.DATE_FORMAT.takeIf {
            activationDate != null || dateOfBirth != null
        },
        locale = ApiDateFormatter.LOCALE,

        mobileNo = text(CreateClientField.MOBILE_NO).trim().takeIf { it.isNotEmpty() },
        externalId = text(CreateClientField.EXTERNAL_ID).trim().takeIf { it.isNotEmpty() },
        genderId = choice(CreateClientField.GENDER)?.id?.takeIf { form.genders.isNotEmpty() },
        staffId = choice(CreateClientField.STAFF)?.id,
        clientTypeId = choice(CreateClientField.CLIENT_TYPE)
            ?.id
            ?.takeIf { form.clientTypes.isNotEmpty() },
        clientClassificationId = choice(CreateClientField.CLASSIFICATION)
            ?.id
            ?.takeIf { form.classifications.isNotEmpty() },

        address = if (hasAddressBlock) listOf(toAddress()) else null,

        // The tenant's datatables, filled in on this form. Null when it has none — the value the
        // original's non-datatable branch sent.
        datatables = dataTablePayloads(form.dataTables, dataTableValues).takeIf { it.isNotEmpty() },
    )
}

private fun CreateNewClientState.toAddress() = Address(
    addressTypeId = choice(CreateClientField.ADDRESS_TYPE)?.id,
    isActive = isAddressActive,
    addressLine1 = text(CreateClientField.ADDRESS_LINE_1).ifBlank { null },
    addressLine2 = text(CreateClientField.ADDRESS_LINE_2).ifBlank { null },
    addressLine3 = text(CreateClientField.ADDRESS_LINE_3).ifBlank { null },
    city = text(CreateClientField.CITY).ifBlank { null },
    stateProvinceId = choice(CreateClientField.STATE_PROVINCE)?.id,
    countryId = choice(CreateClientField.COUNTRY)?.id,
    postalCode = text(CreateClientField.POSTAL_CODE).ifBlank { null },
)

/** Fineract's legal form for an individual person. The original's literal `1`. */
private const val PERSON_LEGAL_FORM_ID = 1

internal sealed interface CreateNewClientEvent {
    data object NavigateBack : CreateNewClientEvent

    /** The client exists. [clientId] is Fineract's id for it. */
    data class Created(val clientId: Int) : CreateNewClientEvent
}

internal sealed interface CreateNewClientAction {
    /** One datatable field — [value] in the encoding [validateDataTables] documents. */
    data class DataTableValueChanged(val field: DataTableField, val value: String) : CreateNewClientAction
    data object NavigateBack : CreateNewClientAction
    data object ResumeDraft : CreateNewClientAction
    data object DiscardDraft : CreateNewClientAction
    data object SaveDraft : CreateNewClientAction
    data object OnRetry : CreateNewClientAction
    data object OnSubmit : CreateNewClientAction
    data object CloseDatePicker : CreateNewClientAction
    data object ShowPhotoOptions : CreateNewClientAction
    data object OpenImagePicker : CreateNewClientAction
    data object OpenCamera : CreateNewClientAction
    data object RemovePhoto : CreateNewClientAction
    data object OnDismissDialog : CreateNewClientAction

    /** Dismiss the creation confirmation, which means leave the form. */
    data object OnCreatedContinue : CreateNewClientAction

    data class OnTextChanged(val field: CreateClientField, val value: String) :
        CreateNewClientAction

    data class OnOptionSelected(val field: CreateClientField, val option: ClientOption) :
        CreateNewClientAction

    data class OnActiveToggled(val active: Boolean) : CreateNewClientAction
    data class OnAddressActiveToggled(val active: Boolean) : CreateNewClientAction
    data class OpenDatePicker(val field: ClientDateField) : CreateNewClientAction
    data class OnDatePicked(val field: ClientDateField, val millis: Long) : CreateNewClientAction
}

/**
 * Fineract's `[year, month, day]` as picker millis: NOON UTC, so the same calendar day formats back
 * in every timezone from UTC-12 to UTC+12 (midnight UTC is the previous day west of Greenwich).
 */
@OptIn(ExperimentalTime::class)
private fun List<Int>.toPickerMillis(): Long? = takeIf { size >= DATE_PARTS }
    ?.let { runCatching { LocalDate(it[0], it[1], it[2]) }.getOrNull() }
    ?.let { it.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds() + NOON_OFFSET_MS }

private const val DATE_PARTS = 3
private const val NOON_OFFSET_MS = 12 * 60 * 60 * 1000L

/**
 * The activation date to show and send: the officer's pick, else the server's business date, else
 * (template not loaded) the device's today. Derived on read — never written back into state, so a
 * late template can never overwrite a pick.
 */
@OptIn(ExperimentalTime::class)
internal fun CreateNewClientState.activationDateOr(form: CreateClientForm?): Long =
    activationDate ?: form?.serverActivationDate ?: Clock.System.now().toEpochMilliseconds()

/**
 * What a client draft keeps of the form: every typed field, every pick, the datatable entries and the
 * dates and toggles — not the photo (bytes in a draft row) nor any per-visit flag.
 */
@Serializable
internal data class ClientFormSnapshot(
    val texts: Map<CreateClientField, String> = emptyMap(),
    val choices: Map<CreateClientField, ClientOption> = emptyMap(),
    val dataTables: List<DataTableEntry> = emptyList(),
    val isActive: Boolean = false,
    val activationDate: Long? = null,
    val dateOfBirth: Long? = null,
    val isAddressActive: Boolean = false,
) {
    @Serializable
    internal data class DataTableEntry(val table: String, val column: String, val value: String)

    fun applyTo(state: CreateNewClientState) = state.copy(
        texts = texts,
        choices = choices,
        dataTableValues = dataTables.associate { DataTableField(it.table, it.column) to it.value },
        isActive = isActive,
        activationDate = activationDate,
        dateOfBirth = dateOfBirth,
        isAddressActive = isAddressActive,
    )

    companion object {
        fun of(state: CreateNewClientState) = ClientFormSnapshot(
            texts = state.texts,
            choices = state.choices,
            dataTables = state.dataTableValues.map { (field, value) ->
                DataTableEntry(field.table, field.column, value)
            },
            isActive = state.isActive,
            activationDate = state.activationDate,
            dateOfBirth = state.dateOfBirth,
            isAddressActive = state.isAddressActive,
        )
    }
}

/** A refusal with no server sentence — still FAILED, never replayed. */
private const val NOT_ACCEPTED = "Not accepted by the server."

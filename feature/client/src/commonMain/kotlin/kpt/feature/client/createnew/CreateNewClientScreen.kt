/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.createnew

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.attafitamim.krop.core.crop.AspectRatio
import com.attafitamim.krop.core.crop.ImageCropper
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.dataOrNull
import kpt.core.base.store.submit.draft.SavedFormDraft
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.base.ui.generated.resources.draft_save
import kpt.core.base.ui.generated.resources.draft_saved
import kpt.core.base.ui.generated.resources.draft_saved_offline
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.base.ui.submit.FormDraftBanner
import kpt.core.common.utils.DateHelper
import kpt.core.designsystem.fieldofficer.component.AppBottomSheet
import kpt.core.designsystem.fieldofficer.component.AppBottomSheetOptionItem
import kpt.core.designsystem.fieldofficer.component.AppButton
import kpt.core.designsystem.fieldofficer.component.AppDatePickerTextField
import kpt.core.designsystem.fieldofficer.component.AppOutlinedTextField
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.fieldofficer.component.AppTextFieldDropdown
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.AppTypography
import kpt.core.designsystem.theme.DesignToken
import kpt.core.ui.components.AppAlertDialog
import kpt.core.ui.components.AppCheckBox
import kpt.core.ui.components.AppImageCropperDialog
import kpt.core.ui.components.AppProgressIndicatorMini
import kpt.core.ui.components.AppStatusDialog
import kpt.core.ui.components.AppUserImage
import kpt.core.ui.components.ResultStatus
import kpt.feature.client.generated.resources.Res
import kpt.feature.client.generated.resources.feature_client_address
import kpt.feature.client.generated.resources.feature_client_address_line_1
import kpt.feature.client.generated.resources.feature_client_address_line_2
import kpt.feature.client.generated.resources.feature_client_address_line_3
import kpt.feature.client.generated.resources.feature_client_cancel
import kpt.feature.client.generated.resources.feature_client_city
import kpt.feature.client.generated.resources.feature_client_country
import kpt.feature.client.generated.resources.feature_client_createnew_activation_date
import kpt.feature.client.generated.resources.feature_client_createnew_address_active
import kpt.feature.client.generated.resources.feature_client_createnew_address_type
import kpt.feature.client.generated.resources.feature_client_createnew_awaiting_checker
import kpt.feature.client.generated.resources.feature_client_createnew_camera
import kpt.feature.client.generated.resources.feature_client_createnew_camera_unavailable
import kpt.feature.client.generated.resources.feature_client_createnew_client_active
import kpt.feature.client.generated.resources.feature_client_createnew_client_classification
import kpt.feature.client.generated.resources.feature_client_createnew_client_type
import kpt.feature.client.generated.resources.feature_client_createnew_created_message
import kpt.feature.client.generated.resources.feature_client_createnew_created_photo_failed_message
import kpt.feature.client.generated.resources.feature_client_createnew_created_title
import kpt.feature.client.generated.resources.feature_client_createnew_crop_failed
import kpt.feature.client.generated.resources.feature_client_createnew_dob
import kpt.feature.client.generated.resources.feature_client_createnew_error_address_type_required
import kpt.feature.client.generated.resources.feature_client_createnew_error_first_name_alphabets
import kpt.feature.client.generated.resources.feature_client_createnew_error_first_name_empty
import kpt.feature.client.generated.resources.feature_client_createnew_error_invalid_phone
import kpt.feature.client.generated.resources.feature_client_createnew_error_last_name_alphabets
import kpt.feature.client.generated.resources.feature_client_createnew_error_last_name_empty
import kpt.feature.client.generated.resources.feature_client_createnew_error_middle_name_alphabets
import kpt.feature.client.generated.resources.feature_client_createnew_error_office_required
import kpt.feature.client.generated.resources.feature_client_createnew_external_id
import kpt.feature.client.generated.resources.feature_client_createnew_failed
import kpt.feature.client.generated.resources.feature_client_createnew_failed_title
import kpt.feature.client.generated.resources.feature_client_createnew_first_name
import kpt.feature.client.generated.resources.feature_client_createnew_gallery
import kpt.feature.client.generated.resources.feature_client_createnew_gender
import kpt.feature.client.generated.resources.feature_client_createnew_last_name
import kpt.feature.client.generated.resources.feature_client_createnew_middle_name
import kpt.feature.client.generated.resources.feature_client_createnew_no_staff
import kpt.feature.client.generated.resources.feature_client_createnew_office
import kpt.feature.client.generated.resources.feature_client_createnew_offline
import kpt.feature.client.generated.resources.feature_client_createnew_phone_no
import kpt.feature.client.generated.resources.feature_client_createnew_remove_photo
import kpt.feature.client.generated.resources.feature_client_createnew_select_date
import kpt.feature.client.generated.resources.feature_client_createnew_staff
import kpt.feature.client.generated.resources.feature_client_createnew_state_province
import kpt.feature.client.generated.resources.feature_client_createnew_submit
import kpt.feature.client.generated.resources.feature_client_createnew_title
import kpt.feature.client.generated.resources.feature_client_createnew_uploading_photo
import kpt.feature.client.generated.resources.feature_client_dialog_continue
import kpt.feature.client.generated.resources.feature_client_postal_code
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kpt.core.base.ui.generated.resources.Res as BaseRes

/**
 * Create a new client — carried over from
 * `6b66e8a43:feature/client/createNewClient/CreateNewClientScreen.kt` (1352 lines).
 *
 * Photo, the five identity fields, the five option dropdowns, activate-now with its date, then the
 * address block on tenants that enable it, and Submit. Same fields, same order, same copy.
 *
 * See [CreateNewClientViewModel] for the eight defects this port fixes — chief among them that the
 * dropdowns stored the wrong option's id, and that every message the screen produced went to a
 * `SnackbarHostState` the screen never hosted.
 *
 * ## §6a adaptations
 *
 * The `CreateNewClientUiState` sealed class was nine cases covering three unrelated things: the
 * option-set READ (`ShowProgressbar`, `ShowClientTemplate`, `ShowError`), an in-flight WRITE
 * (`ShowProgress`, `ShowClientCreatedSuccessfully`, `SetClientId`, `OnImageUploadSuccess`), and a
 * server VERDICT (`ShowWaitingForCheckerApproval`, `ShowStringError`). They are three things here:
 * [ScreenState] for the read, flags on the state for the write, and [CreateClientDialog] for the
 * verdict. That split is what lets a failed write leave the filled-in form standing — the original
 * replaced the whole `Column` with `MifosSweetError`, losing every typed field.
 *
 * Twenty-five `rememberSaveable` values lived in the composable. They are ViewModel state now, for
 * the reason the ViewModel's KDoc gives about the photo: the form was readable from two places and
 * the two disagreed after a rotation. It also means the form survives the composable being replaced
 * when the read state changes branch, which it previously did not.
 *
 * The photo chooser was a `Dialog` holding three full-width `Button`s. It is [AppBottomSheet] with
 * [AppBottomSheetOptionItem] rows, matching the profile-photo and signature siblings — all three
 * screens pick a client image, and three different choosers for one job is the sort of drift this
 * port exists to remove. The sheet's "Please select action" heading goes with the dialog; the three
 * labelled options are the heading.
 *
 * A dropdown whose option set is EMPTY is not drawn, which is the original's `?.let` around each
 * one — a tenant that configures no client classifications gets no classification field rather than
 * an empty picker.
 */
@Composable
internal fun CreateNewClientScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onClientCreated: ((clientId: Int) -> Unit)? = null,
    viewModel: CreateNewClientViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val offeredDraft by viewModel.offeredDraft.collectAsStateWithLifecycle()
    val form by viewModel.form.collectAsStateWithLifecycle()
    val address by viewModel.address.collectAsStateWithLifecycle()
    val staffOptions by viewModel.staffOptions.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            CreateNewClientEvent.NavigateBack -> onNavigateBack()
            // Falls back to leaving rather than to a no-op: the client exists either way, so the
            // officer must not be left on a form for a client that has already been created.
            is CreateNewClientEvent.Created ->
                onClientCreated?.invoke(event.clientId) ?: onNavigateBack()
        }
    }

    AppScaffold(
        title = stringResource(Res.string.feature_client_createnew_title),
        // Dispatched, not called directly — the ViewModel owns the event.
        onBackPressed = { viewModel.trySendAction(CreateNewClientAction.NavigateBack) },
    ) { scaffoldPadding ->
        CreateNewClientContent(
            state = state,
            form = form,
            address = address,
            staffOptions = staffOptions,
            cropper = viewModel.cropState,
            onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
            modifier = modifier,
            scaffoldPadding = scaffoldPadding,
            offeredDraft = offeredDraft,
        )
    }
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun CreateNewClientContent(
    state: CreateNewClientState,
    form: ScreenState<CreateClientForm>,
    staffOptions: ScreenState<List<ClientOption>>,
    cropper: ImageCropper,
    onAction: (CreateNewClientAction) -> Unit,
    modifier: Modifier = Modifier,
    address: AddressFormOptions? = null,
    /**
     * Insets from the host [AppScaffold]'s top bar. Defaulted so this half renders alone in a test
     * or a preview; the production caller always passes the scaffold's own padding.
     */
    scaffoldPadding: PaddingValues = PaddingValues(),
    offeredDraft: SavedFormDraft? = null,
) {
    ScreenContent(
        state = form,
        onRetry = { onAction(CreateNewClientAction.OnRetry) },
        modifier = modifier
            .padding(scaffoldPadding)
            .fillMaxSize()
            .testTag(CreateNewClientTestTags.SCREEN),
    ) { options, _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = KptTheme.spacing.md)
                .testTag(CreateNewClientTestTags.FORM),
        ) {
            offeredDraft?.let {
                FormDraftBanner(
                    failureReason = it.errorMessage,
                    onResume = { onAction(CreateNewClientAction.ResumeDraft) },
                    onDiscard = { onAction(CreateNewClientAction.DiscardDraft) },
                )
            }
            PhotoSection(state = state, onAction = onAction)

            IdentityFields(state = state, onAction = onAction)

            OptionFields(
                state = state,
                options = options,
                staffOptions = staffOptions,
                onAction = onAction,
            )

            ActivationFields(state = state, activationDate = state.activationDateOr(options), onAction = onAction)

            // Absent, not empty: a tenant with the address module off never sees the heading.
            if (address != null) {
                AddressFields(state = state, address = address, onAction = onAction)
            }

            // The tenant's client datatables — absent when it registers none.
            DataTableSections(tables = options.dataTables, state = state, onAction = onAction)

            Spacer(Modifier.height(KptTheme.spacing.md))

            if (state.isUploadingPhoto) {
                // The original put this literal in the ViewModel
                // (`ShowProgress("Uploading Client's Picture...")`). User-facing copy belongs in
                // resources and is rendered here.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppProgressIndicatorMini()
                    Spacer(Modifier.size(KptTheme.spacing.sm))
                    Text(
                        text = stringResource(Res.string.feature_client_createnew_uploading_photo),
                        style = AppTypography.bodySmall,
                        modifier = Modifier.testTag(CreateNewClientTestTags.UPLOADING_PHOTO),
                    )
                }
                Spacer(Modifier.height(KptTheme.spacing.sm))
            }

            AppButton(
                onClick = { onAction(CreateNewClientAction.OnSubmit) },
                text = { Text(text = stringResource(Res.string.feature_client_createnew_submit)) },
                // Disabled only while a write is in flight. Missing fields do NOT disable it:
                // tapping Submit is how the officer learns which ones, and the original's own
                // button was always enabled for the same reason.
                enabled = state.canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(DesignToken.spacing.dp46)
                    .testTag(CreateNewClientTestTags.SUBMIT),
            )

            Spacer(Modifier.height(KptTheme.spacing.md))
        }
    }

    state.openDatePicker?.let { field ->
        ClientDatePicker(
            field = field,
            state = state,
            activationDate = state.activationDateOr(form.dataOrNull),
            onAction = onAction,
        )
    }

    Dialogs(state = state, cropper = cropper, onAction = onAction)
}

/**
 * The avatar, tappable.
 *
 * [AppUserImage] renders the cropped bytes or its own placeholder, so the original's
 * `painterResource(feature_client_ic_dp_placeholder)` branch is the component's job rather than this
 * screen's — and the placeholder is then the same one the profile and photo screens draw.
 */
@Composable
private fun PhotoSection(
    state: CreateNewClientState,
    onAction: (CreateNewClientAction) -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = KptTheme.spacing.md)) {
        AppUserImage(
            bitmap = state.photo?.bytes,
            hasBorder = true,
            modifier = Modifier
                .align(Alignment.Center)
                .size(DesignToken.sizes.dp80)
                .clickable { onAction(CreateNewClientAction.ShowPhotoOptions) }
                .testTag(CreateNewClientTestTags.PHOTO),
        )
    }
}

@Composable
private fun IdentityFields(
    state: CreateNewClientState,
    onAction: (CreateNewClientAction) -> Unit,
) {
    Column {
        ClientTextField(
            field = CreateClientField.FIRST_NAME,
            label = Res.string.feature_client_createnew_first_name,
            state = state,
            onAction = onAction,
        )
        ClientTextField(
            field = CreateClientField.MIDDLE_NAME,
            label = Res.string.feature_client_createnew_middle_name,
            state = state,
            onAction = onAction,
        )
        ClientTextField(
            field = CreateClientField.LAST_NAME,
            label = Res.string.feature_client_createnew_last_name,
            state = state,
            onAction = onAction,
        )
        ClientTextField(
            field = CreateClientField.MOBILE_NO,
            label = Res.string.feature_client_createnew_phone_no,
            state = state,
            onAction = onAction,
            // `Phone`, not the original's `Number`: the validation rule accepts `+`, brackets, dots
            // and spaces, none of which a numeric keypad offers — so the formats the app considered
            // valid were the ones an officer could not type, and a number typed without them was then
            // dropped from the payload in silence.
            keyboardType = KeyboardType.Phone,
        )
        ClientTextField(
            field = CreateClientField.EXTERNAL_ID,
            label = Res.string.feature_client_createnew_external_id,
            state = state,
            onAction = onAction,
        )
    }
}

@Composable
private fun OptionFields(
    state: CreateNewClientState,
    options: CreateClientForm,
    staffOptions: ScreenState<List<ClientOption>>,
    onAction: (CreateNewClientAction) -> Unit,
) {
    Column {
        ClientDropdown(
            field = CreateClientField.GENDER,
            label = Res.string.feature_client_createnew_gender,
            options = options.genders,
            state = state,
            onAction = onAction,
        )

        FieldGap()
        AppDatePickerTextField(
            value = state.dateOfBirth?.let { DateHelper.getDateAsStringFromLong(it) }.orEmpty(),
            label = stringResource(Res.string.feature_client_createnew_dob),
            openDatePicker = {
                onAction(CreateNewClientAction.OpenDatePicker(ClientDateField.DATE_OF_BIRTH))
            },
            modifier = Modifier.fillMaxWidth().testTag(CreateNewClientTestTags.DATE_OF_BIRTH),
        )

        ClientDropdown(
            field = CreateClientField.CLIENT_TYPE,
            label = Res.string.feature_client_createnew_client_type,
            options = options.clientTypes,
            state = state,
            onAction = onAction,
        )
        ClientDropdown(
            field = CreateClientField.CLASSIFICATION,
            label = Res.string.feature_client_createnew_client_classification,
            options = options.classifications,
            state = state,
            onAction = onAction,
        )
        ClientDropdown(
            field = CreateClientField.OFFICE,
            label = Res.string.feature_client_createnew_office,
            options = options.offices,
            state = state,
            onAction = onAction,
        )

        val hasOffice = state.choice(CreateClientField.OFFICE) != null
        ClientDropdown(
            field = CreateClientField.STAFF,
            label = Res.string.feature_client_createnew_staff,
            options = staffOptions.dataOrNull.orEmpty(),
            state = state,
            onAction = onAction,
            // Disabled until an office is chosen, and while that office's staff are still arriving.
            // The original enabled it on `staffInOffices.isNotEmpty()` alone, which was true for
            // office zero's staff before any office had been chosen at all.
            enabled = hasOffice && staffOptions is ScreenState.Content,
            // Only ever drawn when a chosen office genuinely came back with nobody. The original's
            // equivalent message fired on first composition, before any office was picked.
            alwaysShow = hasOffice,
        )
        if (hasOffice && staffOptions is ScreenState.Empty) {
            Text(
                text = stringResource(Res.string.feature_client_createnew_no_staff),
                style = AppTypography.bodySmall,
                color = KptTheme.colorScheme.error,
                modifier = Modifier.testTag(CreateNewClientTestTags.NO_STAFF),
            )
        }
    }
}

@Composable
private fun ActivationFields(
    state: CreateNewClientState,
    activationDate: Long,
    onAction: (CreateNewClientAction) -> Unit,
) {
    FieldGap()
    AppCheckBox(
        text = stringResource(Res.string.feature_client_createnew_client_active),
        checked = state.isActive,
        onCheckChanged = { onAction(CreateNewClientAction.OnActiveToggled(it)) },
        modifier = Modifier.fillMaxWidth().testTag(CreateNewClientTestTags.CLIENT_ACTIVE),
    )

    // The original's reveal animation, minus its hand-built slide: the three-part
    // `slideInVertically { -40.dp.roundToPx() } + expandVertically + fadeIn` needed
    // `LocalDensity.current` threaded in for one offset. The default transition reads the same and
    // leaves no density to carry.
    AnimatedVisibility(visible = state.isActive) {
        Column {
            FieldGap()
            AppDatePickerTextField(
                value = DateHelper.getDateAsStringFromLong(activationDate),
                // The original labelled this `feature_client_center_submission_date` — "Center
                // Submission Date" — on a CLIENT activation field. Wrong copy, not a wrong field:
                // the value goes to `ClientPayload.activationDate`.
                label = stringResource(Res.string.feature_client_createnew_activation_date),
                openDatePicker = {
                    onAction(CreateNewClientAction.OpenDatePicker(ClientDateField.ACTIVATION))
                },
                modifier = Modifier.fillMaxWidth().testTag(CreateNewClientTestTags.ACTIVATION_DATE),
            )
        }
    }
}

@Composable
private fun AddressFields(
    state: CreateNewClientState,
    address: AddressFormOptions,
    onAction: (CreateNewClientAction) -> Unit,
) {
    Column {
        HorizontalDivider(modifier = Modifier.padding(vertical = KptTheme.spacing.md))

        Text(
            text = stringResource(Res.string.feature_client_address),
            style = AppTypography.titleMedium,
        )

        ClientDropdown(
            field = CreateClientField.ADDRESS_TYPE,
            label = Res.string.feature_client_createnew_address_type,
            options = address.types,
            state = state,
            onAction = onAction,
        )
        ClientTextField(
            field = CreateClientField.ADDRESS_LINE_1,
            label = Res.string.feature_client_address_line_1,
            state = state,
            onAction = onAction,
        )
        ClientTextField(
            field = CreateClientField.ADDRESS_LINE_2,
            label = Res.string.feature_client_address_line_2,
            state = state,
            onAction = onAction,
        )
        ClientTextField(
            field = CreateClientField.ADDRESS_LINE_3,
            label = Res.string.feature_client_address_line_3,
            state = state,
            onAction = onAction,
        )
        ClientTextField(
            field = CreateClientField.CITY,
            label = Res.string.feature_client_city,
            state = state,
            onAction = onAction,
        )
        ClientTextField(
            field = CreateClientField.POSTAL_CODE,
            label = Res.string.feature_client_postal_code,
            state = state,
            onAction = onAction,
            keyboardType = KeyboardType.Number,
        )
        ClientDropdown(
            field = CreateClientField.STATE_PROVINCE,
            label = Res.string.feature_client_createnew_state_province,
            options = address.states,
            state = state,
            onAction = onAction,
        )
        ClientDropdown(
            field = CreateClientField.COUNTRY,
            label = Res.string.feature_client_country,
            options = address.countries,
            state = state,
            onAction = onAction,
        )

        FieldGap()
        AppCheckBox(
            text = stringResource(Res.string.feature_client_createnew_address_active),
            checked = state.isAddressActive,
            onCheckChanged = { onAction(CreateNewClientAction.OnAddressActiveToggled(it)) },
            modifier = Modifier.fillMaxWidth().testTag(CreateNewClientTestTags.ADDRESS_ACTIVE),
        )
    }
}

@Composable
private fun ClientTextField(
    field: CreateClientField,
    label: StringResource,
    state: CreateNewClientState,
    onAction: (CreateNewClientAction) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    val error = state.errorFor(field)

    FieldGap()
    AppOutlinedTextField(
        value = state.text(field),
        onValueChange = { onAction(CreateNewClientAction.OnTextChanged(field, it)) },
        label = stringResource(label),
        keyboardType = keyboardType,
        isError = error != null,
        errorText = error,
        modifier = Modifier.fillMaxWidth().testTag(CreateNewClientTestTags.field(field)),
    )
}

/**
 * One dropdown, absent when the tenant offers no options for it.
 *
 * [alwaysShow] is the one exception: the staff dropdown stays on screen with an empty option list,
 * because "this office has no loan officers" is information the officer needs and a field that
 * disappears does not carry it.
 *
 * `onOptionSelected` reads the chosen [ClientOption] out of the SAME list that was displayed, which
 * is the dropdown fix — see [CreateNewClientViewModel]. `onValueChanged` is empty because every one
 * of these is `readOnly`; the original passed a setter that wrote the typed text into the label
 * field it could never receive.
 */
@Composable
private fun ClientDropdown(
    field: CreateClientField,
    label: StringResource,
    options: List<ClientOption>,
    state: CreateNewClientState,
    onAction: (CreateNewClientAction) -> Unit,
    enabled: Boolean = true,
    alwaysShow: Boolean = false,
) {
    if (options.isEmpty() && !alwaysShow) return

    val error = state.errorFor(field)

    FieldGap()
    AppTextFieldDropdown(
        value = state.choice(field)?.label.orEmpty(),
        onOptionSelected = { index, _ ->
            options.getOrNull(index)
                ?.let { onAction(CreateNewClientAction.OnOptionSelected(field, it)) }
        },
        options = options.map { it.label },
        label = stringResource(label),
        readOnly = true,
        enabled = enabled,
        errorMessage = error,
        modifier = Modifier.fillMaxWidth().testTag(CreateNewClientTestTags.field(field)),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClientDatePicker(
    field: ClientDateField,
    state: CreateNewClientState,
    activationDate: Long,
    onAction: (CreateNewClientAction) -> Unit,
) {
    // Keyed on the field: `rememberDatePickerState` saves through `rememberSaveable`, which
    // restores by position in the composition, so without this the date-of-birth picker would
    // reopen holding the activation date the officer picked a moment earlier. The original dodged
    // this by holding two separate picker states and choosing between them inside one dialog.
    val pickerState = key(field) { rememberDatePickerStateFor(field, state, activationDate) }

    DatePickerDialog(
        onDismissRequest = { onAction(CreateNewClientAction.CloseDatePicker) },
        confirmButton = {
            TextButton(
                onClick = {
                    pickerState.selectedDateMillis
                        ?.let { onAction(CreateNewClientAction.OnDatePicked(field, it)) }
                        ?: onAction(CreateNewClientAction.CloseDatePicker)
                },
            ) { Text(stringResource(Res.string.feature_client_createnew_select_date)) }
        },
        dismissButton = {
            TextButton(onClick = { onAction(CreateNewClientAction.CloseDatePicker) }) {
                Text(stringResource(Res.string.feature_client_cancel))
            }
        },
    ) {
        DatePicker(state = pickerState)
    }
}

/**
 * The picker for whichever date is open.
 *
 * Both dates are bounded to today or earlier. The original bounded the ACTIVATION date to
 * `utcTimeMillis >= now` — future only — so today, the one date an officer activating a client now
 * wants, could not be chosen. Date of birth was already `<= now` and is unchanged.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun rememberDatePickerStateFor(
    field: ClientDateField,
    state: CreateNewClientState,
    activationDate: Long,
) = rememberDatePickerState(
    initialSelectedDateMillis = when (field) {
        ClientDateField.ACTIVATION -> activationDate
        ClientDateField.DATE_OF_BIRTH -> state.dateOfBirth
    },
    selectableDates = NotInTheFuture,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
private object NotInTheFuture : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long) =
        utcTimeMillis <= Clock.System.now().toEpochMilliseconds()
}

@Composable
private fun Dialogs(
    state: CreateNewClientState,
    cropper: ImageCropper,
    onAction: (CreateNewClientAction) -> Unit,
) {
    when (val dialog = state.dialog) {
        null -> Unit

        CreateClientDialog.PhotoOptions -> PhotoOptions(hasPhoto = state.photo != null, onAction)

        CreateClientDialog.ImageCrop ->
            // Square, because the avatar is a circle — the original cropped nothing at all and
            // uploaded whatever aspect the camera produced.
            cropper.cropState?.let {
                AppImageCropperDialog(state = it, aspectRatio = AspectRatio(1, 1))
            }

        CreateClientDialog.CropFailed -> Notice(
            message = stringResource(Res.string.feature_client_createnew_crop_failed),
            tag = CreateNewClientTestTags.NOTICE_DIALOG,
            onAction = onAction,
        )

        CreateClientDialog.CameraUnavailable -> Notice(
            message = stringResource(Res.string.feature_client_createnew_camera_unavailable),
            tag = CreateNewClientTestTags.NOTICE_DIALOG,
            onAction = onAction,
        )

        // Confirming leaves the screen, which is the original's `onConfirmation = navigateBack`.
        // There is no client id to open: Fineract queued the client for a checker and returned
        // none.
        CreateClientDialog.AwaitingChecker -> AppAlertDialog(
            dialogText = stringResource(Res.string.feature_client_createnew_awaiting_checker),
            dismissText = null,
            onConfirmation = { onAction(CreateNewClientAction.OnCreatedContinue) },
            onDismissRequest = { onAction(CreateNewClientAction.OnCreatedContinue) },
            modifier = Modifier.testTag(CreateNewClientTestTags.AWAITING_CHECKER_DIALOG),
        )

        is CreateClientDialog.Created -> CreatedDialog(dialog, onAction)

        is CreateClientDialog.Failed -> FailedDialog(dialog, onAction)
    }
}

@Composable
private fun CreatedDialog(dialog: CreateClientDialog.Created, onAction: (CreateNewClientAction) -> Unit) {
    AppStatusDialog(
        status = ResultStatus.SUCCESS,
        onConfirm = { onAction(CreateNewClientAction.OnCreatedContinue) },
        onDismissRequest = { onAction(CreateNewClientAction.OnCreatedContinue) },
        btnText = stringResource(Res.string.feature_client_dialog_continue),
        successTitle = stringResource(Res.string.feature_client_createnew_created_title),
        // The client exists either way; a photo that did not upload is a note on a success,
        // not a failure. The original surfaced it as an error screen OVER a created client,
        // which left that client unreachable.
        successMessage = if (dialog.photoFailed) {
            stringResource(Res.string.feature_client_createnew_created_photo_failed_message)
        } else {
            stringResource(Res.string.feature_client_createnew_created_message)
        },
        // Unreachable arm: this dialog is only ever built for a SUCCESS, and a failed create
        // reaches the branch below. Empty rather than invented copy nothing can show.
        failureTitle = "",
        failureMessage = "",
        showAsDialog = true,
        modifier = Modifier.testTag(CreateNewClientTestTags.CREATED_DIALOG),
    )
}

@Composable
private fun FailedDialog(dialog: CreateClientDialog.Failed, onAction: (CreateNewClientAction) -> Unit) {
    AppStatusDialog(
        status = ResultStatus.FAILURE,
        // Dismiss, keeping the whole filled-in form behind it.
        onConfirm = { onAction(CreateNewClientAction.OnDismissDialog) },
        onDismissRequest = { onAction(CreateNewClientAction.OnDismissDialog) },
        btnText = stringResource(Res.string.feature_client_dialog_continue),
        successTitle = "",
        successMessage = "",
        failureTitle = stringResource(Res.string.feature_client_createnew_failed_title),
        failureMessage = when {
            dialog.offline -> stringResource(Res.string.feature_client_createnew_offline)
            else ->
                dialog.message
                    ?: stringResource(Res.string.feature_client_createnew_failed)
        },
        showAsDialog = true,
        modifier = Modifier.testTag(CreateNewClientTestTags.FAILED_DIALOG),
        // Kept offline automatically; a refusal can be kept and resumed later.
        secondaryBtnText = stringResource(BaseRes.string.draft_save).takeIf { !dialog.draftSaved },
        onSecondary = { onAction(CreateNewClientAction.SaveDraft) },
        note = when {
            !dialog.draftSaved -> null
            dialog.offline -> stringResource(BaseRes.string.draft_saved_offline)
            else -> stringResource(BaseRes.string.draft_saved)
        },
    )
}

/**
 * Gallery, Camera, and Remove when there IS something to remove.
 *
 * The original offered "Remove existing photo" unconditionally, on a form that starts with no
 * photo — a visible control for a state that cannot exist yet. Per §6a it is absent until there is
 * a photo rather than present and inert.
 */
@Composable
private fun PhotoOptions(hasPhoto: Boolean, onAction: (CreateNewClientAction) -> Unit) {
    AppBottomSheet(onDismiss = { onAction(CreateNewClientAction.OnDismissDialog) }) {
        Row(
            modifier = Modifier.padding(
                start = KptTheme.spacing.md,
                end = KptTheme.spacing.md,
                bottom = KptTheme.spacing.md,
            ),
            horizontalArrangement = Arrangement.Start,
        ) {
            AppBottomSheetOptionItem(
                label = stringResource(Res.string.feature_client_createnew_gallery),
                icon = AppIcons.Gallery,
                onClick = { onAction(CreateNewClientAction.OpenImagePicker) },
            )
            AppBottomSheetOptionItem(
                label = stringResource(Res.string.feature_client_createnew_camera),
                icon = AppIcons.Camera,
                onClick = { onAction(CreateNewClientAction.OpenCamera) },
            )
            if (hasPhoto) {
                AppBottomSheetOptionItem(
                    label = stringResource(Res.string.feature_client_createnew_remove_photo),
                    icon = AppIcons.DeleteDocument,
                    onClick = { onAction(CreateNewClientAction.RemovePhoto) },
                )
            }
        }
    }
}

/** A one-button "this did not work" dialog, dismissed back onto the form. */
@Composable
private fun Notice(
    message: String,
    tag: String,
    onAction: (CreateNewClientAction) -> Unit,
) {
    AppAlertDialog(
        dialogText = message,
        dismissText = null,
        confirmationText = stringResource(Res.string.feature_client_dialog_continue),
        onConfirmation = { onAction(CreateNewClientAction.OnDismissDialog) },
        onDismissRequest = { onAction(CreateNewClientAction.OnDismissDialog) },
        modifier = Modifier.testTag(tag),
    )
}

/**
 * The copy for one field's error.
 *
 * The ViewModel names WHICH field failed and WHY as a [CreateClientFieldError]; the screen owns how
 * it reads — the same split as every other form in this port. The per-field wording is the
 * original's, verbatim.
 */
@Composable
private fun CreateNewClientState.errorFor(field: CreateClientField): String? {
    val error = errors[field] ?: return null
    return when (field to error) {
        CreateClientField.FIRST_NAME to CreateClientFieldError.Required ->
            stringResource(Res.string.feature_client_createnew_error_first_name_empty)

        CreateClientField.FIRST_NAME to CreateClientFieldError.AlphabetsOnly ->
            stringResource(Res.string.feature_client_createnew_error_first_name_alphabets)

        CreateClientField.MIDDLE_NAME to CreateClientFieldError.AlphabetsOnly ->
            stringResource(Res.string.feature_client_createnew_error_middle_name_alphabets)

        CreateClientField.LAST_NAME to CreateClientFieldError.Required ->
            stringResource(Res.string.feature_client_createnew_error_last_name_empty)

        CreateClientField.LAST_NAME to CreateClientFieldError.AlphabetsOnly ->
            stringResource(Res.string.feature_client_createnew_error_last_name_alphabets)

        CreateClientField.ADDRESS_TYPE to CreateClientFieldError.Required ->
            stringResource(Res.string.feature_client_createnew_error_address_type_required)

        CreateClientField.OFFICE to CreateClientFieldError.Required ->
            stringResource(Res.string.feature_client_createnew_error_office_required)

        CreateClientField.MOBILE_NO to CreateClientFieldError.InvalidPhoneNumber ->
            stringResource(Res.string.feature_client_createnew_error_invalid_phone)

        // No other pairing is produced by `validate`. Rendering nothing is correct rather than
        // guessing a message: an unlabelled field is visibly fine, a wrong label is not.
        else -> null
    }
}

/** Spacer the sections share, so every field sits on the same rhythm. */
@Composable
private fun FieldGap() = Spacer(Modifier.height(KptTheme.spacing.md))

/** Stable handles for UI tests and Maestro flows — see RULE-KMP-COMPOSE-UITEST-001. */
internal object CreateNewClientTestTags {
    fun dataTable(table: String) = "createNewClient:datatable:$table"
    fun dataTableField(table: String, column: String) = "createNewClient:datatable:$table:$column"

    const val SCREEN = "createNewClient:screen"
    const val FORM = "createNewClient:form"
    const val PHOTO = "createNewClient:photo"
    const val DATE_OF_BIRTH = "createNewClient:dateOfBirth"
    const val ACTIVATION_DATE = "createNewClient:activationDate"
    const val CLIENT_ACTIVE = "createNewClient:clientActive"
    const val ADDRESS_ACTIVE = "createNewClient:addressActive"
    const val NO_STAFF = "createNewClient:noStaff"
    const val UPLOADING_PHOTO = "createNewClient:uploadingPhoto"
    const val SUBMIT = "createNewClient:submit"
    const val CREATED_DIALOG = "createNewClient:created"
    const val FAILED_DIALOG = "createNewClient:failed"
    const val AWAITING_CHECKER_DIALOG = "createNewClient:awaitingChecker"
    const val NOTICE_DIALOG = "createNewClient:notice"

    /** One tag per form field, so a flow can address any of the eighteen by name. */
    fun field(field: CreateClientField): String = "createNewClient:field:${field.name}"

    // The bottom-sheet options carry no tags: `AppBottomSheetOptionItem` takes no `modifier`, so
    // tagging them means widening a core/designsystem component, which would flow upstream per
    // RULE-TEMPLATE-MODULE-FIX-UPSTREAM-001. Same call the photo and signature siblings made.
}

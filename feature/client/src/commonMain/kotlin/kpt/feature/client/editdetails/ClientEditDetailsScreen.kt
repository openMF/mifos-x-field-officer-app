/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, kotlin.time.ExperimentalTime::class)

package kpt.feature.client.editdetails

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.common.utils.DateHelper
import kpt.core.designsystem.fieldofficer.component.AppDatePickerTextField
import kpt.core.designsystem.fieldofficer.component.AppOutlinedButton
import kpt.core.designsystem.fieldofficer.component.AppOutlinedTextField
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.fieldofficer.component.AppTextButton
import kpt.core.designsystem.fieldofficer.component.AppTextFieldDropdown
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.AppTypography
import kpt.core.designsystem.theme.DesignToken
import kpt.core.ui.components.AppStatusDialog
import kpt.core.ui.components.ResultStatus
import kpt.feature.client.generated.resources.Res
import kpt.feature.client.generated.resources.feature_client_btn_submit
import kpt.feature.client.generated.resources.feature_client_cancel
import kpt.feature.client.generated.resources.feature_client_details_account_no
import kpt.feature.client.generated.resources.feature_client_details_activation_date
import kpt.feature.client.generated.resources.feature_client_details_client_classification
import kpt.feature.client.generated.resources.feature_client_details_client_type
import kpt.feature.client.generated.resources.feature_client_details_date_of_birth
import kpt.feature.client.generated.resources.feature_client_details_external_id
import kpt.feature.client.generated.resources.feature_client_details_gender
import kpt.feature.client.generated.resources.feature_client_details_legal_form
import kpt.feature.client.generated.resources.feature_client_details_office
import kpt.feature.client.generated.resources.feature_client_details_staff
import kpt.feature.client.generated.resources.feature_client_details_submission_date
import kpt.feature.client.generated.resources.feature_client_dialog_continue
import kpt.feature.client.generated.resources.feature_client_editdetails_account_information
import kpt.feature.client.generated.resources.feature_client_editdetails_email_address
import kpt.feature.client.generated.resources.feature_client_editdetails_error_first_name_alphabets
import kpt.feature.client.generated.resources.feature_client_editdetails_error_first_name_empty
import kpt.feature.client.generated.resources.feature_client_editdetails_error_last_name_alphabets
import kpt.feature.client.generated.resources.feature_client_editdetails_error_last_name_empty
import kpt.feature.client.generated.resources.feature_client_editdetails_error_middle_name_alphabets
import kpt.feature.client.generated.resources.feature_client_editdetails_error_phone_no_invalid
import kpt.feature.client.generated.resources.feature_client_editdetails_failure_title
import kpt.feature.client.generated.resources.feature_client_editdetails_first_name
import kpt.feature.client.generated.resources.feature_client_editdetails_last_name
import kpt.feature.client.generated.resources.feature_client_editdetails_middle_name
import kpt.feature.client.generated.resources.feature_client_editdetails_no_options
import kpt.feature.client.generated.resources.feature_client_editdetails_office_use
import kpt.feature.client.generated.resources.feature_client_editdetails_offline
import kpt.feature.client.generated.resources.feature_client_editdetails_personal_details
import kpt.feature.client.generated.resources.feature_client_editdetails_phone_no
import kpt.feature.client.generated.resources.feature_client_editdetails_select_date
import kpt.feature.client.generated.resources.feature_client_editdetails_success_message
import kpt.feature.client.generated.resources.feature_client_editdetails_success_title
import kpt.feature.client.generated.resources.feature_client_not_available
import kpt.feature.client.generated.resources.feature_client_update_details
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

/**
 * The "Update Details" form — carried over from
 * `6b66e8a43:feature/client/clientEditDetails/ClientEditDetailsScreen.kt` (1,196 lines).
 *
 * Section order, field order, labels and the Cancel / Submit footer are the original's. See
 * [ClientEditDetailsViewModel] for the state move, the four payload defects and the two reads that
 * went away.
 *
 * ## §6a adaptations
 *
 * **`AppBreadcrumbNavBar` is dropped**, with the `NavController` the original threaded through two
 * composables purely to feed it — the same NavController-coupling every other ported screen drops.
 *
 * **The read's Loading and Error leave `DialogState`** and become [ScreenState] through
 * [ScreenContent], so a failed read is a retryable error screen rather than an `ElseBranch` that
 * rendered nothing. The original's `DialogState` had five members for one screen; two of them
 * (`Loading`, `Error`) were the read's states and one (`ShowUpdateDetailsContent`) existed only to
 * say "the read finished".
 *
 * **Three date pickers became one.** The original declared pickers for the submission and
 * activation dates and wired their affordances as `openDatePicker = { showDate...Datepicker = false }`
 * — assigning `false`, not toggling — so neither could ever open. Both fields were also
 * `enabled = false`. Per §6a an affordance that cannot work is ABSENT, not inert: the two fields
 * stay as the read-only displays they already were, and their picker machinery is gone. Only the
 * date of birth, the one the original could actually change, has a picker.
 *
 * **Four `trailingIcon = if (false) { ... } else { null }` blocks are gone** — error icons on the
 * account number, phone, email and external id fields, each guarded by a literal `false` beside a
 * literal `isError = false`. Dead in the original; absent here. Those four fields now show real
 * errors where the ViewModel produces them (the phone number does).
 *
 * **The footer is inside the scroll column, not `AppScaffold`'s `bottomBar`.** `AppScaffold`'s
 * title overload accepts a `bottomBar` parameter and then passes `bottomBar = {}` to the `Scaffold`
 * it wraps, so anything handed to it is silently discarded — see the report. `clientClosure` places
 * its button row the same way.
 */
@Composable
internal fun ClientEditDetailsScreen(
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ClientEditDetailsViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val template by viewModel.template.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            ClientEditDetailsEvent.NavigateBack -> onNavigateBack()
            ClientEditDetailsEvent.NavigateNext -> onNavigateNext()
        }
    }

    ClientEditDetailsContent(
        state = state,
        template = template,
        onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
        modifier = modifier,
    )
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun ClientEditDetailsContent(
    state: ClientEditDetailsState,
    template: ScreenState<ClientEditDetailsTemplate>,
    onAction: (ClientEditDetailsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppScaffold(
        title = stringResource(Res.string.feature_client_update_details),
        onBackPressed = { onAction(ClientEditDetailsAction.NavigateBack) },
        modifier = modifier.testTag(ClientEditDetailsTestTags.SCREEN),
    ) { padding ->
        ScreenContent(
            state = template,
            onRetry = { onAction(ClientEditDetailsAction.OnRetry) },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag(ClientEditDetailsTestTags.CONTENT),
        ) { content, _ ->
            // ONCE. Re-seeding on every emission is what let a background refresh wipe an
            // officer's half-typed edits in the original — see the ViewModel header.
            LaunchedEffect(state.prefilled) {
                if (!state.prefilled) {
                    onAction(ClientEditDetailsAction.Prefilled(content.prefill))
                }
            }

            // Until the prefill action has been handled the form holds its empty defaults, and
            // drawing those for a frame looks like a client whose every field is blank.
            if (!state.prefilled) return@ScreenContent

            EditDetailsForm(
                state = state,
                options = content.options,
                onAction = onAction,
            )
        }
    }

    if (state.showDatePicker) {
        DateOfBirthPicker(state = state, onAction = onAction)
    }

    when (val dialog = state.dialogState) {
        null -> Unit

        EditDialog.Updated -> AppStatusDialog(
            status = ResultStatus.SUCCESS,
            btnText = stringResource(Res.string.feature_client_dialog_continue),
            // Leaves, so the detail screen behind re-reads and shows the edit. The original
            // navigated FORWARD to a client detail, re-pushing detail on top of the detail the
            // officer came from — the same slip `clientStaff` and `clientClosure` corrected.
            onConfirm = { onAction(ClientEditDetailsAction.OnNext) },
            onDismissRequest = { onAction(ClientEditDetailsAction.OnNext) },
            successTitle = stringResource(Res.string.feature_client_editdetails_success_title),
            successMessage = stringResource(Res.string.feature_client_editdetails_success_message),
            failureTitle = stringResource(Res.string.feature_client_editdetails_failure_title),
            failureMessage = "",
            modifier = Modifier.fillMaxSize().testTag(ClientEditDetailsTestTags.STATUS_DIALOG),
        )

        is EditDialog.Failed -> AppStatusDialog(
            status = ResultStatus.FAILURE,
            btnText = stringResource(Res.string.feature_client_dialog_continue),
            // Dismiss, NOT leave: the form keeps fourteen filled fields and the officer may only
            // need to correct one of them. The original navigated on both outcomes, discarding
            // every edit on a failure.
            onConfirm = { onAction(ClientEditDetailsAction.CloseDialog) },
            onDismissRequest = { onAction(ClientEditDetailsAction.CloseDialog) },
            successTitle = stringResource(Res.string.feature_client_editdetails_success_title),
            successMessage = "",
            failureTitle = stringResource(Res.string.feature_client_editdetails_failure_title),
            failureMessage = when {
                dialog.offline -> stringResource(Res.string.feature_client_editdetails_offline)
                else ->
                    dialog.msg
                        ?: stringResource(Res.string.feature_client_editdetails_failure_title)
            },
            modifier = Modifier.fillMaxSize().testTag(ClientEditDetailsTestTags.STATUS_DIALOG),
        )
    }
}

/**
 * The three sections, in the original's order: Personal Details, Account Information, Office Use.
 *
 * One scrolling column, as the original had. The original additionally wrapped it in a
 * `pointerInput { detectTapGestures { keyboardController?.hide() } }`; that is dropped because it
 * swallows the tap, so tapping straight from a text field onto a dropdown dismissed the keyboard
 * and did NOT open the dropdown. Compose's own IME handling plus `AppScaffold`'s `imePadding`
 * cover the case it was reaching for.
 */
@Composable
private fun EditDetailsForm(
    state: ClientEditDetailsState,
    options: ClientEditDetailsOptions,
    onAction: (ClientEditDetailsAction) -> Unit,
) {
    val form = state.form
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = DesignToken.padding.largeIncreased)
            .verticalScroll(scrollState),
    ) {
        SectionLabel(Res.string.feature_client_editdetails_personal_details)

        EditTextField(
            field = EditField.FIRST_NAME,
            value = form.firstName,
            label = Res.string.feature_client_editdetails_first_name,
            state = state,
            onAction = onAction,
        )
        EditTextField(
            field = EditField.MIDDLE_NAME,
            value = form.middleName,
            label = Res.string.feature_client_editdetails_middle_name,
            state = state,
            onAction = onAction,
        )
        EditTextField(
            field = EditField.LAST_NAME,
            value = form.lastName,
            label = Res.string.feature_client_editdetails_last_name,
            state = state,
            onAction = onAction,
        )
        EditTextField(
            field = EditField.MOBILE,
            value = form.mobileNumber,
            label = Res.string.feature_client_editdetails_phone_no,
            state = state,
            onAction = onAction,
            keyboardType = KeyboardType.Phone,
        )
        EditTextField(
            field = EditField.EMAIL,
            value = form.emailAddress,
            label = Res.string.feature_client_editdetails_email_address,
            state = state,
            onAction = onAction,
            keyboardType = KeyboardType.Email,
        )

        Spacer(Modifier.height(KptTheme.spacing.sm))

        OptionDropdown(
            tag = ClientEditDetailsTestTags.GENDER,
            label = Res.string.feature_client_details_gender,
            selected = form.gender,
            options = options.genders,
            onSelected = { onAction(ClientEditDetailsAction.SelectGender(it)) },
        )

        AppDatePickerTextField(
            // Displayed with the original's `DateHelper` formatting. Only the date on the WIRE
            // changed, and that is in the ViewModel.
            value = form.dateOfBirth
                ?.let { DateHelper.getDateAsStringFromLong(it) }
                ?: stringResource(Res.string.feature_client_not_available),
            label = stringResource(Res.string.feature_client_details_date_of_birth),
            openDatePicker = { onAction(ClientEditDetailsAction.UpdateDatePicker(true)) },
            modifier = Modifier.fillMaxWidth().testTag(ClientEditDetailsTestTags.DATE_OF_BIRTH),
        )

        Spacer(Modifier.height(KptTheme.spacing.sm))
        SectionLabel(Res.string.feature_client_editdetails_account_information)

        // Server-assigned, and the original's field was disabled. Shown, not editable.
        ReadOnlyField(
            tag = ClientEditDetailsTestTags.ACCOUNT_NO,
            label = Res.string.feature_client_details_account_no,
            value = form.accountNo,
        )

        // Read-only, where the original drew a DISABLED dropdown over a fetched office list that
        // could never be opened. The original's label was "Office*" — a mandatory marker on a
        // field nobody can fill in. Transferring a client is `clientTransfer`.
        ReadOnlyField(
            tag = ClientEditDetailsTestTags.OFFICE,
            label = Res.string.feature_client_details_office,
            value = form.officeName,
        )

        EditTextField(
            field = EditField.EXTERNAL_ID,
            value = form.externalId,
            label = Res.string.feature_client_details_external_id,
            state = state,
            onAction = onAction,
        )

        Spacer(Modifier.height(KptTheme.spacing.sm))
        SectionLabel(Res.string.feature_client_editdetails_office_use)

        OptionDropdown(
            tag = ClientEditDetailsTestTags.LEGAL_FORM,
            label = Res.string.feature_client_details_legal_form,
            selected = form.legalForm,
            options = options.legalForms,
            onSelected = { onAction(ClientEditDetailsAction.SelectLegalForm(it)) },
        )
        OptionDropdown(
            tag = ClientEditDetailsTestTags.CLIENT_TYPE,
            // The original labelled this dropdown "Client", which does not say that it selects a
            // client TYPE. The detail screen's own label for the same code value is reused.
            label = Res.string.feature_client_details_client_type,
            selected = form.clientType,
            options = options.clientTypes,
            onSelected = { onAction(ClientEditDetailsAction.SelectClientType(it)) },
        )
        OptionDropdown(
            tag = ClientEditDetailsTestTags.CLASSIFICATION,
            label = Res.string.feature_client_details_client_classification,
            selected = form.classification,
            options = options.classifications,
            onSelected = { onAction(ClientEditDetailsAction.SelectClassification(it)) },
        )

        // Both read-only in the original too, and neither is written back — see
        // `ClientEditDetailsForm.toPayload`.
        ReadOnlyField(
            tag = ClientEditDetailsTestTags.SUBMISSION_DATE,
            label = Res.string.feature_client_details_submission_date,
            value = form.submissionDate?.let { DateHelper.getDateAsStringFromLong(it) },
        )
        ReadOnlyField(
            tag = ClientEditDetailsTestTags.ACTIVATION_DATE,
            label = Res.string.feature_client_details_activation_date,
            value = form.activationDate?.let { DateHelper.getDateAsStringFromLong(it) },
        )
        ReadOnlyField(
            tag = ClientEditDetailsTestTags.STAFF,
            label = Res.string.feature_client_details_staff,
            value = form.staffName,
        )

        Spacer(Modifier.height(DesignToken.padding.largeIncreased))

        EditDetailsFooter(state = state, onAction = onAction)

        Spacer(Modifier.height(DesignToken.padding.largeIncreased))
    }
}

/** Cancel / Submit, the original's footer. */
@Composable
private fun EditDetailsFooter(
    state: ClientEditDetailsState,
    onAction: (ClientEditDetailsAction) -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        AppOutlinedButton(
            onClick = { onAction(ClientEditDetailsAction.NavigateBack) },
            leadingIcon = {
                Icon(
                    imageVector = AppIcons.Close,
                    contentDescription = null,
                    modifier = Modifier.size(DesignToken.sizes.iconAverage),
                    tint = KptTheme.colorScheme.primary,
                )
            },
            text = {
                Text(
                    text = stringResource(Res.string.feature_client_cancel),
                    color = KptTheme.colorScheme.primary,
                    style = AppTypography.labelLarge,
                )
            },
            modifier = Modifier.weight(1f).testTag(ClientEditDetailsTestTags.CANCEL),
        )
        Spacer(Modifier.padding(KptTheme.spacing.sm))
        AppTextButton(
            onClick = { onAction(ClientEditDetailsAction.OnSubmit) },
            // Disabled only while the write is in flight. A form with a validation error keeps
            // Submit enabled on purpose: tapping it is how the officer learns WHICH fields are
            // wrong, and the ViewModel reports all of them at once.
            enabled = !state.isSubmitting,
            leadingIcon = {
                Icon(
                    imageVector = AppIcons.Check,
                    contentDescription = null,
                    modifier = Modifier.size(DesignToken.sizes.iconAverage),
                )
            },
            text = {
                Text(
                    text = stringResource(Res.string.feature_client_btn_submit),
                    style = AppTypography.labelLarge,
                )
            },
            modifier = Modifier.weight(1f).testTag(ClientEditDetailsTestTags.SUBMIT),
        )
    }
}

@Composable
private fun SectionLabel(label: StringResource) {
    Column {
        Spacer(Modifier.height(DesignToken.padding.largeIncreased))
        Text(
            text = stringResource(label),
            style = AppTypography.labelLargeEmphasized,
            color = KptTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(KptTheme.spacing.sm))
    }
}

/** One editable text field, with whatever error the ViewModel has for it. */
@Composable
private fun EditTextField(
    field: EditField,
    value: String,
    label: StringResource,
    state: ClientEditDetailsState,
    onAction: (ClientEditDetailsAction) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    Column {
        AppOutlinedTextField(
            value = value,
            onValueChange = { onAction(ClientEditDetailsAction.UpdateField(field, it)) },
            label = stringResource(label),
            error = state.errors[field]?.let { stringResource(it.messageFor(field)) },
            keyboardType = keyboardType,
            modifier = Modifier.fillMaxWidth().testTag(ClientEditDetailsTestTags.field(field)),
        )
        Spacer(Modifier.height(KptTheme.spacing.sm))
    }
}

/**
 * A value the server owns, shown so the officer can see it and not offered for editing.
 *
 * `readOnly`, not `enabled = false`: a disabled field is dimmed to the point of being hard to read
 * and is skipped by accessibility focus, which for a value whose whole purpose is to be READ is
 * the wrong treatment. The original used `enabled = false` for all five of these.
 */
@Composable
private fun ReadOnlyField(
    tag: String,
    label: StringResource,
    value: String?,
) {
    Column {
        AppOutlinedTextField(
            value = value?.takeIf { it.isNotBlank() }
                ?: stringResource(Res.string.feature_client_not_available),
            onValueChange = {},
            label = stringResource(label),
            error = null,
            readOnly = true,
            modifier = Modifier.fillMaxWidth().testTag(tag),
        )
        Spacer(Modifier.height(KptTheme.spacing.sm))
    }
}

/**
 * One code-value dropdown.
 *
 * `enabled = options.isNotEmpty()`, which is the original's own guard for three of its four
 * dropdowns, and the label falls back to "Not Available" when the tenant configures none — the
 * original showed an empty, enabled-looking field that opened an empty menu.
 */
@Composable
private fun OptionDropdown(
    tag: String,
    label: StringResource,
    selected: EditOption?,
    options: List<EditOption>,
    onSelected: (EditOption) -> Unit,
) {
    Column {
        AppTextFieldDropdown(
            enabled = options.isNotEmpty(),
            value = selected?.label
                ?: stringResource(
                    if (options.isEmpty()) {
                        Res.string.feature_client_editdetails_no_options
                    } else {
                        Res.string.feature_client_not_available
                    },
                ),
            // By INDEX into the list this dropdown was given, then resolved to that list's own option
            // — never to a position held in state, which is how a product change left a stale index
            // selecting a different option on the loan screens.
            onOptionSelected = { index, _ -> options.getOrNull(index)?.let(onSelected) },
            options = options.map { it.label },
            label = stringResource(label),
            readOnly = true,
            modifier = Modifier.fillMaxWidth().testTag(tag),
        )
        Spacer(Modifier.height(KptTheme.spacing.sm))
    }
}

@Composable
private fun DateOfBirthPicker(
    state: ClientEditDetailsState,
    onAction: (ClientEditDetailsAction) -> Unit,
) {
    val pickerState = rememberDatePickerState(
        // Null when the client has no date of birth on file, which opens the picker on the current
        // month with nothing selected. The original seeded it with NOW and then SAVED that — see
        // the ViewModel header.
        initialSelectedDateMillis = state.form.dateOfBirth,
        // The original's rule, kept: a date of birth cannot be in the future.
        selectableDates = remember { NotInTheFuture },
    )

    DatePickerDialog(
        onDismissRequest = { onAction(ClientEditDetailsAction.UpdateDatePicker(false)) },
        confirmButton = {
            TextButton(
                onClick = {
                    pickerState.selectedDateMillis
                        ?.let { onAction(ClientEditDetailsAction.UpdateDateOfBirth(it)) }
                    onAction(ClientEditDetailsAction.UpdateDatePicker(false))
                },
            ) { Text(stringResource(Res.string.feature_client_editdetails_select_date)) }
        },
        dismissButton = {
            TextButton(onClick = { onAction(ClientEditDetailsAction.UpdateDatePicker(false)) }) {
                Text(stringResource(Res.string.feature_client_cancel))
            }
        },
    ) {
        DatePicker(state = pickerState)
    }
}

private object NotInTheFuture : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long) =
        utcTimeMillis <= Clock.System.now().toEpochMilliseconds()
}

/**
 * The message for a field's error.
 *
 * The ViewModel produces [FieldError], not text — resolving a Compose resource is the
 * composition's job, and the original did it from `scope.launch { getString(...) }` inside a
 * non-composable validator. The field has to be part of the lookup because the copy names it
 * ("First name can not be empty").
 */
private fun FieldError.messageFor(field: EditField): StringResource = when (this) {
    FieldError.EMPTY -> when (field) {
        EditField.LAST_NAME -> Res.string.feature_client_editdetails_error_last_name_empty
        else -> Res.string.feature_client_editdetails_error_first_name_empty
    }

    FieldError.NOT_ALPHABETIC -> when (field) {
        EditField.MIDDLE_NAME ->
            Res.string.feature_client_editdetails_error_middle_name_alphabets
        EditField.LAST_NAME -> Res.string.feature_client_editdetails_error_last_name_alphabets
        else -> Res.string.feature_client_editdetails_error_first_name_alphabets
    }

    FieldError.NOT_A_PHONE_NUMBER -> Res.string.feature_client_editdetails_error_phone_no_invalid
}

/** Stable handles for UI tests and Maestro flows — see RULE-KMP-COMPOSE-UITEST-001. */
internal object ClientEditDetailsTestTags {
    const val SCREEN = "clientEditDetails:screen"
    const val CONTENT = "clientEditDetails:content"
    const val STATUS_DIALOG = "clientEditDetails:statusDialog"
    const val CANCEL = "clientEditDetails:cancel"
    const val SUBMIT = "clientEditDetails:submit"
    const val DATE_OF_BIRTH = "clientEditDetails:dateOfBirth"
    const val GENDER = "clientEditDetails:gender"
    const val LEGAL_FORM = "clientEditDetails:legalForm"
    const val CLIENT_TYPE = "clientEditDetails:clientType"
    const val CLASSIFICATION = "clientEditDetails:classification"
    const val ACCOUNT_NO = "clientEditDetails:accountNo"
    const val OFFICE = "clientEditDetails:office"
    const val STAFF = "clientEditDetails:staff"
    const val SUBMISSION_DATE = "clientEditDetails:submissionDate"
    const val ACTIVATION_DATE = "clientEditDetails:activationDate"
    fun field(field: EditField): String = "clientEditDetails:field:${field.name}"
}

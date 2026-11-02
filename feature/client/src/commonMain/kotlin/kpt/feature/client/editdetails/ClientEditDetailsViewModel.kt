/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.editdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.combineScreenStates
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.common.utils.DateFormatPattern
import kpt.core.data.client.ClientCommandRepository
import kpt.core.data.client.ClientRepository
import kpt.core.model.client.Client
import kpt.core.model.client.ClientPayload
import kpt.core.model.client.ClientsTemplate
import kpt.core.model.objects.error.serverErrorMessage
import kpt.core.model.utils.ApiDateFormatter
import kotlin.time.ExperimentalTime

/**
 * The "Update Details" form — ported from
 * `6b66e8a43:feature/client/clientEditDetails/` (a 1,196-line screen and a 212-line ViewModel).
 *
 * Reached from the client DETAIL screen's `onUpdateDetails`, which was `null` until this slice
 * landed. It is the only screen in the app that can change a client's names, phone, email,
 * external id, date of birth, gender, legal form, client type and classification.
 *
 * ## Where the form state lives
 *
 * The original kept all fourteen fields in `rememberSaveable` inside the composable and prefilled
 * them from a `LaunchedEffect(state.client)`. That re-seeds on EVERY emission of the client read,
 * so a background refresh landing while an officer typed reset the whole form to server values —
 * mid-edit, with no warning. The fields are [ClientEditDetailsState]'s here and the prefill runs
 * ONCE through [ClientEditDetailsAction.Prefilled], the same convention `loanDisbursement` uses
 * for exactly this reason.
 *
 * ## Two reads, not four
 *
 * The original issued four: the client (through a use-case), the tenant client template, the
 * office list, and the staff-in-office list. The last two fed dropdowns declared `enabled = false`
 * — an `ExposedDropdownMenuBox` that cannot expand, so neither list was ever reachable, and
 * `officeId` was never put on the payload at all. Office and staff are read-only text drawn from
 * the client here, and the two fetches are gone. Changing either has its own ported screen
 * (`clientTransfer`, `clientStaff`), both already reachable from the same detail screen.
 *
 * ## FIXED: an update of an ACTIVE client posted `active: false`
 *
 * ```kotlin
 * var isActive by rememberSaveable { mutableStateOf(false) }   // never written to
 * // ...
 * active = isActive,
 * ```
 *
 * `isActive` is declared once and assigned nowhere in the original's 1,196 lines, so every update
 * declared the client inactive. Activation is a separate Fineract command
 * ([ClientCommandRepository.activateClient]) and this form has no control for it, so `active`,
 * `activationDate` and `submittedOnDate` are all OFF the payload now: the two dates were
 * `enabled = false` read-only displays, and a field the officer cannot change has no business
 * being written back. See [ClientEditDetailsForm.toPayload].
 *
 * ## FIXED: the date-of-birth of a client who has none was TODAY
 *
 * ```kotlin
 * fun dateListToTimestamp(dateList: List<Int?>): Long {
 *     if (dateList.size < 3 || dateList.any { it == null }) {
 *         return Clock.System.now().toEpochMilliseconds()
 *     }
 * ```
 *
 * `Client.dateOfBirth` is `List<Int?>` and empty for a client with no DOB on file — the
 * common case for a client created without one. The original turned that into NOW, rendered it in
 * the field as if the server had sent it, and posted it on the next save. A silent write of a
 * wrong date of birth, every time such a client's phone number was corrected.
 * [toEpochMillisOrNull] returns null instead; the field renders "Not Available" and the payload
 * omits `dateOfBirth` unless the officer picks one.
 *
 * ## The date/format pairing
 *
 * This original is one of the few that got it RIGHT: `formatDate(millis)` delegates to
 * `ApiDateFormatter.formatForApi(millis)`, which is `dd MMMM yyyy`, and it declared
 * `dateFormat = ApiDateFormatter.DATE_FORMAT`, also `dd MMMM yyyy`. They agreed.
 *
 * They agreed by coincidence of two independent constants, though, which is precisely how the
 * other eight occurrences in this migration came to disagree. [ClientEditDetailsForm.toPayload]
 * makes it structural: ONE required [DateFormatPattern] parameter both formats the date and
 * supplies the `dateFormat` string on the wire, so there is no second place to change.
 *
 * ## Codes are sent only when one is actually chosen
 *
 * ```kotlin
 * if (clientTemplate.genderOptions?.isNotEmpty() == true) {
 *     clientPayload = clientPayload.copy(genderId = genderId)   // genderId defaults to 0
 * }
 * ```
 *
 * The guard asks whether the TENANT configures genders, not whether this client has one. With
 * `genderId` prefilled as `client.gender?.id ?: 0`, a client with no gender on file posted
 * `genderId = 0` — not a Fineract code-value id. Same shape for `clientTypeId` and
 * `clientClassificationId`. Each is a nullable [EditOption] here and reaches the payload only when
 * set.
 *
 * ## `staffId` no longer depends on an unrelated fetch
 *
 * `if (staffInOffices.isNotEmpty()) { staffId = selectedStaffId }` — so whether the client KEPT
 * its loan officer depended on whether the staff-in-office request (for the disabled dropdown)
 * happened to succeed. The client's own `staffId` is sent when it is set.
 */
internal class ClientEditDetailsViewModel(
    repository: ClientRepository,
    private val commands: ClientCommandRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<ClientEditDetailsState, ClientEditDetailsEvent, ClientEditDetailsAction>(
    ClientEditDetailsState(clientId = savedStateHandle.get<Int>(CLIENT_ID_KEY) ?: -1),
) {

    private val clientStream = repository.clientStream(state.clientId, viewModelScope)
    private val templateStream = repository.clientsTemplateStream(viewModelScope)

    /**
     * The client to edit, together with the tenant's option sets.
     *
     * `combineScreenStates`, so the form is drawn only when BOTH have landed and a failure in
     * either is reported once rather than as a half-filled form. The original rendered the form as
     * soon as the client arrived and left the four dropdowns empty if the template read failed,
     * which looks identical to a tenant that configures no genders.
     */
    val template: StateFlow<ScreenState<ClientEditDetailsTemplate>> =
        combineScreenStates(clientStream.state, templateStream.state) { client, options ->
            ClientEditDetailsTemplate(prefill = client.asPrefill(), options = options.asOptions())
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS),
            ScreenState.Loading,
        )

    override fun handleAction(action: ClientEditDetailsAction) {
        when (action) {
            ClientEditDetailsAction.NavigateBack -> sendEvent(ClientEditDetailsEvent.NavigateBack)
            ClientEditDetailsAction.OnNext -> sendEvent(ClientEditDetailsEvent.NavigateNext)

            ClientEditDetailsAction.OnRetry -> {
                // BOTH, because `combineScreenStates` does not say which one failed and the retry
                // affordance is one button.
                clientStream.retry()
                templateStream.retry()
            }

            is ClientEditDetailsAction.Prefilled ->
                mutableStateFlow.value = state.copy(prefilled = true, form = action.form)

            is ClientEditDetailsAction.UpdateField -> mutableStateFlow.value = state.copy(
                form = state.form.edit(action.field, action.value),
                // The error for THIS field clears as it is typed into; the others stand, because
                // nothing about them changed.
                errors = state.errors - action.field,
            )

            is ClientEditDetailsAction.SelectGender ->
                mutableStateFlow.value = state.copy(form = state.form.copy(gender = action.option))

            is ClientEditDetailsAction.SelectLegalForm ->
                mutableStateFlow.value =
                    state.copy(form = state.form.copy(legalForm = action.option))

            is ClientEditDetailsAction.SelectClientType ->
                mutableStateFlow.value =
                    state.copy(form = state.form.copy(clientType = action.option))

            is ClientEditDetailsAction.SelectClassification ->
                mutableStateFlow.value =
                    state.copy(form = state.form.copy(classification = action.option))

            is ClientEditDetailsAction.UpdateDateOfBirth ->
                mutableStateFlow.value =
                    state.copy(form = state.form.copy(dateOfBirth = action.millis))

            is ClientEditDetailsAction.UpdateDatePicker ->
                mutableStateFlow.value = state.copy(showDatePicker = action.status)

            ClientEditDetailsAction.CloseDialog ->
                mutableStateFlow.value = state.copy(dialogState = null)

            ClientEditDetailsAction.OnSubmit -> submit()
        }
    }

    private fun submit() {
        if (state.isSubmitting) return

        // EVERY field, every submit.
        //
        // The original's `isAllFieldsValid` was a short-circuiting `when`: an empty first name
        // meant the middle and last names were neither validated NOR cleared, so an officer who
        // had just fixed the last name still saw its stale error, and never saw more than one
        // problem at a time. It also set the messages from `scope.launch { getString(...) }` —
        // asynchronously, off a non-composable function — so the text arrived a frame after the
        // verdict. The errors are an enum here and the screen resolves them.
        val errors = state.form.validate()
        if (errors.isNotEmpty()) {
            mutableStateFlow.value = state.copy(errors = errors)
            return
        }

        mutableStateFlow.value = state.copy(isSubmitting = true, errors = emptyMap(), dialogState = null)

        viewModelScope.launch {
            val payload = state.form.toPayload(
                locale = ApiDateFormatter.LOCALE,
                dateFormat = DateFormatPattern.FULL_MONTH,
            )

            mutableStateFlow.value = when (
                val result = commands.updateClient(state.clientId, payload)
            ) {
                is MutationResult.Applied -> {
                    // The client row this screen just changed is now stale in the shared store, and
                    // the detail screen behind reads the same key. Forcing it fresh writes the new
                    // values through the source of truth, so popping back shows the edit rather
                    // than the pre-edit cache. This is what the original's
                    // `clientDetailsRepo.triggerClientUpdate()` was for.
                    clientStream.refresh(forceFresh = true)
                    state.copy(isSubmitting = false, dialogState = EditDialog.Updated)
                }

                is MutationResult.Blocked -> state.copy(
                    isSubmitting = false,
                    dialogState = EditDialog.Failed(
                        offline = result.reason == BlockReason.OFFLINE,
                        msg = null,
                    ),
                )

                is MutationResult.Failed -> {
                    Logger.e(tag = TAG, throwable = result.cause) { "update client failed" }
                    state.copy(
                        isSubmitting = false,
                        // Fineract's own reason is the useful half — "mobile number already in
                        // use" tells the officer what to change.
                        dialogState = EditDialog.Failed(offline = false, msg = result.cause.serverErrorMessage()),
                    )
                }

                is MutationResult.Conflicted -> state.copy(
                    isSubmitting = false,
                    dialogState = EditDialog.Failed(offline = false, msg = null),
                )
            }
        }
    }

    private companion object {
        const val TAG = "ClientEditDetails"
        const val CLIENT_ID_KEY = "clientId"
        const val STATE_TIMEOUT_MS = 5_000L
    }
}

/**
 * One dropdown choice — a Fineract code value.
 *
 * An id and a label travel together so the payload never has to resolve a selection back by
 * POSITION, which is how a nameless or idless option silently shifted every later selection on the
 * disbursement and new-loan screens.
 */
internal data class EditOption(val id: Int, val label: String)

/** The four option sets this form offers, as the tenant configured them. */
internal data class ClientEditDetailsOptions(
    val genders: List<EditOption> = emptyList(),
    val legalForms: List<EditOption> = emptyList(),
    val clientTypes: List<EditOption> = emptyList(),
    val classifications: List<EditOption> = emptyList(),
)

/** The client as the form's starting values plus the option sets to choose from. */
internal data class ClientEditDetailsTemplate(
    val prefill: ClientEditDetailsForm,
    val options: ClientEditDetailsOptions,
)

/** The fields that can carry a validation error. */
internal enum class EditField { FIRST_NAME, MIDDLE_NAME, LAST_NAME, MOBILE, EMAIL, EXTERNAL_ID }

/** Why a field is not acceptable. The screen turns each into its message. */
internal enum class FieldError { EMPTY, NOT_ALPHABETIC, NOT_A_PHONE_NUMBER }

/** The write's outcome, as something the screen can render over the still-filled form. */
internal sealed interface EditDialog {
    data object Updated : EditDialog
    data class Failed(val offline: Boolean, val msg: String?) : EditDialog
}

/**
 * Every value the form holds.
 *
 * Read-only members ([accountNo], [officeName], [staffName], [submissionDate], [activationDate])
 * are here because the original DREW them — as `enabled = false` fields — and §6a keeps the UI.
 * They are not on the payload; see [toPayload].
 */
internal data class ClientEditDetailsForm(
    val firstName: String = "",
    val middleName: String = "",
    val lastName: String = "",
    val mobileNumber: String = "",
    val emailAddress: String = "",
    val externalId: String = "",
    val gender: EditOption? = null,
    val legalForm: EditOption? = null,
    val clientType: EditOption? = null,
    val classification: EditOption? = null,
    val dateOfBirth: Long? = null,

    /** Server-assigned. Fineract will not accept a new one, and the original's field was disabled. */
    val accountNo: String = "",

    /** Changed by `clientTransfer`, not here. The original's dropdown was disabled. */
    val officeName: String = "",

    /** Changed by `clientStaff`, not here. The original's dropdown was disabled. */
    val staffName: String = "",

    /** The client's current loan officer, carried through the update unchanged. */
    val staffId: Int? = null,

    /** Read-only. The original's picker could not open — see [ClientEditDetailsScreen]. */
    val submissionDate: Long? = null,

    /** Read-only, and NOT written back — see the ViewModel header on `active: false`. */
    val activationDate: Long? = null,
)

/** Apply a text edit, keyed so the screen never has to know which `copy` member to call. */
internal fun ClientEditDetailsForm.edit(field: EditField, value: String): ClientEditDetailsForm =
    when (field) {
        EditField.FIRST_NAME -> copy(firstName = value)
        EditField.MIDDLE_NAME -> copy(middleName = value)
        EditField.LAST_NAME -> copy(lastName = value)
        EditField.MOBILE -> copy(mobileNumber = value)
        EditField.EMAIL -> copy(emailAddress = value)
        EditField.EXTERNAL_ID -> copy(externalId = value)
    }

/**
 * Only letters and spaces, which is the original's `"[^a-zA-Z ]".toRegex()` rule.
 *
 * Kept verbatim despite excluding hyphens, apostrophes and every non-Latin script, because
 * widening what counts as a name is a product decision for a microfinance tenant to make, not a
 * port's. Logged as a gap.
 */
private val NON_ALPHABETIC = Regex("[^a-zA-Z ]")

/**
 * Android's `PhoneNumberUtils.isGlobalPhoneNumber` rule, in commonMain.
 *
 * The original declared this as a six-file `expect object PhoneNumberUtil` whose Android actual
 * delegated to the platform and whose desktop and native actuals were `// TODO` approximations —
 * and whose **wasmJs actual was `TODO("Not yet implemented")`**, so submitting this form on the web
 * target threw `NotImplementedError`. One `commonMain` predicate matching the Android pattern
 * (`[\+]?[0-9.-]+`) removes the crash, the platform divergence and five files at once.
 */
private val GLOBAL_PHONE_NUMBER = Regex("""\+?[0-9.\-]+""")

internal fun isGlobalPhoneNumber(phoneNumber: String): Boolean =
    GLOBAL_PHONE_NUMBER.matches(phoneNumber)

/**
 * Everything wrong with the form, all at once.
 *
 * `isBlank`, not the original's `isEmpty`: a first name of `" "` passed BOTH of the original's
 * checks, because a space is one of the two characters its alphabetic rule allows. Fineract then
 * stored a client whose display name was whitespace.
 *
 * The phone number is validated rather than silently dropped. The original's builder read
 * `if (PhoneNumberUtil.isGlobalPhoneNumber(mobileNumber)) { ... }` with no `else` — so an officer
 * who mistyped a phone number was told the update succeeded and the number was never sent.
 */
internal fun ClientEditDetailsForm.validate(): Map<EditField, FieldError> = buildMap {
    nameError(firstName, required = true)?.let { put(EditField.FIRST_NAME, it) }
    nameError(middleName, required = false)?.let { put(EditField.MIDDLE_NAME, it) }
    nameError(lastName, required = true)?.let { put(EditField.LAST_NAME, it) }

    if (mobileNumber.isNotBlank() && !isGlobalPhoneNumber(mobileNumber)) {
        put(EditField.MOBILE, FieldError.NOT_A_PHONE_NUMBER)
    }
}

private fun nameError(name: String, required: Boolean): FieldError? = when {
    name.isBlank() -> FieldError.EMPTY.takeIf { required }
    NON_ALPHABETIC.containsMatchIn(name) -> FieldError.NOT_ALPHABETIC
    else -> null
}

/**
 * The form as a Fineract client update.
 *
 * ## Why [locale] and [dateFormat] are required and non-defaulted
 *
 * `ClientPayload` declares all twenty-odd members `= null`, so omitting the pair compiles
 * and produces a payload with a date and no instruction for parsing it. Eight screens in this
 * migration shipped a date that contradicted its own declared format, or raw epoch millis, for
 * that reason. Here they are parameters with no defaults — a caller cannot forget them — and
 * [dateFormat] is a single [DateFormatPattern] that BOTH formats the date and supplies the
 * `dateFormat` string. One value, so the two halves cannot drift apart.
 *
 * This original happened to pair them correctly (`formatDate(millis)` is
 * `ApiDateFormatter.formatForApi(millis)`, and it declared `ApiDateFormatter.DATE_FORMAT`) — but
 * by agreement between two independent constants, which is exactly the arrangement that failed
 * elsewhere.
 *
 * ## What is deliberately absent
 *
 * `active`, `activationDate` and `submittedOnDate` — the first was always `false` (see the
 * ViewModel header), and the other two were read-only displays. `officeId` is absent because the
 * original never set it either, and office changes belong to `clientTransfer`.
 *
 * ## What an empty field means
 *
 * A blank phone / email / external id is OMITTED rather than sent as `""`, which is the
 * original's behaviour and means those three cannot be CLEARED through this form. Sending `""` to
 * clear them is a product decision about data retention, so it is logged as a gap rather than
 * decided here.
 */
internal fun ClientEditDetailsForm.toPayload(
    locale: String,
    dateFormat: DateFormatPattern,
): ClientPayload = ClientPayload(
    // Mandatory, and validated above.
    firstname = firstName.trim(),
    lastname = lastName.trim(),

    middlename = middleName.trim().takeIf { it.isNotEmpty() },
    mobileNo = mobileNumber.trim().takeIf { it.isNotEmpty() },
    emailAddress = emailAddress.trim().takeIf { it.isNotEmpty() },
    externalId = externalId.trim().takeIf { it.isNotEmpty() },

    // Each sent only when a code was actually chosen — never as the `0` the original defaulted to.
    genderId = gender?.id,
    legalFormId = legalForm?.id,
    clientTypeId = clientType?.id,
    clientClassificationId = classification?.id,
    staffId = staffId,

    dateOfBirth = dateOfBirth?.let { ApiDateFormatter.formatForApi(it, dateFormat) },
    dateFormat = dateFormat.pattern,
    locale = locale,
)

/** The client's current values, as what the form opens with. */
internal fun Client.asPrefill(): ClientEditDetailsForm = ClientEditDetailsForm(
    firstName = firstname.orEmpty(),
    middleName = middlename.orEmpty(),
    lastName = lastname.orEmpty(),
    mobileNumber = mobileNo.orEmpty(),
    emailAddress = emailAddress.orEmpty(),
    externalId = externalId.orEmpty(),
    // `.takeIf { it > 0 }` on every id: these entities default their `id` to 0, and 0 is not a
    // Fineract code value. A client with no gender on file must open with the dropdown EMPTY, not
    // showing a blank label bound to id 0 that the save would then send.
    gender = gender?.id?.takeIf { it > 0 }?.let { EditOption(it, gender?.name.orEmpty()) },
    legalForm = legalForm?.id?.takeIf { it > 0 }?.let { EditOption(it, legalForm?.value.orEmpty()) },
    clientType = clientType?.id?.takeIf { it > 0 }?.let { EditOption(it, clientType?.name.orEmpty()) },
    classification = clientClassification?.id?.takeIf { it > 0 }
        ?.let { EditOption(it, clientClassification?.name.orEmpty()) },
    dateOfBirth = dateOfBirth.toEpochMillisOrNull(),
    accountNo = accountNo.orEmpty(),
    officeName = officeName.orEmpty(),
    staffName = staffName.orEmpty(),
    staffId = staffId.takeIf { it > 0 },
    submissionDate = timeline?.submittedOnDate.toEpochMillisOrNull(),
    activationDate = activationDate.toEpochMillisOrNull(),
)

/** The tenant's option sets, with unusable rows dropped rather than offered. */
internal fun ClientsTemplate.asOptions(): ClientEditDetailsOptions = ClientEditDetailsOptions(
    genders = genderOptions.orEmpty().mapNotNull { o ->
        o.id.takeIf { it > 0 }?.let { EditOption(it, o.name) }
    },
    // Sorted by id, which is the original's order for this one list and happens to be the
    // conventional Fineract ordering (1 Person, 2 Entity).
    legalForms = clientLegalFormOptions.orEmpty().sortedBy { it.id }.mapNotNull { o ->
        o.id.takeIf { it > 0 }?.let { EditOption(it, o.value) }
    },
    clientTypes = clientTypeOptions.orEmpty().mapNotNull { o ->
        o.id.takeIf { it > 0 }?.let { EditOption(it, o.name) }
    },
    classifications = clientClassificationOptions.orEmpty().mapNotNull { o ->
        o.id.takeIf { it > 0 }?.let { EditOption(it, o.name) }
    },
)

/**
 * Fineract's `[year, month, day]` date array as epoch milliseconds, or null when there isn't one.
 *
 * ## Why NOON local, and why not `DateHelper.getDateAsLongFromList`
 *
 * The substrate helper exists and anchors the date at `atStartOfDayIn(TimeZone.UTC)`. Every
 * consumer of the result — the field's `DateHelper.getDateAsStringFromLong` and the payload's
 * `ApiDateFormatter.formatForApi` — reads it back in `TimeZone.currentSystemDefault()`, so at any
 * negative UTC offset midnight UTC is the PREVIOUS day locally: a client born on the 15th would
 * display and re-save as the 14th. The original's noon-local anchor has twelve hours of slack in
 * both directions, which is why it is kept rather than replaced.
 *
 * ## Why nullable
 *
 * The original returned `Clock.System.now()` for a malformed or empty list — see the ViewModel
 * header for what that wrote to the server.
 */
@OptIn(ExperimentalTime::class)
internal fun List<Int?>?.toEpochMillisOrNull(): Long? {
    val parts = this?.take(3)?.filterNotNull()?.takeIf { it.size == 3 } ?: return null
    return runCatching {
        LocalDate(parts[0], parts[1], parts[2])
            .atTime(hour = 12, minute = 0)
            .toInstant(TimeZone.currentSystemDefault())
            .toEpochMilliseconds()
    }.getOrNull()
}

internal data class ClientEditDetailsState(
    val clientId: Int = -1,
    /** Guards the one-shot prefill — see the ViewModel header. */
    val prefilled: Boolean = false,
    val form: ClientEditDetailsForm = ClientEditDetailsForm(),
    val errors: Map<EditField, FieldError> = emptyMap(),
    val showDatePicker: Boolean = false,
    val isSubmitting: Boolean = false,
    val dialogState: EditDialog? = null,
)

internal sealed interface ClientEditDetailsEvent {
    data object NavigateBack : ClientEditDetailsEvent

    /**
     * The update landed — leave, so the detail screen behind re-reads.
     *
     * The original's third event, `OnUpdateSuccess`, was declared and never sent: `handleAction`
     * mapped only `NavigateBack` and `OnNext`. Dropped.
     */
    data object NavigateNext : ClientEditDetailsEvent
}

internal sealed interface ClientEditDetailsAction {
    data object NavigateBack : ClientEditDetailsAction
    data object OnNext : ClientEditDetailsAction
    data object OnRetry : ClientEditDetailsAction
    data object OnSubmit : ClientEditDetailsAction
    data object CloseDialog : ClientEditDetailsAction

    /** Dispatched once, from the screen, the first time the read lands. */
    data class Prefilled(val form: ClientEditDetailsForm) : ClientEditDetailsAction

    data class UpdateField(val field: EditField, val value: String) : ClientEditDetailsAction
    data class SelectGender(val option: EditOption) : ClientEditDetailsAction
    data class SelectLegalForm(val option: EditOption) : ClientEditDetailsAction
    data class SelectClientType(val option: EditOption) : ClientEditDetailsAction
    data class SelectClassification(val option: EditOption) : ClientEditDetailsAction
    data class UpdateDateOfBirth(val millis: Long) : ClientEditDetailsAction
    data class UpdateDatePicker(val status: Boolean) : ClientEditDetailsAction
}

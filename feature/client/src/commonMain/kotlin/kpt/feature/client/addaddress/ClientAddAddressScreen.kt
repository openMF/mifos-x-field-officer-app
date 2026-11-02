/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.addaddress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.submit.draft.SavedFormDraft
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.base.ui.submit.FormDraftBanner
import kpt.core.base.ui.submit.FormDraftFailure
import kpt.core.designsystem.fieldofficer.component.AppOutlinedButton
import kpt.core.designsystem.fieldofficer.component.AppOutlinedTextField
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.fieldofficer.component.AppTextFieldDropdown
import kpt.core.model.client.AddressTemplate
import kpt.core.model.client.Options
import kpt.feature.client.generated.resources.Res
import kpt.feature.client.generated.resources.feature_client_addaddress_city_error
import kpt.feature.client.generated.resources.feature_client_addaddress_country_error
import kpt.feature.client.generated.resources.feature_client_addaddress_line_error
import kpt.feature.client.generated.resources.feature_client_addaddress_postal_code_error
import kpt.feature.client.generated.resources.feature_client_addaddress_state_province_error
import kpt.feature.client.generated.resources.feature_client_addaddress_title
import kpt.feature.client.generated.resources.feature_client_addaddress_type_error
import kpt.feature.client.generated.resources.feature_client_addaddress_write_failed
import kpt.feature.client.generated.resources.feature_client_addaddress_write_offline
import kpt.feature.client.generated.resources.feature_client_address_line_1
import kpt.feature.client.generated.resources.feature_client_address_line_2
import kpt.feature.client.generated.resources.feature_client_address_line_3
import kpt.feature.client.generated.resources.feature_client_address_type
import kpt.feature.client.generated.resources.feature_client_btn_back
import kpt.feature.client.generated.resources.feature_client_btn_submit
import kpt.feature.client.generated.resources.feature_client_city
import kpt.feature.client.generated.resources.feature_client_country
import kpt.feature.client.generated.resources.feature_client_postal_code
import kpt.feature.client.generated.resources.feature_client_state_province
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Add a postal address to a client. See [ClientAddAddressViewModel] for the porting decisions,
 * including why the six field errors below can never render.
 *
 * Carried over from `6b66e8a43:feature/client/clientAddress/addAddress/AddAddressScreen.kt` — the
 * address-type dropdown, three address lines, city, state/province, country, postal code, and the
 * Cancel/Submit pair, in that order.
 *
 * ## §6a adaptations
 *
 * The template read's `MifosSweetError` / progress states become [ScreenState] through
 * [ScreenContent] — the original rendered an error that REPLACED the form, so a failed option-list
 * fetch discarded everything typed. The eleven `rememberSaveable` fields moved to the ViewModel
 * (adaptation 2), and the direct `viewModel.createClientAddress(...)` call became an action.
 *
 * The breadcrumb bar is not ported, per `loan-summary-breadcrumb-not-ported`.
 *
 * ## The buttons are in the scroll column, not a bottom bar
 *
 * The original put Cancel/Submit in `MifosScaffold(bottomBar = …)`. `AppScaffold` DECLARES a
 * `bottomBar` parameter and then passes `bottomBar = {}` to the `Scaffold` it wraps, so anything
 * handed to it is silently dropped — see `app-scaffold-silently-discards-its-bottom-bar`. The row
 * sits at the end of the form instead, which is what `clientClosure` already does.
 */
@Composable
internal fun ClientAddAddressScreen(
    onNavigateBack: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ClientAddAddressViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val offeredDraft by viewModel.offeredDraft.collectAsStateWithLifecycle()
    val template by viewModel.template.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            ClientAddAddressEvent.NavigateBack -> onNavigateBack()
            ClientAddAddressEvent.Saved -> onSaved()
        }
    }

    AppScaffold(
        title = stringResource(Res.string.feature_client_addaddress_title),
        onBackPressed = { viewModel.trySendAction(ClientAddAddressAction.NavigateBack) },
    ) { scaffoldPadding ->
        ClientAddAddressContent(
            state = state,
            template = template,
            onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
            modifier = modifier,
            scaffoldPadding = scaffoldPadding,
            offeredDraft = offeredDraft,
        )
    }
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun ClientAddAddressContent(
    state: ClientAddAddressState,
    template: ScreenState<AddressTemplate>,
    onAction: (ClientAddAddressAction) -> Unit,
    modifier: Modifier = Modifier,
    scaffoldPadding: PaddingValues = PaddingValues(),
    offeredDraft: SavedFormDraft? = null,
) {
    ScreenContent(
        state = template,
        onRetry = { },
        modifier = modifier
            .padding(scaffoldPadding)
            .fillMaxSize()
            .testTag(ClientAddAddressTestTags.SCREEN),
    ) { options, _ ->
        AddressForm(
            state = state,
            template = options,
            onAction = onAction,
            offeredDraft = offeredDraft,
        )
    }
}

@Composable
private fun AddressForm(
    state: ClientAddAddressState,
    template: AddressTemplate,
    onAction: (ClientAddAddressAction) -> Unit,
    modifier: Modifier = Modifier,
    offeredDraft: SavedFormDraft? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = KptTheme.spacing.md),
    ) {
        offeredDraft?.let {
            FormDraftBanner(
                failureReason = it.errorMessage,
                onResume = { onAction(ClientAddAddressAction.ResumeDraft) },
                onDiscard = { onAction(ClientAddAddressAction.DiscardDraft) },
            )
        }
        OptionDropdown(
            value = state.addressTypeLabel,
            label = stringResource(Res.string.feature_client_address_type),
            options = template.addressTypeIdOptions,
            error = state.errors.addressType.errorOr(Res.string.feature_client_addaddress_type_error),
            onSelected = { id, label ->
                onAction(ClientAddAddressAction.UpdateAddressType(id, label))
            },
            tag = ClientAddAddressTestTags.ADDRESS_TYPE,
        )

        AppOutlinedTextField(
            value = state.addressLine1,
            onValueChange = { onAction(ClientAddAddressAction.UpdateAddressLine1(it)) },
            label = stringResource(Res.string.feature_client_address_line_1),
            isError = state.errors.addressLine1,
            errorText = state.errors.addressLine1
                .errorOr(Res.string.feature_client_addaddress_line_error),
            modifier = Modifier.fillMaxWidth().testTag(ClientAddAddressTestTags.LINE_1),
        )

        AppOutlinedTextField(
            value = state.addressLine2,
            onValueChange = { onAction(ClientAddAddressAction.UpdateAddressLine2(it)) },
            label = stringResource(Res.string.feature_client_address_line_2),
            modifier = Modifier.fillMaxWidth(),
        )

        AppOutlinedTextField(
            value = state.addressLine3,
            onValueChange = { onAction(ClientAddAddressAction.UpdateAddressLine3(it)) },
            label = stringResource(Res.string.feature_client_address_line_3),
            modifier = Modifier.fillMaxWidth(),
        )

        AppOutlinedTextField(
            value = state.city,
            onValueChange = { onAction(ClientAddAddressAction.UpdateCity(it)) },
            label = stringResource(Res.string.feature_client_city),
            isError = state.errors.city,
            errorText = state.errors.city.errorOr(Res.string.feature_client_addaddress_city_error),
            modifier = Modifier.fillMaxWidth().testTag(ClientAddAddressTestTags.CITY),
        )

        OptionDropdown(
            value = state.stateProvinceLabel,
            label = stringResource(Res.string.feature_client_state_province),
            options = template.stateProvinceIdOptions,
            error = state.errors.stateProvince
                .errorOr(Res.string.feature_client_addaddress_state_province_error),
            onSelected = { id, label ->
                onAction(ClientAddAddressAction.UpdateStateProvince(id, label))
            },
            tag = ClientAddAddressTestTags.STATE_PROVINCE,
        )

        OptionDropdown(
            value = state.countryLabel,
            label = stringResource(Res.string.feature_client_country),
            options = template.countryIdOptions,
            error = state.errors.country
                .errorOr(Res.string.feature_client_addaddress_country_error),
            onSelected = { id, label ->
                onAction(ClientAddAddressAction.UpdateCountry(id, label))
            },
            tag = ClientAddAddressTestTags.COUNTRY,
        )

        AppOutlinedTextField(
            value = state.postalCode,
            onValueChange = { onAction(ClientAddAddressAction.UpdatePostalCode(it)) },
            label = stringResource(Res.string.feature_client_postal_code),
            isError = state.errors.postalCode,
            errorText = state.errors.postalCode
                .errorOr(Res.string.feature_client_addaddress_postal_code_error),
            keyboardType = KeyboardType.Number,
            modifier = Modifier.fillMaxWidth().testTag(ClientAddAddressTestTags.POSTAL_CODE),
        )

        state.writeError?.let { error ->
            Spacer(Modifier.height(KptTheme.spacing.sm))
            FormDraftFailure(
                message = stringResource(error.message),
                serverReason = state.failureReason,
                draftSaved = state.draftSaved,
                offline = error == AddAddressError.Offline,
                onSaveDraft = { onAction(ClientAddAddressAction.SaveDraft) },
                modifier = Modifier.testTag(ClientAddAddressTestTags.WRITE_ERROR),
            )
        }

        Spacer(Modifier.height(KptTheme.spacing.lg))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            AppOutlinedButton(
                onClick = { onAction(ClientAddAddressAction.NavigateBack) },
                modifier = Modifier.weight(1f).testTag(ClientAddAddressTestTags.BACK),
            ) {
                Text(stringResource(Res.string.feature_client_btn_back))
            }

            Spacer(Modifier.width(KptTheme.spacing.sm))

            AppOutlinedButton(
                onClick = { onAction(ClientAddAddressAction.Submit) },
                // The original's `enabled = isSubmitEnabled`. See the ViewModel's KDoc for why
                // this gate makes the six field errors above unreachable, and why it is kept.
                enabled = state.canSubmit,
                modifier = Modifier.weight(1f).testTag(ClientAddAddressTestTags.SUBMIT),
            ) {
                Text(stringResource(Res.string.feature_client_btn_submit))
            }
        }

        Spacer(Modifier.height(KptTheme.spacing.lg))
    }
}

/**
 * One option dropdown.
 *
 * The component reports `(index, label)`, so the id is resolved from the SAME list that supplied
 * the label — never from a second, differently-ordered one. That index-into-the-other-list mistake
 * is what made the create-client form store a different office than it displayed
 * (`create-client-dropdowns-stored-a-different-option-than-shown`).
 */
@Composable
private fun OptionDropdown(
    value: String,
    label: String,
    options: List<Options>,
    error: String?,
    onSelected: (id: Int, label: String) -> Unit,
    tag: String,
    modifier: Modifier = Modifier,
) {
    AppTextFieldDropdown(
        value = value,
        onOptionSelected = { index, selectedLabel ->
            options.getOrNull(index)?.let { onSelected(it.id, selectedLabel) }
        },
        options = options.map { it.name },
        label = label,
        errorMessage = error,
        modifier = modifier.fillMaxWidth().testTag(tag),
    )
}

/** The message for a failing field, or null when it passes. */
@Composable
private fun Boolean.errorOr(resource: StringResource): String? =
    if (this) stringResource(resource) else null

private val AddAddressError.message: StringResource
    get() = when (this) {
        AddAddressError.Offline -> Res.string.feature_client_addaddress_write_offline
        AddAddressError.Failed -> Res.string.feature_client_addaddress_write_failed
    }

internal object ClientAddAddressTestTags {
    const val SCREEN = "ClientAddAddressScreen"
    const val ADDRESS_TYPE = "ClientAddAddressType"
    const val LINE_1 = "ClientAddAddressLine1"
    const val CITY = "ClientAddAddressCity"
    const val STATE_PROVINCE = "ClientAddAddressStateProvince"
    const val COUNTRY = "ClientAddAddressCountry"
    const val POSTAL_CODE = "ClientAddAddressPostalCode"
    const val WRITE_ERROR = "ClientAddAddressWriteError"
    const val BACK = "ClientAddAddressBack"
    const val SUBMIT = "ClientAddAddressSubmit"
}

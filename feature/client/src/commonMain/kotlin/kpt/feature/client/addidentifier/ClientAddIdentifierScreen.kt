/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.addidentifier

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.designsystem.fieldofficer.component.AppOutlinedButton
import kpt.core.designsystem.fieldofficer.component.AppOutlinedTextField
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.fieldofficer.component.AppTextFieldDropdown
import kpt.core.designsystem.theme.AppTypography
import kpt.core.model.objects.noncoreobjects.IdentifierTemplate
import kpt.core.ui.components.AppFilePickerBottomSheet
import kpt.core.ui.components.AppRowWithTextAndButton
import kpt.feature.client.generated.resources.Res
import kpt.feature.client.generated.resources.feature_client_addidentifier_created_note
import kpt.feature.client.generated.resources.feature_client_addidentifier_description
import kpt.feature.client.generated.resources.feature_client_addidentifier_document_key
import kpt.feature.client.generated.resources.feature_client_addidentifier_document_type
import kpt.feature.client.generated.resources.feature_client_addidentifier_error_create_failed
import kpt.feature.client.generated.resources.feature_client_addidentifier_error_key_required
import kpt.feature.client.generated.resources.feature_client_addidentifier_error_offline
import kpt.feature.client.generated.resources.feature_client_addidentifier_error_pick_failed
import kpt.feature.client.generated.resources.feature_client_addidentifier_error_status_required
import kpt.feature.client.generated.resources.feature_client_addidentifier_error_type_required
import kpt.feature.client.generated.resources.feature_client_addidentifier_error_upload_failed
import kpt.feature.client.generated.resources.feature_client_addidentifier_no_file
import kpt.feature.client.generated.resources.feature_client_addidentifier_pick
import kpt.feature.client.generated.resources.feature_client_addidentifier_skip_document
import kpt.feature.client.generated.resources.feature_client_addidentifier_status
import kpt.feature.client.generated.resources.feature_client_addidentifier_status_active
import kpt.feature.client.generated.resources.feature_client_addidentifier_status_inactive
import kpt.feature.client.generated.resources.feature_client_addidentifier_title
import kpt.feature.client.generated.resources.feature_client_addidentifier_upload
import kpt.feature.client.generated.resources.feature_client_btn_back
import kpt.feature.client.generated.resources.feature_client_btn_submit
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Add an identity document to a client. See [ClientAddIdentifierViewModel] for the porting
 * decisions, including why only two of the original's three phases are here.
 *
 * Carried over from `6b66e8a43:feature/client/clientIdentifiersAddUpdate/
 * ClientIdentitiesAddUpdateScreen.kt` — the document-type dropdown, key, status and description
 * fields, then the file row and its upload action once the identifier exists.
 *
 * ## §6a adaptations
 *
 * The template read's Loading/Error dialog states become [ScreenState] through [ScreenContent].
 * The original's `isOverlayLoading` + `DialogState.Loading` pair — two independent flags for one
 * condition, set in different branches — becomes a single `isSubmitting` that disables the buttons.
 *
 * The breadcrumb bar is not ported, per `loan-summary-breadcrumb-not-ported`.
 *
 * ## The second phase announces that the first one is already done
 *
 * Once the identifier is created the form shows a note saying so, and the remaining step is
 * explicitly optional — "Skip" finishes the flow. The original moved to the same phase silently,
 * so a failed upload looked like a failed CREATE and invited the officer to make a second
 * identifier.
 */
@Composable
internal fun ClientAddIdentifierScreen(
    onNavigateBack: () -> Unit,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ClientAddIdentifierViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val template by viewModel.template.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            ClientAddIdentifierEvent.NavigateBack -> onNavigateBack()
            ClientAddIdentifierEvent.Finished -> onFinished()
        }
    }

    AppScaffold(
        title = stringResource(Res.string.feature_client_addidentifier_title),
        onBackPressed = { viewModel.trySendAction(ClientAddIdentifierAction.NavigateBack) },
    ) { scaffoldPadding ->
        ClientAddIdentifierContent(
            state = state,
            template = template,
            onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
            modifier = modifier,
            scaffoldPadding = scaffoldPadding,
        )
    }
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun ClientAddIdentifierContent(
    state: ClientAddIdentifierState,
    template: ScreenState<IdentifierTemplate>,
    onAction: (ClientAddIdentifierAction) -> Unit,
    modifier: Modifier = Modifier,
    scaffoldPadding: PaddingValues = PaddingValues(),
) {
    ScreenContent(
        state = template,
        onRetry = { },
        modifier = modifier
            .padding(scaffoldPadding)
            .fillMaxSize()
            .testTag(ClientAddIdentifierTestTags.SCREEN),
    ) { options, _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = KptTheme.spacing.md),
        ) {
            IdentifierFields(state = state, template = options, onAction = onAction)

            if (state.identifierCreated) {
                Spacer(Modifier.height(KptTheme.spacing.md))
                // The identifier EXISTS from here. Saying so is what stops a failed upload from
                // reading as a failed create.
                Text(
                    text = stringResource(Res.string.feature_client_addidentifier_created_note),
                    style = AppTypography.bodyMedium,
                    modifier = Modifier.testTag(ClientAddIdentifierTestTags.CREATED_NOTE),
                )
                Spacer(Modifier.height(KptTheme.spacing.sm))
                DocumentStep(state = state, onAction = onAction)
            }

            state.error?.let { error ->
                Spacer(Modifier.height(KptTheme.spacing.sm))
                Text(
                    text = stringResource(error.message),
                    style = AppTypography.bodySmall,
                    color = KptTheme.colorScheme.error,
                    modifier = Modifier.testTag(ClientAddIdentifierTestTags.ERROR),
                )
                // Fineract's own words, when it gave any — unedited. The original rewrote them
                // with a pair of string substitutions.
                state.serverMessage?.let { message ->
                    Text(
                        text = message,
                        style = AppTypography.bodySmall,
                        color = KptTheme.colorScheme.error,
                        modifier = Modifier.testTag(ClientAddIdentifierTestTags.SERVER_MESSAGE),
                    )
                }
            }

            Spacer(Modifier.height(KptTheme.spacing.lg))
            Actions(state = state, onAction = onAction)
            Spacer(Modifier.height(KptTheme.spacing.lg))
        }
    }
}

@Composable
private fun IdentifierFields(
    state: ClientAddIdentifierState,
    template: IdentifierTemplate,
    onAction: (ClientAddIdentifierAction) -> Unit,
) {
    Column {
        val types = template.allowedDocumentTypes.orEmpty()
        AppTextFieldDropdown(
            value = state.documentTypeLabel,
            // Resolved from the SAME list that supplied the label — never an index into another.
            onOptionSelected = { index, label ->
                types.getOrNull(index)?.id?.let { onAction(ClientAddIdentifierAction.UpdateDocumentType(it, label)) }
            },
            options = types.mapNotNull { it.name },
            label = stringResource(Res.string.feature_client_addidentifier_document_type),
            errorMessage = if (state.documentTypeError) {
                stringResource(Res.string.feature_client_addidentifier_error_type_required)
            } else {
                null
            },
            // Locked once the identifier exists — the create write has already used these values.
            enabled = !state.identifierCreated,
            modifier = Modifier.fillMaxWidth().testTag(ClientAddIdentifierTestTags.DOCUMENT_TYPE),
        )

        AppOutlinedTextField(
            value = state.documentKey,
            onValueChange = { onAction(ClientAddIdentifierAction.UpdateDocumentKey(it)) },
            label = stringResource(Res.string.feature_client_addidentifier_document_key),
            isError = state.documentKeyError,
            errorText = if (state.documentKeyError) {
                stringResource(Res.string.feature_client_addidentifier_error_key_required)
            } else {
                null
            },
            enabled = !state.identifierCreated,
            modifier = Modifier.fillMaxWidth().testTag(ClientAddIdentifierTestTags.DOCUMENT_KEY),
        )

        // A dropdown, as in the original: the server accepts exactly these values, so a free-text
        // field could only produce a rejected write.
        val statusLabels = IdentifierStatus.values.map { identifierStatusLabel(it) }
        AppTextFieldDropdown(
            value = IdentifierStatus.values.indexOf(state.status).let { statusLabels.getOrNull(it) }.orEmpty(),
            onOptionSelected = { index, _ ->
                IdentifierStatus.values.getOrNull(index)?.let { onAction(ClientAddIdentifierAction.UpdateStatus(it)) }
            },
            options = statusLabels,
            label = stringResource(Res.string.feature_client_addidentifier_status),
            errorMessage = if (state.statusError) {
                stringResource(Res.string.feature_client_addidentifier_error_status_required)
            } else {
                null
            },
            enabled = !state.identifierCreated,
            modifier = Modifier.fillMaxWidth().testTag(ClientAddIdentifierTestTags.STATUS),
        )

        AppOutlinedTextField(
            value = state.description,
            onValueChange = { onAction(ClientAddIdentifierAction.UpdateDescription(it)) },
            label = stringResource(Res.string.feature_client_addidentifier_description),
            enabled = !state.identifierCreated,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun DocumentStep(
    state: ClientAddIdentifierState,
    onAction: (ClientAddIdentifierAction) -> Unit,
) {
    AppRowWithTextAndButton(
        text = state.fileName ?: stringResource(Res.string.feature_client_addidentifier_no_file),
        btnText = stringResource(Res.string.feature_client_addidentifier_pick),
        onBtnClick = { onAction(ClientAddIdentifierAction.OpenFileSource) },
        modifier = Modifier.testTag(ClientAddIdentifierTestTags.FILE_ROW),
    )

    if (state.isSourceSheetOpen) {
        AppFilePickerBottomSheet(
            onDismiss = { onAction(ClientAddIdentifierAction.DismissFileSource) },
            onGalleryClick = { onAction(ClientAddIdentifierAction.PickFromGallery) },
            onFilesClick = { onAction(ClientAddIdentifierAction.PickFromFiles) },
            // Dead in the original, kept — see `client-signature-more-option-inert`.
            onMoreClick = {},
        )
    }
}

@Composable
private fun Actions(
    state: ClientAddIdentifierState,
    onAction: (ClientAddIdentifierAction) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        AppOutlinedButton(
            onClick = {
                // Before the create, Back leaves. After it, the identifier exists and leaving is
                // SKIPPING the optional document — a different act, with different copy.
                onAction(
                    if (state.identifierCreated) {
                        ClientAddIdentifierAction.Finish
                    } else {
                        ClientAddIdentifierAction.NavigateBack
                    },
                )
            },
            modifier = Modifier.weight(1f).testTag(ClientAddIdentifierTestTags.SECONDARY),
        ) {
            Text(
                stringResource(
                    if (state.identifierCreated) {
                        Res.string.feature_client_addidentifier_skip_document
                    } else {
                        Res.string.feature_client_btn_back
                    },
                ),
            )
        }

        Spacer(Modifier.width(KptTheme.spacing.sm))

        AppOutlinedButton(
            onClick = {
                onAction(
                    if (state.identifierCreated) {
                        ClientAddIdentifierAction.UploadDocument
                    } else {
                        ClientAddIdentifierAction.CreateIdentifier
                    },
                )
            },
            enabled = if (state.identifierCreated) state.canUpload else state.canCreate,
            modifier = Modifier.weight(1f).testTag(ClientAddIdentifierTestTags.PRIMARY),
        ) {
            Text(
                stringResource(
                    if (state.identifierCreated) {
                        Res.string.feature_client_addidentifier_upload
                    } else {
                        Res.string.feature_client_btn_submit
                    },
                ),
            )
        }
    }
}

private val IdentifierError.message: StringResource
    get() = when (this) {
        IdentifierError.CreateFailed -> Res.string.feature_client_addidentifier_error_create_failed
        IdentifierError.UploadFailed -> Res.string.feature_client_addidentifier_error_upload_failed
        IdentifierError.PickFailed -> Res.string.feature_client_addidentifier_error_pick_failed
        IdentifierError.Offline -> Res.string.feature_client_addidentifier_error_offline
    }

internal object ClientAddIdentifierTestTags {
    const val SCREEN = "ClientAddIdentifierScreen"
    const val DOCUMENT_TYPE = "ClientAddIdentifierDocumentType"
    const val DOCUMENT_KEY = "ClientAddIdentifierDocumentKey"
    const val STATUS = "ClientAddIdentifierStatus"
    const val CREATED_NOTE = "ClientAddIdentifierCreatedNote"
    const val FILE_ROW = "ClientAddIdentifierFileRow"
    const val ERROR = "ClientAddIdentifierError"
    const val SERVER_MESSAGE = "ClientAddIdentifierServerMessage"
    const val SECONDARY = "ClientAddIdentifierSecondary"
    const val PRIMARY = "ClientAddIdentifierPrimary"
}

@Composable
private fun identifierStatusLabel(status: String): String = stringResource(
    if (status == IdentifierStatus.ACTIVE) {
        Res.string.feature_client_addidentifier_status_active
    } else {
        Res.string.feature_client_addidentifier_status_inactive
    },
)

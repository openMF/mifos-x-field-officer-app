/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.groups.create

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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
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
import kpt.core.designsystem.theme.AppTypography
import kpt.core.model.office.Office
import kpt.feature.groups.generated.resources.Res
import kpt.feature.groups.generated.resources.feature_groups_create_active
import kpt.feature.groups.generated.resources.feature_groups_create_back
import kpt.feature.groups.generated.resources.feature_groups_create_error_failed
import kpt.feature.groups.generated.resources.feature_groups_create_error_name_required
import kpt.feature.groups.generated.resources.feature_groups_create_error_office_required
import kpt.feature.groups.generated.resources.feature_groups_create_error_offline
import kpt.feature.groups.generated.resources.feature_groups_create_external_id
import kpt.feature.groups.generated.resources.feature_groups_create_name
import kpt.feature.groups.generated.resources.feature_groups_create_office
import kpt.feature.groups.generated.resources.feature_groups_create_submit
import kpt.feature.groups.generated.resources.feature_groups_create_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Create a group. See [CreateGroupViewModel] for the porting decisions.
 *
 * Carried over from `6b66e8a43:feature/groups/createNewGroup/CreateNewGroupScreen.kt` — the name
 * field, the office dropdown, external id, the Active checkbox with its activation date, and the
 * Back/Submit pair.
 *
 * ## §6a adaptations
 *
 * The original's `CreateNewGroupUiState` sealed class
 * (`ShowProgressbar / ShowOffices / ShowFetchingError / ShowGroupCreatedSuccessfully`) becomes
 * [ScreenState] over the offices read through [ScreenContent], and the eleven `rememberSaveable`
 * fields moved into the ViewModel.
 *
 * The office dropdown reports the CHOSEN office rather than an index into a separately-held list —
 * the defect the client create form shipped
 * (`create-client-dropdowns-stored-a-different-option-than-shown`).
 *
 * ## The activation date is ABSENT unless the group is being activated
 *
 * The original drew the activation date picker unconditionally while the payload only carried it
 * when `active` was set, so an officer could pick a date the request would then discard. The field
 * appears with the checkbox.
 */
@Composable
internal fun CreateGroupScreen(
    onNavigateBack: () -> Unit,
    onCreated: (synced: Boolean, groupId: Int?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateGroupViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val offeredDraft by viewModel.offeredDraft.collectAsStateWithLifecycle()
    val offices by viewModel.offices.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            CreateGroupEvent.NavigateBack -> onNavigateBack()
            is CreateGroupEvent.Created -> onCreated(event.synced, event.groupId)
        }
    }

    AppScaffold(
        title = stringResource(Res.string.feature_groups_create_title),
        onBackPressed = { viewModel.trySendAction(CreateGroupAction.NavigateBack) },
    ) { scaffoldPadding ->
        CreateGroupContent(
            state = state,
            offices = offices,
            offeredDraft = offeredDraft,
            onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
            modifier = modifier,
            scaffoldPadding = scaffoldPadding,
        )
    }
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun CreateGroupContent(
    state: CreateGroupState,
    offices: ScreenState<List<Office>>,
    onAction: (CreateGroupAction) -> Unit,
    modifier: Modifier = Modifier,
    offeredDraft: SavedFormDraft? = null,
    scaffoldPadding: PaddingValues = PaddingValues(),
) {
    ScreenContent(
        state = offices,
        onRetry = { },
        modifier = modifier
            .padding(scaffoldPadding)
            .fillMaxSize()
            .testTag(CreateGroupTestTags.SCREEN),
    ) { officeList, _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = KptTheme.spacing.md),
        ) {
            offeredDraft?.let {
                FormDraftBanner(
                    failureReason = it.errorMessage,
                    onResume = { onAction(CreateGroupAction.ResumeDraft) },
                    onDiscard = { onAction(CreateGroupAction.DiscardDraft) },
                )
            }
            AppOutlinedTextField(
                value = state.name,
                onValueChange = { onAction(CreateGroupAction.UpdateName(it)) },
                label = stringResource(Res.string.feature_groups_create_name),
                isError = state.nameError,
                errorText = state.nameError
                    .errorOr(Res.string.feature_groups_create_error_name_required),
                modifier = Modifier.fillMaxWidth().testTag(CreateGroupTestTags.NAME),
            )

            AppTextFieldDropdown(
                value = state.office?.label.orEmpty(),
                // The id comes from the SAME list that supplied the label.
                onOptionSelected = { index, label ->
                    officeList.getOrNull(index)?.let {
                        onAction(CreateGroupAction.UpdateOffice(OfficeOption(it.id, label)))
                    }
                },
                options = officeList.mapNotNull { it.name },
                label = stringResource(Res.string.feature_groups_create_office),
                errorMessage = state.officeError
                    .errorOr(Res.string.feature_groups_create_error_office_required),
                modifier = Modifier.fillMaxWidth().testTag(CreateGroupTestTags.OFFICE),
            )

            AppOutlinedTextField(
                value = state.externalId,
                onValueChange = { onAction(CreateGroupAction.UpdateExternalId(it)) },
                label = stringResource(Res.string.feature_groups_create_external_id),
                modifier = Modifier.fillMaxWidth(),
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = state.isActive,
                    onCheckedChange = { onAction(CreateGroupAction.UpdateActive(it)) },
                    modifier = Modifier.testTag(CreateGroupTestTags.ACTIVE),
                )
                Text(
                    text = stringResource(Res.string.feature_groups_create_active),
                    style = AppTypography.bodyMedium,
                )
            }

            state.error?.let { error ->
                Spacer(Modifier.height(KptTheme.spacing.sm))
                FormDraftFailure(
                    message = stringResource(error.message),
                    serverReason = state.failureReason,
                    draftSaved = state.draftSaved,
                    offline = error == CreateGroupError.Offline,
                    onSaveDraft = { onAction(CreateGroupAction.SaveDraft) },
                    modifier = Modifier.testTag(CreateGroupTestTags.ERROR),
                )
            }

            Spacer(Modifier.height(KptTheme.spacing.lg))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                AppOutlinedButton(
                    onClick = { onAction(CreateGroupAction.NavigateBack) },
                    modifier = Modifier.weight(1f).testTag(CreateGroupTestTags.BACK),
                ) {
                    Text(stringResource(Res.string.feature_groups_create_back))
                }

                Spacer(Modifier.width(KptTheme.spacing.sm))

                AppOutlinedButton(
                    onClick = { onAction(CreateGroupAction.Submit) },
                    enabled = state.canSubmit,
                    modifier = Modifier.weight(1f).testTag(CreateGroupTestTags.SUBMIT),
                ) {
                    Text(stringResource(Res.string.feature_groups_create_submit))
                }
            }

            Spacer(Modifier.height(KptTheme.spacing.lg))
        }
    }
}

@Composable
private fun Boolean.errorOr(resource: StringResource): String? =
    if (this) stringResource(resource) else null

private val CreateGroupError.message: StringResource
    get() = when (this) {
        CreateGroupError.Offline -> Res.string.feature_groups_create_error_offline
        CreateGroupError.Failed -> Res.string.feature_groups_create_error_failed
    }

internal object CreateGroupTestTags {
    const val SCREEN = "CreateGroupScreen"
    const val NAME = "CreateGroupName"
    const val OFFICE = "CreateGroupOffice"
    const val ACTIVE = "CreateGroupActive"
    const val ERROR = "CreateGroupError"
    const val BACK = "CreateGroupBack"
    const val SUBMIT = "CreateGroupSubmit"
}

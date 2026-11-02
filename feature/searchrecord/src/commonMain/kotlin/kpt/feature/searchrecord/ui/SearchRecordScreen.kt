/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.searchrecord.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.common.utils.Constants
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.DesignToken
import kpt.core.model.objects.searchrecord.GenericSearchRecord
import kpt.core.model.objects.searchrecord.RecordType
import kpt.core.ui.components.AppActionsIdentifierListingComponent
import kpt.core.ui.components.AppAddressCard
import kpt.core.ui.components.AppAlertDialog
import kpt.core.ui.components.AppProgressIndicator
import kpt.core.ui.fieldofficer.utils.getClientIdentifierStatus
import kpt.feature.searchrecord.generated.resources.Res
import kpt.feature.searchrecord.generated.resources.search_record_address
import kpt.feature.searchrecord.generated.resources.search_record_address_line_1
import kpt.feature.searchrecord.generated.resources.search_record_address_line_2
import kpt.feature.searchrecord.generated.resources.search_record_address_line_3
import kpt.feature.searchrecord.generated.resources.search_record_city
import kpt.feature.searchrecord.generated.resources.search_record_clear_icon_desc
import kpt.feature.searchrecord.generated.resources.search_record_country
import kpt.feature.searchrecord.generated.resources.search_record_empty_state
import kpt.feature.searchrecord.generated.resources.search_record_error
import kpt.feature.searchrecord.generated.resources.search_record_generic_searchLabel
import kpt.feature.searchrecord.generated.resources.search_record_identifiers
import kpt.feature.searchrecord.generated.resources.search_record_input_placeholder
import kpt.feature.searchrecord.generated.resources.search_record_label_format
import kpt.feature.searchrecord.generated.resources.search_record_no_results_description
import kpt.feature.searchrecord.generated.resources.search_record_no_results_title
import kpt.feature.searchrecord.generated.resources.search_record_postal_code
import kpt.feature.searchrecord.generated.resources.search_record_province
import kpt.feature.searchrecord.generated.resources.search_record_search_icon_desc
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun SearchRecordScreen(
    onBackClick: () -> Unit,
    onRecordSelected: (GenericSearchRecord) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchRecordViewModel = koinViewModel(),
) {
    val screen by viewModel.screen.collectAsStateWithLifecycle()
    // The box's text comes from the VM's INPUT state, not the derived screen: a TextField bound to
    // an asynchronously-derived value drops and reorders keystrokes.
    val input by viewModel.stateFlow.collectAsStateWithLifecycle()
    val state = screen.copy(searchQuery = input.searchQuery)

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            SearchRecordEvent.NavigateBack -> onBackClick()
            is SearchRecordEvent.NavigateToRecord -> onRecordSelected(event.record)
        }
    }

    val searchLabel = stringResource(
        Res.string.search_record_label_format,
        stringResource(state.recordType.titleRes()),
    )

    SearchRecordScreen(
        modifier = modifier,
        state = state,
        searchLabel = searchLabel,
        onSearchQueryChanged = { viewModel.trySendAction(SearchRecordAction.SearchQueryChanged(it)) },
        onClearSearch = { viewModel.trySendAction(SearchRecordAction.ClearSearch) },
        onBackClick = { viewModel.trySendAction(SearchRecordAction.NavigateBack) },
        onRecordSelected = { viewModel.trySendAction(SearchRecordAction.SelectRecord(it)) },
        onCloseDialog = { viewModel.trySendAction(SearchRecordAction.CloseDialog) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SearchRecordScreen(
    state: SearchRecordState,
    modifier: Modifier = Modifier,
    searchLabel: String = stringResource(Res.string.search_record_generic_searchLabel),
    onSearchQueryChanged: (String) -> Unit = {},
    onClearSearch: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onRecordSelected: (GenericSearchRecord) -> Unit = {},
    onCloseDialog: () -> Unit = {},
) {
    AppScaffold(
        title = searchLabel,
        onBackPressed = onBackClick,
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag(SearchRecordTestTags.SCREEN),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                SearchRecordToolbar(
                    searchQuery = state.searchQuery,
                    onSearchQueryChanged = onSearchQueryChanged,
                    onClearSearch = onClearSearch,
                )

                when {
                    state.searchRecords.isNotEmpty() -> {
                        SearchRecordResultsList(
                            records = state.searchRecords,
                            onRecordSelected = onRecordSelected,
                        )
                    }
                    state.isNoResultsFound -> {
                        SearchRecordNoResultsState(state.searchQuery)
                    }
                    else -> {
                        SearchRecordEmptyState()
                    }
                }
            }

            when (val dialog = state.dialogState) {
                SearchRecordState.DialogState.Loading -> {
                    AppProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                is SearchRecordState.DialogState.Error -> {
                    AppAlertDialog(
                        dialogTitle = stringResource(Res.string.search_record_error),
                        dialogText = dialog.message,
                        onDismissRequest = onCloseDialog,
                        onConfirmation = onCloseDialog,
                    )
                }
                null -> { }
            }
        }
    }
}

@Composable
private fun SearchRecordToolbar(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onClearSearch: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(KptTheme.spacing.md),
    ) {
        TextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .testTag(SearchRecordTestTags.SEARCH_FIELD),
            shape = DesignToken.shapes.extraLargeIncreased,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            leadingIcon = {
                Icon(
                    imageVector = AppIcons.Search,
                    contentDescription = stringResource(Res.string.search_record_search_icon_desc),
                    tint = KptTheme.colorScheme.onSurfaceVariant,
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = onClearSearch) {
                        Icon(
                            imageVector = AppIcons.Close,
                            contentDescription = stringResource(Res.string.search_record_clear_icon_desc),
                        )
                    }
                }
            },
            placeholder = { Text(stringResource(Res.string.search_record_input_placeholder)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = { keyboardController?.hide() },
            ),
        )
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
private fun SearchRecordResultsList(
    records: List<GenericSearchRecord>,
    onRecordSelected: (GenericSearchRecord) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = KptTheme.spacing.md),
    ) {
        Spacer(modifier = Modifier.height(KptTheme.spacing.lg))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            items(
                items = records,
                key = { record -> "${record.type}-${record.id}" },
            ) { record ->
                Box(Modifier.testTag(SearchRecordTestTags.result(record.type, record.id))) {
                    SearchRecordItem(
                        record = record,
                        onRecordSelected = onRecordSelected,
                    )
                }
                Spacer(Modifier.height(KptTheme.spacing.sm))
            }
        }
    }
}

@Composable
private fun SearchRecordItem(
    record: GenericSearchRecord,
    onRecordSelected: (GenericSearchRecord) -> Unit,
) {
    when {
        record.type.equals(RecordType.ADDRESS.name, ignoreCase = true) -> {
            AddressRecordCard(record, onRecordSelected)
        }
        record.type.equals(RecordType.IDENTIFIER.name, ignoreCase = true) -> {
            IdentifierRecordCard(record, onRecordSelected)
        }
        else -> {
            GenericRecordCard(record, onRecordSelected)
        }
    }
}

@Composable
private fun AddressRecordCard(
    record: GenericSearchRecord,
    onRecordSelected: (GenericSearchRecord) -> Unit,
) {
    Surface(
        onClick = { onRecordSelected(record) },
        modifier = Modifier.fillMaxWidth(),
        shape = KptTheme.shapes.medium,
        border = BorderStroke(
            DesignToken.spacing.dp1,
            KptTheme.colorScheme.secondaryContainer,
        ),
        color = Color.Transparent,
    ) {
        AppAddressCard(
            title = record.name,
            addressList = mapOf(
                stringResource(Res.string.search_record_address_line_1) to
                    (record.metadata[Constants.ADDRESS_LINE_1] ?: ""),
                stringResource(Res.string.search_record_address_line_2) to
                    (record.metadata[Constants.ADDRESS_LINE_2] ?: ""),
                stringResource(Res.string.search_record_address_line_3) to
                    (record.metadata[Constants.ADDRESS_LINE_3] ?: ""),
                stringResource(Res.string.search_record_city) to (record.metadata[Constants.CITY] ?: ""),
                stringResource(Res.string.search_record_province) to (record.metadata[Constants.STATE] ?: ""),
                stringResource(Res.string.search_record_country) to (record.metadata[Constants.COUNTRY] ?: ""),
                stringResource(Res.string.search_record_postal_code) to (record.metadata[Constants.POSTAL_CODE] ?: ""),
            ),
        )
    }
}

@Composable
private fun IdentifierRecordCard(
    record: GenericSearchRecord,
    onRecordSelected: (GenericSearchRecord) -> Unit,
) {
    val rawStatus = record.metadata[Constants.STATUS] ?: ""
    val statusObject = getClientIdentifierStatus(rawStatus)

    AppActionsIdentifierListingComponent(
        type = record.name,
        id = record.id.toString(),
        key = record.metadata[Constants.DOCUMENT_KEY] ?: "",
        status = statusObject,
        description = record.description,
        identifyDocuments = record.name,
        isExpanded = false,
        menuList = emptyList(),
        onActionClicked = {},
        onClick = { onRecordSelected(record) },
    )
}

@Composable
private fun GenericRecordCard(
    record: GenericSearchRecord,
    onRecordSelected: (GenericSearchRecord) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = { onRecordSelected(record) },
    ) {
        Column(
            modifier = Modifier.padding(KptTheme.spacing.md),
        ) {
            Text(
                text = record.name,
                style = KptTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(KptTheme.spacing.xs))
            Text(
                text = record.description,
                style = KptTheme.typography.bodySmall,
                color = KptTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SearchRecordEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(KptTheme.spacing.xl)
            .testTag(SearchRecordTestTags.EMPTY),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.search_record_empty_state),
            style = KptTheme.typography.bodyMedium,
            color = KptTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SearchRecordNoResultsState(query: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(KptTheme.spacing.xl)
            .testTag(SearchRecordTestTags.NO_RESULTS),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.search_record_no_results_title),
            style = KptTheme.typography.headlineSmall,
        )
        Spacer(modifier = Modifier.height(KptTheme.spacing.sm))
        Text(
            text = stringResource(Res.string.search_record_no_results_description, query),
            style = KptTheme.typography.bodyMedium,
            color = KptTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** The title word for a record type — the original hardcoded these in the ViewModel. */
internal fun RecordType.titleRes() = when (this) {
    RecordType.ADDRESS -> Res.string.search_record_address
    RecordType.IDENTIFIER -> Res.string.search_record_identifiers
}

/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package kpt.feature.client.documents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.fieldofficer.component.FloatingActionButtonContent
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.AppTypography
import kpt.core.model.objects.noncoreobjects.Document
import kpt.core.ui.components.Actions
import kpt.core.ui.components.AppActionsClientDocumentListingComponent
import kpt.core.ui.components.AppAlertDialog
import kpt.core.ui.components.AppEmptyCard
import kpt.core.ui.components.AppSearchBar
import kpt.feature.client.generated.resources.Res
import kpt.feature.client.generated.resources.feature_client_add_record
import kpt.feature.client.generated.resources.feature_client_confirm_text
import kpt.feature.client.generated.resources.feature_client_delete_dialog_title
import kpt.feature.client.generated.resources.feature_client_dismiss_text
import kpt.feature.client.generated.resources.feature_client_document_delete_dialog_message
import kpt.feature.client.generated.resources.feature_client_document_delete_failed
import kpt.feature.client.generated.resources.feature_client_document_delete_offline
import kpt.feature.client.generated.resources.feature_client_identifiers_empty
import kpt.feature.client.generated.resources.feature_client_identifiers_item
import kpt.feature.client.generated.resources.feature_client_profile_documents_title
import kpt.feature.client.generated.resources.feature_client_screen_documents
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Carried over from `6b66e8a43:feature/client/clientDocuments/ClientDocumentsScreen.kt` — the
 * header with its count, the collapsible `AppSearchBar`, `PullToRefreshBox`, `AppEmptyCard`,
 * and one `AppActionsClientDocumentListingComponent` per document with the original's
 * single-expanded-row behaviour held in local `remember` state.
 *
 * §6a's adaptations: read states through [ScreenContent]; actions unchanged; View opens the in-app
 * preview instead of an external app. See the ViewModel's header.
 */
@Composable
internal fun ClientDocumentsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onAddDocument: ((Int) -> Unit)? = null,
    onViewDocument: ((ClientDocumentsEvent.ViewDocument) -> Unit)? = null,
    viewModel: ClientDocumentsViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val documents by viewModel.documents.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            ClientDocumentsEvent.NavigateBack -> onNavigateBack()
            is ClientDocumentsEvent.AddDocument -> onAddDocument?.invoke(event.clientId)
            is ClientDocumentsEvent.ViewDocument -> onViewDocument?.invoke(event)
        }
    }

    AppScaffold(
        title = stringResource(Res.string.feature_client_screen_documents),
        // Dispatched, not called directly: the ViewModel owns the event, which is the
        // path `ClientDocumentsAction.NavigateBack` already declared and nothing reached.
        onBackPressed = { viewModel.trySendAction(ClientDocumentsAction.NavigateBack) },
        // The add affordance is a FAB, not the original's header icon — operator-directed UI
        // change (2026-10-22), recorded as a §6a deviation. See ClientDepositAccountsScreen for
        // the full reasoning; in short, the original's header "+" read as "no way to add here" on
        // a real device, and it passed `contentDescription = null` so it was unlabelled too.
        floatingActionButtonContent = onAddDocument?.let {
            FloatingActionButtonContent(
                onClick = { viewModel.trySendAction(ClientDocumentsAction.AddDocument) },
                contentColor = KptTheme.colorScheme.onPrimaryContainer,
                content = {
                    Icon(
                        imageVector = AppIcons.Add,
                        contentDescription = stringResource(Res.string.feature_client_add_record),
                    )
                },
            )
        },
    ) { scaffoldPadding ->
        ClientDocumentsContent(
            state = state,
            documents = documents,
            onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
            modifier = modifier,
            scaffoldPadding = scaffoldPadding,
            canViewDocument = onViewDocument != null,
        )
    }
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun ClientDocumentsContent(
    state: ClientDocumentsState,
    documents: ScreenState<List<Document>>,
    onAction: (ClientDocumentsAction) -> Unit,
    modifier: Modifier = Modifier,
    /**
     * Insets from the host [AppScaffold]'s top bar.
     *
     * Defaulted so tests and previews can render this half alone — the production caller always
     * passes the scaffold's own padding. These screens had NO top bar at all before S5d: the shell
     * draws one only for tab roots, so every client push destination rendered with no title and no
     * back affordance (which is why their `NavigateBack` action had no dispatcher).
     */
    scaffoldPadding: PaddingValues = PaddingValues(),
    canViewDocument: Boolean = false,
) {
    // The host scaffold's insets, folded into the modifier this half already receives so
    // the screen's own root layout places them.
    @Suppress("NAME_SHADOWING")
    val modifier = modifier.padding(scaffoldPadding)

    PullToRefreshBox(
        // Driven by the stream's own state rather than a Boolean nothing reset on the error path.
        isRefreshing = documents is ScreenState.Loading,
        onRefresh = { onAction(ClientDocumentsAction.Refresh) },
        modifier = modifier.fillMaxSize().testTag(ClientDocumentsTestTags.SCREEN),
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = KptTheme.spacing.md)) {
            Header(
                totalItem = ((documents as? ScreenState.Content)?.data?.size ?: 0).toString(),
                onAction = onAction,
            )

            if (state.isSearchBarActive) {
                AppSearchBar(
                    query = state.searchText,
                    onQueryChange = { onAction(ClientDocumentsAction.UpdateSearchQuery(it)) },
                    onSearchClick = { onAction(ClientDocumentsAction.SearchDocument) },
                    onBackClick = { onAction(ClientDocumentsAction.ToggleSearch) },
                    modifier = Modifier.testTag(ClientDocumentsTestTags.SEARCH_BAR),
                )
            }

            Spacer(modifier = Modifier.height(KptTheme.spacing.lg))

            ScreenContent(
                state = documents,
                onRetry = { onAction(ClientDocumentsAction.Refresh) },
                modifier = Modifier.fillMaxSize().testTag(ClientDocumentsTestTags.LIST),
            ) { list, _ ->
                if (list.isEmpty()) {
                    AppEmptyCard(
                        msg = stringResource(Res.string.feature_client_identifiers_empty),
                        modifier = Modifier.testTag(ClientDocumentsTestTags.EMPTY),
                    )
                    return@ScreenContent
                }

                // The original's single-expanded-row behaviour, held locally and keyed by id —
                // tapping the open row closes it, tapping another moves the expansion.
                var expandedId by remember { mutableStateOf(-1) }

                LazyColumn {
                    items(list, key = { it.id }) { document ->
                        AppActionsClientDocumentListingComponent(
                            documentName = document.name ?: "",
                            documentDescription = document.description ?: "",
                            fileName = document.fileName ?: "",
                            isExpanded = expandedId == document.id,
                            onClick = {
                                expandedId = if (expandedId == document.id) -1 else document.id
                            },
                            // View is listed only when the host wired the preview destination.
                            menuList = listOfNotNull(
                                Actions.ViewDocument().takeIf { canViewDocument },
                                Actions.DeleteDocument(),
                            ),
                            onActionClicked = { action ->
                                when (action) {
                                    is Actions.ViewDocument ->
                                        onAction(ClientDocumentsAction.ViewDocument(document))

                                    is Actions.DeleteDocument -> onAction(
                                        ClientDocumentsAction.DeleteDocument(
                                            documentName = document.fileName ?: "",
                                            documentId = document.id,
                                        ),
                                    )

                                    else -> Unit
                                }
                            },
                        )
                        Spacer(modifier = Modifier.height(KptTheme.spacing.sm))
                    }
                }
            }
        }
    }

    Dialogs(state = state, onAction = onAction)
}

@Composable
private fun Header(
    totalItem: String,
    onAction: (ClientDocumentsAction) -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
        Column {
            Text(
                text = stringResource(Res.string.feature_client_profile_documents_title),
                style = AppTypography.titleMedium,
            )
            Text(
                text = totalItem + " " + stringResource(Res.string.feature_client_identifiers_item),
                style = AppTypography.labelMedium,
                modifier = Modifier.testTag(ClientDocumentsTestTags.COUNT),
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // This search icon IS wired — it filters the list in place, so it needs no destination.
        Icon(
            imageVector = AppIcons.Search,
            contentDescription = null,
            modifier = Modifier
                .clickable { onAction(ClientDocumentsAction.ToggleSearch) }
                .testTag(ClientDocumentsTestTags.SEARCH_TOGGLE),
        )
    }
}

@Composable
private fun Dialogs(
    state: ClientDocumentsState,
    onAction: (ClientDocumentsAction) -> Unit,
) {
    when (val dialog = state.dialogState) {
        null -> Unit
        is ClientDocumentsState.DialogState.ConfirmDeletion -> AlertDialog(
            title = { Text(stringResource(Res.string.feature_client_delete_dialog_title)) },
            text = {
                Text(
                    text = stringResource(Res.string.feature_client_document_delete_dialog_message) +
                        " " + dialog.documentName,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onAction(ClientDocumentsAction.ConfirmDeleteDocument(dialog.documentId))
                    },
                ) { Text(stringResource(Res.string.feature_client_confirm_text)) }
            },
            dismissButton = {
                TextButton(
                    onClick = { onAction(ClientDocumentsAction.CloseDialog) },
                ) { Text(stringResource(Res.string.feature_client_dismiss_text)) }
            },
            onDismissRequest = { onAction(ClientDocumentsAction.CloseDialog) },
            modifier = Modifier.testTag(ClientDocumentsTestTags.DELETE_DIALOG),
        )
    }

    state.writeError?.let { error ->
        val message = when (error) {
            DocumentWriteError.Offline -> stringResource(Res.string.feature_client_document_delete_offline)
            DocumentWriteError.Failed -> stringResource(Res.string.feature_client_document_delete_failed)
        }
        AppAlertDialog(
            dialogText = message,
            onDismissRequest = { onAction(ClientDocumentsAction.CloseDialog) },
            onConfirmation = { onAction(ClientDocumentsAction.CloseDialog) },
            dismissText = null,
            modifier = Modifier.testTag(ClientDocumentsTestTags.WRITE_ERROR_DIALOG),
        )
    }
}

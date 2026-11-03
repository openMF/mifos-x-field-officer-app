/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.document.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.fieldofficer.component.FloatingActionButtonContent
import kpt.core.designsystem.icon.AppIcons
import kpt.core.model.objects.noncoreobjects.Document
import kpt.core.ui.components.AppEmptyUi
import kpt.feature.document.generated.resources.Res
import kpt.feature.document.generated.resources.feature_document_download_document
import kpt.feature.document.generated.resources.feature_document_download_successful
import kpt.feature.document.generated.resources.feature_document_failed_to_download_document
import kpt.feature.document.generated.resources.feature_document_failed_to_remove_document
import kpt.feature.document.generated.resources.feature_document_header_description
import kpt.feature.document.generated.resources.feature_document_header_name
import kpt.feature.document.generated.resources.feature_document_no_document
import kpt.feature.document.generated.resources.feature_document_offline
import kpt.feature.document.generated.resources.feature_document_remove_document
import kpt.feature.document.generated.resources.feature_document_remove_successful
import kpt.feature.document.generated.resources.feature_document_select_option
import kpt.feature.document.generated.resources.feature_document_title
import kpt.feature.document.generated.resources.feature_document_update_document
import kpt.feature.document.generated.resources.feature_document_upload_document
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Carried over from `6b66e8a43:feature/document/documentList/DocumentListScreen.kt` — the Name /
 * Description header card, one `Card` per document, and the three-option sheet on tap
 * (Download / Update / Remove), inside `AppScaffold`.
 *
 * §6a's three adaptations: the read's Loading/Error states go through [ScreenContent]; the row's
 * tap dispatches an action instead of mutating screen-local `remember`; and Update is `null` when
 * the upload form has no destination.
 *
 * The Download option is kept even though it saves nothing — see the ViewModel's header and
 * `document-download-saves-nothing` in CAPABILITY_GAPS.
 */
@Composable
internal fun DocumentListScreen(
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    onUpload: ((String, Int) -> Unit)? = null,
    viewModel: DocumentListViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val documents by viewModel.documentList.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            DocumentListEvent.NavigateBack -> onBackPressed()
            is DocumentListEvent.Upload -> onUpload?.invoke(event.entityType, event.entityId)
        }
    }

    DocumentListContent(
        state = state,
        documents = documents,
        onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
        canUpload = onUpload != null,
        onBackPressed = onBackPressed,
        modifier = modifier,
    )
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun DocumentListContent(
    state: DocumentListState,
    documents: ScreenState<List<Document>>,
    onAction: (DocumentListAction) -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    canUpload: Boolean = true,
) {
    Box(modifier = modifier) {
        val snackbarHostState = remember { SnackbarHostState() }
        val notice = state.notice
        val noticeText = notice?.let { stringResource(it.messageRes()) }

        // One snackbar per notice, and the notice CLEARS — the original used two booleans that were
        // set true and never reset, so each message could fire only once per screen visit.
        LaunchedEffect(notice) {
            if (noticeText != null) {
                snackbarHostState.showSnackbar(noticeText)
                onAction(DocumentListAction.DismissNotice)
            }
        }

        AppScaffold(
            title = stringResource(Res.string.feature_document_title),
            onBackPressed = onBackPressed,
            snackbarHostState = snackbarHostState,
            modifier = Modifier.testTag(DocumentListTestTags.SCREEN),
            // The ONLY upload affordance used to be the "Update document" option inside an existing
            // row's dialog — so a list with no documents had no way to add one, on every entity that
            // reaches this screen. Found on device 2026-10-23 on a group's Documents screen: an empty
            // list with a back arrow and nothing else, while `onUpload` was wired the whole time.
            // Gated on the same `canUpload` as that dialog option, so where the destination is
            // unported the FAB is ABSENT rather than inert (§6a adaptation 3).
            floatingActionButtonContent = if (canUpload) {
                FloatingActionButtonContent(
                    onClick = { onAction(DocumentListAction.UploadClicked) },
                    contentColor = KptTheme.colorScheme.onPrimaryContainer,
                    content = {
                        Icon(
                            imageVector = AppIcons.Add,
                            contentDescription = stringResource(Res.string.feature_document_upload_document),
                        )
                    },
                )
            } else {
                null
            },
        ) { padding ->
            ScreenContent(
                state = documents,
                onRetry = { onAction(DocumentListAction.Refresh) },
                modifier = Modifier.fillMaxSize().padding(padding).testTag(DocumentListTestTags.LIST),
            ) { list, _ ->
                if (list.isEmpty()) {
                    AppEmptyUi(
                        text = stringResource(Res.string.feature_document_no_document),
                        modifier = Modifier.testTag(DocumentListTestTags.EMPTY),
                    )
                    return@ScreenContent
                }

                Column {
                    HeaderRow()
                    LazyColumn {
                        items(list, key = { it.id }) { document ->
                            DocumentRow(
                                document = document,
                                onClick = { onAction(DocumentListAction.ShowOptions(document.id)) },
                            )
                        }
                    }
                }
            }
        }

        // Unused by the current callers but kept: the snackbar host above renders inside the scaffold.
        SnackbarHost(snackbarHostState, modifier = Modifier.testTag(DocumentListTestTags.SNACKBAR))

        state.selectedDocumentId?.let { documentId ->
            OptionsDialog(
                documentId = documentId,
                canUpload = canUpload,
                onAction = onAction,
            )
        }
    }
}

@Composable
private fun HeaderRow() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(KptTheme.spacing.sm),
        shape = RectangleShape,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(KptTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1f).padding(start = KptTheme.spacing.md),
                text = stringResource(Res.string.feature_document_header_name),
                style = KptTheme.typography.bodyLarge,
                textAlign = TextAlign.Start,
                maxLines = 1,
            )
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(Res.string.feature_document_header_description),
                style = KptTheme.typography.bodyLarge,
                textAlign = TextAlign.Start,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun DocumentRow(document: Document, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = KptTheme.spacing.xs, horizontal = KptTheme.spacing.sm),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(KptTheme.spacing.md).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // `orEmpty()`, not the original's `toString()` — that printed the literal "null" for a
            // document with no name, which Fineract permits.
            Text(
                modifier = Modifier.weight(1f).padding(start = KptTheme.spacing.md),
                text = document.name.orEmpty(),
                style = KptTheme.typography.bodyMedium,
                textAlign = TextAlign.Start,
            )
            Text(
                modifier = Modifier.weight(1f),
                text = document.description.orEmpty(),
                style = KptTheme.typography.bodyMedium,
                textAlign = TextAlign.Start,
            )
        }
    }
}

@Composable
private fun OptionsDialog(
    documentId: Int,
    canUpload: Boolean,
    onAction: (DocumentListAction) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onAction(DocumentListAction.CloseOptions) },
        title = { Text(stringResource(Res.string.feature_document_select_option)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs)) {
                TextButton(
                    onClick = { onAction(DocumentListAction.Download(documentId)) },
                    modifier = Modifier.testTag(DocumentListTestTags.OPTION_DOWNLOAD),
                ) { Text(stringResource(Res.string.feature_document_download_document)) }

                // Absent when the upload form has no destination, rather than drawn inert.
                if (canUpload) {
                    TextButton(
                        onClick = { onAction(DocumentListAction.UploadClicked) },
                        modifier = Modifier.testTag(DocumentListTestTags.OPTION_UPDATE),
                    ) { Text(stringResource(Res.string.feature_document_update_document)) }
                }

                TextButton(
                    onClick = { onAction(DocumentListAction.Remove(documentId)) },
                    modifier = Modifier.testTag(DocumentListTestTags.OPTION_REMOVE),
                ) { Text(stringResource(Res.string.feature_document_remove_document)) }
            }
        },
        confirmButton = {},
        modifier = Modifier.testTag(DocumentListTestTags.OPTIONS_DIALOG),
    )
}

private fun DocumentNotice.messageRes() = when (this) {
    DocumentNotice.Downloaded -> Res.string.feature_document_download_successful
    DocumentNotice.DownloadFailed -> Res.string.feature_document_failed_to_download_document
    DocumentNotice.Removed -> Res.string.feature_document_remove_successful
    DocumentNotice.RemoveFailed -> Res.string.feature_document_failed_to_remove_document
    DocumentNotice.Offline -> Res.string.feature_document_offline
}

/** Stable handles for UI tests and Maestro flows — see RULE-KMP-COMPOSE-UITEST-001. */
internal object DocumentListTestTags {
    const val SCREEN = "documentList:screen"
    const val LIST = "documentList:list"
    const val EMPTY = "documentList:empty"
    const val SNACKBAR = "documentList:snackbar"
    const val OPTIONS_DIALOG = "documentList:options"
    const val OPTION_DOWNLOAD = "documentList:option:download"
    const val OPTION_UPDATE = "documentList:option:update"
    const val OPTION_REMOVE = "documentList:option:remove"
}

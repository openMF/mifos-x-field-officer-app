/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.documentpreview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.designsystem.fieldofficer.component.AppButton
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.theme.AppTypography
import kpt.core.ui.components.AppDocumentViewer
import kpt.core.ui.components.AppProgressIndicator
import kpt.core.ui.generated.resources.core_ui_retry
import kpt.feature.client.generated.resources.Res
import kpt.feature.client.generated.resources.feature_client_document_preview_failed
import kpt.feature.client.generated.resources.feature_client_document_preview_none
import kpt.feature.client.generated.resources.feature_client_document_preview_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kpt.core.ui.generated.resources.Res as CoreUiRes

/**
 * A saved document, shown in place. See [ClientDocumentPreviewViewModel] for what this replaces.
 *
 * The original's two-button row (Back / Submit-or-Update-New) and its file-picker sheet belonged to
 * the UPLOAD half of `DocumentPreviewScreen` and are not carried over — the top bar's back is the
 * only way out of a read-only view.
 */
@Composable
internal fun ClientDocumentPreviewScreen(
    onNavigateBack: () -> Unit,
    viewModel: ClientDocumentPreviewViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            ClientDocumentPreviewEvent.NavigateBack -> onNavigateBack()
        }
    }

    AppScaffold(
        title = state.title.ifBlank { stringResource(Res.string.feature_client_document_preview_title) },
        onBackPressed = { viewModel.trySendAction(ClientDocumentPreviewAction.NavigateBack) },
    ) { padding ->
        ClientDocumentPreviewContent(
            content = state.content,
            onRetry = { viewModel.trySendAction(ClientDocumentPreviewAction.Retry) },
            scaffoldPadding = padding,
        )
    }
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun ClientDocumentPreviewContent(
    content: PreviewContent,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    scaffoldPadding: PaddingValues = PaddingValues(),
) {
    Box(
        modifier = modifier
            .padding(scaffoldPadding)
            .fillMaxSize()
            .testTag(ClientDocumentPreviewTestTags.SCREEN),
    ) {
        when (content) {
            PreviewContent.Loading -> AppProgressIndicator(
                modifier = Modifier.fillMaxSize().testTag(ClientDocumentPreviewTestTags.LOADING),
            )

            is PreviewContent.Loaded -> AppDocumentViewer(
                bytes = content.bytes,
                fileName = content.fileName,
                mimeType = content.mimeType,
                modifier = Modifier.fillMaxSize().testTag(ClientDocumentPreviewTestTags.VIEWER),
            )

            PreviewContent.NoDocument -> Message(
                text = stringResource(Res.string.feature_client_document_preview_none),
                tag = ClientDocumentPreviewTestTags.NO_DOCUMENT,
            )

            PreviewContent.Failed -> Message(
                text = stringResource(Res.string.feature_client_document_preview_failed),
                tag = ClientDocumentPreviewTestTags.FAILED,
                onRetry = onRetry,
            )
        }
    }
}

@Composable
private fun Message(text: String, tag: String, onRetry: (() -> Unit)? = null) {
    Column(
        modifier = Modifier.fillMaxSize().padding(KptTheme.spacing.lg).testTag(tag),
        verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = text, style = AppTypography.bodyMedium, textAlign = TextAlign.Center)
        if (onRetry != null) {
            AppButton(
                onClick = onRetry,
                modifier = Modifier.testTag(ClientDocumentPreviewTestTags.RETRY),
            ) {
                Text(stringResource(CoreUiRes.string.core_ui_retry))
            }
        }
    }
}

/** Stable handles for UI tests and Maestro flows — see RULE-KMP-COMPOSE-UITEST-001. */
internal object ClientDocumentPreviewTestTags {
    const val SCREEN = "clientDocumentPreview:screen"
    const val LOADING = "clientDocumentPreview:loading"
    const val VIEWER = "clientDocumentPreview:viewer"
    const val NO_DOCUMENT = "clientDocumentPreview:noDocument"
    const val FAILED = "clientDocumentPreview:failed"
    const val RETRY = "clientDocumentPreview:retry"
}

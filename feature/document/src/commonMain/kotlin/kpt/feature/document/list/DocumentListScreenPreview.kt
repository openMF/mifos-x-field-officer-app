/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.document.list

import androidx.compose.runtime.Composable
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.objects.noncoreobjects.Document
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier: `CommonComposablePreviewScanner`
 * discovers these from commonMain and renders them off `desktopTest`. The Koin-bound screens are
 * not previewed; their stateless `*Content` halves are. Fixture values are server content, not
 * app copy — `// i18n:skip`.
 */

// i18n:skip — preview fixtures
private val previewDocuments = listOf(
    Document(
        id = 1,
        parentEntityType = "groups",
        parentEntityId = 11,
        name = "Meeting minutes",
        fileName = "minutes-oct.pdf",
        size = 182_000,
        type = "application/pdf",
    ),
    Document(
        id = 2,
        parentEntityType = "groups",
        parentEntityId = 11,
        name = "Group photo",
        fileName = "group.jpg",
        size = 940_000,
        type = "image/jpeg",
        description = "Taken at the October meeting",
    ),
)

@Preview
@Composable
internal fun DocumentListContentPreview() {
    KptTheme {
        DocumentListContent(
            state = DocumentListState(entityType = "groups", entityId = 11),
            documents = ScreenState.Content(previewDocuments),
            onAction = {},
            onBackPressed = {},
        )
    }
}

@Preview
@Composable
internal fun DocumentListContentEmptyPreview() {
    KptTheme {
        DocumentListContent(
            state = DocumentListState(entityType = "groups", entityId = 11),
            documents = ScreenState.Content(emptyList()),
            onAction = {},
            onBackPressed = {},
        )
    }
}

/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.document.upload

import androidx.compose.runtime.Composable
import kpt.core.designsystem.theme.KptTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier: `CommonComposablePreviewScanner`
 * discovers these from commonMain and renders them off `desktopTest`. The Koin-bound screens are
 * not previewed; their stateless `*Content` halves are. Fixture values are server content, not
 * app copy — `// i18n:skip`.
 */

@Preview
@Composable
internal fun DocumentUploadContentPreview() {
    KptTheme {
        DocumentUploadContent(
            // i18n:skip — preview fixtures
            state = DocumentUploadState(
                entityType = "groups",
                entityId = 11,
                name = "Meeting minutes",
                description = "October",
            ),
            onAction = {},
        )
    }
}

@Preview
@Composable
internal fun DocumentUploadContentValidationPreview() {
    // Submit pressed with nothing chosen: every required field flags itself.
    KptTheme {
        DocumentUploadContent(
            state = DocumentUploadState(
                entityType = "groups",
                entityId = 11,
                nameError = true,
                descriptionError = true,
                fileError = true,
            ),
            onAction = {},
        )
    }
}

/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.documentpreview

import androidx.compose.runtime.Composable
import kpt.core.designsystem.theme.KptTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free render tier. Only the two MESSAGE states are previewed:
 * the PDF and image states open their content asynchronously (PDFium / Coil), so a headless capture
 * would record the spinner, not the document — those are proven on a device instead.
 */

@Preview
@Composable
internal fun ClientDocumentPreviewFailedPreview() {
    // The only state with an action: Retry must be visible, or a dropped connection is a dead end.
    KptTheme {
        ClientDocumentPreviewContent(content = PreviewContent.Failed, onRetry = {})
    }
}

@Preview
@Composable
internal fun ClientDocumentPreviewNoDocumentPreview() {
    // An identifier saved without a scan — a real answer, NOT an error, so no Retry.
    KptTheme {
        ClientDocumentPreviewContent(content = PreviewContent.NoDocument, onRetry = {})
    }
}

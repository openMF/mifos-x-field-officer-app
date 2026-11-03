/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.activate.ui

import androidx.compose.runtime.Composable
import kpt.core.designsystem.theme.KptTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/** 2025-10-09 — fixed so a golden never changes because the clock moved. */
private const val PREVIEW_DATE_MILLIS = 1_760_000_000_000L

/*
 * @Preview siblings for the device-free CMP render tier: `CommonComposablePreviewScanner`
 * discovers these from commonMain and renders them off `desktopTest`. `ActivateScreen` resolves
 * its ViewModel through Koin, so the stateless `ActivateContent` is what is previewed.
 */

@Preview
@Composable
internal fun ActivateContentPreview() {
    KptTheme {
        ActivateContent(
            state = ActivateState(id = 42, type = ActivateType.Client),
            onAction = {},
            onBackPressed = {},
            initialDateMillis = PREVIEW_DATE_MILLIS,
        )
    }
}

@Preview
@Composable
internal fun ActivateContentSubmittingPreview() {
    // The button disables while the request is in flight — the state a double-tap would hit.
    KptTheme {
        ActivateContent(
            state = ActivateState(id = 42, type = ActivateType.Group, isSubmitting = true),
            onAction = {},
            onBackPressed = {},
            initialDateMillis = PREVIEW_DATE_MILLIS,
        )
    }
}

@Preview
@Composable
internal fun ActivateContentQueuedOfflinePreview() {
    // Offline is a distinct outcome from success: the activation is queued, not done.
    KptTheme {
        ActivateContent(
            state = ActivateState(id = 42, type = ActivateType.Center, result = ActivateResult.Offline),
            onAction = {},
            onBackPressed = {},
            initialDateMillis = PREVIEW_DATE_MILLIS,
        )
    }
}

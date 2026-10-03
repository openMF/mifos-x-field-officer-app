/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kpt.core.designsystem.theme.KptTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier (SCREENSHOT_TEST.md CMP-PRIMARY).
 * `CommonComposablePreviewScanner` auto-discovers these from commonMain and renders them off
 * `desktopTest` — no emulator, no Robolectric.
 *
 * Unlike the ViewModel-bound screens, `HomeScreen` IS directly previewable: it is a shell whose
 * content arrives through the `homeBody` slot, so the shell can be rendered with a stand-in body
 * and no Koin graph. That shell — top bar, settings affordance, padding — is exactly what these
 * previews cover; the real dashboard cards carry their own previews in their own features.
 *
 * Literals are PREVIEW FIXTURE DATA — never reachable from the running app.
 */

@Preview
@Composable
internal fun HomeScreenShellPreview() {
    KptTheme {
        HomeScreen(
            onSettingsClick = {},
            homeBody = {
                Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("Dashboard cards render here")
                }
            },
        )
    }
}

@Preview
@Composable
internal fun HomeScreenEmptyBodyPreview() {
    // The default `homeBody = {}` is a real state: it is what renders if a fork registers no
    // dashboard cards. The shell must still be a complete screen rather than a blank window.
    KptTheme {
        HomeScreen(onSettingsClick = {})
    }
}

/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.profile

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
 * `ProfileScreen` is a shell whose content arrives through the `profileBody` slot, so it renders
 * without a Koin graph. Literals are PREVIEW FIXTURE DATA — never reachable from the running app.
 */

@Preview
@Composable
internal fun ProfileScreenShellPreview() {
    KptTheme {
        ProfileScreen(
            profileBody = {
                Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("Profile details render here")
                }
            },
        )
    }
}

@Preview
@Composable
internal fun ProfileScreenEmptyBodyPreview() {
    // The default empty slot — what a fork sees before it registers a profile body.
    KptTheme {
        ProfileScreen()
    }
}

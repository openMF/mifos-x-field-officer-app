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
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import kpt.core.designsystem.theme.KptTheme
import kotlin.test.Test

/**
 * Compose Multiplatform UI test for [ProfileScreen] — the opaque SHELL.
 *
 * `ProfileScreen` renders whatever `BackboneRegistry.profileBody` supplies (the WS01
 * shell/seam split), so the shell is tested with a trivial body: the assertion is that the
 * scaffold root exists regardless of what the fork plugs in. The default demo body is
 * store-backed and covered separately by [ProfileDemoBodyUiTest], which needs a Koin graph.
 */
@OptIn(ExperimentalTestApi::class)
class ProfileScreenUiTest {

    @Test
    fun screenIsDisplayed() = runComposeUiTest {
        setContent {
            KptTheme {
                // A fork's body is opaque to the shell — an empty Box is a faithful stand-in.
                ProfileScreen(profileBody = { Box(androidx.compose.ui.Modifier) })
            }
        }
        onNodeWithTag(TestTags.Profile.SCREEN).assertIsDisplayed()
    }
}

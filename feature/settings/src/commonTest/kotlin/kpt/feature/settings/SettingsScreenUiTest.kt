/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.settings

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runComposeUiTest
import kpt.core.designsystem.theme.KptTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Compose Multiplatform UI test for the fork's [SettingsScreenContent] (was the template's `SettingsDemoBody` seam
 * rendered by `cmp-navigation`'s `BackboneRegistry.settingsBody` inside the template `SettingsScreen`
 * shell).
 *
 * It is a pure-UI body — all dialog state is managed internally; no ViewModel is needed. The test
 * renders it directly inside [KptTheme] with no-op callbacks and asserts the always-present root
 * scaffold node identified by [TestTags.Settings.SCREEN].
 */
@OptIn(ExperimentalTestApi::class)
class SettingsScreenUiTest {

    @Test
    fun screenIsDisplayed() = runComposeUiTest {
        setContent {
            KptTheme {
                // In production cmp-navigation's BackboneRegistry.settingsBody supplies this body.
                // Repointed in S1 T4 from `SettingsDemoBody`, a kmp-project-template DEMO seam that
                // `remove-demo.sh` stripped at fork standup and this fork never supplied — so the
                // test referenced a declaration that exists nowhere in the tree and the module's
                // commonTest had not compiled since. `SettingsScreenContent` is the fork's real
                // screen, which keeps the original assertion's intent.
                SettingsScreenContent(
                    onBackClick = {},
                    onThemeCardClick = {},
                    onLanguageCardClick = {},
                    onSyncAndDraftsClick = {},
                )
            }
        }
        onNodeWithTag(TestTags.Settings.SCREEN).assertIsDisplayed()
    }

    @Test
    fun aboutRowOpensAbout() = runComposeUiTest {
        var opened = 0
        setContent {
            KptTheme {
                SettingsScreenContent(
                    onBackClick = {},
                    onThemeCardClick = {},
                    onLanguageCardClick = {},
                    onSyncAndDraftsClick = {},
                    onAboutClick = { opened++ },
                )
            }
        }
        // About moved out of the drawer into Settings (2026-10-27).
        onNodeWithTag(TestTags.Settings.ABOUT_ROW).performScrollTo().performClick()
        assertEquals(1, opened)
    }

    @Test
    fun offlineSyncRowOpensOfflineSync() = runComposeUiTest {
        var opened = 0
        var drafts = 0
        setContent {
            KptTheme {
                SettingsScreenContent(
                    onBackClick = {},
                    onThemeCardClick = {},
                    onLanguageCardClick = {},
                    onSyncAndDraftsClick = { drafts++ },
                    onOfflineSyncClick = { opened++ },
                )
            }
        }
        // Its own row, separate from Sync & Drafts (2026-10-27).
        onNodeWithTag(TestTags.Settings.OFFLINE_SYNC_ROW).performScrollTo().performClick()
        assertEquals(1, opened)
        assertEquals(0, drafts)
    }
}

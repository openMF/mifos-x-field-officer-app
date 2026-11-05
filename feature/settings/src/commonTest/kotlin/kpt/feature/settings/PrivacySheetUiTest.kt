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
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runComposeUiTest
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.user.DarkThemeConfig
import kpt.core.model.user.LanguageConfig
import kpt.core.model.user.ThemeBrand
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Settings → Privacy: the row opens the sheet, each switch shows the STORED choice (never a pre-ticked default)
 * and a tap reports the flipped value. This is the only opt-in path for an install that predates onboarding.
 */
@OptIn(ExperimentalTestApi::class)
class PrivacySheetUiTest {

    private fun settings(analytics: Boolean, crash: Boolean) = UserEditableSettings(
        brand = ThemeBrand.DEFAULT,
        useDynamicColor = false,
        darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
        language = LanguageConfig.DEFAULT,
        analyticsConsent = analytics,
        crashReportingConsent = crash,
    )

    @Test
    fun privacyRowOpensThePrivacySheet() = runComposeUiTest {
        var opened = 0
        setContent {
            KptTheme {
                SettingsScreenContent(
                    onBackClick = {},
                    onThemeCardClick = {},
                    onLanguageCardClick = {},
                    onSyncAndDraftsClick = {},
                    onPrivacyClick = { opened++ },
                )
            }
        }
        onNodeWithTag(TestTags.Settings.PRIVACY_ROW).performScrollTo().performClick()
        assertEquals(1, opened)
    }

    @Test
    fun switchesShowTheStoredChoiceAndReportTheFlip() = runComposeUiTest {
        val analyticsChanges = mutableListOf<Boolean>()
        val crashChanges = mutableListOf<Boolean>()
        setContent {
            KptTheme {
                PrivacySheet(
                    // Opposite values, so a swapped binding cannot pass.
                    settingsState = ScreenState.Content(settings(analytics = false, crash = true)),
                    onDismiss = {},
                    onRetry = {},
                    onChangeAnalyticsConsent = { analyticsChanges += it },
                    onChangeCrashReportingConsent = { crashChanges += it },
                )
            }
        }
        onNodeWithTag(TestTags.Privacy.ANALYTICS_SWITCH).assertIsOff()
        onNodeWithTag(TestTags.Privacy.CRASH_REPORTS_SWITCH).assertIsOn()

        onNodeWithTag(TestTags.Privacy.ANALYTICS_SWITCH).performClick()
        onNodeWithTag(TestTags.Privacy.CRASH_REPORTS_SWITCH).performClick()

        assertEquals(listOf(true), analyticsChanges)
        assertEquals(listOf(false), crashChanges)
    }
}

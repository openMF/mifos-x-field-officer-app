/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.profile.identity

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.runComposeUiTest
import kpt.core.designsystem.theme.KptTheme
import kpt.feature.profile.TestTags
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ProfileBodyUiTest {

    private val irving = ProfileUiState(
        username = "fieldofficer",
        initials = "IZ",
        officeName = "CENTRAL",
        fullName = "IRVING ZEPEDA",
        roles = listOf("OFICIAL DE CAMPO"),
        name = NameState.Loaded,
        server = ServerSession(
            "Mifos field officer gateway",
            known = true,
            url = "https://apis.mifos.community/1.0/field/v1/",
            tenant = "mifos-bank-1",
        ),
    )

    @Test
    fun everyDeclaredSectionRendersAndTheNameReadsAsAName() = runComposeUiTest {
        setContent {
            KptTheme { ProfileBodyContent(irving, onAction = {}, onBack = {}, onServerSettings = {}, onSignOut = {}) }
        }
        listOf(
            TestTags.Profile.IDENTITY,
            TestTags.Profile.SCOPE,
            TestTags.Profile.ROLES,
            TestTags.Profile.SERVER,
            TestTags.Profile.ACTIONS,
        ).forEach { onNodeWithTag(it).performScrollTo().assertIsDisplayed() }
        // Fineract sends IRVING ZEPEDA; the screen shows a name, not a shout.
        onNodeWithTag(TestTags.Profile.FULL_NAME).performScrollTo().assertIsDisplayed().assert(hasText("Irving Zepeda"))
        onNodeWithTag(TestTags.Profile.LIVE).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun theActionsReachTheirTargets() = runComposeUiTest {
        val taps = mutableListOf<String>()
        val actions = mutableListOf<ProfileAction>()
        setContent {
            KptTheme {
                ProfileBodyContent(
                    irving,
                    onAction = { actions += it },
                    onBack = { taps += "back" },
                    onServerSettings = { taps += "server" },
                    onSignOut = { taps += "signOut" },
                )
            }
        }
        onNodeWithTag(TestTags.Profile.SERVER_SETTINGS).performScrollTo().performClick()
        onNodeWithTag(TestTags.Profile.SIGN_OUT).performScrollTo().performClick()
        onNodeWithTag(TestTags.Profile.CHANGE_PASSWORD).performScrollTo().performClick()
        assertEquals(listOf("server", "signOut"), taps)
        assertEquals(listOf<ProfileAction>(ProfileAction.OpenChangePassword), actions)
    }

    @Test
    fun offlineShowsTheBannerAndDisablesChangePasswordButNotSignOut() = runComposeUiTest {
        setContent {
            KptTheme {
                ProfileBodyContent(
                    irving.copy(online = false),
                    onAction = {},
                    onBack = {},
                    onServerSettings = {},
                    onSignOut = {},
                )
            }
        }
        onNodeWithTag(TestTags.Profile.OFFLINE_BANNER).assertIsDisplayed()
        onNodeWithTag(TestTags.Profile.CHANGE_PASSWORD).performScrollTo().assertIsNotEnabled()
        onNodeWithTag(TestTags.Profile.SIGN_OUT).performScrollTo().assertIsEnabled()
        onNodeWithTag(TestTags.Profile.LIVE).assertDoesNotExist()
    }

    @Test
    fun aFailedNameReadShowsTheErrorAndRetries() = runComposeUiTest {
        val actions = mutableListOf<ProfileAction>()
        setContent {
            KptTheme {
                ProfileBodyContent(
                    irving.copy(fullName = null, name = NameState.Failed("Server error (500).")),
                    onAction = { actions += it },
                    onBack = {},
                    onServerSettings = {},
                    onSignOut = {},
                )
            }
        }
        onNodeWithTag(TestTags.Profile.NAME_ERROR).assertIsDisplayed()
        onNodeWithTag(TestTags.Profile.RETRY).performClick()
        assertEquals(listOf<ProfileAction>(ProfileAction.Retry), actions)
    }

    @Test
    fun saveIsDisabledUntilThePasswordsMatch() = runComposeUiTest {
        setContent {
            KptTheme {
                PasswordSheetBody(PasswordForm(new = "Field#Office2026", repeat = "Field#Office20"), onAction = {})
            }
        }
        onNodeWithTag(TestTags.Profile.PASSWORD_SAVE).assertIsNotEnabled()
    }

    @Test
    fun aServerRefusalIsShownOnTheSheet() = runComposeUiTest {
        val refusal = "Password must be 12 to 50 characters long"
        setContent {
            KptTheme {
                PasswordSheetBody(
                    PasswordForm(new = "abc", repeat = "abc", error = PasswordError.Server(refusal)),
                    onAction = {},
                )
            }
        }
        onNodeWithTag(TestTags.Profile.PASSWORD_ERROR).assertIsDisplayed().assert(hasText(refusal))
        onNodeWithTag(TestTags.Profile.PASSWORD_SAVE).assertIsEnabled()
    }
}

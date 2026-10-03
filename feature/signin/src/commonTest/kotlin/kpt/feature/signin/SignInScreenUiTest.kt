/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.signin

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import kpt.core.designsystem.theme.KptTheme
import kotlin.test.Test

/**
 * RENDER-level cover for what the ViewModel cannot see.
 *
 * `SignInViewModelTest` owns the state machine. These are the things that live in the composable
 * itself, and every one of them shipped WRONG before the 2026-10-02 re-extraction — not because the
 * code disagreed with the docset, but because the docset did not describe them at all.
 */
@OptIn(ExperimentalTestApi::class)
class SignInScreenUiTest {

    private fun content(state: SignInState) = @androidx.compose.runtime.Composable {
        KptTheme {
            SignInContent(state = state, onAction = {}, onUpdateServerConfig = {})
        }
    }

    @Test
    fun theEmptyStateShowsEverythingAFirstRunUserNeeds() = runComposeUiTest {
        setContent(content(SignInState()))

        onNodeWithTag(SignInTestTags.SCREEN).assertIsDisplayed()
        onNodeWithTag(SignInTestTags.LOGO).assertIsDisplayed()
        onNodeWithTag(SignInTestTags.TITLE).assertIsDisplayed()
        onNodeWithTag(SignInTestTags.SUBMIT).assertIsDisplayed()
        // The load-bearing one. A user whose Fineract instance differs from the shipped default
        // cannot sign in until this is reachable BEFORE authenticating, and an earlier authored
        // version of this screen had dropped it entirely.
        onNodeWithTag(SignInTestTags.SERVER_CONFIG).assertIsDisplayed()
        // Nothing has failed yet, so no error affordance exists.
        onNodeWithTag(SignInTestTags.USERNAME_ERROR_ICON, useUnmergedTree = true).assertDoesNotExist()
        onNodeWithTag(SignInTestTags.PROGRESS_OVERLAY).assertDoesNotExist()
    }

    @Test
    fun theVisibilityToggleActuallyToggles() = runComposeUiTest {
        setContent(content(SignInState(password = "secret")))

        // "VisibilityOn" is the icon shown while the password is HIDDEN (tap to reveal) — the
        // shared component's own naming. Masked is the correct initial state.
        onNodeWithContentDescription(VISIBILITY_ON, useUnmergedTree = true).assertIsDisplayed()
        onNodeWithContentDescription(VISIBILITY_ON, useUnmergedTree = true).performClick()

        // THE REGRESSION THIS EXISTS FOR: the shared field draws this toggle whenever
        // keyboardType == Password, but `onPasswordToggleClick` defaults to `{}`. The screen never
        // passed one, so the icon took taps and did nothing — a dead clickable, which reads to a
        // user as a broken screen rather than a missing feature.
        onNodeWithContentDescription(VISIBILITY_OFF, useUnmergedTree = true).assertIsDisplayed()
        onNodeWithContentDescription(VISIBILITY_OFF, useUnmergedTree = true).performClick()
        onNodeWithContentDescription(VISIBILITY_ON, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun submittingCoversTheFormWithAModalOverlayAndKeepsTheWayOut() = runComposeUiTest {
        setContent(content(SignInState(isSubmitting = true)))

        // D4: v1 renders a MODAL overlay over the whole form. The previous port replaced the login
        // button with an inline spinner instead — which also stops a double submit, but leaves the
        // fields live underneath.
        onNodeWithTag(SignInTestTags.PROGRESS_OVERLAY).assertIsDisplayed()
        onNodeWithTag(SignInTestTags.SUBMIT).assertIsNotEnabled()
        // D1: the bottom-bar CTA stays reachable on purpose. A misconfigured server is exactly the
        // case where sign-in hangs, and that is the moment the user needs the way out most.
        onNodeWithTag(SignInTestTags.SERVER_CONFIG).assertIsEnabled()
    }

    @Test
    fun aFieldErrorShowsItsOwnIconAndTheOtherFieldStaysClean() = runComposeUiTest {
        setContent(content(SignInState(username = "ab", error = SignInError.UsernameTooShort)))

        // D2 + the per-field attribution the port added: v1 passed BOTH message resources whenever
        // EITHER rule failed, so a valid password still showed a password error.
        onNodeWithTag(SignInTestTags.USERNAME_ERROR_ICON, useUnmergedTree = true).assertIsDisplayed()
        onNodeWithTag(SignInTestTags.PASSWORD_ERROR_ICON, useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun aPasswordErrorReplacesTheToggleRatherThanSittingBesideIt() = runComposeUiTest {
        setContent(content(SignInState(password = "x", error = SignInError.PasswordTooShort)))

        // D3: v1's trailing slot is MUTUALLY EXCLUSIVE — toggle while clean, error icon while
        // invalid. Rendering both would give the slot two meanings at once.
        onNodeWithTag(SignInTestTags.PASSWORD_ERROR_ICON, useUnmergedTree = true).assertIsDisplayed()
        onNodeWithContentDescription(VISIBILITY_ON, useUnmergedTree = true).assertDoesNotExist()
        onNodeWithContentDescription(VISIBILITY_OFF, useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun anInvalidPasswordIsStillMasked() = runComposeUiTest {
        setContent(content(SignInState(password = "123", error = SignInError.PasswordTooShort)))

        // REGRESSION, caught on device 2026-10-03 and invisible to every other test here.
        //
        // D3 hands the trailing slot to an error icon by disabling the toggle. The shared field
        // derived its mask from `!isPasswordVisible && isPasswordToggleDisplayed`, so turning the
        // toggle off ALSO turned masking off — the password rendered in clear text at exactly the
        // moment the user is told it is wrong. Masking now derives from the keyboard type instead.
        //
        // Asserting on the RENDERED text is the whole point: the state's `password` is "123" either
        // way, so only what reaches the screen distinguishes masked from not.
        onNodeWithTag(SignInTestTags.PASSWORD, useUnmergedTree = true).assertTextContains("•••")
        onNodeWithTag(SignInTestTags.PASSWORD_ERROR_ICON, useUnmergedTree = true).assertIsDisplayed()
    }

    private companion object {
        const val VISIBILITY_ON = "VisibilityOn"
        const val VISIBILITY_OFF = "VisibilityOff"
    }
}

/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.user

import kotlinx.serialization.Serializable

@Serializable
data class UserData(
    val activeUserId: String,
    val themeBrand: ThemeBrand,
    val darkThemeConfig: DarkThemeConfig,
    val useDynamicColor: Boolean,
    val appLanguage: LanguageConfig,
    val showOnboarding: Boolean,
    val firstTimeUser: Boolean,
    val isAuthenticated: Boolean,
    val isUnlocked: Boolean,
    val passcode: String,
    val enableScreenCapture: Boolean,
    val isPasscodeEnabled: Boolean,
    val isBiometricsEnabled: Boolean,
) {
    companion object {
        val DEFAULT = UserData(
            activeUserId = "",
            passcode = "1234",
            themeBrand = ThemeBrand.DEFAULT,
            darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
            useDynamicColor = false,
            appLanguage = LanguageConfig.DEFAULT,
            // FORK DIVERGENCE from kmp-project-template (S1, 2026-10-01).
            //
            // The template demo ships no sign-in, so defaulting a fresh install to authenticated is
            // right FOR IT — see the matching note in UserPreferencesRepositoryImpl.clearUserData().
            // This fork has real Fineract authentication, and with `true` a brand-new install walked
            // straight into the authenticated graph with no credentials: RootNavViewModel's gate read
            // firstTimeUser=false -> isAuthenticated=true -> passcode non-empty -> isUnlocked=true ->
            // UserUnlocked. Verified on device: a fresh install rendered the empty Home shell and the
            // sign-in screen was unreachable.
            //
            // ONLY `isAuthenticated` flips. `isUnlocked` must stay true: `updateFineractUser` sets
            // isAuthenticated alone, so a false default would leave a just-signed-in user matching the
            // gate's `else -> UserLocked` branch, and this fork has no unlock screen to show — the
            // original's passcode UI is an Android-only library with no multiplatform form, so S1
            // routes UserLocked to sign-in. That combination would loop login -> locked -> login.
            isAuthenticated = false,
            isUnlocked = true,
            isPasscodeEnabled = false,
            isBiometricsEnabled = false,
            showOnboarding = false,
            firstTimeUser = false,
            enableScreenCapture = false,
        )
    }
}

/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.datastore.prefs

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kpt.core.model.user.DarkThemeConfig
import kpt.core.model.user.LanguageConfig
import kpt.core.model.user.ThemeBrand
import kpt.core.model.user.UserData

/**
 * Repository interface for managing user preferences with reactive
 * capabilities.
 *
 * This interface provides reactive access to user preferences including
 * theme settings, dark mode configuration, and dynamic color preferences.
 */
interface UserPreferencesRepository {

    val userData: StateFlow<UserData>

    /**
     * The stored credential, or null when signed out. Synchronous read for a caller that already
     * has one in hand; prefer [observeAuthToken] when the value can change under you.
     */
    val authToken: String?

    /**
     * The credential as a stream, re-emitting on sign-in and sign-out.
     *
     * This is what wires `Authorization` automatically: `AuthHeaderBridge` collects it and writes
     * the formatted header into `RuntimeHeaderStore`, so a token restored from disk at startup is in
     * place before the first request and one obtained at login lands the moment it is written.
     */
    val observeAuthToken: Flow<String?>

    val passcode: String

    val observeLanguage: Flow<LanguageConfig>

    val observeDarkThemeConfig: Flow<DarkThemeConfig>

    val observeDynamicColorPreference: Flow<Boolean>

    val observeScreenCapturePreference: Flow<Boolean>

    /**
     * Persist [token] into the ENCRYPTED store, or clear it when null.
     *
     * Call on sign-in with the value the auth endpoint returned (for Basic, the base64 of
     * `user:password`; for OAuth, the access token) and on sign-out with null. Everything downstream
     * — the header, its wire-format prefix — follows from the access point's declared `auth:`.
     */
    suspend fun setAuthToken(token: String?)

    suspend fun setLanguage(language: LanguageConfig)

    suspend fun setThemeBrand(themeBrand: ThemeBrand)

    suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig)

    suspend fun setDynamicColorPreference(useDynamicColor: Boolean)

    suspend fun setIsAuthenticated(isAuthenticated: Boolean)

    suspend fun setIsUnlocked(isUnlocked: Boolean)

    suspend fun setIsPasscodeEnabled(isPasscodeEnabled: Boolean)

    suspend fun setIsBiometricsEnabled(isBiometricsEnabled: Boolean)

    suspend fun setShowOnboarding(showOnboarding: Boolean)

    suspend fun setFirstTimeState(firstTimeState: Boolean)

    suspend fun setPasscode(passcode: String)

    suspend fun setScreenCapturePreference(isScreenCaptureEnabled: Boolean)

    suspend fun clearUserData()
}

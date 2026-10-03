/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.user.impl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kpt.core.base.data.annotation.FromStore
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.data.user.UserDataRepository
import kpt.core.datastore.prefs.UserPreferencesRepository
import kpt.core.model.user.DarkThemeConfig
import kpt.core.model.user.LanguageConfig
import kpt.core.model.user.ThemeBrand
import kpt.core.model.user.UserData
import kpt.core.store.config.AppCacheKeys
import kpt.core.store.config.AppStoreIds
import org.mobilenativefoundation.store.store5.Store

@RepositoryBinding(binds = UserDataRepository::class)
class UserDataRepositoryImpl(
    private val preferencesRepository: UserPreferencesRepository,
    @FromStore(AppStoreIds.UserData) private val userDataStore: Store<Unit, UserData>,
) : UserDataRepository {
    override val userData: StateFlow<UserData>
        get() = preferencesRepository.userData

    override fun userDataStream(scope: CoroutineScope): ScreenDataStream<UserData> =
        userDataStore.asScreenStream(
            key = Unit,
            cacheKey = AppCacheKeys.UserData.KEY,
            scope = scope,
            fetchPolicy = FetchPolicy.CACHE_ONLY,
        )

    override val authToken: String?
        get() = preferencesRepository.authToken

    override val passcode: String
        get() = preferencesRepository.passcode

    override val observeLanguage: Flow<LanguageConfig>
        get() = preferencesRepository.observeLanguage

    override val observeDarkThemeConfig: Flow<DarkThemeConfig>
        get() = preferencesRepository.observeDarkThemeConfig

    override val observeDynamicColorPreference: Flow<Boolean>
        get() = preferencesRepository.observeDynamicColorPreference

    override val observeScreenCapturePreference: Flow<Boolean>
        get() = preferencesRepository.observeScreenCapturePreference

    override suspend fun setLanguage(language: LanguageConfig) = preferencesRepository.setLanguage(language)

    override suspend fun setThemeBrand(themeBrand: ThemeBrand) = preferencesRepository.setThemeBrand(themeBrand)

    override suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) =
        preferencesRepository.setDarkThemeConfig(darkThemeConfig)

    override suspend fun setDynamicColorPreference(useDynamicColor: Boolean) =
        preferencesRepository.setDynamicColorPreference(useDynamicColor)

    override suspend fun setIsAuthenticated(isAuthenticated: Boolean) =
        preferencesRepository.setIsAuthenticated(isAuthenticated)

    override suspend fun setIsUnlocked(isUnlocked: Boolean) = preferencesRepository.setIsUnlocked(isUnlocked)

    override suspend fun setIsPasscodeEnabled(isPasscodeEnabled: Boolean) =
        preferencesRepository.setIsPasscodeEnabled(isPasscodeEnabled)

    override suspend fun setIsBiometricsEnabled(isBiometricsEnabled: Boolean) =
        preferencesRepository.setIsBiometricsEnabled(isBiometricsEnabled)

    override suspend fun setShowOnboarding(showOnboarding: Boolean) =
        preferencesRepository.setShowOnboarding(showOnboarding)

    override suspend fun setFirstTimeState(firstTimeState: Boolean) =
        preferencesRepository.setFirstTimeState(firstTimeState)

    override suspend fun setPasscode(passcode: String) = preferencesRepository.setPasscode(passcode)

    override suspend fun clearUserData() = preferencesRepository.clearUserData()
}

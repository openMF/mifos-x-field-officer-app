/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.settings

import io.github.mobilebytelabs.kmptoolkit.firebase.analytics.AnalyticsEvent
import io.github.mobilebytelabs.kmptoolkit.firebase.analytics.AnalyticsHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.screen.ExperimentalScreenDataStreamTestingApi
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.data.user.UserDataRepository
import kpt.core.model.user.DarkThemeConfig
import kpt.core.model.user.LanguageConfig
import kpt.core.model.user.ThemeBrand
import kpt.core.model.user.UserData
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Locks the [SettingsViewModel] contract after the `SettingsUiState` → `ScreenState` migration.
 *
 * Two things are load-bearing and neither was expressible before:
 *
 * 1. **The ERROR arm survives the projection.** The old hand-rolled `Loading | Success` envelope had
 *    no error case at all, so a failed preferences read left the dialog on "Loading…" forever with
 *    nothing to retry. `errorSurvivesTheProjection` + `retryReachesTheStream` are the regression
 *    lock — delete the error arm from `mapContent` and they fail.
 * 2. **Field mapping.** `UserEditableSettings` is four fields of the same two enum-ish shapes; a
 *    transposition (darkThemeConfig ↔ brand) type-checks fine and silently shows the user the wrong
 *    setting. Each field is asserted against a DISTINCT non-default value so a swap cannot pass.
 *
 * Writes are asserted to land on the repository's own typed setters — routing a single preference
 * through a whole-`UserData` blob write is the anti-pattern the VM doc-comment calls out.
 */
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalScreenDataStreamTestingApi::class)
class SettingsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private class RecordingAnalytics : AnalyticsHelper {
        val events = mutableListOf<String>()
        override fun logEvent(event: AnalyticsEvent) {
            events += event.type
        }
    }

    /**
     * Only the members [SettingsViewModel] actually touches carry behaviour; the rest of the
     * (large) preferences surface is unused here and throws, so a VM that starts reaching for a
     * different member fails loudly instead of silently reading a default.
     */
    private class FakeUserDataRepository(
        private val initial: UserData,
        private val stateFlowOverride: Flow<ScreenState<UserData>>? = null,
        private val refreshTrigger: MutableSharedFlow<Unit> = MutableSharedFlow(extraBufferCapacity = 1),
    ) : UserDataRepository {
        val current = MutableStateFlow(initial)
        val brandWrites = mutableListOf<ThemeBrand>()
        val darkWrites = mutableListOf<DarkThemeConfig>()
        val dynamicColorWrites = mutableListOf<Boolean>()
        val languageWrites = mutableListOf<LanguageConfig>()

        override val userData: StateFlow<UserData> get() = current

        override fun userDataStream(scope: CoroutineScope): ScreenDataStream<UserData> =
            screenDataStreamForTesting(
                state = stateFlowOverride ?: MutableStateFlow(ScreenState.Content(initial)),
                refreshTrigger = refreshTrigger,
                forceFreshTrigger = refreshTrigger,
            )

        override suspend fun setThemeBrand(themeBrand: ThemeBrand) {
            brandWrites += themeBrand
        }
        override suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
            darkWrites += darkThemeConfig
        }
        override suspend fun setDynamicColorPreference(useDynamicColor: Boolean) {
            dynamicColorWrites += useDynamicColor
        }
        override suspend fun setLanguage(language: LanguageConfig) {
            languageWrites += language
        }

        override val authToken: String? get() = unused()
        override val passcode: String get() = unused()
        override val observeLanguage: Flow<LanguageConfig> get() = unused()
        override val observeDarkThemeConfig: Flow<DarkThemeConfig> get() = unused()
        override val observeDynamicColorPreference: Flow<Boolean> get() = unused()
        override val observeScreenCapturePreference: Flow<Boolean> get() = unused()
        override suspend fun setIsAuthenticated(isAuthenticated: Boolean) = unused()
        override suspend fun setIsUnlocked(isUnlocked: Boolean) = unused()
        override suspend fun setIsPasscodeEnabled(isPasscodeEnabled: Boolean) = unused()
        override suspend fun setIsBiometricsEnabled(isBiometricsEnabled: Boolean) = unused()
        override suspend fun setShowOnboarding(showOnboarding: Boolean) = unused()
        override suspend fun setFirstTimeState(firstTimeState: Boolean) = unused()
        override suspend fun setPasscode(passcode: String) = unused()
        override suspend fun clearUserData() = unused()

        private fun unused(): Nothing =
            throw AssertionError("SettingsViewModel must not touch this member")
    }

    /**
     * Every field is a DISTINCT non-default value — the point is that a transposed mapping cannot
     * accidentally produce the expected result.
     */
    private val seed = UserData(
        activeUserId = "u1",
        themeBrand = ThemeBrand.ANDROID,
        darkThemeConfig = DarkThemeConfig.DARK,
        useDynamicColor = true,
        appLanguage = LanguageConfig.HINDI,
        showOnboarding = false,
        firstTimeUser = false,
        isAuthenticated = true,
        isUnlocked = true,
        passcode = "0000",
        enableScreenCapture = false,
        isPasscodeEnabled = true,
        isBiometricsEnabled = true,
    )

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun projectsEachPreferenceOntoItsOwnField() = runTest(dispatcher) {
        val vm = SettingsViewModel(FakeUserDataRepository(seed), RecordingAnalytics())

        val state = vm.settingsState.first { it !is ScreenState.Loading }
        val settings = assertIs<ScreenState.Content<UserEditableSettings>>(state).data

        assertEquals(ThemeBrand.ANDROID, settings.brand)
        assertEquals(DarkThemeConfig.DARK, settings.darkThemeConfig)
        assertEquals(true, settings.useDynamicColor)
        assertEquals(LanguageConfig.HINDI, settings.language)
    }

    @Test
    fun errorSurvivesTheProjection() = runTest(dispatcher) {
        // The whole reason SettingsUiState was removed: this state was previously unrepresentable,
        // so a failed preferences read rendered as a permanent "Loading…".
        val boom = IllegalStateException("prefs read failed")
        val vm = SettingsViewModel(
            FakeUserDataRepository(seed, stateFlowOverride = MutableStateFlow(ScreenState.Error(boom))),
            RecordingAnalytics(),
        )

        val state = vm.settingsState.first { it !is ScreenState.Loading }
        assertEquals(boom, assertIs<ScreenState.Error>(state).error)
    }

    @Test
    fun retryReachesTheStream() = runTest(dispatcher) {
        // Error is only recoverable if the retry affordance actually re-runs the read.
        val trigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
        val seen = mutableListOf<Unit>()
        val repo = FakeUserDataRepository(seed, refreshTrigger = trigger)
        val vm = SettingsViewModel(repo, RecordingAnalytics())
        backgroundScope.launch { trigger.collect { seen += it } }
        // runCurrent(), not advanceUntilIdle(): the latter leaves the collector unstarted, so the
        // trigger has zero subscribers and a replay-0 tryEmit is silently dropped.
        runCurrent()

        vm.onRetry()
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(seen.isNotEmpty(), "onRetry() must emit through the stream's refresh trigger")
    }

    @Test
    fun eachActionLandsOnItsOwnTypedSetter() = runTest(dispatcher) {
        // Distinct arms: a collapsed `when` that routed UpdateLanguage to setThemeBrand would
        // type-check (both are single-arg enum setters) and silently change the wrong preference.
        val repo = FakeUserDataRepository(seed)
        val vm = SettingsViewModel(repo, RecordingAnalytics())

        vm.updateThemeBrand(ThemeBrand.DEFAULT)
        vm.updateDarkThemeConfig(DarkThemeConfig.LIGHT)
        vm.updateDynamicColorPreference(false)
        vm.updateLanguage(LanguageConfig.SPANISH)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf(ThemeBrand.DEFAULT), repo.brandWrites)
        assertEquals(listOf(DarkThemeConfig.LIGHT), repo.darkWrites)
        assertEquals(listOf(false), repo.dynamicColorWrites)
        assertEquals(listOf(LanguageConfig.SPANISH), repo.languageWrites)
    }

    @Test
    fun everyPreferenceWriteIsAnalyticsTracked() = runTest(dispatcher) {
        val analytics = RecordingAnalytics()
        val vm = SettingsViewModel(FakeUserDataRepository(seed), analytics)

        vm.updateThemeBrand(ThemeBrand.DEFAULT)
        vm.updateDarkThemeConfig(DarkThemeConfig.LIGHT)
        vm.updateDynamicColorPreference(false)
        vm.updateLanguage(LanguageConfig.SPANISH)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(
            listOf(
                "theme_brand_changed",
                "dark_theme_config_changed",
                "dynamic_color_preference_changed",
                "language_changed",
            ),
            analytics.events,
        )
    }
}

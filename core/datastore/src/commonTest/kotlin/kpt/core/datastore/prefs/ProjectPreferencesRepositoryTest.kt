/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.datastore.prefs

import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.test.runTest
import kpt.core.base.common.manager.DispatcherManager
import kpt.core.model.objects.users.User
import kpt.core.model.user.DarkThemeConfig
import kpt.core.model.utils.ServerConfig
import kpt.core.model.utils.getInstanceUrl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The fork seam's own contract: Fineract preferences persist, and the framework's token slot never
 * disagrees with the user record it was derived from.
 */
class ProjectPreferencesRepositoryTest {

    private val plain = MapSettings()
    private val secure = MapSettings()

    private val dispatchers = object : DispatcherManager {
        override val default: CoroutineDispatcher = Dispatchers.Unconfined
        override val main: MainCoroutineDispatcher = Dispatchers.Main
        override val io: CoroutineDispatcher = Dispatchers.Unconfined
        override val unconfined: CoroutineDispatcher = Dispatchers.Unconfined
        override val appScope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined)
    }

    private fun repo() = ProjectPreferencesRepositoryImpl(
        delegate = UserPreferencesRepositoryImpl(plain, secure, dispatchers),
        plainSettings = plain,
        secureSettings = secure,
        dispatcher = dispatchers,
    )

    private val user = User(
        username = "officer",
        userId = 7,
        base64EncodedAuthenticationKey = "a2V5",
        isAuthenticated = true,
        officeId = 3,
    )

    @Test
    fun serverConfigSurvivesARestart() = runTest {
        repo().updateServerConfig(ServerConfig.LOCALHOST)

        // A fresh instance reads from the store, which is what a relaunch does.
        val reloaded = repo()
        assertEquals(ServerConfig.LOCALHOST, reloaded.serverConfig.value)
        assertEquals(ServerConfig.LOCALHOST.getInstanceUrl(), reloaded.instanceUrl)
    }

    @Test
    fun signingInDerivesTheAuthHeaderAndSyncsTheFrameworkToken() = runTest {
        val repo = repo()
        repo.updateFineractUser(user)

        // `authHeader` is the ready-formatted header for a direct consumer…
        assertEquals("Basic a2V5", repo.authHeader)
        // …while the FRAMEWORK slot holds the RAW credential. AuthHeaderBridge runs it through the
        // access point's declared scheme (`auth: basic` → `AuthScheme.BASIC.format`), which prepends
        // "Basic " itself, so storing a pre-prefixed value here put `Basic Basic <token>` on the wire
        // and 401'd every authenticated call. This assertion previously encoded that defect.
        assertEquals("a2V5", repo.authToken)
        assertTrue(repo.userData.value.isAuthenticated)
        // Signing in must also UNLOCK. RootNavViewModel sends an authenticated-but-locked user to
        // `UserLocked`, which this fork renders as sign-in — so leaving this false made the first
        // sign-in after a sign-out (which sets isUnlocked = false) bounce back to the login screen.
        assertTrue(repo.userData.value.isUnlocked)
    }

    @Test
    fun signingInAfterASignOutReachesTheAuthenticatedState() = runTest {
        val repo = repo()
        repo.updateFineractUser(user)
        repo.clearFineractUser()
        repo.clearUserData() // what the Profile screen's sign-out does

        repo.updateFineractUser(user)

        // Both halves of RootNavViewModel's gate, in one place: the loop needed only one of them.
        assertTrue(repo.userData.value.isAuthenticated)
        assertTrue(repo.userData.value.isUnlocked)
    }

    @Test
    fun signingOutClearsIdentityButKeepsDevicePreferences() = runTest {
        val repo = repo()
        repo.setDarkThemeConfig(DarkThemeConfig.DARK)
        repo.updateFineractUser(user)

        repo.clearFineractUser()

        val reloaded = repo()
        assertNull(reloaded.fineractUser.value.username)
        assertEquals("", reloaded.authHeader)
        assertNull(reloaded.authToken)
        assertFalse(reloaded.userData.value.isAuthenticated)
        // Theme is a device preference, not part of the session — it must survive sign-out.
        assertEquals(DarkThemeConfig.DARK, reloaded.userData.value.darkThemeConfig)
    }

    @Test
    fun forkKeysAreNamespacedSoTheyCannotCollideWithAFrameworkPreference() = runTest {
        repo().updateServerConfig(ServerConfig.LOCALHOST)
        repo().updateFineractUser(user)

        assertTrue(plain.keys.any { it.startsWith("fineract.") }, "server config key not namespaced")
        assertTrue(secure.keys.any { it.startsWith("fineract.") }, "user key not namespaced")
    }
}

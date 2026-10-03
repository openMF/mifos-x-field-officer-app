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

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import com.russhwolf.settings.serialization.decodeValueOrNull
import com.russhwolf.settings.serialization.encodeValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kpt.core.base.common.manager.DispatcherManager
import kpt.core.model.objects.users.User
import kpt.core.model.utils.ServerConfig
import kpt.core.model.utils.getInstanceUrl

/**
 * Fork preferences, delegating every framework preference to the template's implementation.
 *
 * The keys are namespaced `fineract.*` per the seam's contract, so a future framework preference
 * cannot collide with one of these. That does mean they do not match the pre-port app's `user_details`
 * / `server_config` keys — deliberate: this port rebuilds the local database too, so an upgrading
 * install re-authenticates regardless, and inheriting ambiguous top-level keys into the shared
 * Settings store would be the thing that is hard to undo later.
 *
 * The user record goes in `secureSettings` because it carries the encoded authentication key;
 * the server choice is not a secret and goes in `plainSettings`.
 */
@OptIn(ExperimentalSerializationApi::class, ExperimentalSettingsApi::class)
class ProjectPreferencesRepositoryImpl(
    val delegate: UserPreferencesRepository,
    val plainSettings: Settings,
    val secureSettings: Settings,
    val dispatcher: DispatcherManager,
) : ProjectPreferencesRepository, UserPreferencesRepository by delegate {

    private val _serverConfig = MutableStateFlow(
        plainSettings.decodeValueOrNull(serializer = ServerConfig.serializer(), key = SERVER_CONFIG) ?: ServerConfig.DEFAULT,
    )
    override val serverConfig: StateFlow<ServerConfig> = _serverConfig.asStateFlow()

    private val _fineractUser = MutableStateFlow(
        secureSettings.decodeValueOrNull(serializer = User.serializer(), key = FINERACT_USER) ?: User(),
    )
    override val fineractUser: StateFlow<User> = _fineractUser.asStateFlow()

    override val instanceUrl: String
        get() = _serverConfig.value.getInstanceUrl()

    override val authHeader: String
        get() = _fineractUser.value.base64EncodedAuthenticationKey?.let { "Basic $it" }.orEmpty()

    override suspend fun updateServerConfig(serverConfig: ServerConfig) {
        withContext(dispatcher.io) {
            plainSettings.encodeValue(
                serializer = ServerConfig.serializer(),
                key = SERVER_CONFIG,
                value = serverConfig,
            )
            _serverConfig.value = serverConfig
        }
    }

    override suspend fun updateFineractUser(user: User) {
        withContext(dispatcher.io) {
            secureSettings.encodeValue(serializer = User.serializer(), key = FINERACT_USER, value = user)
            _fineractUser.value = user
            // Keep the framework's token slot in step — the network layer reads it from there, and a
            // stale token outliving the user it belongs to is the failure this pairing prevents.
            // The RAW credential — no "Basic " prefix. `setAuthToken` is documented to take "the value
            // the auth endpoint returned (for Basic, the base64 of user:password)", and the wire
            // format comes from the access point's declared `auth: basic`: AuthHeaderBridge runs it
            // through `AuthScheme.BASIC.format`, which prepends "Basic " itself. Prefixing here sent
            // `Authorization: Basic Basic <token>` and every authenticated call answered 401 — with
            // sign-in itself unaffected, since that request is anonymous. Observed on device
            // 2026-10-01: login succeeded, then the Clients list 401'd.
            delegate.setAuthToken(user.base64EncodedAuthenticationKey)
            delegate.setIsAuthenticated(user.isAuthenticated)
            // …and UNLOCKED. `RootNavViewModel` routes an authenticated-but-locked user to
            // `UserLocked`, which this fork renders as the sign-in screen, because the original's
            // passcode UI is an Android-only library with no multiplatform form (see
            // `UserData.DEFAULT`). `clearUserData()` sets `isUnlocked = false` on sign-out — correct
            // for the template, which has an unlock screen — so without this line the FIRST sign-in
            // after a sign-out bounced straight back: observed on device 2026-10-01 as
            // AuthenticatedGraphRoute → SignInRoute 52 ms apart, with a successful 200 in the log.
            //
            // Provisional, and deliberately paired with the sign-out that clears it: when the
            // passcode/unlock flow is migrated (S7) this becomes that screen's job and this line goes.
            if (user.isAuthenticated) delegate.setIsUnlocked(true)
        }
    }

    override suspend fun clearFineractUser() {
        withContext(dispatcher.io) {
            secureSettings.encodeValue(serializer = User.serializer(), key = FINERACT_USER, value = User())
            _fineractUser.value = User()
            delegate.setAuthToken(null)
            delegate.setIsAuthenticated(false)
        }
    }

    private companion object {
        const val SERVER_CONFIG = "fineract.server_config"
        const val FINERACT_USER = "fineract.user"
    }
}

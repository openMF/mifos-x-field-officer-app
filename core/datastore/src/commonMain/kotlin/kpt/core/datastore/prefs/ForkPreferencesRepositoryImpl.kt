/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.datastore.prefs

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kpt.core.base.common.manager.DispatcherManager
import kpt.core.base.datastore.infra.getJsonOrNull
import kpt.core.base.datastore.infra.putJson
import kpt.core.model.objects.users.User

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
class ForkPreferencesRepositoryImpl(
    /** The template's preferences repository this one extends. */
    val delegate: UserPreferencesRepository,
    /** Plain settings, for non-sensitive values. */
    val plainSettings: Settings,
    /** Encrypted settings, for credentials. */
    val secureSettings: Settings,
    /** Dispatchers for settings I/O. */
    val dispatcher: DispatcherManager,
) : ForkPreferencesRepository, UserPreferencesRepository by delegate {

    // JSON, not `encodeValue`: that layout read `permissions` (and `roles`) back EMPTY on every cold
    // start, so every permission-gated surface — the whole Home dashboard — vanished after a restart.
    private val _fineractUser = MutableStateFlow(
        secureSettings.getJsonOrNull(User.serializer(), FINERACT_USER) ?: User(),
    )

    init {
        // A session saved in that lossy layout has an empty permission list and no way to recover it
        // (Fineract returns permissions only at sign-in, and the password is never stored), so it
        // starts signed out. Once: the next sign-in saves it in the JSON layout. Cached data stays.
        if (_fineractUser.value.isAuthenticated && _fineractUser.value.permissions.isEmpty()) {
            dispatcher.appScope.launch { clearFineractUser() }
            _fineractUser.value = User()
        }
    }
    override val fineractUser: StateFlow<User> = _fineractUser.asStateFlow()

    override val authHeader: String
        get() = _fineractUser.value.base64EncodedAuthenticationKey?.let { "Basic $it" }.orEmpty()

    override suspend fun updateFineractUser(user: User) {
        withContext(dispatcher.io) {
            secureSettings.putJson(User.serializer(), FINERACT_USER, user)
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
            secureSettings.putJson(User.serializer(), FINERACT_USER, User())
            _fineractUser.value = User()
            delegate.setAuthToken(null)
            delegate.setIsAuthenticated(false)
        }
    }

    private companion object {
        const val FINERACT_USER = "fineract.user"
    }
}

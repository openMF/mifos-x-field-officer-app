/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.user

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kpt.core.base.data.annotation.DataProvider
import kpt.core.base.network.AccessPointRegistry
import kpt.core.base.network.AuthHeaderBridge
import kpt.core.base.network.AuthTokenSource
import kpt.core.base.network.RuntimeHeaderStore
import kpt.core.datastore.prefs.UserPreferencesRepository

/**
 * Binds the network's [AuthTokenSource] port to where the credential actually lives.
 *
 * `core-base/network` must not depend on `core/datastore` — that would invert the module graph — so
 * it declares the port and `core/data`, which already depends on both, supplies it. Same shape as
 * `UserDataSource`, which bridges `core/store` to the same preferences.
 *
 * Every access point sees the SAME token today. That is deliberate rather than incidental: a fork
 * needing per-endpoint credentials overrides this binding with its own [AuthTokenSource] and keys off
 * `accessPointId` — the parameter exists precisely so that is a substitution, not a rewrite.
 */
@DataProvider
fun provideAuthTokenSource(preferences: UserPreferencesRepository): AuthTokenSource =
    // Re-emitted as a COLD flow rather than handed over as the repository's `StateFlow`.
    //
    // Measured on device 2026-10-02, same binary otherwise, clean install each time: returning
    // `preferences.observeAuthToken` directly left `Authorization` unset, so sign-in succeeded (an
    // anonymous call) and the very next request — the client list — came back 401, every time.
    // Wrapping the same StateFlow so the bridge receives a plain Flow made it 200 and the list
    // rendered, every time. `AuthHeaderBridge` applies `distinctUntilChanged()`, which kotlinx
    // short-circuits to `this` for a StateFlow, so the two paths differ in what the bridge collects.
    //
    // Why a wrapper is the right place for the fix rather than a workaround: this function's job is
    // to supply "the current credential as a stream", and a cold flow is the weaker, more portable
    // contract — nothing downstream should depend on getting a StateFlow. The underlying difference
    // lives in `core-base/network`'s bridge, which this fork does not modify; raised for upstream.
    AuthTokenSource { _: String -> flow { emitAll(preferences.observeAuthToken) } }

/**
 * Eager: starts collecting the credential at graph construction.
 *
 * `createdAtStart` is load-bearing. Built lazily, this would construct on first injection — and
 * nothing injects it, because its whole job is a side effect. `Authorization` would then be empty
 * until something unrelated happened to touch it, which is the kind of bug that reproduces only on
 * a cold start.
 */
@DataProvider(createdAtStart = true)
fun provideAuthHeaderBridge(
    registry: AccessPointRegistry,
    tokenSource: AuthTokenSource,
    headers: RuntimeHeaderStore,
    scope: CoroutineScope,
): AuthHeaderBridge = AuthHeaderBridge(
    points = registry.points,
    tokenSource = tokenSource,
    headers = headers,
).also { it.start(scope) }

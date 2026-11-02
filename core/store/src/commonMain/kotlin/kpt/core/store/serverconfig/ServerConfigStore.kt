/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:Suppress("MatchingDeclarationName")

package kpt.core.store.serverconfig

import kotlinx.coroutines.flow.Flow
import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.model.utils.ServerConfig
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store

/**
 * The read PORT for the server configuration, which lives in `core/datastore` — a layer
 * `core/store` does not depend on. `core/data` binds it, as it does `UserDataSource`.
 */
fun interface ServerConfigSource {
    fun observe(): Flow<ServerConfig>
}

/**
 * OFFLINE_LOCAL_ONLY store over the Fineract server configuration — the same shape as
 * `provideUserDataStore`: there is no network, so the store streams the preference itself.
 *
 * `logout = false`: the server address is DEVICE configuration, not session data. "Sign out and
 * clear data" purging it would leave the next officer unable to sign in at all until they re-typed
 * the instance URL. Writes go through `ServerConfigRepository.updateServerConfig`, which writes the
 * preference and makes [ServerConfigSource.observe] re-emit — so the writer here is never invoked.
 */
// logout-exempt: the server address is device configuration, not session data
@StoreProvider(id = "serverConfig", logout = false)
@CacheKey(name = "KEY", key = "serverConfig")
fun provideServerConfigStore(source: ServerConfigSource): Store<Unit, ServerConfig> =
    StoreFactory.createOfflineStore(
        sourceOfTruth = SourceOfTruth.of(
            reader = { _: Unit -> source.observe() },
            writer = { _: Unit, _: ServerConfig -> Unit },
        ),
    )

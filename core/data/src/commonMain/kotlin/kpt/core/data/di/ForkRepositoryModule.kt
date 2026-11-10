/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kpt.core.base.network.NetworkConfigRepository
import kpt.core.base.network.UnauthorizedHandler
import kpt.core.data.auth.AuthCommandRepository
import kpt.core.data.auth.impl.SignOutOnRejectedSession
import kpt.core.data.offline.OfflineSyncScheduler
import kpt.core.data.offline.work.WorkManagerOfflineSyncScheduler
import kpt.core.datastore.prefs.ForkPreferencesRepository
import kpt.core.network.mifos.server.toServerConfig
import kpt.core.store.serverconfig.ServerConfigSource
import kpt.core.store.session.SessionSource
import org.koin.dsl.module

/**
 * THE FORK'S repository DI seam. Empty on the neutral template — this is yours to fill.
 *
 * It lives outside `demo/` on purpose, so `scripts/remove-demo.sh` leaves it standing: a fork that
 * runs the customizer (which strips the demo BY DEFAULT — "forking = starting clean") keeps this file
 * and can wire DI immediately. Its demo counterpart is deleted by that same strip.
 *
 * Example:
 * ```
 * single<MyRepository> { MyRepositoryImpl(get(), get()) }
 * ```
 */
val ForkRepositoryModule = module {
    // The read PORT `core/store`'s server-config store declares (it cannot depend on
    // core/datastore) — supplied here, as `DataModule` supplies `UserDataSource`.
    // The server is the MAIN runtime network config (core-base NetworkConfigRepository), as the
    // server-config screen's five fields. One persisted source for the screen and the HTTP client.
    single<ServerConfigSource> {
        val networkConfigs = get<NetworkConfigRepository>()
        ServerConfigSource {
            networkConfigs.configs.map {
                requireNotNull(networkConfigs.mainConfig()) { "app-profile marks no main access point" }
                    .toServerConfig()
            }
        }
    }
    single<SessionSource> { SessionSource { get<ForkPreferencesRepository>().fineractUser } }
    // RecentRecordsRepositoryImpl, OfflineSyncRepositoryImpl and its OfflineSyncEngine are @RepositoryBinding:
    // each engine store is the SAME qualified store the record's screen reads (its @FromStore), which is
    // what makes "synced" mean "opens offline".
    single<OfflineSyncScheduler> { WorkManagerOfflineSyncScheduler(workManager = get()) }
    // A rejected session (401 on a credentialed request) ends it — see SignOutOnRejectedSession.
    // `restApi` picks this up for every REST client; the auth repository is resolved lazily because
    // its own API client is one of them.
    single<UnauthorizedHandler> {
        val koin = getKoin()
        val preferences = get<ForkPreferencesRepository>()
        SignOutOnRejectedSession(
            auth = { koin.get<AuthCommandRepository>() },
            session = { preferences.fineractUser.value },
            scope = get<CoroutineScope>(),
        )
    }
}

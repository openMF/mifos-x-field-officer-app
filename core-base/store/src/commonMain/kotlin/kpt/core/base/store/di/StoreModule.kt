/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.store.di

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkMonitor
import kotlin.time.Clock
import kpt.core.base.store.mutation.DefaultMutationGateway
import kpt.core.base.store.mutation.MutationGateway
import kpt.core.base.store.mutation.conflict.ConflictInbox
import kpt.core.base.store.mutation.conflict.impl.RoomConflictInbox
import kpt.core.base.store.screen.ScreenStreamContext
import org.koin.dsl.module

/**
 * Koin module providing base Store infrastructure.
 *
 * Consumer apps should include this module and add their own store bindings
 * in their `core/data` DI module using `StoreFactory` to create store instances.
 *
 * Also provides the framework write-SoT: the [MutationGateway] (the single write door every repo
 * routes mutations through) and its Room-backed [ConflictInbox]. Requires a `NetworkMonitor` (from
 * `cmp-network-monitor`) and the framework `ConflictDao` (from `DatabaseModule`) on the graph.
 */
val StoreModule = module {
    // Room-backed conflict inbox surfaced in Settings.
    single<ConflictInbox> {
        RoomConflictInbox(dao = get(), now = { Clock.System.now().toEpochMilliseconds() })
    }
    // The single write door — composes MutableStore.write + the conflict inbox + connectivity.
    single<MutationGateway> {
        DefaultMutationGateway(
            isOnline = { get<NetworkMonitor>().isOnline.value },
            conflictInbox = get(),
        )
    }
    // The read-path infra bundle every repository's `store.asScreenStream(...)` needs — resolved by
    // `asScreenStream` itself (Koin default), so it is NOT threaded through repository constructors.
    single { ScreenStreamContext(networkMonitor = get(), fetchedAtRepository = get()) }
}

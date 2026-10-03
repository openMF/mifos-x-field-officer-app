/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.di

import kpt.core.base.store.infra.DraftInventory
import kpt.core.base.store.infra.StoreCacheManager
import kpt.core.base.store.infra.impl.DraftInventoryImpl
import kpt.core.base.store.infra.impl.StoreCacheManagerImpl
import org.koin.core.module.Module
import org.koin.dsl.module
import kpt.core.base.store.di.StoreModule as CoreBaseStoreModule

/**
 * App-level Store wiring — FRAMEWORK ONLY.
 *
 * ## Why this file holds no store bindings
 * It used to carry ~30 hand-written `single(...) { provideXStore(...) }` blocks plus a second
 * hand-kept list registering those stores for logout purge, inside a `demo:` fence in a FORK-OWNED
 * file. Two problems followed. A fork never received upstream fixes to the framework wiring below,
 * because the whole file was excluded from sync to protect the fork's bindings. And the two lists
 * could disagree — a store bound but not registered survives sign-out and shows the previous user's
 * cached rows to the next person on a shared device.
 *
 * Both now come from one `@StoreProvider(logout = ...)` on the provider function, which `store-ksp`
 * turns into [GeneratedStoreBindings]: the binding AND the purge, from a single fact. This file is
 * template-owned again and a sync can blind-copy it.
 *
 * ## Adding a store
 * Annotate the provider. There is no wiring step, and no DI seam to add it to:
 * ```kotlin
 * @StoreProvider(id = "myThing", ttl = "5m")
 * @CacheKey(name = "LIST", key = "myThing")
 * fun provideMyThingStore(api: MyApi, dao: MyDao): Store<Unit, List<MyThing>> = …
 * ```
 * `store-ksp` derives the Koin qualifier, the TTL, the cache keys, the binding and the logout purge.
 * Dependencies come from the SIGNATURE — they are never restated.
 *
 * There is deliberately no `ProjectStoreModule` seam. It existed when stores were hand-wired; with
 * codegen it became a second way to do what the annotation already does, and an unnecessary one —
 * `cmp-navigation`'s FeatureRegistry is fork-owned, so a fork that genuinely needs a bespoke Koin
 * module can add its own there without a pre-wired hook in template code.
 *
 * Wire into Koin start-up:
 * ```kotlin
 * startKoin { modules(appStoreModule, /* … */) }
 * ```
 */
val appStoreModule: Module = module {
    // Framework write-SoT: the base-store module provides the single write door (MutationGateway)
    // + its Room-backed ConflictInbox. Every repo migrated onto `gateway.*` resolves `get()` here,
    // so a fork wiring `appStoreModule` gets the gateway for free (needs ConflictDao from
    // DatabaseModule + NetworkMonitor on the graph — both present in KoinModules.allModules).
    includes(CoreBaseStoreModule)

    // Store cache manager — clears all registered caches on logout (registration-based).
    single<StoreCacheManager> {
        StoreCacheManagerImpl(
            bookkeeperDao = get(),
            draftDao = get(),
        )
    }

    // Cross-form drafts inventory — the live feed + actions behind the template-level
    // Settings → "Sync & Drafts" screen. Framework infra (not a demo store); survives sync.
    single<DraftInventory> { DraftInventoryImpl(draftDao = get()) }

    // Every declared store: its qualifier binding AND its logout registration.
    includes(GeneratedStoreBindings)
}

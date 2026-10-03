/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.di

import kpt.core.base.network.AccessPointRegistry
import kpt.core.base.network.DefaultHeaderProvider
import kpt.core.base.network.MultiUrlConfigProvider
import kpt.core.base.network.RuntimeHeaderStore
import kpt.core.base.network.SupabaseClientFactory
import kpt.core.base.network.SupabaseConfigClient
import kpt.core.base.network.SupabaseExtrasProvider
import kpt.core.network.config.AppAccessPoints
import kpt.core.network.config.AppMultiUrlConfigProvider
import kpt.core.network.config.AppSupabaseAnonKeys
import kpt.core.network.config.ProjectNetworkHeaders
import org.koin.dsl.module

// NOTE: Backend base URLs are NOT hardcoded here or in config classes anymore — every server is a
// declared access point in app-profile/app.yaml#network.access_points (→ AccessPointRegistry via
// syncForkConfig). Every API is wired in the GENERATED [GeneratedApiBindings] via
// `restApi("<id>") { … }`, which auto-builds each transport from its access point.
// Fork-customisation = edit app.yaml + syncForkConfig.
// FRED's API key remains a per-request @Query param (a vault secret: mifos-x-fred-api-key → BuildKonfig).
// Dynamic server config (consumer-facing, from core-base/network):
//   - SupabaseConfigClient(s) are registered below so forks can fetch runtime server config from a
//     Supabase `app_config`-style table. The AccessPointRegistry is the single URL SoT: SupabaseClientFactory
//     materializes one client per declared SUPABASE access point (URL from AccessPoint.baseUrl). Anon keys
//     resolve per-id via AppSupabaseAnonKeys, whose rows syncForkConfig generates from app-profile and whose
//     values come from BuildKonfig (`anon_key_env:`); absent a key the creds are empty, so the client stays
//     inert (isConfigured == false). Forks edit app-profile only.
//     The legacy single-project path (SupabaseConfigConventionPlugin generating a SupabaseCredentials object
//     from a secrets file, plus an unnamed single<SupabaseConfigClient>) was REMOVED on 2026-09-06: nothing
//     referenced the generated object, and the registry had already become the single URL SoT.
//   - DynamicBaseUrlPlugin is an opt-in of setupDefaultHttpClient(...): a fork implements
//     DynamicUrlConfigProvider (reads its selected server, keyed by AppUrlTypes) and passes it as
//     `dynamicUrlProvider = get()` on any client that should switch base URL at runtime. The
//     toolkit's own fixed-URL APIs (FRED / World Bank / CoinGecko / Frankfurter) don't use it.
// INFRA-ONLY, owner: template (E1 / C2). API types live in per-access-point packages
// (`kpt.core.network.<id>.api`), their bindings are GENERATED into [GeneratedApiBindings], and any
// non-derivable single goes in the fork-owned [ProjectNetworkModule]. So this aggregator carries no
// endpoint-specific reference at all and a template sync can blind-copy it.
val NetworkModule = module {
    // Runtime header values — written at login (Basic / OAuth), read on EVERY request. A singleton,
    // because the whole point is that a value set after the clients were built still reaches them.
    single { RuntimeHeaderStore() }

    // Default request headers, from the fork-owned ProjectNetworkHeaders seam. Every REST client
    // built by `restApi(...)` resolves this, so a fork adds an app-wide header without hand-building
    // a Ktorfit and giving up the generated @ApiBinding wiring. Neutral on the template.
    single<DefaultHeaderProvider> { ProjectNetworkHeaders }

    // Every declared endpoint's Koin binding, GENERATED from app-profile#network.access_points into
    // the sibling [GeneratedApiBindings] (same package — no import, so this file keeps its zero-demo
    // -imports property and stays blind-copyable on a template sync). It lives HERE rather than in the
    // demo aggregator because it is derived from the FORK's SoT: `remove-demo.sh` deletes every
    // `**/demo/**` package, which used to take the generated file with it and turn every later
    // syncForkConfig into a silent no-op for a cleaned fork.
    includes(GeneratedApiBindings)

    // The fork's generated access points, wrapped by the framework registry mechanism (core-base/network).
    // The restApi("<id>") DSL and AppMultiUrlConfigProvider both resolve transports/base-URLs from this.
    single { AccessPointRegistry(AppAccessPoints.points) }

    // Unified access-point provider — resolves every named UrlType to its REST base URL from the
    // AccessPointRegistry. Clients thread it via
    // setupDefaultHttpClient(multiUrlProvider = get(), urlType = AppUrlTypes.<NAME>).
    single<MultiUrlConfigProvider> { AppMultiUrlConfigProvider(get()) }

    // Per-point Supabase client factory — URL from AccessPointRegistry, anon key by id.
    single {
        SupabaseClientFactory(
            registry = get(),
            anonKeyFor = AppSupabaseAnonKeys::forId,
            // THE LINK THAT MAKES THE SEAM REAL. SupabaseConfigClient and SupabaseClientFactory both
            // accept the extras hook, but if nothing passes it here the default `{ {} }` wins and a
            // fork's `single<SupabaseExtrasProvider>` is never called — Auth is silently not installed,
            // sign-in and the table APIs stop sharing an authenticated client, and every RLS-gated call
            // resolves no `auth.uid()`. It compiles, and NAP-7 passes, which is what makes the gap so
            // easy to ship: each layer is individually correct.
            //
            // `getOrNull` because the binding is OPTIONAL — a fork needing only Postgrest binds nothing
            // and gets the no-op, which is the neutral template's own behaviour.
            installExtrasFor = { id ->
                getOrNull<SupabaseExtrasProvider>()?.forId(id) ?: {}
            },
        )
    }

    // Map<id, client> — every declared Supabase access point materializes exactly one client.
    // This is the ONLY Supabase client surface. There is deliberately no unnamed
    // `single<SupabaseConfigClient>`: "the" Supabase client is not a meaningful concept once a fork
    // can declare N projects, and the per-id `supabaseApi("<id>")` binding is how consumers reach one.
    single<Map<String, SupabaseConfigClient>> { get<SupabaseClientFactory>().clients() }
}

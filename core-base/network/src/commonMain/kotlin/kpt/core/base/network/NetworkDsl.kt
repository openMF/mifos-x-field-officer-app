/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.network

import de.jensklingenberg.ktorfit.Ktorfit
import org.koin.core.module.Module

/**
 * core-base/network transport DSL — the streamlined, template-owned way to wire a REST API interface.
 *
 * A consumer's `ProjectNetworkModule` declares an API in ONE line by referencing the id of an access
 * point in its [AccessPointRegistry] (SoT: `app-profile/app.yaml#network.access_points`). `core-base`
 * auto-builds the Ktor client + Ktorfit from the access point's base URL + loggable host, so the consumer
 * writes ONLY the API interface + its generated `createXApi()` factory — no per-API Ktorfit boilerplate.
 */

/**
 * Build a Ktorfit for the REST access point declared under [accessPointId]. Base URL + loggable host +
 * proxied host come from this [AccessPointRegistry]; throws if the id isn't declared.
 *
 * [headerProvider] supplies headers sent on EVERY request to this endpoint — see
 * [DefaultHeaderProvider] for what belongs there.
 *
 * [runtimeHeaders] resolves the point's DECLARED `headers[]` (app-profile) at request time: static
 * values are baked in, `runtimeKey` ones are read from the store on every call, so a credential
 * written at login reaches requests made by a client that already existed.
 */
fun AccessPointRegistry.ktorfitFor(
    accessPointId: String,
    headerProvider: DefaultHeaderProvider = DefaultHeaderProvider.None,
    runtimeHeaders: RuntimeHeaderStore = RuntimeHeaderStore(),
): Ktorfit {
    val ap = byId(accessPointId)
        ?: error(
            "No network access point '$accessPointId' declared in app-profile network.access_points " +
                "(AccessPointRegistry). Declare it there, or fix the id.",
        )
    return Ktorfit.Builder()
        .httpClient(
            client = httpClient(
                setupDefaultHttpClient(
                    baseUrl = ap.effectiveUrl,
                    loggableHosts = listOf(ap.loggableHost),
                    proxiedHosts = ap.proxiedHost?.let { listOf(it) } ?: emptyList(),
                    // Per access point: a key belongs to ONE endpoint, and sending it to the others
                    // leaks a credential to hosts that never needed it.
                    defaultHeaders = headerProvider.headersFor(accessPointId),
                    // The point's DECLARED headers, resolved per request so a runtimeKey picks up
                    // the value login wrote after this client was built.
                    dynamicHeaders = { runtimeHeaders.resolve(ap.headers) },
                ),
            ),
        )
        .build()
}

/**
 * Register a Ktorfit REST API [T] wired to the access point [accessPointId]. The [AccessPointRegistry]
 * is resolved from Koin (the fork registers `single { AccessPointRegistry(AppAccessPoints.points) }`).
 * Usage:
 * ```
 * val ProjectNetworkModule = module {
 *     restApi("jsonplaceholder") { it.createJsonPlaceholderApi() }
 * }
 * ```
 */
inline fun <reified T : Any> Module.restApi(
    accessPointId: String,
    crossinline create: (Ktorfit) -> T,
) {
    single<T> {
        create(
            get<AccessPointRegistry>().ktorfitFor(
                accessPointId = accessPointId,
                // Optional: a fork that binds no provider gets the empty default, unchanged behaviour.
                headerProvider = getOrNull<DefaultHeaderProvider>() ?: DefaultHeaderProvider.None,
                runtimeHeaders = getOrNull<RuntimeHeaderStore>() ?: RuntimeHeaderStore(),
            ),
        )
    }
}

/**
 * Register a Supabase-backed API [T] wired to the Supabase access point [accessPointId] — the exact
 * twin of [restApi], so both transports are declared the same way and neither is second-class.
 *
 * The [SupabaseClientFactory] is resolved from Koin (`NetworkModule` binds it); it owns URL resolution
 * (from [AccessPoint.baseUrl]) and anon-key lookup, so a fork writes ONLY the facade [T] over
 * `client.postgrest` and declares the endpoint in `app-profile/app.yaml#network.access_points`.
 *
 * Unlike REST, supabase-kt has no interface-generation step: `T` is the fork's own hand-written facade
 * (a typed wrapper over Postgrest queries), not a generated stub. That is the one real asymmetry
 * between the two paths, and it is inherent to supabase-kt rather than to this DSL.
 *
 * Usage:
 * ```
 * val ProjectNetworkModule = module {
 *     supabaseApi("project") { AppConfigApi(it) }
 * }
 * ```
 */
inline fun <reified T : Any> Module.supabaseApi(
    accessPointId: String,
    crossinline create: (SupabaseConfigClient) -> T,
) {
    single<T> { create(get<SupabaseClientFactory>().requireClientFor(accessPointId)) }
}

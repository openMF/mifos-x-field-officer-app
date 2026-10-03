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

/** Transport kind of a declared network access point. */
enum class AccessPointKind {
    /** A REST endpoint reached over Ktor/Ktorfit. */
    REST,

    /** A Supabase project reached over supabase-kt (Postgrest by default). */
    SUPABASE,
}

/**
 * One declared network access point — a named endpoint the app talks to.
 *
 * INFRA (owner: template) — the STRUCTURE is framework-owned; the per-fork VALUES are generated into
 * `kpt.core.network.config.AppAccessPoints.points` by `syncForkConfig` from
 * `app-profile/app.yaml#network.access_points`.
 *
 * @param id stable string id a fork references from `restApi<T>(id)`.
 * @param kind [AccessPointKind.REST] (Ktor/Ktorfit) or [AccessPointKind.SUPABASE] (supabase-kt).
 * @param baseUrl the resolved base URL (REST) or the Supabase project URL.
 * @param loggableHost the host enabled for HTTP logging on this access point.
 * @param proxiedHost host routed through the platform proxy (Ktor's DynamicBaseUrlPlugin); null = none.
 * @param type [UrlType] key for dynamic base-URL switching; defaults to the id upper-cased.
 */
data class AccessPoint(
    val id: String,
    val kind: AccessPointKind,
    /**
     * ORIGIN only — scheme + host, ending at the first `/` (`https://sandbox.mifos.community/`).
     *
     * Split from [basePath] because they change for different reasons and at different times. The
     * origin is what a fork swaps (sandbox -> production, or a per-flavour host) and what
     * [DynamicBaseUrlPlugin] rewrites per [UrlType] at runtime; the path is a property of the API
     * itself and travels with the endpoint. Keeping them in one string meant a fork could not
     * repoint the host without also copying the vendor's path — and a runtime host swap silently
     * dropped it.
     */
    val baseUrl: String,
    /**
     * API path under [baseUrl] (`fineract-provider/api/v1/`). Empty when the API lives at the root.
     *
     * Joined to the origin exactly once, with a single `/` between, so neither side has to be
     * careful about trailing slashes.
     */
    val basePath: String = "",
    val loggableHost: String,
    val proxiedHost: String? = null,
    val type: UrlType = UrlType(id.uppercase()),
    /**
     * Headers sent on every request to this endpoint, declared in
     * `app-profile/app.yaml#network.access_points[].headers[]`. Static values are baked in; a
     * `runtimeKey` header takes its value from [RuntimeHeaderStore] at request time (login, OAuth).
     */
    val headers: List<HeaderSpec> = emptyList(),
    /**
     * How this endpoint authenticates. Declaring it makes the `Authorization` header automatic —
     * the generator emits its spec and the auth bridge fills the value in the right wire format, so
     * no call site formats a credential by hand.
     */
    val auth: AuthScheme = AuthScheme.NONE,
) {
    /**
     * The URL requests are actually made against — [baseUrl] + [basePath], normalised.
     *
     * Ktor resolves a relative request path against this, so the trailing slash matters: without it
     * the last segment is replaced rather than appended, and `offices` would hit
     * `…/api/offices` instead of `…/api/v1/offices`.
     */
    val effectiveUrl: String
        get() = when {
            basePath.isBlank() -> baseUrl
            else -> baseUrl.trimEnd('/') + "/" + basePath.trim('/') + "/"
        }
}

/**
 * Template registry MECHANISM over a fork-provided list of [points].
 *
 * The fork supplies its generated points (`AppAccessPoints.points`) and registers
 * `single { AccessPointRegistry(AppAccessPoints.points) }` in its `NetworkModule`; `core-base/network`
 * owns the resolution logic + the [restApi]/[ktorfitFor] DSL — so a fork writes only API interfaces plus
 * one `restApi("<id>")` line each, and there is exactly one source of truth for "which servers this app
 * talks to" (`app-profile/app.yaml#network.access_points`).
 */
class AccessPointRegistry(val points: List<AccessPoint>) {

    /** Resolve the access point declared under [id], or `null`. */
    fun byId(id: String): AccessPoint? = points.firstOrNull { it.id == id }

    /** Resolve the REST base URL registered for [type], or `null` if none is declared. */
    fun restBaseUrl(type: UrlType): String? =
        points.firstOrNull { it.type == type && it.kind == AccessPointKind.REST }?.effectiveUrl

    /** Every declared Supabase access point, in registry order. Empty if none declared. */
    fun supabasePoints(): List<AccessPoint> =
        points.filter { it.kind == AccessPointKind.SUPABASE }

    /** The first Supabase access point, or `null` if none is declared. Prefer [supabasePoints]. */
    fun supabasePoint(): AccessPoint? = supabasePoints().firstOrNull()

    /** Every declared host enabled for HTTP logging, across all access points. */
    fun loggableHosts(): List<String> = points.map { it.loggableHost }
}

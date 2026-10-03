/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.network

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.logging.LogLevel
import io.github.jan.supabase.SupabaseClientBuilder
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest

/**
 * Generic Supabase client wrapper for KMP projects.
 *
 * This class provides a reusable Supabase client setup with:
 * - Lazy initialization of Supabase client
 * - Configurable logging level
 * - Direct access to Postgrest for queries
 *
 * ## Usage
 *
 * ```kotlin
 * // Consumers do NOT construct this directly — SupabaseClientFactory builds one per declared
 * // SUPABASE access point, and `supabaseApi("<id>")` injects it into the fork's API type.
 * val configClient = supabaseClientFactory.requireClientFor("project")
 *
 * // Use the client for queries
 * if (configClient.isConfigured) {
 *     val result = configClient.postgrest
 *         .from("app_config")
 *         .select()
 *         .decodeSingle<MyConfig>()
 * }
 * ```
 *
 * @param credentials The Supabase credentials (URL and anon key).
 * @param logLevel The logging level for Supabase client. Defaults to INFO.
 */
class SupabaseConfigClient(
    private val credentials: SupabaseCredentials,
    private val logLevel: LogLevel = LogLevel.INFO,
    /**
     * Fork seam for the modules this exposer does not install.
     *
     * Postgrest is always installed; Auth / ComposeAuth / Realtime / Storage are opt-in. Before this
     * seam existed a fork that needed Auth had to build its OWN `createSupabaseClient` — and then the
     * client the generated `supabaseApi(...)` binding hands to an `@ApiBinding` type was a DIFFERENT
     * instance from the one the fork signed in on, so every RLS-gated call resolved no `auth.uid()`.
     * The only way out was to stop using `@ApiBinding` and hand-wire the singles, which is the drift
     * NAP-7 refuses. With the seam there is ONE client per access point: sign-in and every table API
     * share it, and the annotation keeps working.
     */
    private val installExtras: SupabaseClientBuilder.() -> Unit = {},
) {
    /**
     * Lazily initialized Supabase client.
     * Only created when first accessed.
     */
    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = credentials.url,
            supabaseKey = credentials.anonKey,
        ) {
            defaultLogLevel = logLevel
            install(Postgrest)
            installExtras()
        }
    }

    /**
     * Direct access to Postgrest for database operations (the default data plane).
     *
     * Postgrest is installed by default (see [client]), so this is the always-available surface.
     * Auth / Realtime / Storage are OPT-IN — a fork installs those modules explicitly in its own
     * client builder; they are out of scope for this default exposer.
     */
    val postgrest get() = client.postgrest

    /**
     * Default data-plane exposer — Postgrest. Prefer [data] in fork call-sites so the default
     * transport can evolve without touching every caller.
     */
    val data get() = postgrest

    /**
     * Checks if Supabase is properly configured.
     *
     * @return true if credentials are valid, false otherwise.
     */
    val isConfigured: Boolean
        get() = credentials.isConfigured
}

/**
 * Interface for Supabase credentials.
 *
 * Implement this interface to provide Supabase URL and anon key
 * from your project's build configuration or secrets.
 *
 * ## Where credentials come from
 *
 * `SupabaseClientFactory` supplies them per access point: the URL from
 * [AccessPoint.baseUrl] (the registry is the single URL SoT) and the anon key from the consumer's
 * id-keyed resolver, whose rows `syncForkConfig` generates from
 * `app-profile/app.yaml#network.access_points` and whose values come from `BuildKonfig` via
 * `anon_key_env:` — so no key is committed.
 *
 * A fork therefore configures Supabase by editing app-profile and running `syncForkConfig`, never by
 * writing a credentials object. (An earlier convention plugin generated one from a secrets file for a
 * single hardcoded project; it was removed on 2026-09-06 once the registry made it both redundant and
 * unable to express more than one project.)
 *
 * ## Manual Implementation
 *
 * ```kotlin
 * object MySupabaseCredentials : SupabaseCredentials {
 *     override val url: String = BuildConfig.SUPABASE_URL
 *     override val anonKey: String = BuildConfig.SUPABASE_ANON_KEY
 * }
 * ```
 */
interface SupabaseCredentials {
    /**
     * The Supabase project URL.
     * Example: "https://your-project.supabase.co"
     */
    val url: String

    /**
     * The Supabase anon (public) key.
     * This key is safe to use in client-side code.
     */
    val anonKey: String

    /**
     * Checks if the credentials are properly configured.
     * Returns false if URL or key contain placeholder values.
     */
    val isConfigured: Boolean
        get() = url.isNotBlank() &&
            anonKey.isNotBlank() &&
            !url.contains("YOUR_") &&
            !anonKey.contains("YOUR_") &&
            url.startsWith("https://")
}

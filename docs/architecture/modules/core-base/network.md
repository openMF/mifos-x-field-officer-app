# `core-base/network`

> **Layer:** `core-base` — framework-shared. Generators **consume** these contracts and
> **never write here**; a fix belongs upstream in the template, not in a fork.
> **Instruction surface:** `CORE_BASE_NETWORK.md` — the generator-facing instruction for this module,
> held in the framework at `training-layer/instructions/stream-first/latest/`. This guide
> is the architecture SoT; that surface is how it reaches codegen, and
> `/kmp-project-template-retrain` keeps the two in step.
> **Shape:** 21 Kotlin files (0 test) · source sets: `androidMain`, `commonMain`, `desktopMain`, `jsMain`, `nativeMain`, `wasmJsMain`



## When implementing a feature

**You do not write here.** `core-base` is framework-shared: a feature consumes these
contracts and never modifies them. If a feature seems to need a change here, that is a
TEMPLATE change — it flows upstream as a draft PR (RULE-TEMPLATE-MODULE-FIX-UPSTREAM-001),
never a local edit, because every fork shares this code and a local fix is drift.

What a feature *does* do is import from here and satisfy the contracts this module
defines. The module guides under `../core/` show where the feature-side code goes.

## Position in the module graph

**Uses internally** (`implementation`) — not visible to consumers:

`core-base/security`

**Consumed by** 2 module(s): `core/data`, `core/network`

## Codegen contracts

**None.** Nothing here is declared by annotation, so there is no aggregate to
generate and no propagation target. A generator writing into this module takes its
idiom from `CORE_BASE_NETWORK.md`.

## Principal types

- **`AccessPoint`** — One declared network access point — a named endpoint the app talks to.
- **`AccessPointRegistry`** — Template registry MECHANISM over a fork-provided list of [points].
- **`AuthHeaderBridge`** — Keeps [RuntimeHeaderStore] in step with the stored credential, so `Authorization` is automatic.
- **`AuthProviders`** — The auth-credential providers for [setupDefaultHttpClient], grouped so the client builder stays under
- **`AuthScheme`** — How an access point authenticates, declared as `auth:` in
- **`AuthTokenSource`** — The stored credential, as a stream.
- **`DefaultHeaderProvider`** — Headers this app sends on EVERY request, resolved per access point.
- **`DynamicBaseUrlConfig`** — Configuration class for [DynamicBaseUrlPlugin].
- **`DynamicBaseUrlPlugin`** — Ktor plugin that dynamically sets the base URL for each request based on
- **`DynamicLoggableHosts`** — A dynamic list implementation that provides loggable hosts from a [DynamicUrlConfigProvider].
- **`DynamicUrlConfigProvider`** — Interface for providing dynamic URL configuration at runtime.
- **`HeaderSpec`** — A header an access point sends on every request, declared in
- …and 8 more documented types

Undocumented: `AccessPointKind`, `ResultSuspendConverterFactory`, `UrlType`, `WebApiProxyConfig`

## Demo showcase exposure

**None.** No `demo/` package and no `// demo:begin` fence — `remove-demo.sh` does not
touch this module, so a stripped fork keeps it verbatim.

## Tests

No tests in this module. If you add behaviour here, add the test alongside it —
`CORE_TESTING.md` carries the shared idiom.



## Sample implementation

Real code from this module — the shape a generator should follow here.

```kotlin
/**
 * Per-point Supabase client factory.
 *
 * Builds one [SupabaseConfigClient] per declared Supabase [AccessPoint]. URL always comes from
 * [AccessPoint.baseUrl] (the registry is the single URL SoT — reconciles the legacy path where
 * the URL was read from the secrets creds file). Anon key is resolved via [anonKeyFor], keyed by
 * access-point id, so a fork threads its per-project secrets through one narrow seam.
 *
 * Cached per id so consumers can inject the factory + resolve on demand without re-building.
 */
class SupabaseClientFactory(
    private val registry: AccessPointRegistry,
    private val anonKeyFor: (id: String) -> String,
    private val logLevel: LogLevel = LogLevel.INFO,
    /**
     * Per-access-point fork seam: extra supabase-kt modules to install on that point's client
     * (Auth, ComposeAuth, Realtime, Storage). Defaults to none, which is the neutral template.
     *
     * Keyed by id because a fork may run several projects and want Auth on only one of them.
     */
    private val installExtrasFor: (id: String) -> SupabaseClientBuilder.() -> Unit = { {} },
) {
    private val cache: MutableMap<String, SupabaseConfigClient> = mutableMapOf()

    /** Client for [id], or `null` if [id] is not a declared Supabase access point. */
    fun clientFor(id: String): SupabaseConfigClient? {
        val point = registry.byId(id)?.takeIf { it.kind == AccessPointKind.SUPABASE } ?: return null
        return cache.getOrPut(id) {
            SupabaseConfigClient(
                credentials = object : SupabaseCredentials {
                    override val url: String = point.baseUrl
                    override val anonKey: String = anonKeyFor(id)
                },
                logLevel = logLevel,
                installExtras = installExtrasFor(id),
            )
        }
    }

    /**
     * Client for [id], or throw naming every declared Supabase point.
     *
     * The [clientFor] null is right for "probe whether this fork configured Supabase"; it is wrong for
     * DI wiring, where a typo'd or undeclared id must fail loudly at graph construction rather than
     * inject a null-shaped absence. This is the Supabase twin of [ktorfitFor]'s error, and [supabaseApi]
     * is its only intended caller.
    // … (excerpt)
```

Source: [`src/commonMain/kotlin/kpt/core/base/network/SupabaseClientFactory.kt`](../../../../core-base/network/src/commonMain/kotlin/kpt/core/base/network/SupabaseClientFactory.kt) — excerpt; read the file for the full implementation.

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

# `core/network`

> **Layer:** `core` — fork-owned implementation, and a **codegen target**.
> **Instruction surface:** `CORE_NETWORK.md` — the generator-facing instruction for this module,
> held in the framework at `training-layer/instructions/stream-first/latest/`. This guide
> is the architecture SoT; that surface is how it reaches codegen, and
> `/kmp-project-template-retrain` keeps the two in step.
> **Shape:** 33 Kotlin files (8 test) · source sets: `androidMain`, `commonMain`, `commonTest`



## When implementing a feature

**Tier 3** of the codegen chain, written by **`kmp-client-gen`**.

**Does your feature need this module?** Use when /kmp-implement or /kmp-client needs client networking layer generated.

**Where the code goes**

```
core/network/src/commonMain/kotlin/kpt/core/network/<your-domain>/
```

One package per domain — the template's own `RemoteAppConfigDto.kt` sits at `kpt/core/network/lwmswhoxvvoagzkqxiyd/appconfig/dto/`. Do not flatten
everything into the module root, and do not add to `di/`, `config/` or `migrations/`:
those are infrastructure, not feature surface.

**How it wires**

Annotate — that is the whole registration. There is no DI file to edit:

- `@ApiBinding` → generates `di/GeneratedApiBindings`

The aggregates above are **build artifacts**. Authoring one in source produces
duplicate declarations that fail the build, so a feature adds its annotation and
nothing else.

**Next in the chain:** tier 4 (`core/store`).

## Position in the module graph

**Exposes transitively** (`api`) — a consumer of this module also sees these:

- [`core-base/network`](../core-base/network.md)
- [`core/common`](../core/common.md)
- [`core/model`](../core/model.md)

**Uses internally** (`implementation`) — not visible to consumers:

`core/datastore`

**Consumed by** 2 module(s): `core/data`, `core/store`

## Codegen contracts owned here

These annotations **are** the declaration. Their aggregates are build artifacts;
authoring one in source produces duplicate declarations that fail the build.

| annotation | target | processor | generates |
|---|---|---|---|
| `@ApiBinding` | `CLASS` | `network-ksp` | di/GeneratedApiBindings |

**`@ApiBinding`** — REST annotates the Ktorfit interface; Supabase annotates the IMPL (single SupabaseConfigClient arg). Never build a second createSupabaseClient — the generated binding hands annotated types a different instance carrying no session, so RLS calls resolve nothing.

Declared in [`CONTRACT.yaml`](../../CONTRACT.yaml), which is verified equal to the
code in both directions by `framework-verify-architecture-contract.sh`.

## Principal types

- **`AppAccessPoints`** — The per-fork list of network access points this app talks to — REST and Supabase in ONE place.
- **`AppConfigApi`** — The `app_config` table's API — the CONTRACT, with no Supabase types in sight.
- **`AppMultiUrlConfigProvider`** — Concrete [MultiUrlConfigProvider] backed by the declarative [AccessPointRegistry].
- **`AppSupabaseAnonKeys`** — The per-fork map of Supabase access-point id → anon key.
- **`AppUrlTypes`** — Project-level catalogue of named API endpoints for runtime base-URL switching
- **`FredApiConfig`** — Runtime configuration for [kpt.core.network.fred.api.FredApi].
- **`ProjectNetworkHeaders`** — THE FORK'S default request headers. Neutral on the template — this is yours to fill.
- **`WorldBankResponseSerializer`** — Custom serializer that destructures the World Bank's

Undocumented: `AppConfigApiImpl`, `CloudTodoDto`, `CoinDetailDto`, `CoinGeckoApi`, `CoinImageDto`, `CoinMarketDto`, `DescriptionDto`, `ExchangeRatesDto`, `FineractApi`, `FineractAuthResponseDto`, `FineractOfficeDto`, `FrankfurterApi`, `FredApi`, `FredObservationDto` …and 10 more

## Demo showcase exposure

`scripts/remove-demo.sh --apply` **does** touch this module:

- `build.gradle.kts` — carries `// demo:begin … // demo:end` fences; the fenced block is stripped
- `src/commonMain/kotlin/kpt/core/network/di/ProjectNetworkModule.kt` — carries `// demo:begin … // demo:end` fences; the fenced block is stripped

After a strip this module must still build and configure. See
[`../../cross-cutting/demo-showcase.md`](../../cross-cutting/demo-showcase.md).

## Tests

8 test file(s) under `core/network/src/commonTest/`. 
Shared idiom: `CORE_TESTING.md`.



## Sample implementation

Real code from this module, showing the `@ApiBinding` contract in use.

```kotlin
/**
 * Mifos Fineract sandbox — the showcase for DECLARED, RUNTIME-VALUED headers.
 *
 * Every call carries two headers, and neither is written here: both are declared on the `fineract`
 * access point in `app-profile/app.yaml#network.access_points[].headers[]`.
 *
 *   `Fineract-Platform-TenantId: default`     static — known at build time, baked in.
 *   `Authorization: Basic …`                  runtime — does not exist until [authenticate] returns.
 *
 * That split is the point. The endpoint states WHICH headers it needs; login supplies only the
 * VALUE, by writing it to `RuntimeHeaderStore["fineract.auth"]`. Because the store is read inside
 * `defaultRequest`, the singleton client built at Koin start — long before anyone signs in — picks
 * the credential up on the very next call, with no client rebuild and no bypassing of the generated
 * `@ApiBinding` wiring.
 *
 * API reference: https://sandbox.mifos.community/fineract-provider/swagger-ui/index.html
 */
@ApiBinding("fineract")
interface FineractApi {

    /**
     * Sign in. The `Authorization` header is NOT required for this call — it is what produces it:
     * the returned `base64EncodedAuthenticationKey` becomes `Basic <key>` in the runtime store.
     */
    @POST("authentication")
    suspend fun authenticate(@Body request: Map<String, String>): FineractAuthResponseDto

    /**
     * The smallest authenticated read. Useful as a proof that the runtime header reached the server:
     * it answers 401 before sign-in and 200 after, with no client rebuild in between.
     */
    @GET("offices")
    suspend fun offices(): List<FineractOfficeDto>
}
```

Source: [`src/commonMain/kotlin/kpt/core/network/fineract/api/FineractApi.kt`](../../../../core/network/src/commonMain/kotlin/kpt/core/network/fineract/api/FineractApi.kt).

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

# `core/network`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_NETWORK.md`
> **Measured:** 42 Kotlin files, 18 test files

## Codegen contracts owned here

| annotation | processor | generates |
|---|---|---|
| `@ApiBinding` | `network-ksp` | di/GeneratedApiBindings |

Declared in [`../../CONTRACT.yaml`](../../CONTRACT.yaml); that file is the machine-verified SoT and this table is its human projection.

## Principal types

`AppAccessPoints`, `AppMultiUrlConfigProvider`, `AppUrlTypes`, `CloudTodoDto`, `CoinDetailDto`, `CoinGeckoApi`, `CoinImageDto`, `CoinMarketDto`, `DescriptionDto`, `ExchangeRatesDto`, `FineractApi`, `FineractAuthResponseDto`, `FineractOfficeDto`, `FrankfurterApi`  …and 18 more

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core/network sha=5c8a219c9287a029b7aa39f405505ec47127c556 -->
## API reference

_Generated from `core/network` at tree `5c8a219c9287` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

### `core/network/src/commonMain/kotlin/kpt/core/network/coingecko/api/CoinGeckoApi.kt`

```kotlin
interface CoinGeckoApi
```
CoinGecko public API v3. Base URL: `BASE_URL`.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/crypto/impl/CoinDetailStore.kt:36</code></summary>

```kotlin
@CacheKey(fn = "item", key = "crypto:coinDetail:{coinId}", params = ["coinId:String"])
fun provideCoinDetailStore(
    api: CoinGeckoApi,
    networkMonitor: NetworkMonitor,
    dao: CoinDetailDao,
): Store<String, CoinDetail> {
    val validator = DefaultValidator.withTtl<CoinDetail>(AppStoreRegistry.Ttl.COIN_DETAIL)
```

</details>

- `suspend fun getMarkets(` — One page of the market list, ordered by market cap descending by default.
- `suspend fun getCoinDetail(` — Full detail for one coin. The four `false` defaults switch off payload sections the detail screen does not render — CoinGecko returns them by default and they dominate the response size.

### `core/network/src/commonMain/kotlin/kpt/core/network/coingecko/dto/CoinDetailDto.kt`

```kotlin
data class CoinDetailDto(
```
Wire shape of CoinGecko's `/coins/{id}` response. Mapped to the domain model in the Store's `SourceOfTruth`; nothing outside `core/network` should see this type.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/coingecko/api/CoinGeckoApi.kt:54</code></summary>

```kotlin
        @Query("community_data") communityData: Boolean = false,
        @Query("developer_data") developerData: Boolean = false,
    ): CoinDetailDto
}
```

</details>

```kotlin
data class DescriptionDto(val en: String? = null)
```
Localised description block; only `en` is consumed.

### `core/network/src/commonMain/kotlin/kpt/core/network/coingecko/dto/CoinMarketDto.kt`

```kotlin
data class CoinMarketDto(
```
One row of CoinGecko's `/coins/markets` response — the paged market list.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/coingecko/api/CoinGeckoApi.kt:37</code></summary>

```kotlin
        @Query("per_page") perPage: Int,
        @Query("page") page: Int,
    ): List<CoinMarketDto>

    /**
     * Full detail for one coin.
     *
```

</details>

```kotlin
data class CoinImageDto(val large: String? = null)
```
Image URL set for a coin; only the large variant is used.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/coingecko/dto/CoinDetailDto.kt:29</code></summary>

```kotlin
    val symbol: String,
    /** Image URL set, or null when CoinGecko omits it. */
    val image: CoinImageDto? = null,
    /**
     * Price and market figures. Nullable because the endpoint can omit the block; [toDomain] substitutes zeroes rather
     * than failing the whole response.
     */
```

</details>

```kotlin
data class MarketDataDto(
```
Nested market figures (price, market cap, 24h change) inside a coin detail response.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/coingecko/dto/CoinDetailDto.kt:34</code></summary>

```kotlin
     * than failing the whole response.
     */
    @SerialName("market_data") val marketData: MarketDataDto? = null,
    /** Localised descriptions, of which only `en` is consumed. */
    val description: DescriptionDto? = null,
) {
    /**
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/config/AppAccessPoints.kt`

```kotlin
object AppAccessPoints
```
The per-fork list of network access points this app talks to — REST and Supabase in ONE place.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/di/NetworkModule.kt:76</code></summary>

```kotlin
    // The restApi("<id>") DSL and AppMultiUrlConfigProvider both resolve transports/base-URLs from this.
    // + the app-wide network privacy policy (logging per build type, query redaction, web CORS relay).
    single { AccessPointRegistry(AppAccessPoints.points, AppAccessPoints.policy) }

    // Unified access-point provider — resolves every named UrlType to its REST base URL from the
    // AccessPointRegistry. Clients thread it via
    // setupDefaultHttpClient(multiUrlProvider = get(), urlType = AppUrlTypes.<NAME>).
```

</details>

- `val points: List<AccessPoint> = listOf(` — This project's declared access points, in app-profile order.
- `val policy: NetworkPolicy = NetworkPolicy(` — Network privacy policy — app-profile `network.logging` / `network.web_cors_proxy`.

### `core/network/src/commonMain/kotlin/kpt/core/network/config/AppMultiUrlConfigProvider.kt`

```kotlin
class AppMultiUrlConfigProvider(
```
Concrete `MultiUrlConfigProvider` backed by the declarative `AccessPointRegistry`.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/di/NetworkModule.kt:81</code></summary>

```kotlin
    // AccessPointRegistry. Clients thread it via
    // setupDefaultHttpClient(multiUrlProvider = get(), urlType = AppUrlTypes.<NAME>).
    single<MultiUrlConfigProvider> { AppMultiUrlConfigProvider(get()) }

    // Supabase clients are bound in core-base NetworkBaseModule (included above): each point's publishable
    // key is on its generated AccessPoint, so they need nothing from here.
    // Map<id, client> — every declared Supabase access point materializes exactly one client.
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/config/AppUrlTypes.kt`

```kotlin
object AppUrlTypes
```
Project-level catalogue of named API endpoints for runtime base-URL switching (see `kpt.core.base.network.MultiUrlConfigProvider` / `kpt.core.base.network.DynamicBaseUrlPlugin`).

- `val MAIN: UrlType = UrlType.MAIN` — `main` — REST access point.
- `val STAGING: UrlType = UrlType("STAGING")` — `staging` — REST access point.
- `val LWMSWHOXVVOAGZKQXIYD: UrlType = UrlType("LWMSWHOXVVOAGZKQXIYD")` — `lwmswhoxvvoagzkqxiyd` — Supabase access point.
- `val JSONPLACEHOLDER: UrlType = UrlType("JSONPLACEHOLDER")` — `jsonplaceholder` — REST access point.
- `val FRANKFURTER: UrlType = UrlType("FRANKFURTER")` — `frankfurter` — REST access point.
- `val COINGECKO: UrlType = UrlType("COINGECKO")` — `coingecko` — REST access point.
- `val FRED: UrlType = UrlType("FRED")` — `fred` — REST access point.
- `val FINERACT: UrlType = UrlType("FINERACT")` — `fineract` — REST access point.
- `val WORLDBANK: UrlType = UrlType("WORLDBANK")` — `worldbank` — REST access point.
- `val all: List<UrlType> = listOf(` — Every declared endpoint type, in app-profile order.

### `core/network/src/commonMain/kotlin/kpt/core/network/config/ProjectNetworkHeaders.kt`

```kotlin
object ProjectNetworkHeaders : DefaultHeaderProvider
```
THE FORK'S default request headers. Neutral on the template — this is yours to fill.

<details><summary>Example</summary>

```kotlin
object ProjectNetworkHeaders : DefaultHeaderProvider {
    override fun headersFor(accessPointId: String): Map<String, String> = when (accessPointId) {
        "main" -> mapOf(
            "X-Client-Version" to BuildKonfig.VERSION_NAME,
            "X-Platform" to platformName(),
        )
        else -> emptyMap()
    }
}
```

</details>

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/di/NetworkModule.kt:61</code></summary>

```kotlin
    // built by `restApi(...)` resolves this, so a fork adds an app-wide header without hand-building
    // a Ktorfit and giving up the generated @ApiBinding wiring. Neutral on the template.
    single<DefaultHeaderProvider> { ProjectNetworkHeaders }

    // Every declared endpoint's Koin binding, GENERATED from app-profile#network.access_points into
    // the sibling [GeneratedApiBindings] (same package — no import, so this file keeps its zero-demo
    // -imports property and stays blind-copyable on a template sync). It lives HERE rather than in the
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/di/NetworkModule.kt`

```kotlin
val NetworkModule = module
```
Koin module for `core/network` — the Ktorfit API bindings over the shared client.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/RepositoryModule.kt:39</code></summary>

```kotlin
 */
val DataModule = module {
    includes(platformModule, CommonModule, DatabaseModule, DatastoreModule, NetworkModule)

    // Every repository's Koin binding, GENERATED from `@RepositoryBinding` on the implementation.
    // Emitted into this same package, so this file needs no import and keeps its zero-demo-reference
    // property — which is what lets a template sync blind-copy it. A stripped fork simply generates
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/di/ProjectNetworkModule.kt`

```kotlin
val ProjectNetworkModule = module
```
THE FORK'S network DI seam. Empty on the neutral template — this is yours to fill.

<details><summary>Example</summary>

```kotlin
single<MyApiConfig> { MyApiConfig(apiKey = BuildKonfig.MY_API_KEY.takeIf { it.isNotBlank() }) }
single<SupabaseExtrasProvider> {
    SupabaseExtrasProvider { id ->
        when (id) {
            // Social sign-in: kmp-supabase-auth (io.github.mobilebytelabs:cmp-supabase-auth[-compose])
            // installs Auth + native Google/Apple onto this client. Never hand-write install(Auth).
            // libs.cmp.supabase.auth.compose (0.3.3). secureStorage keeps the session + PKCE verifier
            // in the encrypted named("secure") Settings instead of supabase-kt's plain default.
            "<your-project-ref>" -> kmpSupabaseAuthInstall(authConfig) { secureStorage(get(named("secure"))) }
            // Several projects sharing a module — one branch, no repetition.
            "<project-b>", "<project-c>" -> {
                { install(Realtime) }
            }
            else -> {
                {}
            }
        }
    }
}
includes(kmpSupabaseAuthNetwork(authConfig) { get<SupabaseClientFactory>().requireClientFor("<ref>").client })
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/fineract/api/FineractApi.kt`

```kotlin
interface FineractApi
```
Mifos Fineract sandbox — the showcase for DECLARED, RUNTIME-VALUED headers.

- `suspend fun authenticate(@Body request: Map<String, String>): FineractAuthResponseDto` — Sign in. The `Authorization` header is NOT required for this call — it is what produces it: the returned `base64EncodedAuthenticationKey` becomes `Basic <key>` in the runtime store.
- `suspend fun offices(): List<FineractOfficeDto>` — The smallest authenticated read. Useful as a proof that the runtime header reached the server: it answers 401 before sign-in and 200 after, with no client rebuild in between.

### `core/network/src/commonMain/kotlin/kpt/core/network/fineract/dto/FineractDto.kt`

```kotlin
data class FineractAuthResponseDto(
```
`POST /authentication` response — the Fineract sandbox's sign-in. The showcase for a RUNTIME header: nothing here is known at build time, and every subsequent call needs the credential this returns.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/fineract/api/FineractApi.kt:44</code></summary>

```kotlin
     */
    @POST("authentication")
    suspend fun authenticate(@Body request: Map<String, String>): FineractAuthResponseDto

    /**
     * The smallest authenticated read. Useful as a proof that the runtime header reached the server:
     * it answers 401 before sign-in and 200 after, with no client rebuild in between.
```

</details>

```kotlin
data class FineractOfficeDto(
```
`GET /offices` row — the smallest authenticated read that proves the header reached the server.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/fineract/api/FineractApi.kt:51</code></summary>

```kotlin
     */
    @GET("offices")
    suspend fun offices(): List<FineractOfficeDto>
}
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/frankfurter/api/FrankfurterApi.kt`

```kotlin
interface FrankfurterApi
```
Frankfurter open-source exchange rate API. Base URL: `BASE_URL`.

<details><summary>Used in the template — <code>core/store/src/commonMain/kotlin/kpt/core/store/currency/impl/ExchangeRatesStore.kt:37</code></summary>

```kotlin
@CacheKey(fn = "of", key = "currency:exchangeRates:{baseCurrency}", params = ["baseCurrency:String"])
fun provideExchangeRatesStore(
    api: FrankfurterApi,
    networkMonitor: NetworkMonitor,
    dao: ExchangeRatesDao,
): Store<String, ExchangeRates> {
    val validator = DefaultValidator.withTtl<ExchangeRates>(AppStoreRegistry.Ttl.EXCHANGE_RATES)
```

</details>

- `suspend fun getLatestRates(@Query("from") from: String): ExchangeRatesDto` — Latest rates for one base currency.
- `suspend fun getHistoricalRates(` — Rates for one pair over a date range.

### `core/network/src/commonMain/kotlin/kpt/core/network/frankfurter/dto/ExchangeRatesDto.kt`

```kotlin
data class ExchangeRatesDto(
```
Wire shape of the Frankfurter `/latest` response — base currency plus a rate map.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/frankfurter/api/FrankfurterApi.kt:29</code></summary>

```kotlin
     */
    @GET("v1/latest")
    suspend fun getLatestRates(@Query("from") from: String): ExchangeRatesDto

    /**
     * Rates for one pair over a date range.
     *
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/frankfurter/dto/RateHistoryDto.kt`

```kotlin
data class RateHistoryDto(
```
Wire shape of the Frankfurter time-series response — a date-keyed map of rate maps.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/frankfurter/api/FrankfurterApi.kt:45</code></summary>

```kotlin
        @Query("from") from: String,
        @Query("to") to: String,
    ): RateHistoryDto
}
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/fred/api/FredApi.kt`

```kotlin
interface FredApi
```
FRED (Federal Reserve Economic Data) public API v0. Base URL: `BASE_URL`. Requires a free developer API key — sign up at https://fred.stlouisfed.org/docs/api/api_key.html.

<details><summary>Used in the template — <code>core/network/src/commonTest/kotlin/kpt/core/network/fred/api/FredApiTest.kt:41</code></summary>

```kotlin
    fun seriesObservationsBuildsCorrectRequestUrl() = runTest {
        val capturedUrls = mutableListOf<String>()
        val api = buildFredApi { request ->
            capturedUrls.add(request.url.toString())
            respondJson(FRED_SAMPLE_RESPONSE)
        }
```

</details>

- `suspend fun seriesObservations(` — Fetch observations for a single FRED series in the closed date interval `[observationStart, observationEnd]`.

### `core/network/src/commonMain/kotlin/kpt/core/network/fred/config/FredApiConfig.kt`

```kotlin
data class FredApiConfig(
```
Runtime configuration for `kpt.core.network.fred.api.FredApi`.

<details><summary>Example</summary>

```kotlin
// BuildKonfig (preferred)
single { FredApiConfig(apiKey = BuildKonfig.FRED_API_KEY) }

// System env / Gradle property
single { FredApiConfig(apiKey = System.getenv("FRED_API_KEY")) }

// Hardcoded for local dev (NEVER commit)
single { FredApiConfig(apiKey = "deadbeef...") }
```

</details>

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/di/ProjectNetworkModule.kt:114</code></summary>

```kotlin
    // derived from the access-point declaration the way a base URL or a publishable key can. Fenced, so
    // `remove-demo.sh` drops it together with the `fred` package and its access point.
    single<kpt.core.network.fred.config.FredApiConfig> {
        kpt.core.network.fred.config.FredApiConfig(
            apiKey = BuildKonfig.FRED_API_KEY.takeIf { it.isNotBlank() },
        )
    }
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/fred/dto/FredObservationsDto.kt`

```kotlin
data class FredObservationsDto(
```
Wire-format response from FRED's `fred/series/observations` endpoint. FRED publishes daily observations; missing values are encoded as the string `"."` (an explicit no-data marker, not absence of the field).

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/fred/api/FredApi.kt:53</code></summary>

```kotlin
        @Query("observation_start") observationStart: String,
        @Query("observation_end") observationEnd: String,
    ): FredObservationsDto
}
```

</details>

```kotlin
data class FredObservationDto(
```
Single observation row from FRED. `value` arrives as a string because FRED uses `"."` to mark missing data on otherwise-daily series.

<details><summary>Used in the template — <code>core/network/src/commonTest/kotlin/kpt/core/network/fred/dto/FredObservationsDtoTest.kt:61</code></summary>

```kotlin
        val dto = FredObservationsDto(
            observations = listOf(
                FredObservationDto(date = "2026-05-20", value = "4.33"),
                FredObservationDto(date = "2026-05-21", value = "."),
                FredObservationDto(date = "2026-05-22", value = "4.35"),
            ),
        )
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/jsonplaceholder/api/JsonPlaceholderApi.kt`

```kotlin
interface JsonPlaceholderApi
```
jsonplaceholder `/todos` — a free WRITABLE demo REST API (POST/PUT accepted, echoed back).

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/cloudtodo/CloudTodoRepositoryTest.kt:90</code></summary>

```kotlin
}

private class FakeJsonPlaceholderApi : JsonPlaceholderApi {
    var updateCalled = false
    override suspend fun getTodo(id: Int): CloudTodoDto =
        CloudTodoDto(id = id, title = "todo-$id", completed = false)
    override suspend fun updateTodo(id: Int, todo: CloudTodoDto): CloudTodoDto {
```

</details>

- `suspend fun getTodo(@Path("id") id: Int): CloudTodoDto` — One todo by id.
- `suspend fun updateTodo(@Path("id") id: Int, @Body todo: CloudTodoDto): CloudTodoDto` — Write-back (`PUT /todos/{id}`) — jsonplaceholder echoes the body as if persisted.

### `core/network/src/commonMain/kotlin/kpt/core/network/jsonplaceholder/dto/CloudTodoDto.kt`

```kotlin
data class CloudTodoDto(
```
Wire shape for jsonplaceholder `/todos`.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/cloudtodo/CloudTodoRepositoryTest.kt:92</code></summary>

```kotlin
private class FakeJsonPlaceholderApi : JsonPlaceholderApi {
    var updateCalled = false
    override suspend fun getTodo(id: Int): CloudTodoDto =
        CloudTodoDto(id = id, title = "todo-$id", completed = false)
    override suspend fun updateTodo(id: Int, todo: CloudTodoDto): CloudTodoDto {
        updateCalled = true
        return todo // the server echoes the write back
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/lwmswhoxvvoagzkqxiyd/appserverconfig/api/ServerInstanceApi.kt`

```kotlin
interface ServerInstanceApi
```
The `app_server_config` table's API — one row per server instance. The CONTRACT, no Supabase types: a consumer is tested with a plain fake; `impl.ServerInstanceApiImpl` is the only Supabase-aware part.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/lwmswhoxvvoagzkqxiyd/appserverconfig/api/impl/ServerInstanceApiImpl.kt:26</code></summary>

```kotlin
class ServerInstanceApiImpl(
    private val supabase: SupabaseConfigClient,
) : ServerInstanceApi {

    override val isConfigured: Boolean get() = supabase.isConfigured

    override suspend fun fetchInstances(app: String): List<RemoteServerInstanceDto> {
```

</details>

- `val isConfigured: Boolean` — True when a real project URL + publishable key are configured.
- `suspend fun fetchInstances(app: String): List<RemoteServerInstanceDto>` — The enabled instances whose `supported_apps` contains `app`, in `sort_order` — filtered BY THE SERVER, so one app never downloads another's servers. Empty when unconfigured.

### `core/network/src/commonMain/kotlin/kpt/core/network/lwmswhoxvvoagzkqxiyd/appserverconfig/api/impl/ServerInstanceApiImpl.kt`

```kotlin
class ServerInstanceApiImpl(
```
Supabase implementation of `ServerInstanceApi`. Row-level security limits the publishable key (the `anon` role) to enabled rows, read-only; the `supported_apps` containment filter runs in Postgres (GIN-indexed).

<details><summary>Used in the template — <code>core/network/src/commonTest/kotlin/kpt/core/network/lwmswhoxvvoagzkqxiyd/config/SupabaseAccessPointTest.kt:95</code></summary>

```kotlin
        // (`supabaseApi<ServerInstanceApi>("<id>") { ServerInstanceApiImpl(it) }`), so this asserts against
        // the contract a consumer injects rather than against the implementation.
        val api: ServerInstanceApi = ServerInstanceApiImpl(factory().requireClientFor(point.id))
        assertFalse(api.isConfigured)
        // The guard matters: without it this would reach `client`, build a Supabase client on an empty
        // key, and fail at app start-up on every unconfigured fork.
        assertTrue(api.fetchInstances("field-officer").isEmpty(), "an unconfigured client reads nothing")
```

</details>

### `core/network/src/commonMain/kotlin/kpt/core/network/lwmswhoxvvoagzkqxiyd/appserverconfig/dto/RemoteServerInstanceDto.kt`

```kotlin
data class RemoteServerInstanceDto(
```
One row of `public.app_server_config` — the Mifos server list, ONE ROW PER INSTANCE. It replaces the deprecated `app_config` table (a single row holding an `instances` blob).

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/lwmswhoxvvoagzkqxiyd/appserverconfig/api/ServerInstanceApi.kt:26</code></summary>

```kotlin
     * SERVER, so one app never downloads another's servers. Empty when unconfigured.
     */
    suspend fun fetchInstances(app: String): List<RemoteServerInstanceDto>
}
```

</details>

```kotlin
data class RemoteDemoAccountDto(val role: String, val username: String, val password: String)
```
A PUBLISHED demo sign-in of a public demo instance — public by design, not a credential to protect.

### `core/network/src/commonMain/kotlin/kpt/core/network/worldbank/api/WorldBankApi.kt`

```kotlin
interface WorldBankApi
```
World Bank Open Data API v2. Base URL: `BASE_URL`.

<details><summary>Used in the template — <code>core/network/src/commonTest/kotlin/kpt/core/network/worldbank/api/WorldBankApiTest.kt:42</code></summary>

```kotlin
    fun indicatorBuildsCorrectRequestUrl() = runTest {
        val capturedUrls = mutableListOf<String>()
        val api = buildWorldBankApi { request ->
            capturedUrls.add(request.url.toString())
            respondJson(WORLD_BANK_GDP_RESPONSE)
        }
```

</details>

- `suspend fun indicator(` — Fetch annual observations of a single indicator for a single country.

### `core/network/src/commonMain/kotlin/kpt/core/network/worldbank/dto/WorldBankResponseDto.kt`

```kotlin
data class WorldBankResponseDto(
```
Top-level response from World Bank's `v2/country/{c}/indicator/{i}` endpoint. **Wire-format quirk:** the API returns a 2-element JSON *array* — `[metadata, observations]` — not a regular object.

<details><summary>Used in the template — <code>core/network/src/commonMain/kotlin/kpt/core/network/worldbank/api/WorldBankApi.kt:52</code></summary>

```kotlin
        @Query("per_page") perPage: Int = 50,
        @Query("date") dateRange: String,
    ): WorldBankResponseDto
}
```

</details>

```kotlin
data class WorldBankMetadataDto(
```
First-array element — pagination + record-count metadata. The toolkit only needs `total` to know whether the World Bank returned any data, but the full shape is retained for forward-compat with screens that may want pagination later.

```kotlin
data class WorldBankObservationDto(
```
One observation row from the World Bank's second-array element. `value` is nullable — many `(country, indicator, year)` triples have no reported data; the World Bank emits explicit JSON `null` rather than omitting the row.

```kotlin
data class WorldBankIdValueDto(
```
Generic `{ "id": "USA", "value": "United States" }` shape used by the World Bank for both `country` and `indicator` fields on every observation row.

```kotlin
object WorldBankResponseSerializer : KSerializer<WorldBankResponseDto>
```
Custom serializer that destructures the World Bank's `[metadata, observations[]]` two-element JSON array into a normal Kotlin data class. Read-only — the toolkit never sends WorldBank-shaped payloads.

- `val jsonDecoder = decoder as? JsonDecoder`
- `val root = jsonDecoder.decodeJsonElement()`
- `val array = (root as? JsonArray) ?: return WorldBankResponseDto(`
- `val metadataElement = array.getOrNull(0)`
- `val observationsElement = array.getOrNull(1)`
- `val metadata = metadataElement`
- `val observations = observationsElement`
- `val data = element.jsonObject["data"]`
- `val jsonEncoder = encoder as? JsonEncoder`
- `val metadataJson = value.metadata?.let`
- `val observationsJson = jsonEncoder.json.encodeToJsonElement(`

---

_32 type(s), 37 function(s)/property(ies); 58 carry KDoc at source; 3 authored example(s); 25 live call site(s)._
<!-- api-docs:end -->

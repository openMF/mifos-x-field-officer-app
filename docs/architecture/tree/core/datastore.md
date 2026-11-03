# `core/datastore`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_DATASTORE.md`
> **Measured:** 11 Kotlin files, 3 test files

## Principal types

`AppReviewPromptState`, `AppReviewPromptStore`, `ForkPreferencesRepository`, `ForkPreferencesRepositoryImpl`, `OfflineSyncSettingsStore`, `SettingsAppReviewPromptStore`, `SettingsOfflineSyncSettingsStore`, `UserPreferencesRepository`, `UserPreferencesRepositoryImpl`

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core/datastore sha=10286267122fabcc0facd53f904c960d3111186e -->
## API reference

_Generated from `core/datastore` at tree `10286267122f` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/di/DatastoreModule.kt`

```kotlin
val DatastoreModule = module
```
Koin module for `core/datastore` — the fork's preference stores, over the plain and secure `Settings` instances that `DatastoreBaseModule` binds. Preferences only.

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

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/di/ForkDatastoreModule.kt`

```kotlin
val ForkDatastoreModule = module
```
THE FORK'S datastore DI seam. Empty on the neutral template — this is yours to fill. `DatastoreModule` beside it is `owner: template` and FULL-COPIES on a sync, so a binding added there is replaced on the next adopt.

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/offline/OfflineSyncSettingsStore.kt`

```kotlin
interface OfflineSyncSettingsStore
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/offline/impl/OfflineSyncRepositoryImpl.kt:48</code></summary>

```kotlin
    private val engine: OfflineSyncEngine,
    private val scheduler: OfflineSyncScheduler,
    private val settingsStore: OfflineSyncSettingsStore,
    private val preferences: ForkPreferencesRepository,
    private val nowMs: () -> Long,
) : OfflineSyncRepository {
```

</details>

- `val settings: StateFlow<RefreshSettings>` — The stored refresh settings.
- `fun update(settings: RefreshSettings)` — Stores `settings`.

```kotlin
class SettingsOfflineSyncSettingsStore(private val plainSettings: Settings) : OfflineSyncSettingsStore
```
`Settings`-backed, on the same plain store as the other device preferences.

<details><summary>Used in the template — <code>core/data/src/commonTest/kotlin/kpt/core/data/offline/OfflineSyncRepositoryTest.kt:156</code></summary>

```kotlin
        engine = engine,
        scheduler = scheduler,
        settingsStore = SettingsOfflineSyncSettingsStore(MapSettings()),
        preferences = preferences,
        nowMs = { NOW },
    )
```

</details>

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/prefs/AppReviewPromptStore.kt`

```kotlin
data class AppReviewPromptState(
```
What the review policy needs to know at app open, derived from `AppReviewPromptStore`.

```kotlin
interface AppReviewPromptStore
```
Persisted state behind the in-app review prompt — when it was last shown and whether the user already responded.

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/kpt/core/datastore/di/DatastoreModule.kt:45</code></summary>

```kotlin
    // Review-prompt counters — Settings-backed, DEVICE-scoped (survives sign-out, unlike user prefs).
    // Read once per launch by the app shell, which asks AppReviewConfig whether they justify a prompt.
    single<AppReviewPromptStore> {
        SettingsAppReviewPromptStore(plainSettings = get<Settings>(named("plain")))
    }

    // Sync state persister — Settings-backed (same store as user prefs).
```

</details>

- `fun recordLaunch(): AppReviewPromptState` — Record an app launch and return the resulting state. Called once per launch from the app shell.
- `fun recordPromptShown()` — Stamp that a prompt was just requested, starting the cooldown. Recorded on REQUEST, not on a completed review, because neither Play nor StoreKit reports whether the user actually reviewed.

```kotlin
class SettingsAppReviewPromptStore(
```
`Settings`-backed `AppReviewPromptStore`, using the same plain store as user preferences.

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/kpt/core/datastore/di/DatastoreModule.kt:46</code></summary>

```kotlin
    // Read once per launch by the app shell, which asks AppReviewConfig whether they justify a prompt.
    single<AppReviewPromptStore> {
        SettingsAppReviewPromptStore(plainSettings = get<Settings>(named("plain")))
    }

    // Sync state persister — Settings-backed (same store as user prefs).
    // Read by Synchronizer at sync start; written on snapshot/changeList completion.
```

</details>

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/prefs/ForkPreferencesRepository.kt`

```kotlin
interface ForkPreferencesRepository : UserPreferencesRepository
```
The fork's preference surface: everything the framework has no concept of. Inheritance is the contract — a framework preference cannot be dropped without a compile error, and anything declared here is additive.

<details><summary>Used in the template — <code>feature/profile/src/commonMain/kotlin/kpt/feature/profile/identity/ProfileViewModel.kt:50</code></summary>

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel(
    project: ForkPreferencesRepository,
    serverConfigs: ServerConfigRepository,
    dashboard: DashboardRepository,
    staff: StaffRepository,
    network: NetworkMonitor,
```

</details>

- `val fineractUser: StateFlow<User>` — The signed-in Fineract user. Distinct from `userData`, which is the framework's device-level preference record — this is the server's account object (office, staff, roles, permissions).
- `val authHeader: String` — `Basic <key>` for the current `fineractUser`, or empty when signed out. Derived rather than stored: Fineract returns the encoded key as part of authentication, so a second copy in preferences could disagree with the user record it came from.
- `suspend fun updateFineractUser(user: User)` — Stores the signed-in Fineract user.
- `suspend fun clearFineractUser()` — Sign out of Fineract, leaving device preferences (theme, language) intact.

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/prefs/ForkPreferencesRepositoryImpl.kt`

```kotlin
class ForkPreferencesRepositoryImpl(
```
Fork preferences, delegating every framework preference to the template's implementation. The keys are namespaced `fineract.*` per the seam's contract, so a future framework preference cannot collide with one of these.

<details><summary>Used in the template — <code>feature/profile/src/commonTest/kotlin/kpt/feature/profile/identity/ProfileViewModelTest.kt:103</code></summary>

```kotlin
        val plain = MapSettings()
        val secure = MapSettings()
        ForkPreferencesRepositoryImpl(
            delegate = UserPreferencesRepositoryImpl(plain, secure, dispatchers),
            plainSettings = plain,
            secureSettings = secure,
            dispatcher = dispatchers,
```

</details>

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/prefs/UserPreferencesRepository.kt`

```kotlin
interface UserPreferencesRepository
```
Repository interface for managing user preferences with reactive capabilities. This interface provides reactive access to user preferences including theme settings, dark mode configuration, and dynamic color preferences.

<details><summary>Used in the template — <code>core/data/src/commonMain/kotlin/kpt/core/data/di/RepositoryModule.kt:50</code></summary>

```kotlin
    // Binds the read PORT declared by core/store — core/store cannot depend on core/datastore,
    // so this module (which owns UserPreferencesRepository) supplies the preferences flow.
    single<UserDataSource> { UserDataSource { get<UserPreferencesRepository>().userData } }
    // Framework FetchedAtRepository — durable lastFetchedAt persistence backing
    // DataFreshnessIndicator timestamps. Room-only by design (no in-memory fallback).

    // Framework DraftDao — backing store for SubmitOutbox / DraftSubmitHandler
```

</details>

- `val userData: StateFlow<UserData>` — The whole preference aggregate as hot state. A StateFlow, not a Flow, because almost every screen reads it during composition and needs a value immediately.
- `val authToken: String?` — The stored credential, or null when signed out. Synchronous read for a caller that already has one in hand; prefer `observeAuthToken` when the value can change under you.
- `val observeAuthToken: Flow<String?>` — The credential as a stream, re-emitting on sign-in and sign-out.
- `val passcode: String` — The app-lock passcode. Synchronous — the lock screen compares it on each keystroke.
- `val observeLanguage: Flow<LanguageConfig>` — Selected language, re-emitting on change so the locale switch takes effect without a restart.
- `val observeDarkThemeConfig: Flow<DarkThemeConfig>` — Dark-mode preference as a stream.
- `val observeDynamicColorPreference: Flow<Boolean>` — Whether to derive the palette from platform dynamic colour.
- `val observeScreenCapturePreference: Flow<Boolean>` — Whether screenshots and screen recording are permitted.
- `val observeAnalyticsConsent: Flow<Boolean>` — The user's consent to share usage analytics.
- `val observeCrashReportingConsent: Flow<Boolean>` — The user's consent to send crash reports.
- `suspend fun setAuthToken(token: String?)` — Persist `token` into the ENCRYPTED store, or clear it when null. Call on sign-in with the value the auth endpoint returned (for Basic, the base64 of `user:password`; for OAuth, the access token) and on sign-out with null.
- `suspend fun setLanguage(language: LanguageConfig)` — Persists the app language.
- `suspend fun setThemeBrand(themeBrand: ThemeBrand)` — Persists the colour brand.
- `suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig)` — Persists the dark-mode preference.
  _…more members; read the file._

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/prefs/UserPreferencesRepositoryImpl.kt`

```kotlin
class UserPreferencesRepositoryImpl(
```
Splits user data storage between plain (UI preferences) and secure (credentials/auth state) Settings backends.

<details><summary>Used in the template — <code>feature/profile/src/commonTest/kotlin/kpt/feature/profile/identity/ProfileViewModelTest.kt:104</code></summary>

```kotlin
        val secure = MapSettings()
        ForkPreferencesRepositoryImpl(
            delegate = UserPreferencesRepositoryImpl(plain, secure, dispatchers),
            plainSettings = plain,
            secureSettings = secure,
            dispatcher = dispatchers,
        ).also { it.updateFineractUser(session) }
```

</details>

---

_9 type(s), 24 function(s)/property(ies); 32 carry KDoc at source; 0 authored example(s); 9 live call site(s)._
<!-- api-docs:end -->

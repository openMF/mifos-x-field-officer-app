# `core/datastore`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_DATASTORE.md`
> **Measured:** 9 Kotlin files, 2 test files

## Principal types

`AppReviewPromptState`, `AppReviewPromptStore`, `ProjectPreferencesRepository`, `ProjectPreferencesRepositoryImpl`, `SettingsAppReviewPromptStore`, `UserPreferencesRepository`, `UserPreferencesRepositoryImpl`

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core/datastore sha=f5d457e22548edb0f42d00d0b5d27631538c81a5 -->
## API reference

_Generated from `core/datastore` at tree `f5d457e22548` by `scripts/docs/api-docs-gen.sh`._
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

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/di/ProjectDatastoreModule.kt`

```kotlin
val ProjectDatastoreModule = module
```
THE FORK'S datastore DI seam. Empty on the neutral template — this is yours to fill. `DatastoreModule` beside it is `owner: template` and FULL-COPIES on a sync, so a binding added there is replaced on the next adopt.

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

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/prefs/ProjectPreferencesRepository.kt`

```kotlin
interface ProjectPreferencesRepository : UserPreferencesRepository
```
THE FORK'S preferences. Extends the framework's — this is yours to fill.

<details><summary>Example</summary>

```kotlin
interface ProjectPreferencesRepository : UserPreferencesRepository {
    val observeMyFlag: Flow<Boolean>
    suspend fun setMyFlag(enabled: Boolean)
}
override suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
    analytics.log("theme_changed")
    delegate.setDarkThemeConfig(darkThemeConfig)
}
```

</details>

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/kpt/core/datastore/di/ProjectDatastoreModule.kt:31</code></summary>

```kotlin
 */
val ProjectDatastoreModule = module {
    single<ProjectPreferencesRepository> {
        ProjectPreferencesRepositoryImpl(
            delegate = get<UserPreferencesRepository>(),
            // The SAME instances the framework's impl uses — an ownership boundary, not a second
            // store. Namespace fork keys so they cannot collide with a future framework preference.
```

</details>

### `core/datastore/src/commonMain/kotlin/kpt/core/datastore/prefs/ProjectPreferencesRepositoryImpl.kt`

```kotlin
class ProjectPreferencesRepositoryImpl(
```
Fork implementation of `ProjectPreferencesRepository`. `owner: fork` — never synced.

<details><summary>Example</summary>

```kotlin
override val observeMyFlag: Flow<Boolean> =
    MutableStateFlow(plainSettings.getBoolean(KEY_MY_FLAG, false))

override suspend fun setMyFlag(enabled: Boolean) = withContext(dispatcher.io) {
    plainSettings.putBoolean(KEY_MY_FLAG, enabled)
}
override suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
    analytics.log("theme_changed")
    delegate.setDarkThemeConfig(darkThemeConfig)
}
```

</details>

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/kpt/core/datastore/di/ProjectDatastoreModule.kt:32</code></summary>

```kotlin
val ProjectDatastoreModule = module {
    single<ProjectPreferencesRepository> {
        ProjectPreferencesRepositoryImpl(
            delegate = get<UserPreferencesRepository>(),
            // The SAME instances the framework's impl uses — an ownership boundary, not a second
            // store. Namespace fork keys so they cannot collide with a future framework preference.
            plainSettings = get<Settings>(named("plain")),
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

<details><summary>Used in the template — <code>core/datastore/src/commonMain/kotlin/kpt/core/datastore/di/DatastoreModule.kt:36</code></summary>

```kotlin

    single {
        UserPreferencesRepositoryImpl(
            plainSettings = get<Settings>(named("plain")),
            secureSettings = get<Settings>(named("secure")),
            dispatcher = get(),
        )
```

</details>

---

_7 type(s), 18 function(s)/property(ies); 25 carry KDoc at source; 2 authored example(s); 7 live call site(s)._
<!-- api-docs:end -->

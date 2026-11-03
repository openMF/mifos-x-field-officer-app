# `core/platform`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_PLATFORM.md`
> **Measured:** 5 Kotlin files, 1 test files

## Principal types

`AppReviewConfig`, `Permission`, `PermissionRequester`, `PermissionResult`

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core/platform sha=eacc3545af1d4b7c53b0d88826c150e746fc6b39 -->
## API reference

_Generated from `core/platform` at tree `eacc3545af1d` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

### `core/platform/src/commonMain/kotlin/kpt/core/platform/config/AppReviewConfig.kt`

```kotlin
object AppReviewConfig
```
Store identity + prompt policy for the in-app review flow.

<details><summary>Used in the template — <code>core/platform/src/commonTest/kotlin/kpt/core/platform/config/AppReviewConfigTest.kt:30</code></summary>

```kotlin
        // One launch short. Everything else maximally satisfied, so only MIN_LAUNCHES can decide.
        assertFalse(
            AppReviewConfig.shouldPromptForReview(
                launchCount = AppReviewConfig.MIN_LAUNCHES - 1,
                daysSinceInstall = Int.MAX_VALUE,
                daysSinceLastPrompt = null,
            ),
```

</details>

- `const val PLAY_STORE_PACKAGE: String = "com.mifos.mifosxdroid"` — Play Store package — `identity.app_id`.
- `const val APP_STORE_ID: String = ""` — App Store numeric id — `platforms/apple/apple.yaml#apple.app_store_id`.
- `const val MICROSOFT_STORE_PRODUCT_ID: String = ""` — Microsoft Store product id — `platforms/windows/windows.yaml#windows.store_id`.
- `const val WEB_URL: String = "https://mifos.org"` — Open-web fallback for targets with no store — `org.marketing_url`.
- `const val ENABLED: Boolean = true` — `in_app_review.enabled` — false disables the custom prompt entirely.
- `const val MIN_LAUNCHES: Int = 5` — `in_app_review.min_launches`.
- `const val MIN_DAYS_SINCE_INSTALL: Int = 3` — `in_app_review.min_days_since_install`.
- `const val COOLDOWN_DAYS: Int = 90` — `in_app_review.cooldown_days`.
- `fun shouldPromptForReview(` — Whether an AUTOMATIC review prompt is due, given counters the caller keeps. The counters are parameters rather than state owned here, so this stays a pure function of the generated thresholds.
- `val storeListing: StoreListing = StoreListing(` — The listing to hand `AppReview.configure(...)`. Built from whichever ids are non-blank.

### `core/platform/src/commonMain/kotlin/kpt/core/platform/permission/PermissionRequester.kt`

```kotlin
enum class Permission
```
A runtime permission, named by what the app wants rather than by any platform's identifier. The Android manifest strings this maps to are an implementation detail of the Android actual — they must not appear in commonMain.

<details><summary>Used in the template — <code>core/firebase/src/commonMain/kotlin/kpt/core/firebase/config/analytics/KptAnalyticsExtensions.kt:117</code></summary>

```kotlin

/** A runtime permission decision. */
fun AnalyticsHelper.trackPermission(permissionName: String, granted: Boolean) {
    logEvent(
        AnalyticsEvent(
            if (granted) KptEventTypes.PERMISSION_GRANTED else KptEventTypes.PERMISSION_DENIED,
            listOf(Param(KptParamKeys.PERMISSION_NAME, permissionName)),
```

</details>

```kotlin
sealed interface PermissionResult
```
The outcome of a permission request. Every platform returns this same vocabulary so that callers stay platform-agnostic (CORE_PLATFORM.md).

<details><summary>Used in the template — <code>core/platform/src/androidMain/kotlin/kpt/core/platform/permission/PermissionRequester.android.kt:66</code></summary>

```kotlin

    /** Asks the user for any of `permissions` not yet granted; Denied when no activity can show the dialog. */
    actual suspend fun request(permissions: List<Permission>): PermissionResult {
        val manifestPermissions = permissions.flatMap { it.manifestPermissions() }.distinct()
        // No activity means no way to show the system dialog. Denied (not DeniedPermanently) —
        // this is a host problem the caller may retry from a real activity, not a user decision.
        val host = activity
```

</details>

```kotlin
expect class PermissionRequester(context: AppContext)
```
Requests runtime permissions. Deliberately Compose-free: `core/platform` applies only `kmp.library.convention` — no Compose compiler plugin — and per CORE_PLATFORM.md an expect/actual *Composable* is a code smell.

<details><summary>Used in the template — <code>core/platform/src/androidMain/kotlin/kpt/core/platform/permission/PermissionRequester.android.kt:38</code></summary>

```kotlin
 * a screen that asks repeatedly does not leak a registration per attempt.
 */
actual class PermissionRequester actual constructor(private val context: AppContext) {

    /**
     * The hosting activity, or null when this context has none.
     *
```

</details>

---

_4 type(s), 10 function(s)/property(ies); 14 carry KDoc at source; 0 authored example(s); 4 live call site(s)._
<!-- api-docs:end -->

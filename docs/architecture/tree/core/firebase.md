# `core/firebase`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_FIREBASE.md`
> **Measured:** 11 Kotlin files, 0 test files

## Principal types

`KptAnalyticsTracker`, `KptCrashKeys`, `KptEventTypes`, `KptParamKeys`, `KptParamValues`, `LoansAnalyticsTracker`, `LoansCrashKeys`, `LoansEventTypes`, `LoansParamKeys`, `LoansParamValues`

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core/firebase sha=4c4830b57216abe615adf55db0896641735f84de -->
## API reference

_Generated from `core/firebase` at tree `4c4830b57216` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

### `core/firebase/src/commonMain/kotlin/kpt/core/firebase/config/analytics/KptAnalyticsEvents.kt`

```kotlin
object KptEventTypes
```
CROSS-CUTTING analytics event keys — TEMPLATE-OWNED, full-copied by every sync.

<details><summary>Used in the template — <code>core/firebase/src/commonMain/kotlin/kpt/core/firebase/config/analytics/KptAnalyticsExtensions.kt:39</code></summary>

```kotlin
    logEvent(
        AnalyticsEvent(
            KptEventTypes.NAVIGATION,
            listOf(
                Param(KptParamKeys.FROM_SCREEN, from),
                Param(KptParamKeys.TO_SCREEN, to),
                Param(KptParamKeys.TRIGGER, trigger),
```

</details>

- `const val LOGIN_SUCCESS = "login_success"` — Session + authentication: login success. Emitted as `login_success`.
- `const val LOGIN_FAILURE = "login_failure"` — Session + authentication: login failure. Emitted as `login_failure`.
- `const val LOGOUT = "logout"` — Session + authentication: logout. Emitted as `logout`.
- `const val SESSION_START = "session_start"` — Session + authentication: session start. Emitted as `session_start`.
- `const val SESSION_END = "session_end"` — Session + authentication: session end. Emitted as `session_end`.
- `const val SESSION_TIMEOUT = "session_timeout"` — Session + authentication: session timeout. Emitted as `session_timeout`.
- `const val BIOMETRIC_AUTH_SUCCESS = "biometric_auth_success"` — Session + authentication: biometric auth success. Emitted as `biometric_auth_success`.
- `const val BIOMETRIC_AUTH_FAILURE = "biometric_auth_failure"` — Session + authentication: biometric auth failure. Emitted as `biometric_auth_failure`.
- `const val SCREEN_VIEW = "screen_view"` — Navigation: screen view. Emitted as `screen_view`.
- `const val NAVIGATION = "navigation"` — Event logged on every destination change. One event with from/to params rather than a per-screen event, so a funnel can be built without knowing the screen set in advance.
- `const val DEEP_LINK_OPENED = "deep_link_opened"` — Navigation: deep link opened. Emitted as `deep_link_opened`.
- `const val BACK_PRESSED = "back_pressed"` — Navigation: back pressed. Emitted as `back_pressed`.
- `const val API_CALL_SUCCESS = "api_call_success"` — Network + API: api call success. Emitted as `api_call_success`.
- `const val API_CALL_FAILURE = "api_call_failure"` — Network + API: api call failure. Emitted as `api_call_failure`.
  _…more members; read the file._

```kotlin
object KptParamKeys
```
CROSS-CUTTING parameter keys — TEMPLATE-OWNED, full-copied by every sync.

<details><summary>Used in the template — <code>core/firebase/src/commonMain/kotlin/kpt/core/firebase/config/analytics/KptAnalyticsExtensions.kt:41</code></summary>

```kotlin
            KptEventTypes.NAVIGATION,
            listOf(
                Param(KptParamKeys.FROM_SCREEN, from),
                Param(KptParamKeys.TO_SCREEN, to),
                Param(KptParamKeys.TRIGGER, trigger),
            ),
        ),
```

</details>

- `const val LOGIN_METHOD = "login_method"` — Session: login method. Emitted as `login_method`.
- `const val SESSION_DURATION_MS = "session_duration_ms"` — Session: session duration ms. Emitted as `session_duration_ms`.
- `const val SCREEN_NAME = "screen_name"` — Navigation: screen name. Emitted as `screen_name`.
- `const val FROM_SCREEN = "from_screen"` — Route navigated FROM. Empty on the first destination of a session, which is how a cold start is told from an in- app move.
- `const val TO_SCREEN = "to_screen"` — Route navigated TO.
- `const val TRIGGER = "trigger"` — What caused the navigation — a tap, a back press, a deep link. Separates user intent from a programmatic redirect, which otherwise look identical in the funnel.
- `const val ENDPOINT = "endpoint"` — Network / API: endpoint. Emitted as `endpoint`.
- `const val HTTP_METHOD = "http_method"` — Network / API: http method. Emitted as `http_method`.
- `const val STATUS_CODE = "status_code"` — Network / API: status code. Emitted as `status_code`.
- `const val DURATION_MS = "duration_ms"` — Network / API: duration ms. Emitted as `duration_ms`.
- `const val SYNC_TYPE = "sync_type"` — Sync: sync type. Emitted as `sync_type`.
- `const val RECORDS_SYNCED = "records_synced"` — Sync: records synced. Emitted as `records_synced`.
- `const val CONFLICT_STRATEGY = "conflict_strategy"` — Sync: conflict strategy. Emitted as `conflict_strategy`.
- `const val ERROR_TYPE = "error_type"` — Errors: error type. Emitted as `error_type`.
  _…more members; read the file._

```kotlin
object KptParamValues
```
CROSS-CUTTING parameter values — TEMPLATE-OWNED, full-copied by every sync. Only values whose meaning is independent of any feature.

<details><summary>Used in the template — <code>core/firebase/src/commonMain/kotlin/kpt/core/firebase/config/analytics/KptAnalyticsExtensions.kt:35</code></summary>

```kotlin
    from: String,
    to: String,
    trigger: String = KptParamValues.TRIGGER_USER_ACTION,
) {
    logEvent(
        AnalyticsEvent(
            KptEventTypes.NAVIGATION,
```

</details>

- `const val TRIGGER_USER_ACTION = "user_action"` — Trigger sources: trigger user action. Emitted as `user_action`.
- `const val TRIGGER_DEEP_LINK = "deep_link"` — Trigger sources: trigger deep link. Emitted as `deep_link`.
- `const val TRIGGER_NOTIFICATION = "notification"` — Trigger sources: trigger notification. Emitted as `notification`.
- `const val TRIGGER_SYSTEM = "system"` — Trigger sources: trigger system. Emitted as `system`.
- `const val SYNC_FULL = "full"` — Sync kinds: sync full. Emitted as `full`.
- `const val SYNC_INCREMENTAL = "incremental"` — Sync kinds: sync incremental. Emitted as `incremental`.
- `const val SYNC_MANUAL = "manual"` — Sync kinds: sync manual. Emitted as `manual`.
- `const val RESULT_SUCCESS = "success"` — Outcomes: result success. Emitted as `success`.
- `const val RESULT_FAILURE = "failure"` — Outcomes: result failure. Emitted as `failure`.
- `const val RESULT_CANCELLED = "cancelled"` — Outcomes: result cancelled. Emitted as `cancelled`.

### `core/firebase/src/commonMain/kotlin/kpt/core/firebase/config/analytics/KptAnalyticsExtensions.kt`

```kotlin
fun AnalyticsHelper.trackNavigation(
```
A screen-to-screen transition. `trigger` is one of the `KptParamValues.TRIGGER_*` values.

```kotlin
fun AnalyticsHelper.trackScreenView(screenName: String)
```
A screen becoming visible. Separate from `trackNavigation` so first-render is countable alone.

```kotlin
fun AnalyticsHelper.trackApiCall(
```
One outbound API call. Intended for a single Ktor plugin rather than per-call sites — endpoint strings should already be templated (`/loans/{id}`), never interpolated with real ids.

```kotlin
fun AnalyticsHelper.trackValidationError(
```
A field-level validation failure. Never pass the rejected VALUE — only the field and the reason.

```kotlin
fun AnalyticsHelper.trackPreferenceChange(
```
A settings/preference change. `oldValue` and `newValue` must be non-PII.

```kotlin
fun AnalyticsHelper.trackPermission(permissionName: String, granted: Boolean)
```
A runtime permission decision.

```kotlin
fun AnalyticsHelper.trackTutorial(action: String, step: Int, tutorialName: String)
```
Onboarding progress. `action` is `started` / `step_completed` / `skipped` / `completed`.

### `core/firebase/src/commonMain/kotlin/kpt/core/firebase/config/analytics/KptAnalyticsTracker.kt`

```kotlin
class KptAnalyticsTracker(
```
CROSS-CUTTING analytics tracker — TEMPLATE-OWNED, full-copied by every sync. Wraps the kmptoolkit `AnalyticsHelper` with the events every app on this template raises, whatever it ships: session, navigation, network, sync, performance.

```kotlin
fun rememberKptAnalyticsTracker(): KptAnalyticsTracker
```
Composition-scoped `KptAnalyticsTracker`, remembered against the ambient `AnalyticsHelper`. A feature tracker gets its own `remember…` in its own package; they share this helper instance.

### `core/firebase/src/commonMain/kotlin/kpt/core/firebase/config/crashlytics/KptCrashExtensions.kt`

```kotlin
fun CrashReporter.setCurrentScreen(screenName: String, previousScreen: String? = null)
```
Record the screen the user is on, so a crash report opens with the right context. Call from the navigation observer that already raises `trackScreenView`.

```kotlin
fun CrashReporter.setNetworkState(online: Boolean, captivePortal: Boolean = false)
```
Connectivity at crash time. Offline-first apps behave differently offline, so a report without this is ambiguous between "broken" and "correctly degraded".

```kotlin
fun CrashReporter.setSyncState(inFlight: Boolean, pendingWrites: Int)
```
Whether a sync was in flight, and how many writes were still queued. A crash during replay of a 40-deep outbox is a different defect from a crash with an empty queue.

```kotlin
fun CrashReporter.setLastRequest(endpoint: String, statusCode: Int?)
```
The last request the app made. `endpoint` must be the TEMPLATED path (`/loans/{id}`) — an interpolated one would put a real id into the report.

```kotlin
fun CrashReporter.setAppearance(locale: String, themeMode: String)
```
Locale and theme — cheap to set, and they explain a surprising share of layout crashes.

```kotlin
fun CrashReporter.recordHandled(throwable: Throwable, context: String)
```
A non-fatal that the app handled but should not have hit — a `Result.failure` surfaced to the user, an unexpected empty state. Fatal crashes arrive on their own; these are the ones that stay invisible.

### `core/firebase/src/commonMain/kotlin/kpt/core/firebase/config/crashlytics/KptCrashKeys.kt`

```kotlin
object KptCrashKeys
```
CROSS-CUTTING crash context — TEMPLATE-OWNED, full-copied by every sync. Mirrors its `config/analytics/` sibling: this file knows about NO feature.

<details><summary>Used in the template — <code>core/firebase/src/commonMain/kotlin/kpt/core/firebase/config/crashlytics/KptCrashExtensions.kt:32</code></summary>

```kotlin
 */
fun CrashReporter.setCurrentScreen(screenName: String, previousScreen: String? = null) {
    setCustomKey(KptCrashKeys.CURRENT_SCREEN, screenName)
    previousScreen?.let { setCustomKey(KptCrashKeys.PREVIOUS_SCREEN, it) }
    log("screen -> $screenName")
}
```

</details>

- `const val CURRENT_SCREEN = "current_screen"` — Route the user is on — the single most useful key for reproducing a crash.
- `const val PREVIOUS_SCREEN = "previous_screen"` — Route they came from, which distinguishes a bad destination from a bad transition.
- `const val SESSION_ID = "session_id"` — Groups every crash and breadcrumb from one app run.
- `const val NETWORK_STATE = "network_state"` — Connectivity at crash time; separates an offline path from a server fault.
- `const val SYNC_IN_FLIGHT = "sync_in_flight"` — Whether a background sync was running — the usual source of a race.
- `const val LAST_ENDPOINT = "last_endpoint"` — Most recent request path.
- `const val LAST_STATUS_CODE = "last_status_code"` — HTTP status of the last response. Paired with `LAST_ENDPOINT` it distinguishes a crash after a 401 from one after a 500, which usually have different causes.
- `const val APP_LOCALE = "app_locale"` — Active locale, for a crash that only reproduces under one translation or script direction.
- `const val THEME_MODE = "theme_mode"` — Light/dark, for a crash confined to one palette.
- `const val PENDING_WRITES = "pending_writes"` — How many queued offline mutations were outstanding.

### `core/firebase/src/commonMain/kotlin/kpt/core/firebase/di/AnalyticsModule.kt`

```kotlin
val coreFirebaseModule: Module = module
```
Project-layer Koin module — the toolkit **default** analytics binding.

### `core/firebase/src/commonMain/kotlin/kpt/core/firebase/loans/LoansAnalyticsEvents.kt`

```kotlin
object LoansEventTypes
```
`loans` feature analytics keys — DEMO-SHOWCASE, deleted by `--clean` with the loans feature. A fork's equivalent for its own feature is fork-owned and needs no declaration.

<details><summary>Used in the template — <code>core/firebase/src/commonMain/kotlin/kpt/core/firebase/loans/LoansAnalyticsExtensions.kt:33</code></summary>

```kotlin
    logEvent(
        AnalyticsEvent(
            if (completed) LoansEventTypes.LOAN_FORM_OPENED else LoansEventTypes.LOAN_FORM_ABANDONED,
            listOf(
                Param(LoansParamKeys.FORM_STEP, step),
                Param(ParamKeys.SUCCESS, completed.toString()),
            ),
```

</details>

- `const val LOANS_LIST_VIEWED = "loans_list_viewed"` — The loan list was opened.
- `const val LOAN_DETAIL_VIEWED = "loan_detail_viewed"` — A loan's detail screen was opened. Carries `LOAN_KIND` and the principal BAND, never the loan's id or amount.
- `const val LOAN_AMORTIZATION_VIEWED = "loan_amortization_viewed"` — The amortization schedule was opened.
- `const val LOAN_FORM_OPENED = "loan_form_opened"` — The add/edit form was opened. Paired with `LOAN_FORM_ABANDONED` this gives the form's drop-off rate.
- `const val LOAN_FORM_ABANDONED = "loan_form_abandoned"` — The form was left without submitting — paired with `LOAN_FORM_OPENED` this gives the drop-off rate.
- `const val LOAN_CREATED = "loan_created"` — A loan was committed for the first time. Fires on the local commit, so it counts even for a fork whose submit never reaches a server.
- `const val LOAN_UPDATED = "loan_updated"` — An existing loan was committed again. Distinct from `LOAN_CREATED` so edit frequency is measurable on its own.
- `const val LOAN_DELETED = "loan_deleted"` — A loan was deleted. Carries only `LOAN_KIND` — a deleted loan's figures have no analytic use.
- `const val LOAN_REMINDER_SCHEDULED = "loan_reminder_scheduled"` — A payment reminder was registered with the platform scheduler.
- `const val LOAN_REMINDER_CANCELLED = "loan_reminder_cancelled"` — A payment reminder was withdrawn, because the loan was deleted or reminders were turned off.
- `const val LOAN_REMINDER_FIRED = "loan_reminder_fired"` — A payment reminder was delivered. Fired-minus-scheduled is how reminder loss on a given platform is spotted.

```kotlin
object LoansParamKeys
```
`loans` parameter keys. NOTE none of these carry money or identity. `principal` is bucketed by `LoansParamValues`, and the loan id is deliberately absent: a Firebase event is not the place to reconstruct a user's debts.

<details><summary>Used in the template — <code>core/firebase/src/commonMain/kotlin/kpt/core/firebase/loans/LoansAnalyticsExtensions.kt:35</code></summary>

```kotlin
            if (completed) LoansEventTypes.LOAN_FORM_OPENED else LoansEventTypes.LOAN_FORM_ABANDONED,
            listOf(
                Param(LoansParamKeys.FORM_STEP, step),
                Param(ParamKeys.SUCCESS, completed.toString()),
            ),
        ),
    )
```

</details>

- `const val LOAN_KIND = "loan_kind"` — The loan's category. Low-cardinality by construction, so it is safe to break every event down by it.
- `const val PRINCIPAL_BAND = "principal_band"` — Principal as a BAND, never the amount — an exact figure would make the event personal data.
- `const val TENURE_MONTHS = "tenure_months"` — Tenure in months. A raw number rather than a band — tenure is not identifying on its own the way an amount is.
- `const val RATE_BAND = "rate_band"` — APR as a band, for the same reason as `PRINCIPAL_BAND`.
- `const val FORM_STEP = "form_step"` — Which wizard step the event refers to, so abandonment can be attributed to a specific step rather than to the form as a whole.
- `const val LOAN_COUNT = "loan_count"` — How many loans the user has.
- `const val REMINDER_LEAD_DAYS = "reminder_lead_days"` — How many days before the due date the reminder is set for.

```kotlin
object LoansParamValues
```
`loans` parameter values. Bands rather than amounts. Analytics answers "do people track large loans?", which a band answers and an exact figure answers at the cost of shipping a financial profile to a third party.

<details><summary>Used in the template — <code>core/firebase/src/commonMain/kotlin/kpt/core/firebase/loans/LoansAnalyticsTracker.kt:58</code></summary>

```kotlin

    /** Add/edit form opened. [step] is one of the `LoansParamValues.FORM_STEP_*` values. */
    fun trackFormOpened(editing: Boolean, step: String = LoansParamValues.FORM_STEP_DETAILS) {
        analyticsHelper.logEvent(
            AnalyticsEvent(
                LoansEventTypes.LOAN_FORM_OPENED,
                listOf(
```

</details>

- `const val PRINCIPAL_BAND_SMALL = "lt_1k"` — Under 1,000.
- `const val PRINCIPAL_BAND_MEDIUM = "1k_10k"` — 1,000 to 10,000.
- `const val PRINCIPAL_BAND_LARGE = "10k_100k"` — 10,000 to 100,000.
- `const val PRINCIPAL_BAND_XLARGE = "gte_100k"` — 100,000 and above.
- `const val RATE_BAND_LOW = "lt_5pct"` — Under 5%.
- `const val RATE_BAND_MID = "5_15pct"` — 5% to 15%.
- `const val RATE_BAND_HIGH = "gte_15pct"` — 15% and above.
- `const val FORM_STEP_DETAILS = "details"` — Name and category.
- `const val FORM_STEP_TERMS = "terms"` — Principal, rate and tenure.
- `const val FORM_STEP_REVIEW = "review"` — Final confirmation.

### `core/firebase/src/commonMain/kotlin/kpt/core/firebase/loans/LoansAnalyticsExtensions.kt`

```kotlin
fun AnalyticsHelper.trackLoanFormStep(step: String, completed: Boolean)
```
One step of the add/edit funnel. Emitting a step event rather than only start/finish is what makes "where do people give up entering a loan?" answerable.

```kotlin
fun AnalyticsHelper.trackAmortizationViewed(kind: String, tenureMonths: Int)
```
The amortization schedule opened for a loan. Lives here rather than in the `amortization` feature because the schedule is a projection OF a loan — the funnel it belongs to is the loans funnel.

```kotlin
fun AnalyticsHelper.trackLoanReminderScheduled(leadDays: Int)
```
A reminder scheduled from `LoanReminderUseCase`, where no tracker is in scope.

### `core/firebase/src/commonMain/kotlin/kpt/core/firebase/loans/LoansAnalyticsTracker.kt`

```kotlin
class LoansAnalyticsTracker(
```
`loans` feature tracker — DEMO-SHOWCASE. It is the template's worked example of the per-feature layout: shipped and synced like template code, and deleted by `remove-demo.sh --clean` along with the `feature/loans` module it tracks.

```kotlin
fun rememberLoansAnalyticsTracker(): LoansAnalyticsTracker
```
Composition-scoped `LoansAnalyticsTracker`, sharing the ambient `AnalyticsHelper`.

### `core/firebase/src/commonMain/kotlin/kpt/core/firebase/loans/LoansCrashExtensions.kt`

```kotlin
fun CrashReporter.setLoanContext(
```
The loan being edited or viewed when a crash occurs. `principal` is banded on the way in — the caller passes the real figure and this function decides what reaches the report, exactly as `LoansAnalyticsTracker` does.

```kotlin
fun CrashReporter.setAmortizationContext(scheduleRows: Int)
```
Amortisation is the one place in this feature that builds an unbounded list — one row per month — so the row count is the first thing worth knowing about an OOM or a jank report here.

```kotlin
fun CrashReporter.setLoanFormStep(step: String)
```
Which step of the add/edit form was open. Pairs with the analytics funnel of the same name.

```kotlin
fun CrashReporter.setLoanCount(count: Int)
```
How many loans the user holds — list-rendering crashes scale with this.

### `core/firebase/src/commonMain/kotlin/kpt/core/firebase/loans/LoansCrashKeys.kt`

```kotlin
object LoansCrashKeys
```
`loans` crash context — DEMO-SHOWCASE, deleted by `--clean` with the loans feature. The same file in a fork's own feature package is fork-owned.

<details><summary>Used in the template — <code>core/firebase/src/commonMain/kotlin/kpt/core/firebase/loans/LoansCrashExtensions.kt:36</code></summary>

```kotlin
    tenureMonths: Int,
) {
    setCustomKey(LoansCrashKeys.LOAN_KIND, kind)
    setCustomKey(LoansCrashKeys.PRINCIPAL_BAND, principalBand(principal))
    setCustomKey(LoansCrashKeys.TENURE_MONTHS, tenureMonths.toString())
    log("loans -> $kind / ${tenureMonths}mo")
}
```

</details>

- `const val LOAN_KIND = "loans_kind"` — Category of the loan being acted on.
- `const val PRINCIPAL_BAND = "loans_principal_band"` — Principal as a BAND, never the amount. A crash report leaves the device, so an exact balance must not be in it.
- `const val TENURE_MONTHS = "loans_tenure_months"` — Tenure in months — the input that drives schedule size, and so the memory a schedule crash scales with.
- `const val SCHEDULE_ROWS = "loans_schedule_rows"` — How many rows the amortization schedule produced, which is what an out-of-memory crash there scales with.
- `const val FORM_STEP = "loans_form_step"` — Which wizard step was active when the crash happened, so a step-specific crash is not averaged across the whole form.
- `const val LOAN_COUNT = "loans_count"` — How many loans the user has.

---

_10 type(s), 105 function(s)/property(ies); 115 carry KDoc at source; 0 authored example(s); 8 live call site(s)._
<!-- api-docs:end -->

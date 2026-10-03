# `core-base/security`

> **Layer:** `core-base` — framework-shared. Generators **consume** these contracts and
> **never write here**; a fix belongs upstream in the template, not in a fork.
> **Instruction surface:** `CORE_BASE_SECURITY.md` — the generator-facing instruction for this module,
> held in the framework at `training-layer/instructions/stream-first/latest/`. This guide
> is the architecture SoT; that surface is how it reaches codegen, and
> `/kmp-project-template-retrain` keeps the two in step.
> **Shape:** 43 Kotlin files (7 test) · source sets: `androidMain`, `commonMain`, `commonTest`, `desktopMain`, `jsCommonMain`, `jsCommonTest`, `jsMain`, `jsTest`, `nativeMain`, `wasmJsMain`, `wasmJsTest`



## When implementing a feature

**You do not write here.** `core-base` is framework-shared: a feature consumes these
contracts and never modifies them. If a feature seems to need a change here, that is a
TEMPLATE change — it flows upstream as a draft PR (RULE-TEMPLATE-MODULE-FIX-UPSTREAM-001),
never a local edit, because every fork shares this code and a local fix is drift.

What a feature *does* do is import from here and satisfy the contracts this module
defines. The module guides under `../core/` show where the feature-side code goes.

## Position in the module graph

No module dependencies — this is a leaf.

**Consumed by** 1 module(s): `core-base/network`

## Codegen contracts

**None.** Nothing here is declared by annotation, so there is no aggregate to
generate and no propagation target. A generator writing into this module takes its
idiom from `CORE_BASE_SECURITY.md`.

## Principal types

- **`BiometricAuthenticator`** — Platform-agnostic biometric authentication interface.
- **`CertificatePinConfig`** — Configuration for TLS certificate pinning per hostname.
- **`DeepLinkValidator`** — Validates deep link URIs against a whitelist of allowed schemes and hosts
- **`FailedAttemptTracker`** — Tracks failed authentication attempts and triggers lockout or data wipe
- **`SecureAuthManager`** — Unified authentication manager that coordinates failure tracking,
- **`SecureNavHandler`** — Deep link security handler wrapping [DeepLinkValidator].
- **`SecureWiper`** — Securely wipes sensitive data from storage and memory.
- **`SecurityConfig`** — Central configuration for all security behavior. Controls debug/release gates
- **`SecurityPolicy`** — Configurable security policy. Consumer apps can adjust thresholds
- **`SecurityState`** — Observable security state exposed to the UI layer via [LocalSecurityState].
- **`SensitiveString`** — Zeroable credential wrapper backed by [CharArray] instead of [String].
- **`SessionManager`** — Manages user session lifecycle with inactivity timeout.
- …and 1 more documented types

Undocumented: `BiometricResult`, `FailureAction`

## Demo showcase exposure

**None.** No `demo/` package and no `// demo:begin` fence — `remove-demo.sh` does not
touch this module, so a stripped fork keeps it verbatim.

## Tests

7 test file(s) under `core-base/security/src/commonTest/`. 
Shared idiom: `CORE_TESTING.md`.



## Sample implementation

Real code from this module — the shape a generator should follow here.

```kotlin
/**
 * Manages user session lifecycle with inactivity timeout.
 *
 * Call [touch] on every user interaction to reset the inactivity timer.
 * Call [checkTimeout] periodically (e.g., on app foreground) to verify
 * the session hasn't expired.
 *
 * @param policy Security policy with session timeout configuration.
 * @param onSessionExpired Callback invoked when session expires.
 * @param clock Time source for testability; defaults to system clock.
 */
class SessionManager(
    private val policy: SecurityPolicy,
    private val onSessionExpired: () -> Unit = {},
    private val clock: () -> Long = { currentTimeMillis() },
) {
    private val _isSessionActive = MutableStateFlow(false)
    val isSessionActive: StateFlow<Boolean> = _isSessionActive.asStateFlow()

    @kotlin.concurrent.Volatile
    private var lastActivityTime: Long = clock()

    /** Start a new session. */
    fun startSession() {
        lastActivityTime = clock()
        _isSessionActive.value = true
        Logger.d("SessionManager") { "Session started" }
    }

    /** Record user activity to reset the inactivity timer. */
    fun touch() {
        if (_isSessionActive.value) {
            lastActivityTime = clock()
        }
    }

    /**
     * Check if the session has timed out due to inactivity.
     * If expired, invokes [onSessionExpired] and returns true.
     */
    fun checkTimeout(): Boolean {
        if (!_isSessionActive.value) return false

        val elapsed = clock() - lastActivityTime
        val timeoutMs = policy.sessionTimeoutMinutes * 60 * 1_000L
    // … (excerpt)
```

Source: [`src/commonMain/kotlin/kpt/core/base/security/SessionManager.kt`](../../../../core-base/security/src/commonMain/kotlin/kpt/core/base/security/SessionManager.kt) — excerpt; read the file for the full implementation.

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

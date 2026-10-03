# feature/signin — migration ledger (module was `feature/auth` until 2026-10-02)

> Written at Phase 9 T3 per `FEATURE_LAYER_MIGRATION_SPEC.md` §7. Consumed by Phase 10 (S1).
> Original reference: `6b66e8a43`. Deleted `core/domain` reference: `897ffdac1`.

**Status:** audit complete; port not started. The module as it stands was **authored, not ported**
(spec §1) — this ledger is the gap list S1 T3 closes.

---

## 1. Original inventory — `6b66e8a43:feature/auth/`

| file | present now? |
|---|---|
| `src/commonMain/kotlin/com/mifos/feature/auth/login/SignInScreen.kt` (**258 lines**) | ✗ replaced by a 120-line authored file |
| `src/commonMain/kotlin/com/mifos/feature/auth/login/SignInViewModel.kt` | ✗ replaced |
| `src/commonMain/kotlin/com/mifos/feature/auth/login/LoginUiState.kt` | ✗ absent — no state type |
| `src/commonMain/kotlin/com/mifos/feature/auth/navigation/AuthNavigation.kt` | ✗ absent — see §6 |
| `src/commonMain/kotlin/com/mifos/feature/auth/di/AuthModule.kt` | ✓ present |
| `src/commonMain/composeResources/values/strings.xml` | ✓ present — **CORRECTED 2026-10-01**, see below |
| `src/commonMain/composeResources/drawable/feature_signin_mifos_logo.jpg` | **✗ MISSING** |
| `src/androidMain/AndroidManifest.xml` | ✓ present — **CORRECTED 2026-10-01** |
| `build.gradle.kts` · `consumer-rules.pro` · `proguard-rules.pro` · `README.md` | ✓ / n-a |

Current module adds `SignInRoute.kt` and `TestTags.kt` (both template-idiomatic, keep) and
`commonTest/SignInViewModelTest.kt`.

**Original `LoginUiState`:** `Empty` · `ShowProgress` · `ShowError(StringResource)` ·
`ShowValidationError(usernameError, passwordError)` · `HomeActivityIntent` · `PassCodeActivityIntent`.
Six states. The authored replacement has no equivalent type, so validation-error and
passcode-vs-home routing have nowhere to live.

## 2. Use-case classification — 3 injected, 9 references

| use-case | verdict | becomes |
|---|---|---|
| `LoginUseCase` | **DROP-to-repository** | `AuthCommandRepository.authenticate(PostAuthenticationRequest): MutationResult<PostAuthenticationResponse>`. The use-case body was `flow { emit(loginRepository.login(u,p)) }.asDataStateFlow()` — pure plumbing. |
| `UsernameValidationUseCase` | **REWRITE to `kpt.core.domain.validation`** | rule: non-empty, length ≥ 4. Not ported — the original calls `getString(Res.string…)`, making it `suspend` and Compose-coupled (spec §2). New shape returns a typed reason; the screen maps it to a string. |
| `PasswordValidationUseCase` | **REWRITE to `kpt.core.domain.validation`** | rule: non-empty, length ≥ 6. Same reasoning. |

`ValidationResult(success, message)` is replaced by the typed-reason return; `message: String?` is what
forced the Compose coupling.

S1 T4 (server-config) needs the other five validators from the same surface, so S1 authors
`kpt.core.domain.validation` once and both tasks consume it.

## 3. Substrate mapping

| concern | original | new |
|---|---|---|
| login write | `LoginRepository.login(username, password)` | `kpt.core.data.auth.AuthCommandRepository.authenticate(…)` |
| session persistence | `com.mifos.core.datastore.UserPreferencesRepository.updateUser(User)` | `kpt.core.datastore.prefs.UserPreferencesRepository` (fork prefs also on `ProjectPreferencesRepository` — confirm which owns `passcode` during the port) |
| passcode read | `prefManager.settingsInfo.map { it.passcode }` | same repository; drives the `UserLocked` root state, which S1 T1 wires |
| reads | none — login is write-only | none |

No store is involved. Login is a mutation, not a cached read.

## 4. Write policy

| mutation | policy | why |
|---|---|---|
| `authenticate` | **`OnlineRequired`** | an authentication cannot be queued. A credential check that "succeeds" locally and syncs later would admit a user the server never agreed to. On `MutationResult.Blocked` the screen shows the offline message — never a spinner. |
| `updateUser` (prefs) | local write, not a `MutationGateway` mutation | preferences are device state, not server state |

## 5. New-state decisions

| state | decision |
|---|---|
| `Empty` | n/a — no list on this screen |
| `NoNetwork` | render the offline message with a Retry that re-attempts `authenticate`. New: the original had no offline branch. |
| `Conflicted` | n/a — no offline write, so no conflict is reachable |
| `Failed(rolledBack)` | Snackbar carrying the server's message; **username and password preserved in the fields**. The original showed a Snackbar from a `Scaffold`; the authored replacement dropped the Scaffold entirely, so there is nowhere to show one. |
| validation errors | typed reason from §2's validator surface → per-field error text, matching the original's `ShowValidationError(usernameError, passwordError)` |
| success | `authenticated == true` → persist session → `onLoggedIn()`. The original branched `HomeActivityIntent` vs `PassCodeActivityIntent`; under the template, passcode is the shell's `userUnlockDestination()` reached via `RootNavState.UserLocked`, so the screen emits one success signal and the root state machine decides. |

Note the original treats `authenticated != true` on an HTTP-200 as a login failure — Fineract can
return 200 with `authenticated: false`. Preserve that check; dropping it admits a failed login.

## 6. Nav registration

- `signInDestination(onLoggedIn: () -> Unit)` stays **unannotated** — `@FeatureDestination` registers on
  the *authenticated* graph, which is exactly where the sign-in gate must not be. The shell installs it
  (S1 T1).
- The original's `authNavGraph(navigateHome, navigatePasscode, updateServerConfig)` becomes a
  shell-side `authNavGraph(navController)`; the three callbacks collapse because (a) home vs passcode is
  now the root state machine's decision, and (b) server-config is a `@FeatureDestination` the login
  screen navigates to.
- No tab. No deep link.
- Build edge required: `"commonMainImplementation"(project(":feature:signin"))` in root
  `feature-deps.gradle.kts` — present as of `77e9433e7`. Registration is three steps (spec §4).

## 7. Deviations from the original

| deviation | reason |
|---|---|
| `LoginUiState`'s `PassCodeActivityIntent` not reproduced | the root state machine owns passcode routing under the template; a second source of that decision would diverge from `RootNavViewModel` |
| validators rewritten rather than ported | spec §2 — the originals are Compose-coupled and `suspend`; the rules are preserved exactly (≥4, ≥6) |
| `DataState` → `ScreenState`/`MutationResult` | substrate change, spec §3 |
| **OPEN — plaintext password persistence** | the original writes `User(username, password = password, …)`, storing the raw password in preferences. The new `kpt.core.model.objects.users.User` **also** declares `password: String?`, so the weakness carried into the substrate. `base64EncodedAuthenticationKey` is persisted alongside it and is the credential Fineract needs for subsequent Basic auth, which suggests the raw password is redundant. **Not decided here** — dropping it may break a re-auth path this ledger has not traced. S1 T3 must either justify keeping it in writing or remove it and prove re-auth still works. It must not be carried over silently. |

---

## Audit verdict

**Not migrated** — but less by absence than by silent divergence, which the correction below revises.
One artifact missing (the logo drawable), 3 of 9 original strings absent, the screen 138 lines shorter,
**a validation threshold changed from 4 to 5 with the user-facing copy rewritten to match the invented
number**, and one open security question. S1 T3 closes everything except the plaintext-password
question, which needs a decision rather than a port.

The threshold regression is the finding that matters: a missing file fails loudly at compile time,
while a changed rule ships and quietly locks out users whose username is four characters long.

### Correction — 2026-10-01 (Phase 10 T3)

Two rows above originally read **MISSING** and were wrong. `strings.xml` and `AndroidManifest.xml`
both exist. The Phase 9 audit enumerated the module with
`find feature/auth -name "*.kt" -o -name 'build.gradle.kts'` — a filter that excludes `.xml`
entirely — and then reported absence from that listing. The measurement could not have seen them.

The real defects in those two artifacts are different, and one is worse than a missing file:

| artifact | actual finding |
|---|---|
| `strings.xml` | present with 7 strings, but **3 of the original's 9 are absent**: `feature_signin_enter_credentials`, `feature_signin_error_enter_credentials`, `feature_signin_error_not_connected_internet`. It also adds `feature_signin_error_offline`, which is a genuine improvement (the substrate can now distinguish offline). |
| `strings.xml` copy | `"Username must be at least 5 characters"` **hardcodes a threshold into user-facing copy**, and hardcodes the WRONG one — the original is 4. The original's copy carried no number (`"Invalid username length"`), so it could not drift. |
| `AndroidManifest.xml` | present; no finding. |
| drawable logo | genuinely missing — the original's `feature_signin_mifos_logo.jpg` is not in the module. |

**The regression this correction uncovered.** `SignInViewModel` declares its own
`MIN_USERNAME = 5`, while the original `UsernameValidationUseCase` rejects `length < 4`. A
four-character username that signed in on the original app is refused by this one. No requirement
changed — the number was invented while re-authoring, and the string copy was then written to match
the invented number. This is the single clearest instance of why §1 audits rather than trusts
existing code.

Fix (T3): the ViewModel consumes `kpt.core.domain.validation.CredentialRules`, whose thresholds are
preserved verbatim from `897ffdac1`, and the copy stops naming a number.

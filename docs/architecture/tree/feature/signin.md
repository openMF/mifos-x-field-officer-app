# `feature/signin/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 19 tracked files — 9× `.kt`, 5× `.png`, 2× `.md`, 2× `.xml`, 1× `.kts`

**9 Kotlin files** — 1 screen(s), 1 ViewModel(s).

Screens: `SignInScreen`

ViewModels: `SignInViewModel`

**Consumes:** `core/store/auth`, `core/store/serverconfig`, `core/store/session`, `core/store/testing`

### Docs in the tree

| Path | |
|---|---|
| `feature/signin/MIGRATION.md` | feature/signin — migration ledger (module was `feature/auth` until 2026-10-02) |
| `feature/signin/README.md` | :feature:signin |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/signin sha=7d438fdbaea94f27c334392445ddd93e7e88e82f -->
## API reference

_Generated from `feature/signin` at tree `7d438fdbaea9` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/signin/src/commonMain/kotlin/kpt/feature/signin/SignInRoute.kt`

```kotlin
data object SignInRoute
```
_No KDoc at source._

```kotlin
fun NavController.navigateToSignIn(navOptions: NavOptions? = null) = navigate(SignInRoute, navOptions)
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.signInDestination(
```
Login is NOT annotated `@FeatureDestination`. That annotation registers a destination on the AUTHENTICATED graph, which is exactly where login must not be — it is the gate in front of it.

### `feature/signin/src/commonMain/kotlin/kpt/feature/signin/di/SignInModule.kt`

```kotlin
val SignInModule = module
```
_No KDoc at source._

### `feature/signin/src/commonMain/kotlin/kpt/feature/signin/ui/SignInScreen.kt`

```kotlin
fun SignInScreen(
```
Sign-in screen. **UI source: the stitch renders under `idea-layer/mockups/signin/stitch/`** — the highest design tier on disk, so per RULE-UI-SOURCE-001 US9 this file is converted from them, not from `ui.yaml`.

<details><summary>Used in the template — <code>feature/signin/src/commonMain/kotlin/kpt/feature/signin/SignInRoute.kt:36</code></summary>

```kotlin
) {
    composableWithStayTransitions<SignInRoute> {
        SignInScreen(
            onLoggedIn = onLoggedIn,
            onUpdateServerConfig = onUpdateServerConfig,
        )
    }
```

</details>

### `feature/signin/src/commonMain/kotlin/kpt/feature/signin/ui/SignInTestTags.kt`

```kotlin
object SignInTestTags
```
Stable handles for the login screen — asserted by UI tests, never shown to a user.

<details><summary>Used in the template — <code>feature/signin/src/commonMain/kotlin/kpt/feature/signin/ui/SignInScreen.kt:189</code></summary>

```kotlin
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .testTag(SignInTestTags.CONSENT),
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
```

</details>

- `const val SCREEN = "signin:screen"`
- `const val USERNAME = "signin:username"`
- `const val PASSWORD = "signin:password"`
- `const val DEMO_CREDENTIALS = "signin:demo-credentials"`
- `const val SUBMIT = "signin:submit"`
- `const val ERROR = "signin:error"`
- `const val PROGRESS = "signin:progress"`
- `const val LOGO = "signin:logo"` — Restored in S1 T3 — absent from the authored replacement.
- `const val TITLE = "signin:title"` — The headline + prompt block. The stitch mockup renders TWO lines here (app name, then the prompt); the pre-migration screen rendered only the prompt.
- `const val PROGRESS_OVERLAY = "signin:progress-overlay"` — The modal submit overlay (`ui.yaml#components.progress_overlay`, RE-EXTRACT D4). Distinct from `PROGRESS`: the port replaced the login button with an inline spinner, which is what PROGRESS tagged.
- `const val PASSWORD_TOGGLE = "signin:password-toggle"` — The password visibility toggle. It renders only while the password field has NO error — on error the slot becomes a non-interactive error icon (ui.yaml#password_field.trailing_slot).
- `const val INSTANCE_CHIP = "signin:instance-chip"` — The instance chip: which Fineract host/tenant this sign-in targets. Replaces v1's blind full-width server-config button, whose tag is retired in v2.
- `const val CONSENT = "signin:consent"` — The legal consent line, pinned at the bottom. Absent in v1.
- `const val BIOMETRIC = "signin:biometric"` — Biometric unlock. Present ONLY when there is a stored session AND the device can perform it, so a test asserting its absence is asserting a real product rule, not a layout accident.
  _…more members; read the file._

### `feature/signin/src/commonMain/kotlin/kpt/feature/signin/ui/SignInViewModel.kt`

```kotlin
class SignInViewModel(
```
Sign-in against the configured Fineract instance.

<details><summary>Used in the template — <code>feature/signin/src/commonMain/kotlin/kpt/feature/signin/di/SignInModule.kt:17</code></summary>

```kotlin

val SignInModule = module {
    viewModelOf(::SignInViewModel)
}
```

</details>

```kotlin
data class SignInState(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/signin/src/commonMain/kotlin/kpt/feature/signin/ui/SignInScreen.kt:153</code></summary>

```kotlin
@Composable
internal fun SignInContent(
    state: SignInState,
    onAction: (SignInAction) -> Unit,
    onUpdateServerConfig: () -> Unit,
    modifier: Modifier = Modifier,
) {
```

</details>

```kotlin
enum class SignInError
```
Distinct causes, because the officer's next step differs for each.

<details><summary>Used in the template — <code>feature/signin/src/commonMain/kotlin/kpt/feature/signin/ui/SignInScreen.kt:326</code></summary>

```kotlin
        // the server rejected it with a message the user could do nothing about.
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None),
        isError = state.error == SignInError.UsernameTooShort,
        errorText = (state.error == SignInError.UsernameTooShort)
            .takeIf { it }
            ?.let { stringResource(Res.string.feature_signin_error_username_length) },
        // D2 — v1 renders a trailing error icon on this field when its rule failed.
```

</details>

```kotlin
sealed interface SignInAction
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/signin/src/commonMain/kotlin/kpt/feature/signin/ui/SignInScreen.kt:154</code></summary>

```kotlin
internal fun SignInContent(
    state: SignInState,
    onAction: (SignInAction) -> Unit,
    onUpdateServerConfig: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
```

</details>

```kotlin
sealed interface SignInEvent
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/signin/src/commonMain/kotlin/kpt/feature/signin/ui/SignInScreen.kt:138</code></summary>

```kotlin
        viewModel.eventFlow.collect { event ->
            when (event) {
                SignInEvent.LoggedIn -> onLoggedIn()
            }
        }
    }
```

</details>

---

_7 type(s), 18 function(s)/property(ies); 12 carry KDoc at source; 0 authored example(s); 7 live call site(s)._
<!-- api-docs:end -->

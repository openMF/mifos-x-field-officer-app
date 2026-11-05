# `feature/onboarding/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 46 tracked files — 21× `.xml`, 15× `.png`, 9× `.kt`, 1× `.kts`

**9 Kotlin files** — 1 screen(s), 1 ViewModel(s).

Screens: `OnboardingScreen`

ViewModels: `OnboardingViewModel`

**Consumes:** `core/store/user`

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/onboarding sha=eb1744817f069e48c10b7ac866883844d499c572 -->
## API reference

_Generated from `feature/onboarding` at tree `eb1744817f06` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/onboarding/src/commonMain/kotlin/kpt/feature/onboarding/OnboardingRoute.kt`

```kotlin
data object OnboardingRoute
```
First-run onboarding — three intro pages and the privacy consent page.

```kotlin
fun NavController.navigateToOnboarding(navOptions: NavOptions? = null) = navigate(OnboardingRoute, navOptions)
```
Opens onboarding.

```kotlin
fun NavGraphBuilder.onboardingDestination(onOpenServerConfig: () -> Unit)
```
Onboarding is NOT annotated `@FeatureDestination`, for the same reason sign-in is not: that annotation registers onto the AUTHENTICATED graph, and onboarding runs before sign-in.

### `feature/onboarding/src/commonMain/kotlin/kpt/feature/onboarding/di/OnboardingModule.kt`

```kotlin
val OnboardingModule = module
```
Koin bindings for the onboarding feature's view model.

### `feature/onboarding/src/commonMain/kotlin/kpt/feature/onboarding/ui/OnboardingTestTags.kt`

```kotlin
object OnboardingTestTags
```
Stable handles for the onboarding screen — the Preview, the Compose UI test and the Maestro flow (idea-layer/screens/onboarding/maestro) all read these, never a hand-typed string.

<details><summary>Used in the template — <code>feature/onboarding/src/commonMain/kotlin/kpt/feature/onboarding/ui/OnboardingScreen.kt:177</code></summary>

```kotlin
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .testTag(OnboardingTestTags.SCREEN)
            .pointerInput(state.page) {
                // A horizontal swipe is the same as Next / back and never saves.
                var dragged = 0f
                detectHorizontalDragGestures(
```

</details>

- `const val SCREEN = "onboarding_pager"` — The screen root.
- `const val SKIP = "onboarding_skip"` — Skip, on the intro pages.
- `const val BRAND_MARK = "onboarding_brand_mark"` — The Mifos X mark.
- `const val TITLE = "onboarding_title"` — The intro page's title.
- `const val BODY = "onboarding_body"` — The intro page's body.
- `const val PAGE_INDICATOR = "onboarding_page_indicator"` — The page dots.
- `const val NEXT = "onboarding_next"` — Next, on the intro pages.
- `const val PRIVACY_TITLE = "onboarding_privacy_title"` — The privacy page's title.
- `const val ANALYTICS_SWITCH = "onboarding_analytics_switch"` — The "Share usage analytics" switch row.
- `const val CRASH_REPORTS_SWITCH = "onboarding_crash_reports_switch"` — The "Send crash reports" switch row.
- `const val AGREEMENT = "onboarding_agreement"` — "By using our service you accept our Terms & Conditions and Privacy Policy." — one localized sentence whose two links are inline `LinkAnnotation`s (so each locale keeps its own word order).
- `const val CONNECT_INSTANCE = "onboarding_connect_instance"` — "Connect to your Mifos instance".
- `const val ALLOW_GET_STARTED = "onboarding_allow_get_started"` — "Allow and get started" — the primary, one-tap consent.
- `const val CONTINUE_CHOICES = "onboarding_continue_choices"` — "Continue without sharing" / "Continue with my choices".
  _…more members; read the file._

### `feature/onboarding/src/commonMain/kotlin/kpt/feature/onboarding/ui/OnboardingViewModel.kt`

```kotlin
class OnboardingViewModel(
```
First-run onboarding: three intro pages and the privacy page (idea-layer/screens/onboarding).

<details><summary>Used in the template — <code>feature/onboarding/src/commonMain/kotlin/kpt/feature/onboarding/di/OnboardingModule.kt:18</code></summary>

```kotlin
/** Koin bindings for the onboarding feature's view model. */
val OnboardingModule = module {
    viewModelOf(::OnboardingViewModel)
}
```

</details>

```kotlin
enum class OnboardingPage
```
The four onboarding pages, in order.

<details><summary>Used in the template — <code>feature/onboarding/src/commonMain/kotlin/kpt/feature/onboarding/ui/OnboardingScreen.kt:147</code></summary>

```kotlin
    // System back steps to the previous page; on the first page it falls through to the system, which leaves
    // without saving — both choices stay off and onboarding shows again next launch.
    BackHandler(enabled = state.page != OnboardingPage.Clients) {
        viewModel.trySendAction(OnboardingAction.Previous)
    }

    OnboardingScreenContent(
```

</details>

```kotlin
data class OnboardingState(
```
What the onboarding screen renders.

<details><summary>Used in the template — <code>feature/onboarding/src/commonMain/kotlin/kpt/feature/onboarding/ui/OnboardingScreen.kt:164</code></summary>

```kotlin
@Composable
internal fun OnboardingScreenContent(
    state: OnboardingState,
    onAction: (OnboardingAction) -> Unit,
    onOpenServerConfig: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenTerms: () -> Unit,
```

</details>

```kotlin
sealed interface OnboardingAction
```
Intents accepted by `OnboardingViewModel`.

<details><summary>Used in the template — <code>feature/onboarding/src/commonMain/kotlin/kpt/feature/onboarding/ui/OnboardingScreen.kt:148</code></summary>

```kotlin
    // without saving — both choices stay off and onboarding shows again next launch.
    BackHandler(enabled = state.page != OnboardingPage.Clients) {
        viewModel.trySendAction(OnboardingAction.Previous)
    }

    OnboardingScreenContent(
        state = state,
```

</details>

---

_6 type(s), 17 function(s)/property(ies); 23 carry KDoc at source; 0 authored example(s); 5 live call site(s)._
<!-- api-docs:end -->

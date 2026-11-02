# `core/ui`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_UI.md`
> **Measured:** 14 Kotlin files, 0 test files

**Defines annotations:** `@FeatureTab`

## Principal types

`FeatureTab`, `FloatingActionButtonContent`, `KptPullToRefreshState`, `NavigationItem`, `PasswordChecker`, `PasswordStrength`, `PasswordStrengthResult`, `PasswordStrengthState`, `RevealDirection`, `RevealState`, `RevealValue`

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core/ui sha=85afc92e30cf0bb9555ada7da748b0557ca78e7e -->
## API reference

_Generated from `core/ui` at tree `85afc92e30cf` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

### `core/ui/src/commonMain/kotlin/kpt/core/ui/bottombar/KptBottomBar.kt`

```kotlin
fun KptBottomBar(
```
App-level bottom navigation bar, driven by the generated tab registry. Tabs register by `@FeatureTab` annotation — never by editing a list here, which would make adding a feature a two-place change and silently drop it if one were missed.

### `core/ui/src/commonMain/kotlin/kpt/core/ui/bottombar/KptNavigationBarItem.kt`

```kotlin
fun RowScope.KptNavigationBarItem(
```
One item in `KptBottomBar`: icon, label and selected state.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/bottombar/KptBottomBar.kt:44</code></summary>

```kotlin
    ) {
        navigationItems.forEach { navigationItem ->
            KptNavigationBarItem(
                contentDescriptionRes = navigationItem.contentDescriptionRes,
                selectedIcon = navigationItem.selectedIcon,
                unselectedIcon = navigationItem.icon,
                isSelected = selectedItem == navigationItem,
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/bottombar/KptNavigationRail.kt`

```kotlin
fun KptNavigationRail(
```
The side rail used instead of a bottom bar on wide windows — same tab source, different surface.

### `core/ui/src/commonMain/kotlin/kpt/core/ui/bottombar/KptNavigationRailItem.kt`

```kotlin
fun ColumnScope.KptNavigationRailItem(
```
One item in `KptNavigationRail`.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/bottombar/KptNavigationRail.kt:69</code></summary>

```kotlin
        ) {
            navigationItems.forEach { navigationItem ->
                KptNavigationRailItem(
                    contentDescriptionRes = navigationItem.contentDescriptionRes,
                    selectedIconRes = navigationItem.selectedIcon,
                    unselectedIconRes = navigationItem.icon,
                    isSelected = navigationItem == selectedItem,
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/input/PasswordStrengthIndicator.kt`

```kotlin
fun PasswordStrengthIndicator(
```
Live strength meter for a password field, from `PasswordChecker`'s verdict. Advisory only: it reflects the rules, it does not enforce them — validation belongs at submit.

```kotlin
enum class PasswordStrengthState
```
Rendered strength bands, from weakest to strongest.

### `core/ui/src/commonMain/kotlin/kpt/core/ui/input/RevealSwipe.kt`

```kotlin
fun RevealSwipe(
```
Swipe-to-reveal row: drag aside to expose actions (delete, archive) behind the content. The revealed actions must ALSO be reachable another way — a swipe is invisible to a screen reader and unavailable to keyboard or pointer users.

```kotlin
fun BaseRevealSwipe(
```
Unstyled `RevealSwipe` core — gesture and offset handling with no chrome, for a caller that wants its own presentation.

```kotlin
enum class RevealDirection
```
Which side(s) a row may be dragged toward to reveal its actions.

```kotlin
enum class RevealValue
```
Possible values of `RevealState`.

```kotlin
fun rememberRevealState(
```
Create and `remember` a `RevealState` with the default animation clock.

```kotlin
data class RevealState(
```
Current offset and reveal state of one `RevealSwipe` row; hoist it to reset a row programmatically.

```kotlin
suspend fun RevealState.reset()
```
Reset the component to the default position, with an animation.

```kotlin
suspend fun RevealState.resetFast()
```
Reset the component to the default position, with an animation.

### `core/ui/src/commonMain/kotlin/kpt/core/ui/navigation/FeatureTab.kt`

```kotlin
public annotation class FeatureTab
```
Marks a `NavigationItem` object as a bottom-navigation tab contributed by a feature.

### `core/ui/src/commonMain/kotlin/kpt/core/ui/navigation/NavigationItem.kt`

```kotlin
interface NavigationItem
```
Represents a user-interactable item to navigate a user via the bottom app bar or navigation rail.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/bottombar/KptBottomBar.kt:30</code></summary>

```kotlin
@Composable
fun KptBottomBar(
    navigationItems: List<NavigationItem>,
    selectedItem: NavigationItem?,
    onClick: (NavigationItem) -> Unit,
    modifier: Modifier = Modifier,
    /** Show each tab's name under its icon. See `KptNavigationBarItem.labelRes`. */
```

</details>

- `val selectedIcon: ImageVector` — The resource ID for the icon representing the tab when it is selected.
- `val icon: ImageVector` — Resource id for the icon representing the tab.
- `val labelRes: StringResource` — Resource id for the label describing the tab.
- `val contentDescriptionRes: StringResource` — Resource id for the content description describing the tab.
- `val graphRoute: String` — Route of the tab's graph.
- `val startDestinationRoute: String` — Route of the tab's start destination.
- `val testTag: String` — The test tag of the tab.
- `val inlineTab: Boolean get() = true` — Whether this tab renders INLINE inside the navbar scaffold — its content swaps within the inner NavHost so the bottom bar stays visible and the tab keeps its own back stack (exactly like the backbone Home/Profile tabs) — versus a FULL-SCREEN destination pushed on the outer authenticated graph (the bottom bar is hidden; e.g. an immersive focus-timer that declares `bottom_navigation_visible: false`). Default `true` (inline). A full-screen tab overrides to `false`. An inline extra tab (beyond Home/Profile) MUST also register its start destination via `TabRegistry.extraInlineTabDestinations` so the inner NavHost can host it; otherwise the tab has nothing to render inline.

### `core/ui/src/commonMain/kotlin/kpt/core/ui/scaffold/KptPullToRefreshState.kt`

```kotlin
data class KptPullToRefreshState(
```
Data class representing the pull-to-refresh state and behavior.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/scaffold/KptScaffold.kt:57</code></summary>

```kotlin
    containerColor: Color = KptTheme.colorScheme.background,
    floatingActionButtonContent: FloatingActionButtonContent? = null,
    pullToRefreshState: KptPullToRefreshState = rememberKptPullToRefreshState(),
    contentWindowInsets: WindowInsets = ScaffoldDefaults
        .contentWindowInsets
//        .union(WindowInsets.displayCutout)
        .only(WindowInsetsSides.Horizontal),
```

</details>

```kotlin
fun rememberKptPullToRefreshState(
```
Remembers and returns a `KptPullToRefreshState` instance.

```kotlin
fun rememberKptPullToRefreshState(
```
Bridge: derive a `KptPullToRefreshState` from a `PagingScreenStream` so `KptScaffold` / `KptRootScaffold` can drive pull-to-refresh on paginated screens without the consumer wiring isRefreshing/onRefresh manually.

<details><summary>Example</summary>

```kotlin
KptScaffold(
    pullToRefreshState = rememberKptPullToRefreshState(viewModel.pagingStream),
    ...
) { ... }
// and disable PagingScreenContent's built-in pull-to-refresh:
PagingScreenContent(
    pagingStream = viewModel.pagingStream,
    onRetry = viewModel::onRetry,
    enablePullToRefresh = false,   // scaffold owns the gesture
) { items(coins) { ... } }
```

</details>

```kotlin
fun <T> rememberKptPullToRefreshState(
```
Bridge for non-paginated single-key screens driven by `ScreenDataStream`. Use with `KptScaffold` for detail pages that benefit from pull-to-refresh.

<details><summary>Example</summary>

```kotlin
KptScaffold(
    pullToRefreshState = rememberKptPullToRefreshState(
        stream = viewModel.stream,
        currentState = uiState,  // your collectAsStateWithLifecycle value
    ),
    ...
) { ... }
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/scaffold/KptScaffold.kt`

```kotlin
fun KptScaffold(
```
App screen scaffold for a screen with a BACK affordance — the nav icon is always shown and `onNavigationIconClick` is required.

<details><summary>Used in the template — <code>feature/cloudtodo/src/commonMain/kotlin/kpt/feature/cloudtodo/ui/CloudTodoScreen.kt:89</code></summary>

```kotlin
    val outcome by viewModel.lastOutcome.collectAsStateWithLifecycle()

    KptScaffold(
        onNavigationIconClick = onBackClick,
        title = stringResource(Res.string.screens_cloudtodo_detail_title),
        modifier = modifier.testTag(TestTags.CloudTodo.SCREEN),
    ) {
```

</details>

```kotlin
fun KptScaffold(
```
App screen scaffold whose back affordance is CONDITIONAL — pass `showNavigationIcon` when the same screen is reachable both as a tab root and as a pushed destination.

<details><summary>Used in the template — <code>feature/cloudtodo/src/commonMain/kotlin/kpt/feature/cloudtodo/ui/CloudTodoScreen.kt:89</code></summary>

```kotlin
    val outcome by viewModel.lastOutcome.collectAsStateWithLifecycle()

    KptScaffold(
        onNavigationIconClick = onBackClick,
        title = stringResource(Res.string.screens_cloudtodo_detail_title),
        modifier = modifier.testTag(TestTags.CloudTodo.SCREEN),
    ) {
```

</details>

```kotlin
fun KptScaffold(
```
Fully slot-based scaffold: the caller supplies `topBar`, `bottomBar` and the FAB composables.

<details><summary>Used in the template — <code>feature/cloudtodo/src/commonMain/kotlin/kpt/feature/cloudtodo/ui/CloudTodoScreen.kt:89</code></summary>

```kotlin
    val outcome by viewModel.lastOutcome.collectAsStateWithLifecycle()

    KptScaffold(
        onNavigationIconClick = onBackClick,
        title = stringResource(Res.string.screens_cloudtodo_detail_title),
        modifier = modifier.testTag(TestTags.CloudTodo.SCREEN),
    ) {
```

</details>

```kotlin
data class FloatingActionButtonContent(
```
Declarative FAB spec — icon, description and click — so a caller configures the FAB without passing a composable slot.

### `core/ui/src/commonMain/kotlin/kpt/core/ui/utils/PasswordChecker.kt`

```kotlin
object PasswordChecker
```
Scores a password against the app's rules and explains what is missing. Pure and platform-free, so the same verdict drives the indicator and any validation.

- `fun getPasswordStrengthResult(password: String): PasswordStrengthResult` — Validates then scores a password. Returns an error for empty or over-length input rather than a band, because neither has a meaningful strength.
- `val result = getPasswordStrength(password)`
- `fun getPasswordStrength(password: String): PasswordStrength` — Scores a password into a band from character-class variety, length and entropy.
- `val length = password.length`
- `val hasUpperCase = password.any { it.isUpperCase() }`
- `val hasLowerCase = password.any { it.isLowerCase() }`
- `val hasNumbers = password.any { it.isDigit() }`
- `val hasSymbols = password.any { !it.isLetterOrDigit() }`
- `val numTypesPresent =`
- `val entropyBits = calculateEntropy(password)`
- `val charPool = 26 + 26 + 10 + 33 // lowercase + uppercase + digits + symbols`
- `fun getPasswordFeedback(password: String): List<String>` — The unmet requirements, as user-facing sentences — what to fix, not just that it is weak. Empty when the password satisfies every rule.
- `val feedback = mutableListOf<String>()`

```kotlin
sealed class PasswordStrengthResult
```
The verdict: a strength band plus the unmet requirements, so the UI can say WHAT to fix rather than only that it is weak.

### `core/ui/src/commonMain/kotlin/kpt/core/ui/utils/PasswordStrength.kt`

```kotlin
enum class PasswordStrength
```
Password strength bands used by `PasswordChecker`.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/utils/PasswordChecker.kt:44</code></summary>

```kotlin
        }

        val result = getPasswordStrength(password)

        return PasswordStrengthResult.Success(result)
    }
```

</details>

### `core/ui/src/commonMain/kotlin/kpt/core/ui/utils/PasswordStrengthExtensions.kt`

```kotlin
fun Int.toPasswordStrengthOrNull(): PasswordStrength? = when (this)
```
Converts the given `Int` to a `PasswordStrength`. A `null` value is returned if this value is not in the [0, 4] range.

```kotlin
fun PasswordStrength.toInt(): Int = when (this)
```
Converts the given `PasswordStrength` to an `Int`.

---

_11 type(s), 39 function(s)/property(ies); 40 carry KDoc at source; 2 authored example(s); 8 live call site(s)._
<!-- api-docs:end -->

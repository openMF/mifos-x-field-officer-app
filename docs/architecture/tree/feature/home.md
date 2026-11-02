# `feature/home/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 36 tracked files — 21× `.xml`, 11× `.kt`, 1× `.gitignore`, 1× `.md`, 1× `.kts`

**11 Kotlin files** — 1 screen(s), 1 ViewModel(s).

Screens: `HomeScreen`

ViewModels: `HomeViewModel`

**Consumes:** `core/store/banking`, `core/store/currency`, `core/store/economic`

**Store5 archetype(s) exercised:** `CACHE_ONLY`, `LOAD_ONCE`, `MEMORY_ONLY`, `NETWORK_WITH_CACHE`, `OFFLINE_LOCAL_ONLY`, `PERIODIC` — from `core/store/STORE_ARCHETYPES.yaml`, which product-health fails the build against when an archetype loses its last showcase.

### Docs in the tree

| Path | |
|---|---|
| `feature/home/README.md` | :feature:home |

<!-- tree-scaffold:end -->
## Significance

The dashboard, and the **4-way `combineScreenStates` fan-in**: loans summary, upcoming bills, rates and
an exchange rate, each an independent card with its own loading, empty, error and content state.

It is also a **fork seam**. `HomeDashboard` is public rather than internal because `cmp-navigation`'s
`BackboneRegistry.homeBody` renders it as the default home body, and it lives in the fork-owned `demo/`
package so `--clean` removes it together with that default. A fork replaces the body without touching
the shell.

<!-- api-docs:begin module=feature/home sha=16f6cccec8c3487ab8c46a99e914a450b7f1419b -->
## API reference

_Generated from `feature/home` at tree `16f6cccec8c3` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/home/src/commonMain/kotlin/kpt/feature/home/HomeDestination.kt`

```kotlin
data object HomeDestination
```
Route for the home GRAPH — what `navigateToHome` targets. Distinct from `HomeRoute` so a fork can nest extra destinations under home without the bottom bar's selection tracking breaking.

```kotlin
data object HomeRoute
```
Route for the home SCREEN itself, the graph's start destination.

```kotlin
fun NavController.navigateToHome(navOptions: NavOptions? = null)
```
Navigates to the home graph.

```kotlin
fun NavGraphBuilder.homeGraph(
```
The backbone home graph. `homeBody` is the fork-owned home content (default supplied by `cmp-navigation`'s `BackboneRegistry.homeBody`); this template graph carries zero demo imports.

### `feature/home/src/commonMain/kotlin/kpt/feature/home/demo/HomeDashboard.kt`

```kotlin
fun HomeDashboard(
```
The demo dashboard: loans summary, upcoming bills, rates and an exchange rate, each an independent card. Public rather than internal because `cmp-navigation`'s fork-owned `BackboneRegistry.homeBody` seam renders it as the default home body.

<details><summary>Used in the template — <code>feature/home/src/commonTest/kotlin/kpt/feature/home/HomeScreenUiTest.kt:55</code></summary>

```kotlin
                    // production where cmp-navigation's BackboneRegistry.homeBody supplies it.
                    homeBody = {
                        HomeDashboard(
                            onNavigateToLoans = {},
                            onNavigateToBills = {},
                            onNavigateToRates = {},
                            onNavigateToExchangeRates = {},
```

</details>

### `feature/home/src/commonMain/kotlin/kpt/feature/home/demo/ui/HomeViewModel.kt`

```kotlin
class HomeViewModel(
```
**Home dashboard ViewModel.** Composes four independent reactive sources into a single UI state with per-widget loading/empty/error/content slots.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/demo/HomeDashboard.kt:155</code></summary>

```kotlin
    onNavigateToCrypto: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = retainedKoinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val exchangeFreshness by viewModel.exchangeFreshness.collectAsStateWithLifecycle()
    val ratesFreshness by viewModel.ratesFreshness.collectAsStateWithLifecycle()
```

</details>

```kotlin
data class HomeUiState(
```
Aggregate state for the home dashboard. Each slot is an independent `ScreenState` so the screen can render per-card Loading / Empty / Error / Content states.

<details><summary>Used in the template — <code>feature/home/src/commonTest/kotlin/kpt/feature/home/demo/ui/HomeDashboardViewModelTest.kt:141</code></summary>

```kotlin
            first,
            second,
            "HomeUiState must be identical across a subscriber-count dip — a change here " +
                "signals that nav-scoped VM caching lost or mutated the retained state.",
        )
    }
```

</details>

```kotlin
data class LoansSummary(
```
Compact projection of the user's loan portfolio for the home dashboard. Only the dashboard's summary surface needs these aggregates; the full list lives on the Loans screen.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/demo/HomeDashboard.kt:290</code></summary>

```kotlin
 */
@Composable
private fun HeroSnapshot(state: ScreenState<LoansSummary>) {
    kpt.core.base.designsystem.component.HeroCard {
        ScreenContent(
            state = state,
            onRetry = {},
```

</details>

```kotlin
data class RatesQuickView(
```
Compact projection of the two headline rate series for the home dashboard.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/demo/HomeDashboard.kt:536</code></summary>

```kotlin
@Composable
private fun RatesQuickCard(
    state: ScreenState<RatesQuickView>,
    freshness: FreshnessSignal,
    onRetry: () -> Unit,
    onSeeAll: () -> Unit,
) {
```

</details>

```kotlin
sealed interface HomeAction
```
What the dashboard can be asked to do.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/demo/HomeDashboard.kt:210</code></summary>

```kotlin
            state = state.rates,
            freshness = ratesFreshness,
            onRetry = { viewModel.trySendAction(HomeAction.RetryRates) },
            onSeeAll = onNavigateToRates,
        )

        ExchangeRateCard(
```

</details>

### `feature/home/src/commonMain/kotlin/kpt/feature/home/di/HomeModule.kt`

```kotlin
val HomeModule = module
```
Koin bindings for the home feature. Picked up by the generated feature-module aggregate, so removing the feature removes the module with it.

### `feature/home/src/commonMain/kotlin/kpt/feature/home/ui/TestTags.kt`

```kotlin
object TestTags
```
Test-tag registry for the home / bottom-nav dashboard. Consumed by: - `HomeScreen` via `Modifier.testTag(...)`.

<details><summary>Used in the template — <code>feature/add-to-watchlist/src/commonMain/kotlin/kpt/feature/addtowatchlist/ui/AddToWatchlistStar.kt:51</code></summary>

```kotlin
    IconButton(
        onClick = viewModel::onToggle,
        modifier = modifier.testTag(TestTags.AddToWatchlist.STAR_PREFIX + coinId),
    ) {
        Icon(
            imageVector = if (isTracked) Icons.Filled.Star else Icons.Outlined.StarBorder,
            contentDescription = stringResource(
```

</details>

- `const val SCREEN: String = "home.screen"` — Root `Scaffold` surface of `HomeScreen` — always rendered regardless of dashboard loading state. Stable target for Compose UI tests.
- `const val DASHBOARD_SCROLL: String = "home_dashboard_scroll"` — The vertically-scrollable dashboard `Column`.

---

_8 type(s), 6 function(s)/property(ies); 14 carry KDoc at source; 0 authored example(s); 7 live call site(s)._
<!-- api-docs:end -->

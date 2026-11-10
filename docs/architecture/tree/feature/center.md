# `feature/center/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 34 tracked files — 20× `.kt`, 10× `.png`, 2× `.md`, 1× `.kts`, 1× `.xml`

**20 Kotlin files** — 3 screen(s), 3 ViewModel(s).

Screens: `CenterDetailScreen`, `CenterListScreen`, `CreateCenterScreen`

ViewModels: `CenterDetailViewModel`, `CenterListViewModel`, `CreateCenterViewModel`

**Consumes:** `core/store/center`, `core/store/group`, `core/store/office`, `core/store/offline`, `core/store/recent`, `core/store/testing`

### Docs in the tree

| Path | |
|---|---|
| `feature/center/MIGRATION.md` | feature/center — migration ledger |
| `feature/center/README.md` | :feature:center |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/center sha=cb169447d5317b695f44e02c9c2d0969354427eb -->
## API reference

_Generated from `feature/center` at tree `cb169447d531` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/center/src/commonMain/kotlin/kpt/feature/center/di/CenterModule.kt`

```kotlin
val CenterModule = module
```
Collected into `GeneratedFeatureKoinBindings` by `:cmp-navigation:generateFeatureKoinBindings`, which scans each feature module's `di` package — no registry edit.

### `feature/center/src/commonMain/kotlin/kpt/feature/center/navigation/CenterNavigation.kt`

```kotlin
data object CenterDestination
```
The tab's graph. Mirrors `ClientDestination` — the tab points at the graph, not at a screen.

<details><summary>Used in the template — <code>feature/center/src/commonMain/kotlin/kpt/feature/center/navigation/CenterTab.kt:43</code></summary>

```kotlin

    /** The GRAPH, not the list screen — so the tab owns its own back stack. */
    override val graphRoute: String get() = CenterDestination.toObjectNavigationRoute()
    override val startDestinationRoute: String get() = CenterListRoute.toObjectNavigationRoute()
    override val testTag: String get() = "CentersTab"
}
```

</details>

```kotlin
data object CenterListRoute
```
The centre list.

<details><summary>Used in the template — <code>feature/center/src/commonMain/kotlin/kpt/feature/center/navigation/CenterTab.kt:44</code></summary>

```kotlin
    /** The GRAPH, not the list screen — so the tab owns its own back stack. */
    override val graphRoute: String get() = CenterDestination.toObjectNavigationRoute()
    override val startDestinationRoute: String get() = CenterListRoute.toObjectNavigationRoute()
    override val testTag: String get() = "CentersTab"
}
```

</details>

```kotlin
data class CenterDetailRoute(val centerId: Int)
```
Center detail. `centerId` is a type-safe route argument, not a SavedStateHandle default.

```kotlin
fun NavController.navigateToCenterList(navOptions: NavOptions? = null) =
```
Opens the centre list.

```kotlin
fun NavController.navigateToCenters(navOptions: NavOptions? = null) =
```
Opens the centres graph (the tab's destination).

```kotlin
fun NavController.navigateToCenterDetail(centerId: Int, navOptions: NavOptions? = null) =
```
Opens centre `centerId`'s details.

```kotlin
data object CreateCenterRoute
```
The new-centre form.

```kotlin
fun NavController.navigateToCreateCenter(navOptions: NavOptions? = null) = navigate(CreateCenterRoute, navOptions)
```
Opens the new-centre form.

```kotlin
data class CenterLinks(
```
Where the centre screens lead outside this feature. A feature never depends on another, so these are callbacks; `ForkFeatureRegistry` fills them with the owning feature's `navigateTo…`.

```kotlin
fun NavGraphBuilder.centerListDestination(navController: NavController, links: CenterLinks)
```
The center list — a TOP-LEVEL destination on the authenticated graph. Not `@FeatureDestination`: the detail screen opens other features, so `ForkFeatureRegistry` registers it with `CenterLinks`.

```kotlin
fun NavGraphBuilder.centerTabGraph(outerNav: NavController)
```
The center graph for the bottom-nav tab's INNER NavHost.

### `feature/center/src/commonMain/kotlin/kpt/feature/center/navigation/CenterTab.kt`

```kotlin
object CenterTab : NavigationItem
```
The Centers bottom-nav tab. Declared here via `@FeatureTab` rather than in `cmp-navigation`'s `AuthenticatedNavBarTabItem`, the same way `ClientTab` is — a feature contributes its own tab without the fork editing a module it does not own.

---

_6 type(s), 7 function(s)/property(ies); 13 carry KDoc at source; 0 authored example(s); 2 live call site(s)._
<!-- api-docs:end -->

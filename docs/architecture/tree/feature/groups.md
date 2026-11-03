# `feature/groups/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 32 tracked files — 22× `.kt`, 6× `.png`, 2× `.md`, 1× `.kts`, 1× `.xml`

**22 Kotlin files** — 3 screen(s), 3 ViewModel(s).

Screens: `CreateGroupScreen`, `GroupsDetailScreen`, `GroupsListScreen`

ViewModels: `CreateGroupViewModel`, `GroupsDetailViewModel`, `GroupsListViewModel`

**Consumes:** `core/store/group`, `core/store/office`, `core/store/offline`, `core/store/recent`, `core/store/testing`

### Docs in the tree

| Path | |
|---|---|
| `feature/groups/MIGRATION.md` | feature/groups — migration ledger |
| `feature/groups/README.md` | :feature:groups |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/groups sha=fc5ce0c3ffbf1a3cfc531ec7b50f7d7f5a460cb7 -->
## API reference

_Generated from `feature/groups` at tree `fc5ce0c3ffbf` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/groups/src/commonMain/kotlin/kpt/feature/groups/di/GroupsModule.kt`

```kotlin
val GroupsModule = module
```
Collected into `GeneratedFeatureKoinBindings` by `:cmp-navigation:generateFeatureKoinBindings`, which scans each feature module's `di` package — no registry edit.

### `feature/groups/src/commonMain/kotlin/kpt/feature/groups/navigation/GroupsNavigation.kt`

```kotlin
data object GroupsDestination
```
The tab's graph. Mirrors `ClientDestination` — the tab points at the graph, not at a screen.

<details><summary>Used in the template — <code>feature/groups/src/commonMain/kotlin/kpt/feature/groups/navigation/GroupsTab.kt:42</code></summary>

```kotlin

    /** The GRAPH, not the list screen — so the tab owns its own back stack. */
    override val graphRoute: String get() = GroupsDestination.toObjectNavigationRoute()
    override val startDestinationRoute: String get() = GroupsListRoute.toObjectNavigationRoute()
    override val testTag: String get() = "GroupsTab"
}
```

</details>

```kotlin
data object GroupsListRoute
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/groups/src/commonMain/kotlin/kpt/feature/groups/navigation/GroupsTab.kt:43</code></summary>

```kotlin
    /** The GRAPH, not the list screen — so the tab owns its own back stack. */
    override val graphRoute: String get() = GroupsDestination.toObjectNavigationRoute()
    override val startDestinationRoute: String get() = GroupsListRoute.toObjectNavigationRoute()
    override val testTag: String get() = "GroupsTab"
}
```

</details>

```kotlin
data class GroupsDetailRoute(val groupId: Int)
```
Group detail. `groupId` is a type-safe route argument, not a SavedStateHandle default.

```kotlin
data object CreateGroupRoute
```
Create a group. Reached from the groups list's FAB.

```kotlin
fun NavController.navigateToGroupsList(navOptions: NavOptions? = null) =
```
_No KDoc at source._

```kotlin
fun NavController.navigateToGroups(navOptions: NavOptions? = null) =
```
_No KDoc at source._

```kotlin
fun NavController.navigateToGroupsDetail(groupId: Int, navOptions: NavOptions? = null) =
```
_No KDoc at source._

```kotlin
fun NavController.navigateToCreateGroup(navOptions: NavOptions? = null) =
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.groupsListDestination(navController: NavController)
```
The group list — a TOP-LEVEL destination on the authenticated graph, collected into `GeneratedFeatureDestinations` by `:cmp-navigation:generateFeatureDestinations`.

```kotlin
fun NavGraphBuilder.groupsTabGraph(outerNav: NavController)
```
The group graph for the bottom-nav tab's INNER NavHost.

```kotlin
fun clientProfileRoute(clientId: Int): String = "$CLIENT_PROFILE_ROUTE_NAME/$clientId"
```
_No KDoc at source._

### `feature/groups/src/commonMain/kotlin/kpt/feature/groups/navigation/GroupsTab.kt`

```kotlin
object GroupsTab : NavigationItem
```
The Groups bottom-nav tab. Declared here via `@FeatureTab` rather than in `cmp-navigation`, the same way `ClientTab` and `CenterTab` are.

---

_5 type(s), 8 function(s)/property(ies); 7 carry KDoc at source; 0 authored example(s); 2 live call site(s)._
<!-- api-docs:end -->

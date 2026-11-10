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

<!-- api-docs:begin module=feature/groups sha=8440dcdbbc1f3509d27c11a12aa7d960f6d299a2 -->
## API reference

_Generated from `feature/groups` at tree `8440dcdbbc1f` by `scripts/docs/api-docs-gen.sh`._
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
The group list.

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
Opens the group list.

```kotlin
fun NavController.navigateToGroups(navOptions: NavOptions? = null) =
```
Opens the groups graph (the tab's destination).

```kotlin
fun NavController.navigateToGroupsDetail(groupId: Int, navOptions: NavOptions? = null) =
```
Opens group `groupId`'s details.

```kotlin
fun NavController.navigateToCreateGroup(navOptions: NavOptions? = null) =
```
Opens the new-group form.

```kotlin
data class GroupLinks(
```
Where the group screens lead outside this feature. A feature never depends on another, so these are callbacks; `ForkFeatureRegistry` fills each with the owning feature's `navigateTo…` of the same shape.

```kotlin
fun NavGraphBuilder.groupsListDestination(navController: NavController, links: GroupLinks)
```
The group list — a TOP-LEVEL destination on the authenticated graph. Not `@FeatureDestination`: the detail screen opens other features, so `ForkFeatureRegistry` registers it with `GroupLinks`.

```kotlin
fun NavGraphBuilder.groupsTabGraph(outerNav: NavController)
```
The group graph for the bottom-nav tab's INNER NavHost.

### `feature/groups/src/commonMain/kotlin/kpt/feature/groups/navigation/GroupsTab.kt`

```kotlin
object GroupsTab : NavigationItem
```
The Groups bottom-nav tab. Declared here via `@FeatureTab` rather than in `cmp-navigation`, the same way `ClientTab` and `CenterTab` are.

---

_6 type(s), 7 function(s)/property(ies); 13 carry KDoc at source; 0 authored example(s); 2 live call site(s)._
<!-- api-docs:end -->

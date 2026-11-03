# `feature/notification/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 33 tracked files — 20× `.xml`, 7× `.kt`, 4× `.png`, 1× `.md`, 1× `.kts`

**7 Kotlin files** — 1 screen(s), 1 ViewModel(s).

Screens: `NotificationsScreen`

ViewModels: `NotificationsViewModel`

**Consumes:** `core/store/notification`, `core/store/testing`

### Docs in the tree

| Path | |
|---|---|
| `feature/notification/MIGRATION.md` | feature/notification — ledger |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/notification sha=52d617990523b8be6e2b4fa32bea4c61cd345180 -->
## API reference

_Generated from `feature/notification` at tree `52d617990523` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/notification/src/commonMain/kotlin/kpt/feature/notification/di/NotificationModule.kt`

```kotlin
val NotificationModule = module
```
Collected by `:cmp-navigation:generateFeatureKoinBindings` — the `di` package is load-bearing.

### `feature/notification/src/commonMain/kotlin/kpt/feature/notification/navigation/NotificationsRoute.kt`

```kotlin
data object NotificationsRoute
```
The top bar's bell.

```kotlin
fun NavGraphBuilder.notificationsDestination(navController: NavController)
```
_No KDoc at source._

```kotlin
fun NavController.navigateToNotifications() = navigate(NotificationsRoute)
```
_No KDoc at source._

---

_1 type(s), 3 function(s)/property(ies); 2 carry KDoc at source; 0 authored example(s); 0 live call site(s)._
<!-- api-docs:end -->

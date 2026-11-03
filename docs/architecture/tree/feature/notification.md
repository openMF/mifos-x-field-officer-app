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

<!-- api-docs:begin module=feature/notification sha=4cd2e64c470afd7fbdda4001eea84153e5855b3b -->
## API reference

_Generated from `feature/notification` at tree `4cd2e64c470a` by `scripts/docs/api-docs-gen.sh`._
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
Registers the notifications screen in the nav graph.

```kotlin
fun NavController.navigateToNotifications() = navigate(NotificationsRoute)
```
Opens the notifications screen.

---

_1 type(s), 3 function(s)/property(ies); 4 carry KDoc at source; 0 authored example(s); 0 live call site(s)._
<!-- api-docs:end -->

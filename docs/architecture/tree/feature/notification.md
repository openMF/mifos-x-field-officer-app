# `feature/notification/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 34 tracked files — 20× `.xml`, 7× `.kt`, 4× `.png`, 2× `.md`, 1× `.kts`

**7 Kotlin files** — 1 screen(s), 1 ViewModel(s).

Screens: `NotificationsScreen`

ViewModels: `NotificationsViewModel`

**Consumes:** `core/store/notification`, `core/store/testing`

### Docs in the tree

| Path | |
|---|---|
| `feature/notification/MIGRATION.md` | feature/notification — ledger |
| `feature/notification/README.md` | :feature:notification |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/notification sha=d05235a890cfe80f6207b7376785c0c79b5a4b13 -->
## API reference

_Generated from `feature/notification` at tree `d05235a890cf` by `scripts/docs/api-docs-gen.sh`._
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
data class NotificationLinks(
```
Where a screen in this feature opens a record owned by another feature. A feature never depends on another, so these are callbacks; `ForkFeatureRegistry` fills each with the owning feature's `navigateTo…`.

```kotlin
fun NavGraphBuilder.notificationsDestination(navController: NavController, links: NotificationLinks)
```
Registers the notifications screen in the nav graph. Not `@FeatureDestination`: a notification opens the record it is about, in another feature, so `ForkFeatureRegistry` registers it with `NotificationLinks`.

```kotlin
fun NavController.navigateToNotifications() = navigate(NotificationsRoute)
```
Opens the notifications screen.

---

_2 type(s), 3 function(s)/property(ies); 5 carry KDoc at source; 0 authored example(s); 0 live call site(s)._
<!-- api-docs:end -->

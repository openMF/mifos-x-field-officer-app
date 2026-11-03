# `feature/pathtracking/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 32 tracked files — 20× `.xml`, 7× `.kt`, 3× `.png`, 1× `.md`, 1× `.kts`

**7 Kotlin files** — 1 screen(s), 1 ViewModel(s).

Screens: `PathTrackingScreen`

ViewModels: `PathTrackingViewModel`

**Consumes:** `core/store/pathtracking`, `core/store/session`, `core/store/testing`

### Docs in the tree

| Path | |
|---|---|
| `feature/pathtracking/MIGRATION.md` | feature/pathtracking — migration ledger |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/pathtracking sha=a7f0283feb011ad9eab5c542ccadc93e621c633b -->
## API reference

_Generated from `feature/pathtracking` at tree `a7f0283feb01` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/pathtracking/src/commonMain/kotlin/kpt/feature/pathtracking/di/PathTrackingModule.kt`

```kotlin
val PathTrackingModule = module
```
Collected by `:cmp-navigation:generateFeatureKoinBindings` — the `di` package is load-bearing.

### `feature/pathtracking/src/commonMain/kotlin/kpt/feature/pathtracking/navigation/PathTrackingRoute.kt`

```kotlin
data object PathTrackingRoute
```
The drawer's Path Tracker — the original's `PathTrackerRoute`.

```kotlin
fun NavGraphBuilder.pathTrackingDestination(navController: NavController)
```
_No KDoc at source._

```kotlin
fun NavController.navigateToPathTracking() = navigate(PathTrackingRoute)
```
_No KDoc at source._

---

_1 type(s), 3 function(s)/property(ies); 2 carry KDoc at source; 0 authored example(s); 0 live call site(s)._
<!-- api-docs:end -->

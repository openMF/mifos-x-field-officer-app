# `feature/offlinesync/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 39 tracked files — 20× `.xml`, 10× `.kt`, 7× `.png`, 1× `.md`, 1× `.kts`

**10 Kotlin files** — 1 screen(s), 1 ViewModel(s).

Screens: `OfflineSyncScreen`

ViewModels: `OfflineSyncViewModel`

**Consumes:** `core/store/center`, `core/store/client`, `core/store/group`, `core/store/offline`, `core/store/testing`

### Docs in the tree

| Path | |
|---|---|
| `feature/offlinesync/MIGRATION.md` | feature/offlinesync — migration notes |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/offlinesync sha=b8c378b2d672effd2ec04db82b0e69424e71bd41 -->
## API reference

_Generated from `feature/offlinesync` at tree `b8c378b2d672` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/offlinesync/src/commonMain/kotlin/kpt/feature/offlinesync/di/OfflineSyncModule.kt`

```kotlin
val OfflineSyncModule = module
```
Collected by `:cmp-navigation:generateFeatureKoinBindings` — the `di` package is load-bearing.

### `feature/offlinesync/src/commonMain/kotlin/kpt/feature/offlinesync/navigation/OfflineSyncRoute.kt`

```kotlin
data object OfflineSyncRoute
```
Settings → Offline sync.

```kotlin
fun NavGraphBuilder.offlineSyncDestination(navController: NavController)
```
Registers the offline-sync screen in the nav graph.

```kotlin
fun NavController.navigateToOfflineSync() = navigate(OfflineSyncRoute)
```
Opens the offline-sync screen.

---

_1 type(s), 3 function(s)/property(ies); 4 carry KDoc at source; 0 authored example(s); 0 live call site(s)._
<!-- api-docs:end -->

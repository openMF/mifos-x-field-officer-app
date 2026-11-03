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

<!-- api-docs:begin module=feature/offlinesync sha=34797f22a16c7519fc72d8b29307565cbd1b9795 -->
## API reference

_Generated from `feature/offlinesync` at tree `34797f22a16c` by `scripts/docs/api-docs-gen.sh`._
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
_No KDoc at source._

```kotlin
fun NavController.navigateToOfflineSync() = navigate(OfflineSyncRoute)
```
_No KDoc at source._

---

_1 type(s), 3 function(s)/property(ies); 2 carry KDoc at source; 0 authored example(s); 0 live call site(s)._
<!-- api-docs:end -->

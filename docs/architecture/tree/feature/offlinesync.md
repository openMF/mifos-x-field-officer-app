# `feature/offlinesync/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 40 tracked files — 20× `.xml`, 10× `.kt`, 7× `.png`, 2× `.md`, 1× `.kts`

**10 Kotlin files** — 1 screen(s), 1 ViewModel(s).

Screens: `OfflineSyncScreen`

ViewModels: `OfflineSyncViewModel`

**Consumes:** `core/store/center`, `core/store/client`, `core/store/group`, `core/store/offline`, `core/store/testing`

### Docs in the tree

| Path | |
|---|---|
| `feature/offlinesync/MIGRATION.md` | feature/offlinesync — migration notes |
| `feature/offlinesync/README.md` | :feature:offlinesync |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/offlinesync sha=efd5d5d632aabf128564f49716b4c5755cb22262 -->
## API reference

_Generated from `feature/offlinesync` at tree `efd5d5d632aa` by `scripts/docs/api-docs-gen.sh`._
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
data class OfflineSyncLinks(
```
Where the offline-sync screen leads outside this feature — callbacks `ForkFeatureRegistry` fills, since a feature never depends on another.

```kotlin
fun NavGraphBuilder.offlineSyncDestination(navController: NavController, links: OfflineSyncLinks)
```
Registers the offline-sync screen in the nav graph. Not `@FeatureDestination`: it opens records and drafts owned by other features, so `ForkFeatureRegistry` registers it with `OfflineSyncLinks`.

```kotlin
fun NavController.navigateToOfflineSync() = navigate(OfflineSyncRoute)
```
Opens the offline-sync screen.

---

_2 type(s), 3 function(s)/property(ies); 5 carry KDoc at source; 0 authored example(s); 0 live call site(s)._
<!-- api-docs:end -->

# `feature/collectionsheet/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 19 tracked files — 10× `.kt`, 5× `.png`, 2× `.md`, 1× `.kts`, 1× `.xml`

**10 Kotlin files** — 2 screen(s), 2 ViewModel(s).

Screens: `CollectionSheetDetailsScreen`, `NewCollectionSheetScreen`

ViewModels: `CollectionSheetDetailsViewModel`, `NewCollectionSheetViewModel`

**Consumes:** `core/store/collectionsheet`, `core/store/office`, `core/store/staff`, `core/store/testing`

### Docs in the tree

| Path | |
|---|---|
| `feature/collectionsheet/MIGRATION.md` | feature/collectionsheet — migration ledger |
| `feature/collectionsheet/README.md` | :feature:collectionsheet |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/collectionsheet sha=03462f5302eda73eff310cb4e309a96934118357 -->
## API reference

_Generated from `feature/collectionsheet` at tree `03462f5302ed` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/collectionsheet/src/commonMain/kotlin/kpt/feature/collectionsheet/di/CollectionSheetModule.kt`

```kotlin
val CollectionSheetModule = module
```
Collected by `:cmp-navigation:generateFeatureKoinBindings` — the `di` package is load-bearing.

### `feature/collectionsheet/src/commonMain/kotlin/kpt/feature/collectionsheet/navigation/CollectionSheetNavigation.kt`

```kotlin
data object IndividualCollectionSheetRoute
```
The drawer's "Collection Sheet" — the original's `IndividualCollectionSheetScreenRoute`.

```kotlin
data class CollectionSheetDetailsRoute(
```
One generated sheet, addressed by the REQUEST that selects it — never the sheet itself.

```kotlin
fun NavGraphBuilder.collectionSheetDestinations(navController: NavController)
```
_No KDoc at source._

```kotlin
fun NavController.navigateToIndividualCollectionSheet() = navigate(IndividualCollectionSheetRoute)
```
_No KDoc at source._

---

_2 type(s), 3 function(s)/property(ies); 3 carry KDoc at source; 0 authored example(s); 0 live call site(s)._
<!-- api-docs:end -->

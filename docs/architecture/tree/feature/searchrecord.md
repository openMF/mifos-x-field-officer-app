# `feature/searchrecord/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 17 tracked files — 8× `.kt`, 5× `.png`, 2× `.md`, 1× `.kts`, 1× `.xml`

**8 Kotlin files** — 1 screen(s), 1 ViewModel(s).

Screens: `SearchRecordScreen`

ViewModels: `SearchRecordViewModel`

**Consumes:** `core/store/searchrecord`, `core/store/testing`

### Docs in the tree

| Path | |
|---|---|
| `feature/searchrecord/MIGRATION.md` | feature/searchrecord — migration ledger |
| `feature/searchrecord/README.md` | :feature:searchrecord |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/searchrecord sha=3593e5775e1e1f57c077b0538cf16f85092580bf -->
## API reference

_Generated from `feature/searchrecord` at tree `3593e5775e1e` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/searchrecord/src/commonMain/kotlin/kpt/feature/searchrecord/di/SearchRecordModule.kt`

```kotlin
val SearchRecordModule = module
```
Collected by `:cmp-navigation:generateFeatureKoinBindings` — the `di` package is load-bearing.

### `feature/searchrecord/src/commonMain/kotlin/kpt/feature/searchrecord/navigation/SearchRecordRoute.kt`

```kotlin
data class SearchRecordRoute(val type: String)
```
`type` is a `RecordType` name — a String so the route stays serializable as the original's.

```kotlin
fun NavGraphBuilder.searchRecordDestination(
```
Ported from `6b66e8a43:feature/search-record/.../navigation/SearchRecordNavigation.kt`.

```kotlin
fun NavController.navigateToSearchRecord(type: RecordType)
```
Opens the record search for one record `type`.

---

_1 type(s), 3 function(s)/property(ies); 4 carry KDoc at source; 0 authored example(s); 0 live call site(s)._
<!-- api-docs:end -->

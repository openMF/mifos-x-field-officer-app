# `feature/datatable/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 18 tracked files — 10× `.kt`, 4× `.png`, 2× `.md`, 1× `.kts`, 1× `.xml`

**10 Kotlin files** — 2 screen(s), 2 ViewModel(s).

Screens: `DataTableListScreen`, `DataTableRowsScreen`

ViewModels: `DataTableListViewModel`, `DataTableRowsViewModel`

**Consumes:** `core/store/datatable`

### Docs in the tree

| Path | |
|---|---|
| `feature/datatable/MIGRATION.md` | feature/datatable — migration ledger |
| `feature/datatable/README.md` | :feature:datatable |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/datatable sha=2abd114a4289d4721bf95fb52c5d96a453faf613 -->
## API reference

_Generated from `feature/datatable` at tree `2abd114a4289` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/datatable/src/commonMain/kotlin/kpt/feature/datatable/di/DataTableModule.kt`

```kotlin
val DataTableModule = module
```
Collected into `GeneratedFeatureKoinBindings` by `:cmp-navigation:generateFeatureKoinBindings`, which scans each feature module's `di` package — no registry edit (spec §4).

### `feature/datatable/src/commonMain/kotlin/kpt/feature/datatable/navigation/DataTableNavigation.kt`

```kotlin
data class DataTableListRoute(val tableName: String, val entityId: Int)
```
The datatables registered against one application table, for one entity. `tableName` is the APPLICATION table (`m_client`, `m_group`) — it selects which entity kind's datatables to list.

```kotlin
data class DataTableRowsRoute(val tableName: String, val entityId: Int)
```
One datatable's rows. `tableName` here is the DATATABLE's registered name, not the apptable.

```kotlin
fun NavController.navigateToDataTableList(
```
Opens the data tables registered for one entity.

```kotlin
fun NavController.navigateToDataTableRows(
```
Opens one data table's rows for an entity.

```kotlin
fun NavGraphBuilder.dataTableDestinations(navController: NavController)
```
`@FeatureDestination`, so the generator registers these ONCE — the same reasoning as `activateDestination` and `documentDestinations`.

---

_2 type(s), 4 function(s)/property(ies); 6 carry KDoc at source; 0 authored example(s); 0 live call site(s)._
<!-- api-docs:end -->

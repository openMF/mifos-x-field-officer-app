# `feature/report/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 27 tracked files — 17× `.kt`, 6× `.png`, 2× `.md`, 1× `.kts`, 1× `.xml`

**17 Kotlin files** — 2 screen(s), 2 ViewModel(s).

Screens: `ReportListScreen`, `ReportRunScreen`

ViewModels: `ReportListViewModel`, `ReportRunViewModel`

**Consumes:** `core/store/report`, `core/store/testing`

### Docs in the tree

| Path | |
|---|---|
| `feature/report/MIGRATION.md` | feature/report — migration ledger |
| `feature/report/README.md` | :feature:report |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/report sha=0712569855f9f6990320f3466704822e06327575 -->
## API reference

_Generated from `feature/report` at tree `0712569855f9` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/report/src/commonMain/kotlin/kpt/feature/report/di/ReportModule.kt`

```kotlin
val ReportModule = module
```
Collected by `:cmp-navigation:generateFeatureKoinBindings` — the `di` package is load-bearing.

### `feature/report/src/commonMain/kotlin/kpt/feature/report/navigation/ReportNavigation.kt`

```kotlin
data object ReportListRoute
```
The drawer's "Run Reports" — the original's `RunReportScreenRoute`.

```kotlin
data class ReportRunRoute(val reportName: String)
```
One report, by name: its parameter form, then its result.

```kotlin
fun NavGraphBuilder.reportDestinations(navController: NavController)
```
Registers the report list and the run-report screen in the nav graph.

```kotlin
fun NavController.navigateToReports() = navigate(ReportListRoute)
```
Opens the report list.

---

_2 type(s), 3 function(s)/property(ies); 5 carry KDoc at source; 0 authored example(s); 0 live call site(s)._
<!-- api-docs:end -->

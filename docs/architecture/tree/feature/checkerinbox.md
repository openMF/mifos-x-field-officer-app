# `feature/checkerinbox/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 18 tracked files — 7× `.kt`, 7× `.png`, 2× `.md`, 1× `.kts`, 1× `.xml`

**7 Kotlin files** — 1 screen(s), 1 ViewModel(s).

Screens: `CheckerInboxScreen`

ViewModels: `CheckerInboxViewModel`

**Consumes:** `core/store/checkerinbox`, `core/store/testing`

### Docs in the tree

| Path | |
|---|---|
| `feature/checkerinbox/MIGRATION.md` | feature/checkerinbox — migration ledger |
| `feature/checkerinbox/README.md` | :feature:checkerinbox |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/checkerinbox sha=cb722fa8381c546271048ee2b6af918a45e7b0af -->
## API reference

_Generated from `feature/checkerinbox` at tree `cb722fa8381c` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/checkerinbox/src/commonMain/kotlin/kpt/feature/checkerinbox/di/CheckerInboxModule.kt`

```kotlin
val CheckerInboxModule = module
```
Collected by `:cmp-navigation:generateFeatureKoinBindings` — the `di` package is load-bearing.

### `feature/checkerinbox/src/commonMain/kotlin/kpt/feature/checkerinbox/navigation/CheckerInboxRoute.kt`

```kotlin
data object CheckerInboxRoute
```
The drawer's "Checker Inbox & Tasks" — the original's `CheckerInboxTaskScreenRoute` hub + `CheckerInboxScreenRoute`.

```kotlin
data class CheckerInboxLinks(val openLoanReschedules: (loanId: Int) -> Unit)
```
Where the checker inbox leads outside this feature — callbacks `ForkFeatureRegistry` fills, since a feature never depends on another.

```kotlin
fun NavGraphBuilder.checkerInboxDestinations(navController: NavController, links: CheckerInboxLinks)
```
Registers the checker inbox and its task detail in the nav graph. Not `@FeatureDestination`: it opens a loan's reschedules, so `ForkFeatureRegistry` registers it with `CheckerInboxLinks`.

```kotlin
fun NavController.navigateToCheckerInbox() = navigate(CheckerInboxRoute)
```
Opens the maker-checker inbox.

---

_2 type(s), 3 function(s)/property(ies); 5 carry KDoc at source; 0 authored example(s); 0 live call site(s)._
<!-- api-docs:end -->

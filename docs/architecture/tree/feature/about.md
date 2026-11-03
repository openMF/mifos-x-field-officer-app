# `feature/about/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 16 tracked files — 7× `.kt`, 3× `.xml`, 3× `.png`, 2× `.md`, 1× `.kts`

**7 Kotlin files** — 1 screen(s), 1 ViewModel(s).

Screens: `AboutScreen`

ViewModels: `AboutViewModel`

### Docs in the tree

| Path | |
|---|---|
| `feature/about/MIGRATION.md` | feature/about — migration ledger |
| `feature/about/README.md` | :feature:about |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/about sha=8118c5bc8e9f989c7f0e0b03bd0c602b11e67452 -->
## API reference

_Generated from `feature/about` at tree `8118c5bc8e9f` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/about/src/commonMain/kotlin/kpt/feature/about/di/AboutModule.kt`

```kotlin
val AboutModule = module
```
Collected by `:cmp-navigation:generateFeatureKoinBindings` — the `di` package is load-bearing.

### `feature/about/src/commonMain/kotlin/kpt/feature/about/navigation/AboutRoute.kt`

```kotlin
data object AboutRoute
```
The About screen: app version, licences and links.

```kotlin
fun NavGraphBuilder.aboutDestination(navController: NavController)
```
Top-level: Settings → About opens it (moved out of the drawer 2026-10-27). It is reached through `ForkBackboneRegistry.settingsBody`, so the merge-owned shell never imports this module.

```kotlin
fun NavController.navigateToAbout() = navigate(AboutRoute)
```
Opens the About screen.

---

_1 type(s), 3 function(s)/property(ies); 4 carry KDoc at source; 0 authored example(s); 0 live call site(s)._
<!-- api-docs:end -->

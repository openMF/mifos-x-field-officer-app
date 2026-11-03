# `feature/activate/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 16 tracked files — 8× `.kt`, 4× `.png`, 2× `.md`, 1× `.kts`, 1× `.xml`

**8 Kotlin files** — 1 screen(s), 1 ViewModel(s).

Screens: `ActivateScreen`

ViewModels: `ActivateViewModel`

**Consumes:** `core/store/center`, `core/store/client`, `core/store/group`, `core/store/testing`

### Docs in the tree

| Path | |
|---|---|
| `feature/activate/MIGRATION.md` | feature/activate — migration ledger |
| `feature/activate/README.md` | :feature:activate |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/activate sha=1ce6bce78fd47dc37d6fd29e24024e8c12385d83 -->
## API reference

_Generated from `feature/activate` at tree `1ce6bce78fd4` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/activate/src/commonMain/kotlin/kpt/feature/activate/di/ActivateModule.kt`

```kotlin
val ActivateModule = module
```
Collected into `GeneratedFeatureKoinBindings` by `:cmp-navigation:generateFeatureKoinBindings`, which scans each feature module's `di` package — no registry edit (spec §4).

### `feature/activate/src/commonMain/kotlin/kpt/feature/activate/navigation/ActivateRoute.kt`

```kotlin
data class ActivateRoute(val id: Int, val type: String)
```
Activate a client, centre or group. `type` is the entity kind — one of `Constants.ACTIVATE_CLIENT` / `_CENTER` / `_GROUP`, resolved to a closed enum by the ViewModel.

```kotlin
fun NavController.navigateToActivateClient(clientId: Int, navOptions: NavOptions? = null) =
```
_No KDoc at source._

```kotlin
fun NavController.navigateToActivateCenter(centerId: Int, navOptions: NavOptions? = null) =
```
_No KDoc at source._

```kotlin
fun NavController.navigateToActivateGroup(groupId: Int, navOptions: NavOptions? = null) =
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.activateDestination(navController: NavController)
```
Annotated `@FeatureDestination`, so the generator registers it ONCE on the authenticated graph.

---

_1 type(s), 5 function(s)/property(ies); 3 carry KDoc at source; 0 authored example(s); 0 live call site(s)._
<!-- api-docs:end -->

# `feature/document/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 19 tracked files — 11× `.kt`, 4× `.png`, 2× `.md`, 1× `.kts`, 1× `.xml`

**11 Kotlin files** — 2 screen(s), 2 ViewModel(s).

Screens: `DocumentListScreen`, `DocumentUploadScreen`

ViewModels: `DocumentListViewModel`, `DocumentUploadViewModel`

**Consumes:** `core/store/document`

### Docs in the tree

| Path | |
|---|---|
| `feature/document/MIGRATION.md` | feature/document — migration ledger |
| `feature/document/README.md` | :feature:document |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/document sha=3d2e8442e17dc5952313fcf70f68e3f7361b5c1c -->
## API reference

_Generated from `feature/document` at tree `3d2e8442e17d` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/document/src/commonMain/kotlin/kpt/feature/document/di/DocumentModule.kt`

```kotlin
val DocumentModule = module
```
Collected into `GeneratedFeatureKoinBindings` by `:cmp-navigation:generateFeatureKoinBindings`, which scans each feature module's `di` package — no registry edit (spec §4).

### `feature/document/src/commonMain/kotlin/kpt/feature/document/navigation/DocumentNavigation.kt`

```kotlin
data class DocumentListRoute(val entityType: String, val entityId: Int)
```
Documents for any Fineract entity. `entityType` is the API path segment — "clients", "groups", "loans" — and `entityId` the entity. Neither has a default.

```kotlin
data class DocumentUploadRoute(
```
The upload / replace form. `documentId` is `-1` for a CREATE: a nav argument cannot be absent, so the sentinel is how "no document yet" reaches the screen, and the ViewModel narrows it to `null`.

<details><summary>Used in the template — <code>feature/document/src/commonTest/kotlin/kpt/feature/document/upload/DocumentUploadViewModelTest.kt:74</code></summary>

```kotlin
    private fun viewModel(
        commands: FakeCommands = FakeCommands(),
        documentId: Int = DocumentUploadRoute.CREATE_NEW,
    ) = DocumentUploadViewModel(
        commands,
        SavedStateHandle(
            mapOf("entityType" to "groups", "entityId" to 7, "documentId" to documentId),
```

</details>

```kotlin
fun NavController.navigateToDocumentList(
```
Opens the documents of one entity.

```kotlin
fun NavController.navigateToDocumentUpload(
```
Opens the upload or replace form for one entity's document.

```kotlin
fun NavGraphBuilder.documentDestinations(navController: NavController)
```
Annotated `@FeatureDestination`, so the generator registers these ONCE on the authenticated graph — the same reasoning as `activateDestination`.

---

_2 type(s), 4 function(s)/property(ies); 6 carry KDoc at source; 0 authored example(s); 1 live call site(s)._
<!-- api-docs:end -->

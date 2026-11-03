# `feature/charge/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 29 tracked files — 15× `.kt`, 10× `.png`, 2× `.md`, 1× `.kts`, 1× `.xml`

**15 Kotlin files** — 3 screen(s), 3 ViewModel(s).

Screens: `ChargeDetailScreen`, `ChargeFormScreen`, `ChargeListScreen`

ViewModels: `ChargeDetailViewModel`, `ChargeFormViewModel`, `ChargeListViewModel`

**Consumes:** `core/store/charge`, `core/store/testing`

### Docs in the tree

| Path | |
|---|---|
| `feature/charge/MIGRATION.md` | feature/charge — migration ledger |
| `feature/charge/README.md` | feature/charge |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/charge sha=4a4b46cad9498b729fc26608382c161519ac83e8 -->
## API reference

_Generated from `feature/charge` at tree `4a4b46cad949` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/charge/src/commonMain/kotlin/kpt/feature/charge/di/ChargeModule.kt`

```kotlin
val ChargeModule = module
```
Collected into `GeneratedFeatureKoinBindings` by `:cmp-navigation:generateFeatureKoinBindings`, which scans each feature module's `di` package — no registry edit (spec §4).

### `feature/charge/src/commonMain/kotlin/kpt/feature/charge/navigation/ChargeNavigation.kt`

```kotlin
data class ChargeListRoute(val resourceType: String, val resourceId: Int)
```
The charges levied on ONE resource.

```kotlin
data class ChargeDetailRoute(val resourceType: String, val resourceId: Int, val chargeId: Int)
```
One charge, read from the table the list fills.

```kotlin
data class ChargeFormRoute(
```
The add / edit form. `chargeId` is `-1` for a CREATE: a nav argument cannot be absent, so the sentinel is how "no charge yet" reaches the screen, and the ViewModel narrows it to `null`.

```kotlin
fun NavController.navigateToCharges(
```
_No KDoc at source._

```kotlin
fun NavController.navigateToAddCharge(
```
Straight to the add form — the client details screen's "Add Charge" opens this.

```kotlin
fun NavGraphBuilder.chargeDestinations(navController: NavController)
```
`@FeatureDestination`, so the generator registers these ONCE on the authenticated graph — the same reasoning as `documentDestinations`: client, loan and savings all open it, so there is no single parent graph to nest it under.

### `feature/charge/src/commonMain/kotlin/kpt/feature/charge/ui/TestTags.kt`

```kotlin
object TestTags
```
Append-only test-tag registry for the charge feature — Compose UI tests and Maestro flows (`assertVisible: { id: "..." }`) resolve these.

<details><summary>Used in the template — <code>feature/charge/src/commonMain/kotlin/kpt/feature/charge/detail/ChargeDetailScreen.kt:104</code></summary>

```kotlin
        title = stringResource(Res.string.feature_charge_details),
        onBackPressed = { onAction(ChargeDetailAction.NavigateBack) },
        modifier = modifier.testTag(TestTags.ChargeDetail.SCREEN),
        actions = {
            // Only with something to edit, and only where Fineract can update it.
            if (canEdit && charge is ScreenState.Content) {
                IconButton(
```

</details>

- `const val SCREEN: String = "charge_list_screen"`
- `const val CONTENT: String = "charge_list_content"`
- `const val LIST: String = "charge_list"`
- `const val EMPTY: String = "charge_list_empty"`
- `const val ADD: String = "charge_list_add"`
- `const val DELETE_DIALOG: String = "charge_list_delete_dialog"`
- `const val WRITE_ERROR: String = "charge_list_write_error"` — A failed write renders ABOVE the list, never instead of it.
- `fun row(id: Int): String = "charge_list_row_$id"`
- `const val DELETING: String = "charge_list_deleting"` — Progress overlay while a delete is in flight.
- `const val SCREEN: String = "charge_detail_screen"`
- `const val CONTENT: String = "charge_detail_content"`
- `const val EDIT: String = "charge_detail_edit"`
- `const val NOT_FOUND: String = "charge_detail_not_found"`
- `const val SCREEN: String = "charge_form_screen"`
  _…more members; read the file._

---

_4 type(s), 18 function(s)/property(ies); 9 carry KDoc at source; 0 authored example(s); 1 live call site(s)._
<!-- api-docs:end -->

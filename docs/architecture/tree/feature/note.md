# `feature/note/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 29 tracked files — 16× `.kt`, 9× `.png`, 2× `.md`, 1× `.kts`, 1× `.xml`

**16 Kotlin files** — 2 screen(s), 2 ViewModel(s).

Screens: `AddEditNoteScreen`, `NoteListScreen`

ViewModels: `AddEditNoteViewModel`, `NoteListViewModel`

**Consumes:** `core/store/note`

### Docs in the tree

| Path | |
|---|---|
| `feature/note/MIGRATION.md` | feature/note — migration ledger |
| `feature/note/README.md` | :feature:note |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/note sha=f0e31264d162fb8c07aac48071412be36ab44549 -->
## API reference

_Generated from `feature/note` at tree `f0e31264d162` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/note/src/commonMain/kotlin/kpt/feature/note/di/NoteModule.kt`

```kotlin
val NoteModule = module
```
Collected automatically.

### `feature/note/src/commonMain/kotlin/kpt/feature/note/navigation/NoteNavigation.kt`

```kotlin
data class NoteListRoute(val resourceType: String, val resourceId: Int)
```
Notes attached to a client — ported from `6b66e8a43:feature/note/src/commonMain/kotlin/com/mifos/feature/note/navigation/NoteNavigation.kt`.

```kotlin
data class AddEditNoteRoute(val resourceType: String, val resourceId: Int, val noteId: Long?)
```
Add or edit one note. `noteId == null` means ADD. The original encoded the same thing and then branched on it in four places inside the ViewModel; here it is read once into `editEnabled`.

```kotlin
fun NavController.navigateToNotes(resourceType: String, resourceId: Int, navOptions: NavOptions? = null) =
```
One owner's notes — `resourceType` is Fineract's path (`clients`, `groups`).

```kotlin
fun NavController.navigateToClientNotes(clientId: Int, navOptions: NavOptions? = null) =
```
Opens client `clientId`'s notes.

```kotlin
fun NavController.navigateToAddNote(resourceType: String, resourceId: Int) =
```
Opens the add-note form for one resource.

```kotlin
fun NavController.navigateToEditNote(resourceType: String, resourceId: Int, noteId: Long) =
```
Opens the edit form for note `noteId` of one resource.

```kotlin
fun NavGraphBuilder.noteDestinations(navController: NavController)
```
Register both note destinations into the caller's graph. The original exposed `noteDestination(navController, onBackPressed)` and `feature/client` depended on `projects.feature.note` to call it.

### `feature/note/src/commonMain/kotlin/kpt/feature/note/ui/TestTags.kt`

```kotlin
object TestTags
```
Append-only test-tag registry for the note feature. Consumed by: - Compose UI tests in `feature/note/src/commonTest/` (`NoteListScreenUiTest`, `AddEditNoteScreenUiTest`).

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

- `const val SCREEN: String = "note_list_screen"` — The note list screen's root.
- `const val LIST: String = "note_list"` — The list of notes.
- `const val BACK: String = "note_list_back"` — The note list's back button.
- `const val FAB: String = "note_list_fab"` — FAB that opens the "Add note" screen. Always present regardless of repository state.
- `const val ROW_ACTIONS: String = "note_list_row_actions"` — The expanded action strip on a row — edit + delete. Absent until the row is expanded.
- `const val ROW_EDIT: String = "note_list_row_edit"` — A note row's edit action.
- `const val ROW_DELETE: String = "note_list_row_delete"` — A note row's delete action.
- `const val DELETE_DIALOG: String = "note_list_delete_dialog"` — Delete confirmation. Destructive and irreversible, so it is always confirmed.
- `const val WRITE_ERROR: String = "note_list_write_error"` — Write-failure banner. Distinct from the list's own error state: a failed WRITE leaves the notes intact, so it must not be rendered as a screen-level error that replaces them.
- `fun row(id: Long?): String = "note_list_row_$id"` — One row, suffixed with the note id so a test can address a specific note.
- `const val SCREEN: String = "note_addedit_screen"` — The add/edit note screen's root.
- `const val FIELD: String = "note_addedit_field"` — The note text field.
- `const val SUBMIT: String = "note_addedit_submit"` — The save button.
- `const val BACK: String = "note_addedit_back"` — The add/edit screen's back button.
  _…more members; read the file._

---

_3 type(s), 20 function(s)/property(ies); 23 carry KDoc at source; 0 authored example(s); 1 live call site(s)._
<!-- api-docs:end -->

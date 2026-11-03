# :feature:note

Notes on a client — list, add and edit.

- **Screens:** `ui/NoteListScreen`, `ui/AddEditNoteScreen`.
- **ViewModels:** `NoteListViewModel` — `NoteRepository.notesStream` (an empty list is real
  content, drawn as the empty state); `AddEditNoteViewModel` — writes through
  `NoteCommandRepository`.
- **Routes:** `NoteListRoute`, `AddEditNoteRoute`. Entry points: `navigateToClientNotes`,
  `navigateToAddNote`, `navigateToEditNote`.
- **DI:** `NoteModule`.

Reads go through the repositories' `ScreenDataStream`s (Room → Store5 → `core/data`); writes go through the `*CommandRepository`, which calls `core/network` and records the result locally in the same command — never a ViewModel calling an API or the datastore directly. Previews render in `desktopTest` via `CommonComposablePreviewScanner` (`*PreviewScreenshotTest`, goldens under `src/desktopTest/resources/screenshots/`).

# :feature:document

Documents attached to any entity (group, loan, savings, …) — list, download, remove, and an upload
form that creates or replaces one.

- **Screens:** `list/DocumentListScreen`, `upload/DocumentUploadScreen`.
- **ViewModels:** `DocumentListViewModel` — `DocumentRepository.documentsStream`;
  `DocumentUploadViewModel` — validates name/description/file, then
  `DocumentCommandRepository`, which uploads and stores the document and its bytes locally.
- **Routes:** `DocumentListRoute`, `DocumentUploadRoute`. Entry points: `navigateToDocumentList`,
  `navigateToDocumentUpload`.
- **DI:** `DocumentModule`.

Client documents live in `:feature:client` (`documents/`), which adds the in-app preview.
Reads go through the repositories' `ScreenDataStream`s (Room → Store5 → `core/data`); writes go through the `*CommandRepository`, which calls `core/network` and records the result locally in the same command — never a ViewModel calling an API or the datastore directly. Previews render in `desktopTest` via `CommonComposablePreviewScanner` (`*PreviewScreenshotTest`, goldens under `src/desktopTest/resources/screenshots/`).

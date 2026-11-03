# :feature:datatable

Fineract data tables (extra fields an institution attaches to a client, loan or group): the tables
registered for an entity, and the rows of one table.

- **Screens:** `list/DataTableListScreen`, `rows/DataTableRowsScreen`.
- **ViewModels:** `DataTableListViewModel` — `DataTableRepository.dataTablesStream`;
  `DataTableRowsViewModel` — the rows stream plus row delete through `DataTableCommandRepository`.
- **Routes:** `DataTableListRoute`, `DataTableRowsRoute`. Entry points: `navigateToDataTableList`,
  `navigateToDataTableRows`.
- **DI:** `DataTableModule`.

Reads go through the repositories' `ScreenDataStream`s (Room → Store5 → `core/data`); writes go through the `*CommandRepository`, which calls `core/network` and records the result locally in the same command — never a ViewModel calling an API or the datastore directly. Previews render in `desktopTest` via `CommonComposablePreviewScanner` (`*PreviewScreenshotTest`, goldens under `src/desktopTest/resources/screenshots/`).

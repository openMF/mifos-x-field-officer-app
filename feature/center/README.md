# :feature:center

Centres a field officer services — searchable, filterable list and a detail with the centre's groups.

- **Screens:** `list/CenterListScreen`, `detail/CenterDetailScreen`.
- **ViewModels:** `CenterListViewModel` — `CenterRepository.centersStream`, filtered by
  `applyFilters` (core-base `combineContent` + `emptyIfContent`, so "no match" is Empty);
  `CenterDetailViewModel` — the centre plus `GroupRepository`'s groups for it.
- **Routes:** `CenterListRoute`, `CenterDetailRoute`. Entry points: `navigateToCenters`,
  `navigateToCenterList`, `navigateToCenterDetail`. Activation uses `:feature:activate`.
- **DI:** `CenterModule`.

Reads go through the repositories' `ScreenDataStream`s (Room → Store5 → `core/data`); writes go through the `*CommandRepository`, which calls `core/network` and records the result locally in the same command — never a ViewModel calling an API or the datastore directly. Previews render in `desktopTest` via `CommonComposablePreviewScanner` (`*PreviewScreenshotTest`, goldens under `src/desktopTest/resources/screenshots/`).

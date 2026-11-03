# :feature:groups

Groups — searchable, filterable list, a detail with the group's actions, and a create form.

- **Screens:** `list/GroupsListScreen`, `detail/GroupsDetailScreen`, `create/CreateGroupScreen`.
- **ViewModels:** `GroupsListViewModel` — `GroupRepository.groupsStream` filtered by
  `applyFilters` (core-base `combineContent` + `emptyIfContent`); `GroupsDetailViewModel`;
  `CreateGroupViewModel` — `OfficeRepository` for the office picker, `GroupCommandRepository`
  to create.
- **Routes:** `GroupsListRoute`, `GroupsDetailRoute`, `CreateGroupRoute`. Entry points:
  `navigateToGroups`, `navigateToGroupsList`, `navigateToGroupsDetail`, `navigateToCreateGroup`.
- **DI:** `GroupsModule`.

Reads go through the repositories' `ScreenDataStream`s (Room → Store5 → `core/data`); writes go through the `*CommandRepository`, which calls `core/network` and records the result locally in the same command — never a ViewModel calling an API or the datastore directly. Previews render in `desktopTest` via `CommonComposablePreviewScanner` (`*PreviewScreenshotTest`, goldens under `src/desktopTest/resources/screenshots/`).

# :feature:activate

Activates a pending client, group or centre on a chosen date — one shared screen, reached from each
entity's detail.

- **Screens:** `ui/ActivateScreen` (stateless half `ActivateContent`).
- **ViewModel:** `ActivateViewModel` — the route's `type` picks `ClientCommandRepository`,
  `GroupCommandRepository` or `CenterCommandRepository`; the outcome is `Succeeded`, `Offline`
  (nothing sent) or `Failed`.
- **Routes:** `ActivateRoute(id, type)`. Entry points: `navigateToActivateClient`,
  `navigateToActivateGroup`, `navigateToActivateCenter`.
- **DI:** `ActivateModule`.

Reads go through the repositories' `ScreenDataStream`s (Room → Store5 → `core/data`); writes go through the `*CommandRepository`, which calls `core/network` and records the result locally in the same command — never a ViewModel calling an API or the datastore directly. Previews render in `desktopTest` via `CommonComposablePreviewScanner` (`*PreviewScreenshotTest`, goldens under `src/desktopTest/resources/screenshots/`).

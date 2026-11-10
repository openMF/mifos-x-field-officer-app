# :feature:pathtracking

The routes the signed-in officer has recorded — each path's points, start and end address, date and times.

- **Screens:** `ui/PathTrackingScreen`.
- **ViewModels:** `PathTrackingViewModel` — keys `PathTrackingRepository.userPathsStream` on the user from `SessionRepository.sessionStream`, and decodes each path's stored points into a `TrackedPath`; pull-to-refresh and retry go to the same stream.
- **Routes:** `PathTrackingRoute`. Entry point: `navigateToPathTracking`.
- **DI:** `PathTrackingModule`.

Read-only: the paths come from the `m_staff_path_tracking` datatable through the repository's `ScreenDataStream` (Room → Store5 → `core/data`), so a list seen once still shows offline. Previews render in `desktopTest` via `CommonComposablePreviewScanner` (`*PreviewScreenshotTest`, goldens under `src/desktopTest/resources/screenshots/`).

# :feature:offlinesync

Settings → Offline sync: which centres, groups and clients are on the device, what still needs syncing, the running download, and the Manual / Automatic refresh schedule. Download-only — work captured offline is a draft, sent from Sync & Drafts.

- **Screens:** `ui/OfflineSyncScreen` (with `OfflineSyncRecords`, `OfflineSyncPicker`, `OfflineSyncSettings`).
- **ViewModels:** `OfflineSyncViewModel` — the dashboard observes `OfflineSyncRepository.records`, `latestJob` and `refresh` plus connectivity, all local, so the background job's progress re-renders as it happens. The record picker is the only server read: the officer's own `centersStream` / `groupsStream` / `clientsStream`, opened when the picker is.
- **Routes:** `OfflineSyncRoute`. Entry point: `navigateToOfflineSync`. A record opens its centre, group or client; the drafts link opens Sync & Drafts.
- **DI:** `OfflineSyncModule`.

Choosing, refreshing, retrying, cancelling and removing records go through `OfflineSyncRepository`, which records the choice locally and hands the download to the background scheduler — the ViewModel never runs a sync itself, and works offline except for the actions that need the server. Previews render in `desktopTest` via `CommonComposablePreviewScanner` (`*PreviewScreenshotTest`, goldens under `src/desktopTest/resources/screenshots/`).

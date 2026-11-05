# :feature:notification

The signed-in officer's notifications — unread first, then the recent read history — with "mark all read" and a tap through to the record a notification is about.

- **Screens:** `ui/NotificationsScreen`.
- **ViewModels:** `NotificationsViewModel` — `NotificationRepository.notificationsStream` (network with cache: the cached list shows at once, a fetch runs behind it); "mark all read" writes through `NotificationRepository.markAllRead`.
- **Routes:** `NotificationsRoute`. Entry point: `navigateToNotifications`. A notification opens its loan, client, group, centre or savings account; any other kind is not tappable.
- **DI:** `NotificationModule`.

Reads go through the repository's `ScreenDataStream` (Room → Store5 → `core/data`); "mark all read" is a command through the write door, which calls `core/network` and marks the cached list read in the same command — never a ViewModel calling an API directly. Previews render in `desktopTest` via `CommonComposablePreviewScanner` (`*PreviewScreenshotTest`, goldens under `src/desktopTest/resources/screenshots/`).

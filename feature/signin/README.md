# :feature:signin

The sign-in screen — username/password against the configured Fineract instance, attempt lockout,
and biometric unlock of a stored session.

- **Screens:** `ui/SignInScreen` (stateless half `SignInContent`). The instance chip opens
  `:feature:settings`' server-config editor.
- **ViewModel:** `SignInViewModel` — validates the credentials, then `AuthCommandRepository.authenticate`
  (which persists the session); `resumeStoredSession` for biometric unlock. The instance chip reads
  `ServerConfigRepository.serverConfigStream`.
- **Routes:** `SignInRoute` (root package, beside the wiring). Registered by `cmp-navigation`'s
  auth graph.
- **DI:** `SignInModule`.

Previews render in `desktopTest` via `CommonComposablePreviewScanner` (`SignInPreviewScreenshotTest`,
goldens under `src/desktopTest/resources/screenshots/signin/`).

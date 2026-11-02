# :feature:about

The app's identity page, opened from the navigation drawer: brand header (Mifos X logo, app name,
purpose, version), **help & community** (the product site and the Mifos Slack, `#mobile`), the
organisation behind it, its open-source home, and the copyright line.

- **Screen:** `ui/AboutScreen` (stateless half `AboutContent`).
- **ViewModel:** `AboutViewModel` — holds the identity and turns taps into `OpenUrl` / `OpenAppInfo`
  events; no data layer, no `init {}`.
- **Route:** `AboutRoute`, `@FeatureDestination aboutDestination`; entry `navigateToAbout()`, reached
  from the drawer through `ForkBackboneRegistry.navigateToAbout`.
- **DI:** `AboutModule`.

**Every brand value comes from `app-profile/app.yaml`** (identity.app_name + `org.*`, incl. `org.links.community` + `org.links.product`) via
`gradle/fork.properties` → this module's BuildKonfig; the version is the one the Android build stamps
(`version.txt` / `dynamicVersion`). Rebrand in app-profile, never in this module. A link whose value is
not configured draws no row. Previews render in `desktopTest` (`AboutPreviewScreenshotTest`).

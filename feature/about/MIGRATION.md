# feature/about — migration ledger

**Source:** `6b66e8a43:feature/about` — 4 `.kt`, 1 `strings.xml`, 4 drawables
**Landed:** 2026-10-24
**Status:** complete. All 4 original files accounted for.

## 1. Original inventory

| original | migrated to | note |
|---|---|---|
| `AboutScreen.kt` | `ui/AboutScreen.kt` | redesigned header + sectioned rows; see §2 |
| `AboutData.kt` (`aboutItems`) | `ui/AboutViewModel.kt` (`aboutLinks`) | derived from app-profile, not hardcoded |
| `AboutItem.kt` | `ui/AboutViewModel.kt` (`AboutLink`, `AboutLinkKind`) | |
| `AboutNavigation.kt` | `navigation/AboutNavigation.kt` | `@FeatureDestination`; drawer → `ForkBackboneRegistry.navigateToAbout` |
| `drawable/feature_about_ic_website.xml`, `…_ic_source_code.xml` | same | the original's glyphs, kept |
| `drawable/feature_about_ic_launcher.png` (182 px) | `drawable/feature_about_logo.png` (512 px) | the original's own 1024 px Mifos X mark (`cmp-ios/…/mifos_logo_1024.png`), so it stays sharp |
| `drawable/feature_about_icon_twitter.xml` | — | dropped with the Twitter row (§2) |

## 2. Deviations

- **No hardcoded identity.** The original printed "MifosXDroid", "App Version 1.0.1", "©2016–2022
  Mifos Initiative" and five URLs as literals — the version and the year were long stale. All of it now
  comes from `app-profile/app.yaml`; the year is the current one.
- **Links only where configured.** The Twitter row is gone: app-profile declares no such account, and a
  row is never drawn for a link that does not exist. Contributors appears only for a GitHub source URL
  (its path is GitHub's). License opens the license TEXT (`https://mozilla.org/MPL/2.0/`, the URL every
  source header cites) — a repo-file link 404'd on device because the default branch names it `LICENCE`.
- **Source points at THIS app.** `app-profile` `org.support_url` was the template's repository;
  it is now `openMF/mifos-x-field-officer-app` (it also feeds the store support link).
- **Help & community (new).** A Product site row (`org.links.product`, the app's guides site) and a
  Community row (`org.links.community`, the Mifos Slack — ask in `#mobile`), first on the page: help is
  what an officer opens About for. Both keys are new in app-profile and in BOTH fork.properties writers
  (`SyncForkConfigPlugin` + `deployment/_shared/config.rb`; parity check green).
- **Taps are events** routed through the ViewModel (the original called `ShareUtils` inside the
  composable), so each row's destination is a tested fact.

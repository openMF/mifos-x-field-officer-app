# feature/pathtracking — migration ledger

Ported 2026-10-27 from `6b66e8a43:feature/path-tracking` (5 Kotlin files + 5 platform actuals),
`core/data/…/PathTrackingRepository(Imp).kt` and `core/domain/…/GetUserPathTrackingUseCase.kt`.

| Original | Here | Notes |
|---|---|---|
| `PathTrackingScreen.kt` (common) | `ui/PathTrackingScreen.kt` | same card: route · "start to end" · "date from start to stop"; pull-to-refresh |
| `PathTrackingScreen.android.kt` | — (folded into common) | the Maps-directions intent became a plain directions URL via `LocalUriHandler` |
| `PathTrackingScreen.{desktop,js,native,wasmJs}.kt` | — | these drew NO map; the common route sketch now covers every target |
| `PathTrackingViewModel.kt` | `ui/PathTrackingViewModel.kt` | `DataState` → `ScreenDataStream`; session from `SessionRepository` |
| `PathTrackingUiState.kt` | — | `ScreenState` (loading / empty / error / content) |
| `PathTrackingRepository(Imp)` + use-case | `core/data/pathtracking` + `core/store/pathtracking` | offline-first store over `DataTableApi.getUserPathTracking` |
| `navigation/PathTrackingNavigation.kt` | `navigation/PathTrackingNavigation.kt` | `@FeatureDestination`; drawer entry flipped to migrated |
| `strings.xml` (English only) | `composeResources/values*/` | kept the original keys used; 20 locales |

## Deviations (each deliberate)

- **Wrong id fixed.** The original passed the user's OFFICE id to a read keyed by USER id
  (`getUserPathTrackingUseCase(officeId)`), so it fetched someone else's row or none. Reads the
  signed-in user's own id. Pinned by `PathTrackingViewModelTest.readsTheSignedInUsersOwnPathsNotTheOffice`.
- **Start/Stop tracking not ported.** The original's top-bar action only flipped
  `userStatus` in preferences behind a location-permission prompt; nothing in the KMP original
  recorded a path. Porting it would ship a control that records nothing. Recording needs a
  platform location service writing `addUserPathTracking` — a separate piece of work.
- **No Google Map.** Android's `GoogleMap` needs a Maps API key this fork does not ship (a blank
  tile without one), and the other four targets had no map at all. A Canvas route sketch replaces it.
- **No Nominatim reverse geocoding.** The original sent each path's ends to
  `nominatim.openstreetmap.org` from the composable — the officer's location to a third party, with
  no User-Agent (against its usage policy). A card shows the stored address, else coordinates.

## Server

`GET datatables/m_staff_path_tracking/{userId}`. On the field-officer gateway the `fieldofficer`
role is **403** on this datatable (2026-10-27) — the screen shows that error; data needs the grant.

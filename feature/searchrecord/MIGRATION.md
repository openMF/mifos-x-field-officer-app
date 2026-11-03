# feature/searchrecord — migration ledger

**Source:** `6b66e8a43:feature/search-record` — 4 `.kt`, 1 `strings.xml`
**Landed:** 2026-10-24
**Status:** complete. All 4 original files accounted for.

---

## 1. Original inventory

| original | migrated to | note |
|---|---|---|
| `SearchRecordScreen.kt` | `ui/SearchRecordScreen.kt` | Mifos* → App* components; previews moved to `ui/SearchRecordScreenPreview.kt` (internal, so the render tier sees them) |
| `SearchRecordViewModel.kt` | `ui/SearchRecordViewModel.kt` | see §2 |
| `navigation/SearchRecordNavigation.kt` | `navigation/SearchRecordNavigation.kt` | registered by the client graph, not the app shell |
| `di/SearchRecordModule.kt` | `di/SearchRecordModule.kt` | |
| `composeResources/values/strings.xml` | same | verbatim |

## 2. Deviations

- **Data path.** The original's `SearchRecordRepository → SearchRecordLocalDataSource` ran two Room
  queries (`ClientDao.searchAddressesByQuery` / `searchIdentifiersByQuery`). This fork caches those
  lists as `api_response_cache` rows, so the search is `core/store/searchrecord/SearchRecordStore`
  — the original SQL's matching rules, applied in memory over the cached rows
  (`SearchRecordMatchingTest`). One stream per screen, keyed by the debounced query.
- **Title.** The original hardcoded `"Address"` / `"Identifiers"` in the ViewModel beside its own
  unused `search_record_address` / `search_record_identifiers` strings; the screen now uses them.
- **Cleared query.** Clearing the box empties the results at once; the original kept the previous
  query's results listed until the debounce fired.
- **Routing.** The app shell sent ADDRESS → client address list and IDENTIFIER → client identifiers
  list. Same here, from the client graph; a record with no client id is logged and ignored.

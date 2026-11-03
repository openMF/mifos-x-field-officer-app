# :feature:searchrecord

Search across every client address or identifier this device has cached — opened from the search
icon on a client's Addresses / Identifiers screen; a result opens that client's list.

- **Screen:** `ui/SearchRecordScreen` (results, loading, no-results, error dialog).
- **ViewModel:** `SearchRecordViewModel` — debounces the query 300 ms and feeds it as a KEY to ONE
  `SearchRecordRepository.searchStream` held for the screen's life.
- **Route:** `SearchRecordRoute(type)`; entry point `navigateToSearchRecord(RecordType)`.
  Registered by the CLIENT graph (`searchRecordDestination`), which also routes a selected result.
- **DI:** `SearchRecordModule`.

Local only (`FetchPolicy.CACHE_ONLY`): `core/store`'s `searchRecords` store scans the
`clientGetClientAddresses:*` / `clientGetClientListIdentifiers:*` cache rows, so a search finds only
clients whose addresses / identifiers were opened on this device — the same scope as the original,
which searched its local Room tables. Previews render in `desktopTest`
(`SearchRecordPreviewScreenshotTest`, goldens under `src/desktopTest/resources/screenshots/`).

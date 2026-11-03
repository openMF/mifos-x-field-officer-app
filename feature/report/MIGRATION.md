# feature/report — migration ledger

**Source:** `6b66e8a43:feature/report` — 16 `.kt`, 1 `strings.xml`, 1 drawable
**Landed:** 2026-10-24
**Status:** complete.

## 1. Original inventory

| original | migrated to | note |
|---|---|---|
| `runReport/*` (Screen, ViewModel, UiState) | `ui/list/*` | category menu → chips over one catalogue read (§2) |
| `reportDetail/ReportDetailScreen.kt`, `ReportDetailViewModel.kt`, `ReportDetailUiState.kt` | `ui/run/ReportRunScreen.kt` + `ReportRunViewModel.kt` | form generated from metadata (§2) |
| `report/ReportScreen.kt` (result) | `ui/run/ReportRunScreen.kt` (`ResultTable`) | same screen, after the form |
| `reportDetail/FileHelper.kt` + 5 platform actuals | `ui/ReportCsv.kt` + FileKit save dialog | one common implementation (§2) |
| `navigation/ReportNavigation.kt`, `ReportScreens.kt` | `navigation/ReportNavigation.kt` | |
| `di/ReportModule.kt` | `di/ReportModule.kt` | |
| `drawable/feature_report_ic_report_item.xml` | — | cards use `AppIcons.ChevronRight` |

Data: `ReportRepository` gained `reportParametersStream` (new `@StoreProvider reportParameters`, which
maps `FullParameterList` in `core/store/report/ReportParameterMapping.kt`), `parameterOptions` and
`runReport` (both through the existing `reportGetRunReportWithQuery` store). `ColumnHeader` /
`FullParameterListResponse` fields got defaults so a header field Fineract omits cannot fail a report.

## 2. Defects fixed, not carried

- **Most reports could not be run.** The form recognised ten hardcoded parameter names (office, loan
  officer, product, fund, currency, …) and silently dropped every other parameter — including the start /
  end **dates** most transaction reports take, so the server rejected them. The form is now the report's
  own `FullParameterList`: every parameter, typed (select · date · text · hidden), with its label,
  default and parent. Tested (`runSendsEveryParameterUnderItsVariableWithDerivedDefaults`).
- **Result rows were misaligned.** The table drew each COLUMN as its own list and skipped null cells, so
  one empty value shifted every later value in that column up a row — amounts showed against the wrong
  client. It is a row-major table now; a null is an empty cell.
- **CSV export was Android-only and broke on commas.** It wrote to a hardcoded
  `/storage/emulated/0/Download/MifosReports` behind a storage permission, was `TODO()` on desktop, and
  joined raw values with "," (a client named "Das, A." split into two columns; nulls written as `null`).
  Now one RFC 4180 writer, the save dialog on Android/desktop/iOS and a browser download on the web
  (`saveReportCsv`, one `actual` per platform). Tested (`ReportCsvTest`).
- **A report listed once per parameter.** `reportCategoryList` returns one row per report-parameter;
  the original showed a 4-parameter report 4 times. Distinct by report id now.
- **A child select kept a value from the old parent** (an officer of the previous office); changing a
  parent now clears its children.
- **A failed run said "Failed to Load Report Details"**; it now shows Fineract's own reason and keeps the
  form as filled.

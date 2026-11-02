# :feature:report

The drawer's **Run Reports**: browse the server's report catalogue by category, fill in what a report
asks for, run it, read the result as a table, and export it as CSV.

- **Screens:** `ui/list/ReportListScreen` (category chips · one card per report),
  `ui/run/ReportRunScreen` (parameter form → result table, Export CSV in the top bar).
- **ViewModels:** `ReportListViewModel` — the catalogue (`ReportRepository.reportTypesStream`) read
  once, filtered by `ReportCategory`; `ReportRunViewModel` — the report's parameters
  (`reportParametersStream`), each SELECT's options (`parameterOptions`, narrowed by its parent's
  value), and the run (`runReport`).
- **Routes:** `ReportListRoute`, `ReportRunRoute(reportName)`. `@FeatureDestination reportDestinations`;
  the drawer reaches it through `ForkBackboneRegistry.navigateToReports`.
- **DI:** `ReportModule`.

The form is generated from the report's own `FullParameterList` metadata (`ReportParameter`: variable,
label, display type, default, select-all, parent) — no parameter name is hardcoded here. The catalogue
and each report's parameters are cached for offline browsing; running a report always asks the server.
CSV export is `saveReportCsv` (FileKit): the platform save dialog on Android, desktop and iOS (no storage
permission), a browser download on the web.
Previews render in `desktopTest` (`ReportPreviewScreenshotTest`).

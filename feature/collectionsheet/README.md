# :feature:collectionsheet

The drawer's **Collection Sheet**: generate an office's individual collection sheet for a date, record
what was collected per loan, and submit it.

- **Screens:** `ui/newsheet/NewCollectionSheetScreen` (office · repayment date · optional staff),
  `ui/details/CollectionSheetDetailsScreen` (one row per loan due, a payment-editor sheet, submit).
- **ViewModels:** `NewCollectionSheetViewModel` — offices + per-office staff streams;
  `CollectionSheetDetailsViewModel` — streams the sheet for its request
  (`CollectionSheetRepository.individualCollectionSheetStream`) and OWNS one draft per loan, which is
  exactly what `CollectionSheetCommandRepository.saveIndividualCollectionSheet` sends.
- **Routes:** `IndividualCollectionSheetRoute`, `CollectionSheetDetailsRoute(officeId, staffId?,
  transactionDate)` — the request, never the sheet. `@FeatureDestination collectionSheetDestinations`;
  the drawer reaches it through `ForkBackboneRegistry.navigateToCollectionSheet`.
- **DI:** `CollectionSheetModule`.

The sheet is network-first with an offline copy (the last sheet generated for that request); submitting
needs a connection (`MutationPolicy.OnlineRequired`) and says so when offline. Previews render in
`desktopTest` (`CollectionSheetPreviewScreenshotTest`).

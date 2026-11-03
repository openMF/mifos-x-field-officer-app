# :feature:loan

A loan account end to end: individual and group applications, approval, disbursement, the profile
and its schedule / transactions / summary, repayment (queued when offline), rescheduling and
transfer.

- **Screens (12):** `newloan/NewLoanApplicationScreen`, `grouploan/GroupLoanApplicationScreen`,
  `approval/LoanApprovalScreen`, `disbursement/LoanDisbursementScreen`,
  `profile/LoanProfileScreen`, `schedule/LoanScheduleScreen`,
  `transactions/LoanTransactionsScreen`, `summary/LoanSummaryScreen`,
  `repayment/LoanRepaymentScreen`, `reschedules/LoanReschedulesScreen`,
  `rescheduleform/LoanRescheduleFormScreen`, `transfer/LoanTransferScreen`.
- **Repositories:** `LoanRepository` (streams + one-shot templates, server-first with a cache
  fallback), `LoanCommandRepository`, `OfflineQueueRepository` (a repayment already waiting to
  sync blocks a second one).
- **Routes:** one `*Route` per screen. Entry points: `navigateToLoanProfile`,
  `navigateToLoanRepayment`, `navigateToLoanSummary`, `navigateToNewLoanApplication`,
  `navigateToGroupLoanApplication`. Loan charges live in `:feature:charge`.
- **DI:** `LoanModule`.

Reads go through the repositories' `ScreenDataStream`s (Room → Store5 → `core/data`); writes go through the `*CommandRepository`, which calls `core/network` and records the result locally in the same command — never a ViewModel calling an API or the datastore directly. Previews render in `desktopTest` via `CommonComposablePreviewScanner` (`*PreviewScreenshotTest`, goldens under `src/desktopTest/resources/screenshots/`).

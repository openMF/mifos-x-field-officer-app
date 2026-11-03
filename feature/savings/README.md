# :feature:savings

Savings accounts: client and group applications, recurring-deposit application, approve/activate,
the account summary, and deposit/withdrawal.

- **Screens:** `application/SavingsApplicationScreen`, `recurring/RecurringDepositScreen`,
  `statuschange/SavingsStatusChangeScreen`, `summary/SavingsSummaryScreen`,
  `transaction/SavingsTransactionScreen`.
- **Repositories:** `SavingsAccountRepository` / `SavingsCommandRepository` (templates are read
  server-first with a cache fallback), `RecurringDepositRepository` /
  `RecurringDepositCommandRepository`.
- **Routes:** `SavingsApplicationRoute`, `RecurringDepositApplicationRoute`,
  `SavingsStatusChangeRoute`, `SavingsSummaryRoute`, `SavingsTransactionRoute`. Entry points:
  `navigateToSavingsApplication`, `navigateToRecurringDepositApplication`,
  `navigateToSavingsApproval`, `navigateToSavingsActivation`, `navigateToSavingsSummary`.
  Savings charges live in `:feature:charge`.
- **DI:** `SavingsModule`.

Reads go through the repositories' `ScreenDataStream`s (Room → Store5 → `core/data`); writes go through the `*CommandRepository`, which calls `core/network` and records the result locally in the same command — never a ViewModel calling an API or the datastore directly. Previews render in `desktopTest` via `CommonComposablePreviewScanner` (`*PreviewScreenshotTest`, goldens under `src/desktopTest/resources/screenshots/`).

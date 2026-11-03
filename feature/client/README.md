# :feature:client

Everything about one client: the list, onboarding, the profile and its sub-screens — addresses,
identifiers, documents (with in-app PDF/image preview), collateral, staff, transfer, closure and
account applications.

- **Screens (26):** `list/ClientListScreen`, `createnew/CreateNewClientScreen`,
  `profile/ClientProfileScreen` and its sections (`details`, `general`, `editdetails`,
  `editprofile`, `address`/`addaddress`, `identifiers`/`addidentifier`,
  `documents`/`adddocument`/`documentpreview`, `signature`, `collateral`/`collateraldetails`,
  `staff`, `transfer`, `closure`, `loanaccounts`, `depositaccounts`, `shareaccounts`,
  `defaultaccount`, `applynew`, `fixeddepositapplication`, `shareapplication`).
- **Repositories:** `ClientRepository` / `ClientCommandRepository`, `DocumentRepository` /
  `DocumentCommandRepository`, `SavingsAccountRepository` / `SavingsCommandRepository`,
  `OfficeRepository`, `StaffRepository`.
- **Routes:** one `*Route` per screen (e.g. `ClientListRoute`, `ClientProfileRoute`,
  `ClientDocumentPreviewRoute`), registered in `navigation/`.
- **DI:** `ClientModule`.

Reads go through the repositories' `ScreenDataStream`s (Room → Store5 → `core/data`); writes go through the `*CommandRepository`, which calls `core/network` and records the result locally in the same command — never a ViewModel calling an API or the datastore directly. Previews render in `desktopTest` via `CommonComposablePreviewScanner` (`*PreviewScreenshotTest`, goldens under `src/desktopTest/resources/screenshots/`). See `MIGRATION.md` for what was ported, dropped and why.

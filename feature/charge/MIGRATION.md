# feature/charge — migration ledger

**Source:** `6b66e8a43:feature/client/charges/**` (4 `.kt`) + `6b66e8a43:feature/loan/loanCharge/**`
(4) + `6b66e8a43:feature/loan/loanChargeForm/**` (2), and their first ports in this repo
(`feature/client/charges/`, `feature/loan/charges/`, `feature/loan/chargeform/`).
**Landed:** 2026-10-23, as ONE module for every owner — operator brief: *"single module charge that
will support all type of charges — we will send type of client, loan, savings — and charges list and
details, edit, delete etc should be managed and implemented there only."*
**Status:** complete for list / detail / add / edit / delete on all three owners. Pay / waive /
inactivate are not ported (§6).

---

## 1. What was consolidated

| was | now |
|---|---|
| `feature/client/charges/ClientChargesScreen.kt` + `ClientChargesViewModel.kt` (client list, from "Upcoming Charges") | `list/ChargeListScreen.kt` + `ChargeListViewModel.kt` |
| `feature/loan/charges/LoanChargesScreen.kt` + `LoanChargesViewModel.kt` (loan list) | `list/` (same) + `detail/ChargeDetailScreen.kt` (the loan card's labelled rows) |
| `feature/loan/chargeform/LoanChargeFormScreen.kt` + `LoanChargeFormViewModel.kt` (loan add form) | `form/ChargeFormScreen.kt` + `ChargeFormViewModel.kt` (add **and** edit, any owner) |
| *(none)* — savings had no levied-charge surface at all | the same three screens, `resourceType = savingsaccounts` |
| `ClientChargesRoute(clientId)`, `LoanChargesRoute(loanId)`, `LoanChargeFormRoute(loanId)` | `ChargeListRoute` / `ChargeDetailRoute` / `ChargeFormRoute` — all `(resourceType, resourceId[, chargeId])` |

Data layer, consolidated in the same change:

| was | now |
|---|---|
| `provideClientChargeStore` + `provideLoanChargeStore` (no savings) | `core/store/charge/ChargeStore.kt` `provideChargeStore`, keyed `ChargeKey(resourceType, resourceId)` |
| `provideClientChargeItemStore` + `provideLoanChargeItemStore` | `provideChargeItemStore`, keyed `ChargeItemKey(resourceType, resourceId, chargeId)` |
| `ClientChargeRepository.clientChargesStream`, `LoanRepository.loanChargesStream` / `loanChargeTemplate` | `ChargeRepository.chargesStream` / `chargeTemplate` / `charge` |
| `ChargeCommandRepository(resourceType: String, …)` branching on two owners | the same three writes over `ChargeResource`, one code path |

## 2. Callers — every one now opens this module

| caller | opens |
|---|---|
| client General → "Upcoming Charges" | `navigateToCharges(CLIENT, clientId)` |
| client Details → "Add Charge" (was `null`/absent: there was no client create) | `navigateToAddCharge(CLIENT, clientId)` |
| loan Profile → "Charges" row | `navigateToCharges(LOAN, loanId)` |
| loan Summary → overflow "Charges" | `navigateToCharges(LOAN, loanId)` |
| savings Summary → overflow "Charges" (new entry) | `navigateToCharges(SAVINGS, accountId)` |

Registered once with `@FeatureDestination` (three parent graphs open it — same reasoning as
`feature/document`); `include(":feature:charge")` in `settings.local.gradle.kts`, the edge in
`feature-deps.gradle.kts`, the row in `feature/module-packages.yaml`.

## 3. What each owner supports — from the Fineract OpenAPI spec, not assumed

Read off `https://sandbox.mifos.community/fineract-provider/fineract.json` (what `swagger-ui`
loads), 2026-10-23. Encoded once in `core/model/.../charge/ChargeResource.kt`.

| | `clients/{id}/charges` | `loans/{id}/charges` | `savingsaccounts/{id}/charges` |
|---|---|---|---|
| list | GET — **paged** envelope (`totalFilteredRecords` + `pageItems`) | GET — bare array | GET — bare array |
| template | GET `…/template` | GET `…/template` | GET `…/template` |
| get one | GET `…/{chargeId}` | GET `…/{loanChargeId}` (+ `external-id/…`) | GET `…/{savingsAccountChargeId}` |
| create | POST | POST (`?command=pay` also pays) | POST |
| **update** | **none** | PUT `…/{loanChargeId}` | PUT `…/{savingsAccountChargeId}` |
| delete | DELETE | DELETE | DELETE |
| commands | POST `…/{chargeId}?command=` pay · waive | POST `…/{id}?command=` pay · waive · adjustment | POST `…/{id}?command=` paycharge · waive · inactivate |

So **Edit is offered for loan and savings only**. The original offered it on a client charge too and
answered *"Updating client charges is not supported"* after the form was filled
(`charges_client_update_not_support_error_msg`); the action is simply not on a client row here.

## 4. The `Charges` table key

`ChargesEntity` was keyed on `id` alone (`@PrimaryKey(autoGenerate = true)`), and client, loan and
savings charges are three Fineract tables whose ids overlap — so fetching loan 1's charge 5 REPLACED
client 9's charge 5. The key is now `(resourceType, resourceId, id)`, the identity the API itself uses;
the store stamps the owner on write because the list endpoints do not echo it (loans send no `loanId`).

Room schema **15 → 16**, ledger unit `fork-charges-composite-key`: a plain AutoMigration — a
primary-key change is a table recreate Room performs itself. The new columns carry defaults (`''`,
`0`), so copied rows belong to no owner and are never read; every list refetches on open and logout
purges them. A spec to delete them was tried and dropped: `execSQL` / `SQLiteConnection.prepare` are
not visible from `commonMain` while `core/database` targets web. The destructive fallback does not
fire.

## 5. Behaviour changes vs the original (each one a defect, not a redesign)

- **The list is the destination.** The original opened on the add FORM with a "N Charges [View]"
  row and kept the list in a bottom sheet. Every caller wants the list; the form is its "+".
- **Delete is confirmed.** The original deleted on the first tap of the row menu.
- **A failed write keeps the list.** The original replaced the screen with `MifosErrorComponent`.
- **The due date is sent only for a specified-due-date charge**, and only that kind shows the field.
  The original CLIENT form always sent one; Fineract rejects a due date on other kinds.
- **The row's two date slots.** `ShowClientCharge` passed `formattedDueDate` as both `date` and
  `collectedOn`; the second slot now shows what is outstanding.
- **Edit prefills by definition id**, not by name (`indexOfFirst { name == … }` picked the wrong
  option whenever two definitions shared a name), and the update payload omits `chargeId`.
- **Amounts are formatted with the charge's currency**; a missing value is "—", never "null".
- **Strings live in composeResources** — the loan form's `OFFLINE_MSG` / `FAILED_MSG` Kotlin literals
  are gone.

## 6. Not ported, and why

| original / API surface | reason |
|---|---|
| pay / waive (client, loan), adjustment (loan), paycharge / waive / inactivate (savings) | no original screen performed them — the original's charge menu was Edit + Delete only. Each needs its own input (payment date, transaction amount) and belongs in its own slice. |
| `clientUpcomingCharges/**` (3 files) | the same `{type}/{id}/charges` read as `charges/`, differing only in paging; "Upcoming Charges" opens this list (`upcoming-charges-is-the-same-list-as-charges`). |
| `ShowClientCharge.kt` paging (`PagingData`) | a client's charges are a short list; the store reads the whole list (`limit=0`). |
| `MifosBreadcrumbNavBar(navController)` | the breadcrumb is not ported anywhere; the scaffold title stands in. |
| collected-on date picker (`showCollectedOnDatePicker`) | declared in the original state, never rendered. |
| `core/ui` `AddChargeBottomSheet` | a DIFFERENT surface — charges picked inside an account APPLICATION wizard (loan / savings / share / FD) before the account exists. Not a levied charge; stays with those wizards. |

## 7. Tests

- `core/data` `ChargeRepositoryTest` — real Store5 stores over one in-memory `Charges` table: reads
  per owner, the same id under client / loan / savings coexisting, each write landing where the
  read observes, delete scoped to one owner, offline Blocked with nothing written.
- `feature/charge` `ChargeListViewModelTest`, `ChargeDetailViewModelTest`, `ChargeFormViewModelTest`.
- `ChargePreviewScreenshotTest` — every `@Preview` in `ui/ChargePreviews.kt`, goldens under
  `src/desktopTest/resources/screenshots/charge/`.

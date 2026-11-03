# feature/collectionsheet — migration ledger

**Source:** `6b66e8a43:feature/collectionSheet` — 17 `.kt`, 1 `strings.xml`, 1 drawable
**Landed:** 2026-10-24
**Status:** individual flow complete; the generate (center/group) flow CLASSIFIED — see §3.

## 1. Original inventory

| original | migrated to | note |
|---|---|---|
| `individualCollectionSheet/IndividualCollectionSheetScreen.kt` (tab host) | `ui/newsheet/NewCollectionSheetScreen.kt` | one tab left, so no tab host (§2) |
| `newIndividualCollectionSheet/*` (Screen, ViewModel, UiState) | `ui/newsheet/*` | |
| `individualCollectionSheetDetails/*` (Screen, ViewModel, UiState) | `ui/details/*` | |
| `paymentDetails/*` (Screen, ViewModel) | `ui/details/CollectionSheetDetailsScreen.kt` (`PaymentEditorSheet`) | a sheet over details, same ViewModel (§2) |
| `savedIndividualCollectionSheet/*` | — | a fake; removed (§2) |
| `navigation/CollectionSheetNavigation.kt`, `CollectionSheetScreens.kt` | `navigation/CollectionSheetNavigation.kt` | |
| `di/CollectionSheetModule.kt` | `di/CollectionSheetModule.kt` | |
| `generateCollectionSheet/*` (Screen 516 lines, ViewModel, UiState) | — | unreachable in the original (§3) |
| `drawable/feature_collection_sheet_ic_dp_placeholder.png` | — | the rows no longer show a placeholder avatar |

## 2. Defects fixed, not carried

- **Entered payments were never submitted.** The payment screen decoded its own copy of the payload
  from route JSON and wrote into that; Submit sent the details screen's untouched payload. Now one
  ViewModel owns one draft per loan and the editor is a sheet over the list — tested
  (`anEditedPaymentIsExactlyWhatIsSubmitted`).
- **Wrong loan / crash.** Rows were clients and each showed `client.loans[clientIndex]`; a client with
  fewer loans than its row number threw. Rows are loans now.
- **`transactionDate` was never sent**, which Fineract requires on `saveCollectionSheet`.
- **A typo submitted a zero repayment** (`toDoubleOrNull() ?: 0.0`); a bad or negative amount is refused.
- **Date picker allowed only the future** — excluding today's meeting; now today and the past.
- **`staffId = 0` sent when no staff was chosen**; absent now.
- The account label printed the product twice; it is the account number.
- A failed save replaced the list with an error whose Retry did nothing; the sheet stays and the dialog
  shows Fineract's own reason. A saved sheet closes the screen, so it cannot be submitted twice.
- An empty sheet opened an empty list after a "Found Collection Sheet" prompt; it is a "no repayments
  due" state now. The prompt's facts (date, members) are the list's header.
- **The "Saved" tab rendered a fixed "No Saved Collection Sheet"** (a TODO for an API that never existed).
  Removed — it looked functional and was not.

## 3. Classified, not ported

`generateCollectionSheet/*` — the center / group (productive) sheet. It is registered in the original's
graph, but **nothing navigates to it**: the drawer opens the individual sheet and no other screen links
it. Porting it lands a screen nothing opens. Its data layer is ready (`productiveSheet`,
`groupCollectionSheet`, `submitProductiveSheet`, `submitCollectionSheet`); giving it an entry point is a
product decision.

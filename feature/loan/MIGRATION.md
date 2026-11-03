# feature/loan — migration ledger

S4, first slice. Ported from `6b66e8a43:feature/loan/` — the **loan account PROFILE**. 62 `.kt`
upstream, 4 ported so far.

## What the 62 files actually are

Applying §6a's caller rule first, because three of the package names are nearly identical:

| package | lines | what it is | live caller |
|---|---|---|---|
| `loanAccountProfile` | ~800 | the loan DETAIL screen — **ported** | client loan list · group detail |
| `loanAccountSummary` | ~1,250 | a SEPARATE, larger detail screen | group detail · search results |
| `loanAccount` | ~830 | the OLD add-loan form | `clientDetailRoute` only — **dead** |
| `newLoanAccount` | ~2,900 | the modern 5-page loan application wizard | client loan list · apply-new-applications |
| `groupLoanAccount` | ~810 | create a GROUP loan | group detail |
| 11 more (`loanRepayment`, `loanDisbursement`, `loanApproval`, `loanCharge`, `loanTransaction`, `loanRepaymentSchedule`, `loanReschedules`, `createLoanReschedules`, `amountTransfer`, `loanChargeForm`, `component`) | — | the actions + detail screens the profile links to | the profile |

`loanAccount` (830 lines) reaches only the dead screen and is **not** ported — same finding as the
survey screens and client activation.

Two screens are *both* loan details with different callers (`loanAccountProfile` and
`loanAccountSummary`); this slice ports the one the client's loan list opens.

## Ported

`profile/` — ViewModel + screen + all thirteen `LoanProfileActionItem`s. Its substrate already
existed: `loanStream(loanId)` on `LoanRepository`, and the four loan-status colours in `AppColors`.
Icons come from `core/ui`'s shared resources, as the original took them.

Registered as a `@FeatureDestination` for the same reason as `activate`/`document`/`datatable`:
more than one parent graph opens it.

## The worst `networkMonitor` observer of the set

Every ported screen had one. This one used it as the load TRIGGER, with a null-guard:

```kotlin
networkMonitor.isOnline.collect { isConnected ->
    if (isConnected) { if (state.loanAccount == null) loadLoanAccountDetails(route.loanId) }
    else if (state.loanAccount == null) { … Error(network_not_available) }
}
```

So a loan was fetched **once and then never again** — `loanAccount != null` suppressed every later
load, including after a reconnect. And Retry checked the same `networkConnection` flag and showed
"details not found" when it believed itself offline, rather than retrying. A stale loan could sit
on screen indefinitely with no way to refresh it. Pinned by
`retryAlwaysReachesTheReadEvenWhenOffline`.

## Status drives three things, so it is derived once

`statusUiModel` (chip label + colour) and `nextActionButtonRes` (which action the primary button
offers) were both written into the state at load time. They are derived from the stream here, so
the chip and the button cannot describe different loans — the class of bug that shipped on the
group screen, where the status text and the Activate button tested the same field differently.

The precedence is the original's and is load-bearing: **pending → overpaid → active**. Fineract can
report a loan both active and overpaid, and the original showed OVERPAID (Transfer) for that
combination, not ACTIVE (Repayment). Pinned.

## Carried over: four Fineract states collapse to "Unknown"

`waitingForDisbursal` and the three `closed*` flags all fall through to UNKNOWN, so a closed loan
reads "Unknown" with a "View Action" button. The original's behaviour; widening it is a product
question. Tracked as `loan-profile-unknown-status-catch-all`.

## The detail section renders only the rows that go somewhere

Two of the thirteen have destinations after S4b (Repayment Schedule, Transactions); the other
eleven are not drawn at all, rather than drawn and dead (RULE-IMPL-DEAD-CLICKABLE-001). Which rows
appear is one list in `LoanNavigation.kt` — `PORTED_DETAILS` — so landing a detail screen is a
route plus a line there.

12 tests, mostly over the status derivation, plus 6 render tests (below).

---

# S4b — repayment schedule, transactions, approval

The profile's primary button did nothing and its detail section was empty. This slice gives both
somewhere to go.

## Two of the three screens needed NO new data layer

`LoanWithAssociations` already carries `repaymentSchedule` and `transactions` — they come back with
the profile's own `loans/{id}?associations=all` read. So the schedule and the transaction list are
**projections of the same cached `loanStream`**, where the original made three separate repository
calls (`getLoanRepaySchedule`, `getLoanTransactions`, plus the profile's own). Opening the profile
and then both detail screens now costs one request instead of three, and the three screens cannot
disagree about the loan.

That also fixes a quieter problem: in the original the schedule's amounts and the loan's CURRENCY
came from different requests, so a refresh landing between them could format one request's figures
with another's currency symbol. The currency now travels with the periods in one projection.

## The disbursement row is period 0, and it is not an installment

`RepaymentSchedule.getListOfActualPeriods()` drops the first period because Fineract returns the
DISBURSEMENT there — it has no due amount and is not a repayment. It is rendered as its own
highlighted row above the schedule and excluded from the rows, the totals and all three counters.
Counting it would inflate every loan's installment count by one. Pinned by
`theDisbursementIsItsOwnRowAndIsNotAnInstallment` and `theTotalsSumTheInstallmentsOnlyNotTheDisbursement`.

A second test (`aScheduleWithNoDisbursementRowStillProjectsItsInstallments`) pins the fact that the
helper drops the first period **unconditionally** — so a schedule whose period 0 were a real
installment would lose it. Fineract always sends the disbursement first, so this is the original's
behaviour kept on purpose; the test exists so a future change to the helper cannot swallow a row
without something turning red.

## FIXED: an NPE that took the whole schedule screen down

`RepaymentSchedule.getNumberOfRepaymentsPending` was `if (!period.complete!!)` and the overdue
counter dereferenced the same nullable. A period Fineract sent without `complete` crashed the
screen. Now `period.complete != true`, which agrees on every non-null value and treats an absent
flag as not-complete — what the `!!` meant before it threw. Pinned by
`aPeriodWithNoCompleteFlagDoesNotCrashTheScreen`.

## FIXED: `CurrencyFormatter` crashed on data Fineract routinely sends

Found by these tests, in `core/common`, affecting **every** money screen in the app:

| actual | defect |
|---|---|
| desktop | `format(balance)` passed the nullable straight to `NumberFormat` → `IllegalArgumentException: Cannot format given Object as a Number`. Android had guarded the amount from the start, so the same transaction list rendered on Android and crashed on desktop. |
| desktop **and** android | `Currency.getInstance(currencyCode)` throws on null and on any non-ISO-4217 code — the empty string included, which is what a loan with no `currency` block projects to. |

Both now fall back: a null amount formats as zero, an unusable code keeps the locale's currency.
`CurrencyFormatter` is fork-added (the template has no such file), so no upstream PR applies.

`js` and `wasmJs` are still `TODO()` and throw on any money value — the web target has no currency
formatting at all. Pre-existing, logged as `currency-formatter-unimplemented-on-web`.

## The transaction row-action sheet is NOT ported

The original made every row tappable, opening a sheet with Undo / View receipts / View journal
entries / View details, chosen per transaction type. Its `onActionSelected` handler only
dismissed the sheet, so all four did nothing. Four controls that look functional and are not, so the rows are not clickable and no
sheet is drawn. The table ships read-only, which is what the original effectively was. Tracked as
`loan-transaction-row-actions-unimplemented`; the type→action mapping is preserved in the original
for whoever implements it.

Reversed transactions are KEPT and struck through in the error colour, as the original had them —
an officer reconciling an account has to see that a payment was undone.

## Approval: the nav argument was an entire serialized loan

The original route carried `LoanApprovalData(loanID, loanWithAssociations)` — the whole loan with
all its associations — JSON-encoded into a `SavedStateHandle` string and decoded in the
ViewModel's **constructor**:

```kotlin
private val arg = savedStateHandle.getStateFlow(key = "arg", initialValue = "")
private val loanAccountData: LoanApprovalData = Json.decodeFromString(arg.value)
```

Three problems, all gone by carrying the loan id: the decode ran in the constructor and threw on
the empty default, so the screen died when the process was restored; the blob went through the
back stack, which is size-capped; and the prefilled amount was a snapshot taken when the PROFILE
loaded, so an amount changed server-side in between was silently approved at the stale figure.
Reading `loanStream` here is free — the profile has just populated that cache.

## FIXED: the approval date contradicted its own declared format — the third occurrence

`DateHelper.getDateAsStringFromLong(approveDate)` emits **`dd-MM-yyyy`** ("21-10-2026") while
`LoanApproval.dateFormat` declares `"dd MMMM yyyy"`. Fineract was told to parse a string it cannot
parse. The identical bug was already fixed in `clientClosure` and `clientTransfer` — three times
in one port, which is why the payload is now built **only** from `ApiDateFormatter`, the helper
that owns the format constant, and the test spells the expected string out ("14 March 2026") so a
revert to the display helper fails loudly.

Watch the constant names while reading the original: **`DateHelper.SHORT_MONTH` is `"dd-MM-yyyy"`
and `DateFormatPattern.SHORT_MONTH` is `"dd MMM yyyy"`** — the same name for two different formats,
and `getDateAsStringFromLong` uses the first. Logged as `two-short-month-constants-disagree`.

The expected-disbursement date had the same problem one layer up: it arrived as a `[y, m, d]`
triple, was formatted for DISPLAY, and that display string was sent verbatim. Millis are held in
state and formatted at submit, so the field the officer reads and the field the server parses come
from one value through two formatters that each know their job.

## FIXED: the approval date rule excluded every date Fineract accepts

Both pickers shared one rule — `isSelectableDate { utcTimeMillis >= now }`, future only. For
approved-on that is wrong twice over: the picker's day cells are midnight instants, so today's cell
is already past when the dialog opens and **today was not selectable**; and Fineract rejects a
future approval date, so every date the officer could pick was one the server would refuse. The
field defaulted to now (valid) and touching the picker made it invalid.

Approved-on is now `<= now`, matching Fineract and the fork's own `clientClosure`. Expected
disbursement is unrestricted — a future scheduled disbursement is the ordinary case. Logged as
`loan-approval-date-rule-was-inverted`.

## Three smaller approval fixes

- `loanWithAssociations?.timeline!!` — a `?.` followed by a `!!` on the same chain, which crashed
  on a loan whose timeline Fineract omitted. Now nullable all the way down.
- `approvedPrincipal.toString()` on a nullable Double put the literal text **"null"** in the amount
  field, which then failed its own numeric validation and produced a dead button. Now `?: ""`.
- `disbursementDate ?: "null"` rendered the same four characters in the date field. Now "Not set".
- The submit branch was `if (isFieldValid(…)) { … }` with the else **commented out**, so a blank or
  non-numeric amount made the button look broken with nothing on screen to explain it. The amount
  error is now state the field renders.

The "Transaction Amount" field is **not** ported: the original required it to parse and then
discarded it — `LoanApproval` has no such field. A required input with no effect could only block
an approval. Logged as `loan-approval-transaction-amount-discarded`.

The outcome moved out of the composition body. The original did this:

```kotlin
is ShowLoanApproveSuccessfully -> { scope.launch { snackbar.showSnackbar(msg) }; navigateBack() }
```

— a coroutine launch and a navigation performed **during composition**, re-fired on every
recomposition, and the officer was navigated away before the snackbar could be read. It is now a
dialog state the ViewModel owns, matching `clientClosure`.

## The primary button is disabled, not inert

The detail rows are absent when unported; the primary button is the one control the original always
draws, so for an unported status it renders DISABLED with its real label — a greyed "Make
Repayment" says truthfully that this is the action for this loan and it is not available here. A
live button that silently did nothing is the defect the rule exists to prevent. Approve is live
after this slice. `LoanProfileScreenUiTest` proves both halves by RENDER, which is the only place
"drawn but dead" is visible.

## Test fakes moved to the module that owns the contract

`FakeLoanRepository` and `FakeLoanCommandRepository` live in `core/data`'s `commonMain` behind
`@ExperimentalDataTestingApi`, beside the interfaces they fake and the client-side pair that
already worked this way. The command interface names `core/network` DTOs, so faking it inside a
feature would drag `core:network` onto that feature's test classpath. Unimplemented members throw.

**70 tests in `feature/loan`** — 52 ViewModel, 18 render.


---

# S4c — repayment

The ACTIVE status's primary action, and the most-pressed button in the feature: a field officer
records a repayment on nearly every visit. It is also the highest-stakes screen here, because the
two ways to get it wrong are **recording the cash twice** and **losing it entirely**. Both have
tests.

## Three sequential loads became one combined read

The original ran `getLoanById` (header) → `getDatabaseLoanRepaymentByLoanId` (the already-queued
check) → `getLoanRepayTemplate` (payment types and amount due), each overwriting a single
`uiState`, and triggered the third **from the composition body**:

```kotlin
LoanRepaymentUiState.ShowLoanRepaymentDoesNotExistInDatabase -> {
    onLoanRepaymentDoesNotExistInDatabase.invoke()   // loads the template, during composition
}
```

so the template load re-fired on every recomposition while that state showed. Here the loan and
the template are two streams `combineScreenStates`-d into one `ScreenState`, and the queue check is
a separate boolean that gates the form. Nothing is loaded by drawing.

The combine also fixes a display bug: the original rendered the header from one request and the
form from another and showed whichever had landed, so the screen could show one loan's arrears
beside another request's payment types. `theFormWaitsForBOTHReads` pins it.

## FIXED: the write read two flows that had no value

`submit()` reads the queued-check flow (its last defence against paying twice) and the loan's
account number. Both were `stateIn(..., WhileSubscribed(5s))`, and a `WhileSubscribed` share sits
at its INITIAL value whenever nothing is collecting — so the guard read `false` and the account
number read `null`. On device the screen is collecting, so this would only ever have surfaced as a
double payment in the field. Both are now `SharingStarted.Eagerly`; `form` stays
`WhileSubscribed` because it is render-only. Caught by
`submitRefusesWhenOneWasQueuedWhileTheFormSatOpen` and
`theRequestCarriesThePaymentTypeIdAndTheAccountNumberFromTheLoan`.

## Recording it twice

Three guards, each tested:

| guard | why |
|---|---|
| already-queued → refuse, form not drawn | a queued repayment has NOT reached Fineract, so the server cannot dedupe it. The original refused too; this keeps the refusal and reads the same queue the sync screen drains. |
| re-checked at submit | the form can sit open for minutes while a sync elsewhere queues one. |
| in-flight submit ignores a second tap | a double tap on "Pay Now". |

A repayment queued for a DIFFERENT loan does not block this one — pinned, because the check is a
filter on `loanId` and an unfiltered version would lock the whole feature after one offline capture.

## Losing it entirely

OFFLINE is **not** a failure for this screen — it is the one write in `feature/loan` with a designed
offline path, because the officer has already taken the cash. The request is queued and the dialog
says plainly that the server has not seen it ("Saved to Sync"), where the original threw the
offline case into its generic `payment_failed` message and queued nothing.

The worst case gets its own test: offline **and** the queue write fails, so nothing holds the
payment anywhere. That must never report "saved" — it reports failure naming the fact that nothing
was recorded.

## FIXED: a zero-amount repayment was submittable

All three amount fields blank passed the original's `isAllFieldsValid` — each read as `0.0`, each
`>= 0` — so an officer could post a zero repayment, which Fineract accepts as a real transaction on
the account. The total must now be `> 0`.

Also fixed: the payment type was validated on its **display string** (`paymentType.isNotBlank()`),
so a type whose name came back blank read as unselected while a selection carrying id `0` read as
selected. The id is what the request needs, so the id is what is checked — and the chosen option is
dispatched whole rather than resolved as `paymentTypeOptions?.get(index)?.id ?: 0`, where `0` is not
a payment type.

## The date pairing here is CORRECT, unlike the approval's

`transactionDate = DateHelper.getDateAsStringFromLong(date)` with `dateFormat =
DateHelper.SHORT_MONTH` — that constant is `"dd-MM-yyyy"` and the helper formats with it, so the two
agree. Carried over unchanged and pinned, precisely because the identical-looking line in
`LoanApprovalViewModel` declared a different format. See the S4b note on the two `SHORT_MONTH`s.

The date RULE is the inverted one again: `>= now`, future-only, on a field recording cash that has
already been collected — so every selectable date was one Fineract refuses, and today was not
selectable at all. Now `<= now`.

## Two hardcoded colours are NOT carried over

`FarApartTextItem` drew its label `Color.Black` and its value `Color.DarkGray`, ignoring the theme
— on the dark scheme that is near-black on near-black, so the client's arrears and amount due would
be unreadable. §6a carries the UI verbatim "including anything the agent believes is wrong", but a
literal colour that makes live figures invisible is a RENDER failure, which
RULE-IMPL-DESIGN-TOKEN-CONSUMPTION-001 blocks outright. Mapped to `onSurface` /
`onSurfaceVariant`. The review sheet's label had the mirror problem — `surfaceVariant`, a
BACKGROUND colour used as text — now `onSurfaceVariant`. Logged as
`repayment-header-hardcoded-colours`.

## Smaller fixes

- the success sheet printed `resourceId.toString()` on a nullable Int, rendering the word "null"
  as the transaction ID. Now an em dash.
- a failure NAVIGATES AWAY in the original — every outcome did, so a rejected payment lost
  everything the officer had typed. Failure now dismisses to the still-filled form, so they fix the
  date and retry. Pinned.
- retry re-ran whichever load a flag said had failed, leaving a failure in the other unreachable
  from the retry button. It reaches both.

## `AppTwoButtonRow` gained per-button test tags

`modifier` reaches the Row, and a Row carries no enabled semantics — so no test or Maestro flow
could ask whether the CONFIRM button was live, which is the question the absent-never-inert rule
turns on. Two optional `*BtnTestTag` parameters, defaulting to none, so every existing call site is
unchanged.

**104 tests in `feature/loan`** — 76 ViewModel, 28 render.


---

# S4d — account summary, and the reason this whole vertical was unreachable

## The finding that reordered this slice

Before porting anything, the §6a caller rule turned up something larger than the screen it was
asked about: **nothing in the running app navigated to any loan screen.** S4a–S4c had shipped seven
screens and 104 tests behind a route that compiled, registered, and had no caller.

The original has TWO loan detail screens with different callers:

| tapped | opens | fork status before S4d |
|---|---|---|
| a loan hit in **search** | account **summary** | search rows were deliberately inert |
| a loan on **group detail** | account **summary** | group detail has no loan affordance ported |
| a loan in **`clientLoanAccounts`** (the client's loan list) | loan **profile** | that list is not ported |
| a loan on **`clientDetails`** | summary | nothing navigates to `clientDetails` at all |

So the profile — what S4a ported — is reached only through the client's loan LIST, and the summary
— not ported — is what the two live callers open. Porting more detail screens behind the profile
would have added more unreachable code; the fix was the summary plus one tap.

## The search row's tap, three slices late

`SearchResultRow` shipped inert in S2 with a note saying it would gain its tap "in S3, with the
first destination that can receive it" — and the note went stale: client and group details landed
and the row stayed inert. It is clickable now for the three entity types that HAVE a destination
(`CLIENT` → client profile, `GROUP` → group detail, `LOAN` → loan summary) and still inert for
`CENTER` and `SAVING`, rather than all five going live together.

The `when` lives in the SHELL (`SearchNavigation.navigateToSearchResult`), not in `feature/home`.
In the original it lived in the search feature, which is why `feature/search` imported
`feature/client`, `feature/loan` and the rest; the shell is the only layer allowed to know about
more than one feature.

Matching is case-INSENSITIVE on `entityType`. That value comes off the wire, and this fork's own
existing test fixture spells it `"client"` while Fineract sends `"CLIENT"` — an exact match would
have made every client result silently inert for one of those spellings. Pinned.

## FOUR status derivations became one

The original derived the loan's status from the same `LoanStatus` four times on this screen alone:
the dot's colour had its own `when`, the dot's accessibility label had an identical one written out
again, `getPrimaryAction()` had a third with a different precedence, and `isButtonActive()` a
fourth. The loan PROFILE has a fifth.

One `LoanSummaryStatus` now drives the dot, its label and the button. The precedence kept is THIS
screen's — `active` first — which is **the opposite of the profile's**, where `pendingApproval` is
tested first. A loan Fineract reports as both therefore reads ACTIVE here and PENDING there. That
is the original's behaviour on both screens and it is carried over rather than silently unified;
logged as `loan-status-precedence-differs-by-screen`.

This screen also distinguishes the two states the profile collapses into "Unknown" —
AWAITING_DISBURSAL (→ Disburse Loan) and CLOSED (→ no action) — so the profile's catch-all gap has
an answer in the original even though the profile itself has none.

## The disbursement gate, kept

`shouldInflateLoanSummary` = `active || closedObligationsMet || overpaid`. A loan that has never
disbursed has a summary block full of zeroes and projections, and "Amount Paid: ₹0.00" beside
"Outstanding: ₹0.00" reads as a loan that has been paid off. Applied by NULLING the summary rather
than per-row, so every figure is blank together or populated together and no row can keep a stale
value. The disbursement DATE is gated too — an expected date shown as an actual one says money has
moved when it has not.

The loan OFFICER is deliberately NOT gated: assigned before disbursement, and exactly what someone
looking at a pending loan needs. The original did not gate it either.

## Dead controls the original shipped, now disabled or absent

- the **Transfer Funds** button for an OVERPAID loan passed `isButtonActive()`, so it was drawn
  ENABLED — and its action dispatched `NavigateToLoanTransfer`, whose handler was an empty `{ }` on
  both the ViewModel and the screen side. A live button that did nothing.
- the **CLOSED** button's `onClick` branch was also `{ }`; it was disabled only by the separate
  `isButtonActive()` predicate, which could have disagreed with the label it sat under. CLOSED now
  returns no action at all, so the disabled state and the label come from one place.
- the **overflow icon** is not drawn when no menu entry has a destination. A menu button that opens
  an empty menu is the same dead control as a dead row. Two of five entries are ported
  (Transactions, Repayment Schedule); More Loan Info, Documents and Charges are absent.

## Smaller fixes

- `stringResource(feature_loan_loan_id) + accountNo` — string CONCATENATION, which no translation
  can reorder. Now a placeholder.
- the four status labels were inline Kotlin literals (`"Active"`, `"Pending Approval"`, …) used
  only as an accessibility `contentDescription`, so a screen reader got English in every locale.
  Now string resources.
- the overflow menu's first entry passed the raw constant `Constants.DATA_TABLE_LOAN_NAME`
  ("More Loan Info") straight into the menu, so one entry was untranslatable while its four
  siblings were resources.
- `formatCurrency` returned `amount.toString()` when the currency code was blank — a raw "6000.0"
  on screen. That branch existed because `CurrencyFormatter` CRASHED on a blank code; now that it
  falls back to the locale's currency (S4b), every figure goes through the formatter.
- `actualDisbursementDate` is `List<Int?>?` and the original did `all { it != null }` followed by an
  unchecked cast. Nulls are filtered and the size re-checked.
- the copied-to-clipboard message was driven by a `snapshotFlow` inside `LaunchedEffect(Unit)`,
  collecting for the screen's whole lifetime to watch one boolean. Keyed on the boolean instead.

## Not carried over: the breadcrumb bar

The original rendered `MifosBreadcrumbNavBar(navController)` and took a `NavController` as a SCREEN
parameter to do it. This fork has no breadcrumb component and no screen takes a NavController —
navigation is `onX` callbacks, which is what lets every screen render in a test without a NavHost.
Logged as `loan-summary-breadcrumb-not-ported`.

**129 tests in `feature/loan`** (93 ViewModel, 36 render) and **4 new** in `feature/home` for the
result tap. 920 desktop tests across the project.


---

# S4f — disbursement and loan charges

Both unlock affordances that were rendering disabled or absent: the summary's **Disburse Loan**
button (the AWAITING_DISBURSAL action, one of the two states the profile collapses to "Unknown")
and the **Charges** entry on the summary's menu *and* the profile's detail rows.

## FIXED: the date/format mismatch, FOURTH occurrence — and this time it cannot recur

The disbursement sent `actualDisbursementDate = DateHelper.getDateAsStringFromLong(date)`
("14-03-2026") on a payload whose `dateFormat` defaulted to `DateConstants.DATE_FORMAT`
("dd MMMM yyyy"). Same bug as `clientClosure`, `clientTransfer` and `loanApproval`.

The structural fix: **`locale` and `dateFormat` are now REQUIRED, non-defaulted parameters** of
`LoanDisburseInput`. A payload that supplies its own format is exactly how this kept recurring —
the format field was always filled in correctly and the DATE was formatted by some other helper.
Required parameters move the decision to the call site that produces the date string, where the two
can be read together.

An earlier version of that file had defaulted `dateFormat` to the string literal `"dd-MM-yyyy"`
while its sibling `locale` referenced a constant — so the pairing happened to be right by
accident, in a file nothing called.

## `feature/loan` must not depend on `core:network`

`disburse` took a `core/network` request DTO and returned a `core/network` response DTO, so the
feature could not construct the one or match on the other. Resolved the same way as the seven
client commands: the contract takes `LoanDisburseInput` (a `core/model` type, which already existed
unused) and returns `MutationResult<Unit>`; `core/data`'s impl owns the one mapping to the wire
payload. The response carries office/client/loan/resource ids the screen does not render.

## Loan charges needed a data layer, and the client store had predicted it

`provideClientChargeStore`'s own note says *"a loan-charge store would be its own provider with its
own key"*. That is `provideLoanChargeStore`: keyed by loanId, its own `loans/{id}/charges` fetcher
(a bare list, where the client endpoint returns a `Page`), and its own Room reader on the
`loanId` column — `ChargesEntity` carries both `clientId` and `loanId`, and a charge belongs to
exactly one, so a shared `where clientId = :id OR loanId = :id` would serve a client's charges to a
loan whose id matched a client's.

The writer stamps the owning loan, because `loans/{id}/charges` does not echo `loanId` back on each
row — without it every charge would persist with a null `loanId` and the reader would return nothing
on the next cold start.

## Defects fixed in these two screens

- **the pull-to-refresh spinner could never appear.** `refreshLoanChargeList()` set `isRefreshing`
  true, called a function that only LAUNCHED a coroutine, then set it false — all within one frame.
  The stream owns that signal now, so there is no flag to get wrong.
- **`paymentTypeId` was initialised to `0` and sent.** Payment type 0 does not exist; Fineract
  rejects it, or accepts it against whatever row holds that id. Null when unchosen.
- **a payment type with no id was offered.** The original mapped options to NAMES and resolved the
  id back by INDEX, so an idless entry silently shifted every later selection onto the wrong type.
  Unsendable options are not offered.
- **`amount.toString()` / `chargeId.toString()` on nullable fields** rendered the literal "null",
  and the amount carried no currency at all — a 1500.0 charge read "1500.0".
- **a zero or negative disbursement was submittable.** The original checked only that the string
  parsed.
- **`isFieldValid` returned false with both of its Toasts commented out**, so an invalid amount made
  the button look broken.
- **the charge due date read "2026-3-14"** — `formattedDueDate` builds
  `"${d[0]}-${d[1]}-${d[2]}"`: year-first, unpadded, unlike every other date in the app. Formatted
  with `DateHelper` in the projection; the entity's property is left alone (it is a `core/database`
  concern with other callers), as is its hardcoded English "No Due Date" fallback.
- **the disbursement's success snackbar + navigation fired from the composition body**, re-firing
  on every recomposition with the officer navigated away before the message could be read. Same
  shape as the approval screen's.
- **a failure replaced the whole form** with a full-screen error, losing the amount and note.
- **more hardcoded colours**: the charge detail rows used `Color.Black` / `Color.DarkGray` with an
  inline `TextStyle(fontSize = 16.sp)`. Third screen with this pattern; mapped to tokens for the
  same reason as the repayment header — on the dark scheme the amounts would be unreadable.
- **the empty-state copy said "No Loan Charges Available for this client"** on the LOAN charges
  screen, reached from a loan.

## A hand-rolled fake finally cost what it was predicted to

`LoanProfileViewModelTest` had an anonymous `object : LoanRepository` listing every member — written
before `FakeLoanRepository` existed. Adding `loanChargesStream` to the contract broke it, in a test
with nothing to say about loan charges. That is precisely the maintenance cost the shared fake was
introduced to remove, so the test now uses it. One fake, one place to add a member.

## Disbursement has NO offline path, deliberately

Unlike a repayment, the money has not moved yet — so there is nothing captured that refusing would
lose, and telling the officer to find signal is the correct outcome rather than a queued promise.
Pinned by `offlineSaysTheDisbursementWasNotSentAndQueuesNOTHING`.

**167 tests in `feature/loan`** — 120 ViewModel, 47 render.


---

# S4g — amount transfer

The OVERPAID action, and the last of the four primary-button states. Both loan detail screens now
offer every action their status implies.

## The same action worked on one screen and was dead on the other

In the original, the PROFILE's Transfer called `navigateToTransferScreen(loanId, accountNo,
clientId, currencyCode, officeId)` and worked. The SUMMARY's dispatched
`NavigateToLoanTransfer`, whose handler was an empty `{ }` on **both** the ViewModel and the screen
side — a drawn, enabled button that did nothing. One feature, one action, two screens, one broken.
Both are wired here.

## A cascading query, not a subscription

Office → client → account type → account: each selection asks `accounttransfers/template` a
DIFFERENT question with more `to*` parameters filled in. So the template read is a suspend one-shot
on `LoanRepository` rather than a `ScreenDataStream` — a stream is keyed at construction, so
modelling this as one would mean constructing a new stream per selection and leaking the collector
of each. The store still caches every distinct question, so stepping back up the cascade is served
from cache.

## FIXED: Retry crashed

`submitTransfer` resolved four nullable ids with `!!`:

```kotlin
toOfficeId = state.selectedOfficeId!!, toClientId = state.selectedClientId!!,
toAccountType = state.accountTypeId!!, toAccountId = state.accountId!!,
```

Safe from the happy path, because `validateFields()` checked all four first — but `OnRetryClick`
called `submitTransfer()` **directly**, skipping validation. So retrying after a failure threw.
`state.amount.toDouble()` was unguarded on the same path.

The four ids and the amount are now resolved ONCE into a `TransferDraft` at validation, and submit
takes that draft — so retry re-sends something already proven complete and no path reaches the
payload with a null. `AccountTransferRequest`'s thirteen fields are required now too (they were all
nullable with null defaults), so the compiler enforces it: a nullable field there could only ever
have meant "the caller forgot".

## FIXED: two of four selections resolved by list INDEX

`OnOfficeChanged(index)` and `OnClientChange(index)` looked their id up as
`state.offices.getOrNull(index)?.id`, while `OnAccountTypeChange` and `OnAccountChange` carried the
id. The lists are re-sorted on every template fetch, so an index captured before a refetch could
resolve to a different office. All four carry the option.

## FIXED: changing the account type left the chosen ACCOUNT selected

The original cleared the levels below office and below client, but not below account type — so an
account chosen under "Savings" survived a switch to "Loan", and that account may not exist under the
new type. Every level clears the ones below it.

## FIXED: a loading spinner nobody could see

Caught by a render test, which is the point of having them: the spinner was composed but never
displayed, because it sat after the submit button in a six-field scrolling column. Selecting an
office showed no sign of anything happening while the next level reloaded. It is beside the
"TRANSFERRING TO" heading now — inline rather than an overlay, because every cascade step reloads
and an overlay would flash over the form four times on the way to one transfer.

## The route is one id, where the original carried five fields

Client id, office id, account number, currency and account id were each snapshotted by the caller
from the loan it had just rendered. The screen reads the loan itself — free, because whichever
detail screen sent the officer here has just populated that cache — so the route is `loanId` and
there is no snapshot to go stale. The template read genuinely depends on those values, which is why
it waits for the loan rather than firing in `init`.

## A date pairing that was RIGHT, and is now held in one place

The original paired `DateHelper.formattedFullDate` with `Constants.DATE_FORMAT_LONG` — both
"dd MMMM yyyy", so this screen's date was one of only two in the feature that worked. That was luck
held in two unrelated constants; it comes from `ApiDateFormatter` now, like every other payload.

## Smaller fixes

- a cascade level is **disabled** until the level above it is chosen. The original expressed this
  only by leaving the list empty, so an un-chooseable dropdown looked identical to one whose office
  genuinely had no clients.
- options with a null id are dropped — unsendable, so not selectable.
- a FIRST template failure replaces the form (nothing to fill in); a LATER one keeps it, so a
  failed cascade step does not discard everything the officer has chosen.
- the six field errors became a closed enum the screen maps to copy, rather than six
  `StringResource?` fields threaded through state.
- `submitAccountTransfer` returned Ktor's `HttpResponse`, so no feature could call it. `Unit` now —
  the gateway already turns a non-2xx into `Failed`, which is the distinction the screen renders.

**199 tests in `feature/loan`** — 140 ViewModel, 59 render.


---

# S4h — the add-charge form

The charges list's only action, and the reason its "+" was not drawn. With this, every affordance
on every ported loan screen leads somewhere.

## A screen, where the original was a sheet inside the list

The charges LIST owned a `showLoanChargeForm` boolean and composed the `ModalBottomSheet` itself,
then reported the outcome by firing snackbars from the sheet's `onSuccess` / `onError` callbacks. So
the list owned the form's visibility, the form owned its own state, and the result travelled back up
through two lambdas.

It is its own destination here, like every other write in this feature. The list needs no callback
to learn a charge was added — its stream re-reads when the officer comes back.

## FIXED: the due-date field was drawn for charges that have no due date

A disbursement or installment-fee charge has no due date, and sending one makes Fineract reject the
whole request. The original handled that in the PAYLOAD (`if (chargeTime == SPECIFIED_DUE_DATE)`)
but drew the field regardless — so an officer could pick a date that was then silently discarded.
The field follows the payload now; `needsDueDate` drives both.

Its picker is also deliberately UNRESTRICTED, unlike the approval / repayment / disbursement ones:
a charge's due date is when it falls due, normally in the future — the opposite of a date recording
something that already happened.

## FIXED: the amount was never validated at submit

`updateAmount` set `amountError = amount.toDoubleOrNull() == null` as a side effect of TYPING, and
`createLoanCharges` never re-checked — so an amount left exactly as prefilled, or a prefill that was
somehow unparseable, went through unvalidated. Re-checked at submit, and zero/negative is refused.

## The data layer gained two things

- **`loanChargeTemplate(loanId)`** on `LoanRepository` — a suspend one-shot like
  `accountTransferTemplate`, because the sheet asks once when it opens and the answer does not
  change while it is open. The STORE for it already existed (`provideGetChargeTemplateStore`) with
  nothing reading it.
- **`createCharges` off `MutationResult<HttpResponse>`** — the fourth command to need this, after
  the seven client ones, `disburse`, and `submitAccountTransfer`. A feature cannot name a Ktor type.

## Charge types the form cannot handle are DROPPED, not offered

`chargeOptions` is filtered to entries with an id, a name, an amount and BOTH a recognised
charge-time and charge-calculation kind — the original's `mapNotNull`, kept. Fineract has many more
charge-time kinds than the three this form knows (overdue installment, tranche disbursement, …) and
percentage charges need a base the form does not collect, so an unrecognised option cannot be
submitted and must not be selectable.

A tenant with no usable type now SAYS so, where the original would have shown an empty dropdown over
a live submit button.

## The date pairing here was RIGHT

`getDateAsStringFromLong` emits `"dd-MM-yyyy"` and the payload declared that same literal — one of
only three places in the original where a date and its `dateFormat` agreed. Kept, with the literal
replaced by `DateHelper.SHORT_MONTH`, the constant the helper actually formats with, so the two
cannot drift apart.

**227 tests in `feature/loan`** — 157 ViewModel, 70 render.


---

# S4i — reschedules

The profile's "Loan Reschedules" row, with approve and reject on a pending proposal.

## One of the better-written screens in the original

This ViewModel already used `BaseViewModel`, already paired its dates through `ApiDateFormatter`,
and already checked the PENDING precondition **twice** — once when the icon is tapped and again
when the confirmation is accepted, so a row whose status changed in between cannot be acted on. All
three kept; the second check in particular is a guard most of this feature's other screens lacked.

## FIXED (mine): the precondition guard read a `WhileSubscribed` flow

`isPending()` read `history.value`, and `history` is `WhileSubscribed` — so with no collector it
sits at `ScreenState.Loading` and the guard refused **every** approve and reject. Eight tests
caught it at once.

**Third time this exact shape has bitten in this port**, and the second time it was a real defect
rather than a test artifact:

| where | read by | verdict |
|---|---|---|
| repayment: double-payment guard + account number | the WRITE | real defect — now `Eagerly` |
| client loan accounts: header count | the composition | correct as `WhileSubscribed`; the TEST had to collect |
| reschedules: PENDING guard | the ACTION | real defect — now an eager `pendingIds` |

The rule, written down after the second and violated anyway: **a flow read by anything other than
the composition must not be `WhileSubscribed`.** `history` itself stays `WhileSubscribed`, because
only the screen reads it.

## FIXED: the empty decision lived in two places

The repository's stream declared `isEmpty = { it.isEmpty() }` while the ViewModel wanted to own it
— the same rule twice, free to drift, and the repository's half is not provable without the real
store. It is `emptyIfContent` in the ViewModel only now, matching the transactions, charges and
client-loan-accounts lists.

## FIXED: one dialog state covered two different waits

`DialogState.Loading` served both the initial fetch AND an approve/reject in flight, so approving a
row replaced the whole list with a loading dialog and then rebuilt it. The read is a `ScreenState`
and the action's in-flight state is `actingOn: Int?` — so the list stays on screen and only the row
being acted on has its icons held.

## "Delete" is really REJECT

The original's icon, action, repository method and error string were all named delete — but the wire
command is `?command=reject` with a `LoanRescheduleRejectionRequest` carrying a date and a user, so
the server records a decision and removes nothing. Named reject throughout, and the confirmation
copy says so. That the LABEL was wrong is a product-facing change, so it is logged as
`loan-reschedule-delete-is-really-reject` rather than treated as a silent fix.

## Approve and reject are ABSENT on a decided row

The original drew both icons on every row and its handlers returned silently for the non-pending
ones — live and inert. There is nothing to do to a reschedule the server has already decided.

## Smaller fixes

- the three reschedule commands returned Ktor's `HttpResponse`. Fifth, sixth and seventh commands
  to need de-Ktoring.
- a failure showed one fixed string per action ("could not approve"), discarding Fineract's
  explanation. "loan is not active" is the common one and names the problem.
- every one of a reschedule's four dates is optional on the wire and `getDateAsString` indexes
  `[2]` directly; all four are size-guarded.
- the reschedule REASON catalog read (`rescheduleloans/template`) had a store with nothing reading
  it — the fourth piece of unreachable ported code this slice has found. It is behind
  `loanRescheduleReasons()` now, ready for the propose form.

## Documents came free, Notes did not

The profile's "Loan Documents" row and the summary's Documents menu entry needed **no new screen**:
`feature/document` is entity-agnostic (`DocumentListRoute(entityType, entityId)`) and the original
passed `Constants.ENTITY_TYPE_LOANS` to that same screen. A build edge and two lines.

I assumed Notes would be the same and checked before saying so — it is not. `feature/note` was
ported CLIENT-ONLY (`NoteListRoute(clientId)`), although the original's note screen is itself
entity-agnostic. Wiring loan notes means generalising that feature's route, ViewModel and
repository surface from a `clientId` to an `(entityType, entityId)` pair. Worth doing once, since
savings and groups will want the same; logged rather than half-done here.

**256 tests in `feature/loan`** — 176 ViewModel, 80 render. Five of the profile's thirteen detail
rows are live, and three of the summary's five menu entries.


---

# S4j — propose a reschedule

The reschedules list's "+", which completes the reschedule vertical: propose, then approve or
reject.

## Five optional changes, each gated by its own checkbox

Change repayment date · introduce grace periods · extend repayment period · adjust interest rate ·
waive penalties. Unchecking one clears its fields — the original's behaviour, and right, because a
value left behind by an unchecked option is one the officer cannot see and did not mean.

## FIXED: unchecked options were sent as EMPTY STRINGS

```kotlin
adjustedDueDate = if (state.changeRepaymentDateSelected) state.adjustedDueDate else "",
graceOnPrincipal = if (state.introduceGracePeriodsSelected) state.graceOnPrincipal else "",
```

All five, every time. `LoanRescheduleRequest` declares them nullable with null defaults: `null`
means "not part of this proposal", `""` means "here is a date, and it is the empty string" — which
Fineract then tries to parse against the declared `dateFormat`. Omitted now, and pinned by
`anUNCHECKEDOptionIsOMITTEDNotSentAsAnEmptyString`.

`waivePenalties` keeps the original's `true`-or-omitted shape: Fineract reads the field's presence
as the instruction, so `false` is not the same as absent.

## FIXED: the four numeric fields were never validated

`graceOnPrincipal`, `graceOnInterest`, `extraTerms` and `newInterestRate` were free text sent as
strings, and `isSubmitEnabled` only checked they were non-blank — so **"two" reached the server**.
Each is now checked for a parseable non-negative number, with the error on the field.

## FIXED: a disabled submit button that could not say why

The original's `isSubmitEnabled` disabled submit whenever any of six required fields was blank. An
officer saw a dead control with no indication of which field it was waiting for. Submit stays
enabled and tapping it REPORTS every missing field at once, each error on its own field — where the
original had one error property, for the reason only.

## Dates are millis, keyed fields are a map

The three dates are millis in state and formatted at submit, so the picker has something to reopen
at (the original stored them pre-formatted — the wire representation was the only one). The four
numeric fields are a `Map<RescheduleField, String>` rather than four properties plus four actions
plus four error slots, so the validation and the screen cannot disagree about a field's name.

The adjusted-due-date picker opens at the FROM date rather than today: the installment is being
moved relative to the one being rescheduled.

## The reschedule vertical, end to end

| | |
|---|---|
| propose | S4j — this screen |
| see proposals | S4i — the list, from the profile's "Loan Reschedules" row |
| approve / reject | S4i — on a pending row only |

**290 tests in `feature/loan`** — 200 ViewModel, 90 render.

## S4k — `groupLoanAccount` → `grouploan`, and the caller that did not exist

Ported from `6b66e8a43:feature/loan/groupLoanAccount/` (654-line screen + 1 UiState sealed class).
The group loan application: pick a product, its template drives every field, submit.

### The caller had to be ported in the same slice

Spec §6a's path rule — *name the affordance in the RUNNING fork that will open it* — is what made
this slice two features wide. `feature/groups`' detail screen had no loan affordance at all: the
original's six-item overflow menu was never carried across. Worse, Documents and More-Group-Info
had their **whole chain** already ported — action, event, ViewModel handler, nav edge, even the
`GroupsDetailTestTags` constants — with no control that dispatched any of it. `onDocuments` and
`onDataTables` were passed into the screen, folded into `canOpenDocuments` / `canOpenDataTables`,
and never read.

That is the inverse of the defect "absent, never inert" guards against, and a dead-clickable audit
cannot see it: it looks for clickables with no handler, and here there was a handler with no
clickable. The cheap mechanical check is the other direction — **for each `*Action` member, grep the
feature's own screen sources for a dispatch; zero dispatchers is this defect.** Logged as
`group-detail-menu-was-never-ported-so-three-destinations-were-unreachable`, with the sweep.

So S4k ships the menu with the three items whose destinations exist (Add Loan Account · Documents ·
More Group Info), each gated on its own flag. Add Savings Account (S5), Group Clients and Notes stay
absent. The icon itself is absent when no item is available, rather than opening an empty menu.

### Six defects in the original's payload

The payload is this screen's logic, and every one of these is a value Fineract accepts without
complaint — which is why each is a test rather than a note.

| # | Original | Port |
|---|---|---|
| 1 | `loanType = "individual"` on a **group** loan screen | `"group"` |
| 2 | `DateHelper.getDateAsStringFromLong` (`dd-MM-yyyy`) sent with `dateFormat = ApiDateFormatter.DATE_FORMAT` — the **5th** occurrence, and the first on a CREATE | both dates via `ApiDateFormatter.formatForApi`, paired with the format + locale it owns |
| 3 | five hardcoded defaults — `"10000.0"`, `"10"`, `"5.0"`, `"2"`, `"20"` — belonging to no product | `GroupLoanTemplate.defaultNumbers()`, re-prefilled when the product changes |
| 4 | `repaymentFrequencyType` fed from the **loan term's** dropdown, so a weekly repayment on a monthly term was unexpressible | its own dropdown |
| 5 | `repaymentFrequencyDayOfWeekType` **and** `repaymentFrequencyNthDayType` both fed from the day-of-week dropdown, so the nth-day field got a day-of-week value | omitted — this form has no control for either |
| 6 | `productId` submittable as `0`; unchosen optional dropdowns sent as `0` | selecting a product is what loads its template, so they cannot disagree; unchosen → `null` |

Plus: unguarded `loanTerms.toInt()` / `nominal.toDouble()` crashed the submit on any non-numeric →
per-field `isPositiveNumber()` validation, and the submit button stays **live** so a fifteen-field
form reports what is missing instead of being disabled with no explanation.

### External Id removed, not carried

The original's External Id field writes to a `rememberSaveable` that nothing reads — `GroupLoanPayload`
has no such member, in the fork or at `6b66e8a43`. The officer's input was dropped on submit with a
successful-looking result. Made **absent**, per the same rule. Restoring it needs the payload member
*and* proof of Fineract's format, which enforces uniqueness — a wrong guess fails the whole
application. Left OPEN in CAPABILITY_GAPS as `group-loan-external-id-was-discarded`: the omission is
tracked, not the removal.

### Also fixed, found by the tests

- **`GroupsDetailScreen` had no `verticalScroll`** — seven fields plus the Activate button overflow a
  short screen, putting the only write out of reach on exactly the groups that render it.
- The submission date picker allowed **future dates only** on both pickers (the same inverted rule as
  four other screens), so *today* was unselectable for "Submitted On". Submitted-On is now
  not-in-the-future; expected disbursement stays unrestricted.

### Shape

`GroupLoanApplicationState` holds `numbers` / `selections` / `errors` as maps keyed by
`GroupLoanField`, so the defaults, the screen and the validation cannot disagree about a field's
name — the original's sixteen `rememberSaveable` locals could. The four-case `UiState` splits into
the products `ScreenState`, the template's own flags and a dialog for the write's outcome: the
original replaced the filled-in form with a progress bar mid-submit, and on success fired a snackbar
**from the composition body**.

21 tests (14 ViewModel + 7 render), plus 6 render tests on the group menu in `feature/groups`.

## S4l — `newLoanAccount` → `newloan`, the last S4 item

Ported from `6b66e8a43:feature/loan/newLoanAccount/` — a 1,208-line ViewModel, a 538-line screen
and five pages totalling 1,520 lines. The new INDIVIDUAL loan application.

### The caller was already there

`feature/client`'s loan-accounts list had the whole "add account" chain ported — action, event,
`onAddAccount` callback, test tag — and correctly `null`-gated at `ClientNavigation.kt`, so the
affordance was **absent rather than drawn dead**. Wiring it was one line. That is the
absent-never-inert rule paying off: contrast S4k, where the group menu had a live destination and no
control at all.

The original has a *second* individual-loan screen (`loanAccount/`, 794 lines) reached only from
`clientDetailRoute` — the dead route. `newLoanAccount` is the live one, reached from the loan list
and from an "apply new application" chooser that is not ported.

### Four steps, not five — the Charges page carried nothing

`LoansPayload` has **no `charges` and no `collateral` member**, in the fork or at `6b66e8a43`;
`CreateLoanAccountUseCase` is a straight passthrough; and neither payload builder reads
`state.addedCharges` or `state.addedCollaterals`. So an entire wizard page plus the add/edit/delete
collateral dialogs were **dead inputs** — the officer attached charges to a loan application and
every one was discarded on submit, with a successful-looking result.

Absent, not inert. Four steps: Details · Terms · Schedule · Preview. Logged **open** as
`new-loan-charges-and-collateral-were-discarded`, because charges on an application is a real
requirement — Fineract accepts `charges[]`; restoring it starts in `core/model`, not in the UI.

### One payload builder, because the original's two had drifted

|  | preview builder | submit builder |
|---|---|---|
| option lookup | `?.getOrNull(index)?.id` — safe | `?.options[index]?.id` — **throws on -1** |
| interest rate | `.toDoubleOrNull()` — safe | `.toDouble()` — **throws on non-numeric** |

Someone hardened the preview copy and never backported it, so the crashes survived on the path that
matters: preview a schedule fine, then crash on submit. Six lookups had no `-1` guard and **all six
were reachable without the officer doing anything unusual** — `loadLoanAccountTemplate` resolved
them with `indexOfFirst`, which returns -1 when the template's own chosen value is absent from its
own option list. And `moveToNextStep()` validated *nothing*, so the empty form reached submit too.

### Twelve parallel indices became one keyed map

The original held twelve `Int = -1` fields storing dropdown *positions*, with its own comment
conceding "it is not actual value for the field". Two real consequences: `-1` reached the payload
(above), and **stale indices survived a product change** — the template load reset six of the twelve,
so position 3 of the previous product's fund list was submitted against the new product's.

`selections: Map<LoanField, LoanOption>` holds ids (and the *code* for the repayment strategy, the
one option Fineract identifies that way). Unchosen means absent from the map, so "unchosen" and
"position -1" cannot be confused, and the whole map clears on a product change.

### Also fixed

- **Every step validates.** Each `LoanStep` owns its required fields; Submit re-validates *all*
  steps and lands on the earliest one with a missing field — reachable by walking back and emptying
  one.
- **`Back` is two different things.** The top bar leaves the wizard; the footer walks a step. The
  original wired both to the same callback, so the top bar abandoned a four-page form on what looked
  like a step-back.
- **The preview summary read the previous calculation** — `state.repaymentSchedule` while the new
  schedule sat in the response, so figures belonged to the terms before the last edit (and were zero
  on first run). Every figure now comes from the response, and editing a schedule-affecting field
  clears the preview instead of leaving a stale one up.
- **A server-rejected schedule keeps the form** and shows the server's own reason. The original
  replaced the whole wizard with a generic error page, discarding both.
- `locale = "en"` hardcoded → `ApiDateFormatter.LOCALE`; blank `externalId` sent as `""` → omitted.
- The submission date picker allowed **future dates only** (so *today* was unselectable) — the same
  inverted rule as five other screens in this migration.
- `HorizontalPager` + two `LaunchedEffect`s syncing it to `currentStep` both ways → the step is
  state and the content is a `when`, so a swipe cannot skip validation.
- `launchEffectKey = Random.nextInt()`, which existed to re-fire a `LaunchedEffect` that showed a
  snackbar from the composition → a dialog in state.

### Data layer

`createLoanAccount` and `calculateLoanSchedule` both returned `MutationResult<HttpResponse>`, which
a feature module cannot consume. Both now take **`LoanApplicationInput`** (a `core/model` type with
`locale`/`dateFormat` as *required* parameters — the structural fix for the pairing defect that
recurred five times) and return `MutationResult<Unit>` / `MutationResult<RepaymentSchedule>`. Added
`LoanRepository.loanTemplate(clientId, productId)` as a one-shot, for the same reason
`groupLoanTemplate` is one: the wizard re-asks on every product change and a `ScreenDataStream` is
keyed at construction. `loanTemplateStream` remains, still with no production caller.

The schedule-table mapping moved to `schedule/RepaymentScheduleTable.kt` and is now shared by the
existing loan-schedule screen and this wizard's preview — rather than becoming a third copy of logic
this slice already caught drifting twice.

37 tests (23 ViewModel + 14 render). The action-dispatch sweep reports **zero** new hits.

## S5d — retry was never offered, and three refresh actions outlived their controls

From draining the action-dispatch sweep to zero.

**`OnRetrySubmit` was unreachable on two screens.** Both re-send an already-validated draft — the
safe retry the original could not do — and neither had a control. The transfer's dialog state even
carried a `retryable: Boolean` that the ViewModel *set* and nothing read, under a comment claiming
"Retry is offered separately". It never was: `AppStatusDialog` has one action slot. That single
action now *is* the retry when the failure is retryable, falling back to dismiss-and-keep-the-form
otherwise. The reschedule form's `Failed` state gained the same flag.

**`LoanChargesAction.OnRefresh` and `LoanReschedulesAction.OnRefresh` were deleted** — left behind
when those screens deliberately dropped a hand-rolled `PullToRefreshBox` in favour of the stream
reporting its own `freshnessSignal`. Adopting `RefreshableScreenContent` (as the savings summary
did) was tried first and does not fit: that primitive exposes no `empty` slot and all three screens
pass one. Extending it means editing template-derived `core-base/ui`, which
RULE-TEMPLATE-MODULE-FIX-UPSTREAM-001 routes through a draft upstream PR — so the member deletion is
the right local fix, and the missing `empty` passthrough is recorded as the upstream half.

Four screens' back navigation now routes through its action rather than calling the hoisted callback
directly, removing a dead second path.

## S5c — loan charges and reschedules adopt `RefreshableScreenContent`

Once `core-base/ui`'s wrapper forwarded `ScreenContent`'s state slots
([openMF/kmp-project-template#310](https://github.com/openMF/kmp-project-template/pull/310), applied
locally pending the merge), both screens could use it — they pass a custom `empty`, which is exactly
what the old signature made impossible.

So the refresh actions deleted in S5d came back, and `OnRetry` **collapsed into them**: the wrapper
routes the pull gesture and the error-state retry to one callback, and `ScreenDataStream.retry()` is
declared `= refresh(forceFresh = true)` — literally the same call. Two members would have been two
names for one operation, with whichever the wrapper did not route sitting undispatched. The
dispatch sweep confirms zero.

What the primitive buys over the original's hand-rolled version:

```kotlin
// 6b66e8a43:LoanChargeViewModel
fun refreshLoanChargeList() {
    _isRefreshing.value = true
    loadLoanChargesList()        // ← `= viewModelScope.launch { … }`, returns immediately
    _isRefreshing.value = false
}
```

The flag went true → false in the same frame while the request was still in flight, so that spinner
could never be observed true. `isRefreshing` now comes from the stream's own `freshnessSignal` — the
layer that knows.

# Charges moved to `feature/charge` (2026-10-23)

The S4f loan charges list and the S4h add-charge form are **deleted** from this module —
`charges/`, `chargeform/`, their ViewModel and UI tests, `LoanChargesRoute`, `LoanChargeFormRoute`,
the two `LoanModule` bindings, the S4f/S4h strings, and `LoanRepository.loanChargesStream` /
`loanChargeTemplate` (+ `provideLoanChargeStore`). One charge module now serves clients, loans and
savings; see `feature/charge/MIGRATION.md`.

- Profile "Charges" row and Summary overflow "Charges" → `navigateToCharges(ChargeResource.LOAN, loanId)`.
- What carried over: the add form's option filter (disbursement / installment / specified-due-date,
  flat only), the due-date field shown only for a specified-due-date charge, submit-time amount
  validation, and the four labelled detail rows (now `ChargeDetailScreen`).
- What the loan gains: Edit (`PUT loans/{id}/charges/{chargeId}`) and Delete, which the original
  loan screens never offered.
- The `Charges` table is re-keyed `(resourceType, resourceId, id)`, so a loan charge no longer
  overwrites the client charge that shares its id (schema 15 → 16).

`feature/loan` now depends on `feature/charge` (one-directional).


## Audit pass (2026-10-25)

Loan profile gains **Notes** (owner `loans`) and the summary gains **More loan info** (`feature/datatable`, `m_loan`). Verified on the device: 15 loan data tables listed, and a charge added from the summary (SEGURO, loan 38; the server rounded it to the currency's 0 decimal places). **Bug fixed:** every GROUP loan failed to decode ("Something went wrong") because `LoanWithAssociationsDto.clientId` was required. A group loan carries `group` instead; `clientId` now defaults to 0 and the summary header shows the group's name (`GroupLoanDecodeTest`). Still open: a pending loan's summary blanks every figure, including the requested principal.

The profile status chip for an Approved / waiting-for-disbursal loan now shows the server's status ("Approved") instead of the original's "Unknown" (`anApprovedLoanNamesTheServersStatusRatherThanUnknown`). Device-verified on loan 1074, along with profile → Notes (`loans/1074/notes`).

Pending loan summary (2026-10-26): a loan not yet disbursed now shows its **Requested Amount** (`principal`), or its **Approved Amount** once approved, in the Total Loan card. Every other figure, and Staff when no officer is set, reads "—" instead of blank. Repayment figures are still hidden until disbursal, as in the original. The card amount shrinks to fit one line instead of wrapping mid-number. Device-verified on loan 38: MX$50,000. Tests: `aPendingLoanShowsTheRequestedAmount`, `anApprovedLoanShowsTheApprovedAmountNotTheRequestedOne`, plus the dash assertions.

## Create drafts (2026-10-26)

Every create form here keeps a draft through core-base's `FormDraftSlot` (template: `submit/draft/`). An offline submit, or one that fails on the network, is kept automatically and replayed on reconnect. A refusal is kept only on **Save as draft**, as FAILED with the server's reason, and is not replayed. When the form opens on a slot that holds a draft, a Resume / Discard banner is shown. Drafts are listed in Settings › Sync & Drafts with an **Open** action that returns to the form.

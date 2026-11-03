# `feature/savings` — migration notes

Ported from `6b66e8a43:feature/savings/` + `feature/recurringDeposit/`. Slice S5 of
`FEATURE_LAYER_MIGRATION_SPEC.md` §6.

The original ships **two** savings create screens — `savingsAccount/` (547+146 lines) and
`savingsAccountv2/` (374+736 + 4 pages) — the same duplicate-create-screen shape the loan vertical
had. `v2` is the live one.

## S5a — the account summary, and the search row that reaches it

Ported from `savingsAccountSummary/` (a 681-line screen + a 44-line ViewModel). First screen of the
vertical: there was **no savings surface in the fork at all** before this slice.

### The caller

`SearchNavigation` routed `CLIENT`, `GROUP` and `LOAN` and left `SAVING` with no destination, so
`SearchBody` did not draw its row as tappable. Wiring it is what makes the vertical reachable
(spec §6a). `CENTER` is now the only search type still absent — that list shrinks by one per slice,
which is the point of gating on the entity type rather than on whether a callback was supplied.

### `status.active` is nullable and the original used `!!` — four times

`!status?.active!!` appeared in the action button's `onClick` selector, in its label, in its
visibility predicate and in the deposit/withdraw visibility predicate. `active` is `Boolean?` and
Fineract omits it on an account that has never been activated — precisely the account an officer
opens this screen to approve or activate. All four sites are evaluated during **composition**, so
such an account did not render a broken button; it crashed the screen.

Two more in the same cluster:

- `getSavingsButtonText`'s `else` branch was `"Button not visible" as StringResource` — a String
  cast to a resource handle. It survived only because a *second* function re-derived the same `when`
  chain and happened to return false there.
- The closed case drew an **enabled** Button labelled "Savings account closed" with `onClick = { }`.

Replaced by one `SavingsAction` (`APPROVE | ACTIVATE | TRANSACT | NONE`) derived from the status with
`== true` throughout. The screen reads the result and never re-derives it — which is what let the
original's four copies disagree. `NONE` renders the status as **text**, per absent-never-inert.

### The transaction rows' dates crashed

`DateHelper.getDateAsString(transaction.date as List<Int>)` — an unchecked cast of a `List<Int?>`
then indexing `[2]`. The savings endpoint sends a `{year, month, day}` object
(`savingsTransactionDate`) and leaves the generic `date` array **empty**, which is also the entity's
default, so the cast passed by erasure and the index threw inside the composition. Every row on a
savings account crashed the list. Now projected in the ViewModel, blank when no source is usable —
a transaction with no date still has an amount the officer needs.

### Also fixed

- **The visibility booleans were `rememberSaveable`** — computed once from the first account emitted
  and then *saved*, so after approving an account the button kept its old label and old action until
  the screen was recreated. Derived state does not belong in `rememberSaveable`.
- **Two currency sources.** The four totals were formatted with `summary.currency`, the transaction
  rows with the account's — two fields that can disagree mid-refresh. One source now.
- **The action row was overlaid on the transactions**: a `Box(align = BottomStart)` over a
  `fillMaxSize` column covered the last rows on a long list. The list is weighted instead.
- **A nav-arg JSON blob.** The route passed a JSON-encoded `SavingsSummaryData` as one `arg` string
  and the ViewModel decoded it with an uncaught `Json.decodeFromString` *in its constructor* — a
  malformed argument crashed before the screen existed. Two typed route fields instead.
- `Color.Green`/`Color.Red` literals on transaction amounts → theme roles (the same near-invisible-on-
  dark defect as the repayment header), and a reversed transaction is now **stated** rather than
  conveyed by colour alone.
- Null client/product name fell back to the **label** strings, so a missing name rendered the words
  "Client Name". Distinct "unavailable" strings now.
- The detail card had no visible dismiss affordance (tap-outside only, not reachable by a screen
  reader) → an `AlertDialog` with a Close button.

### `RefreshableScreenContent` had zero callers

The action-dispatch sweep flagged a *new* `SavingsSummaryAction.OnRefresh` with no dispatcher — the
ViewModel wired it and no control sent it. `core-base/ui` already ships
`RefreshableScreenContent` (a `PullToRefreshBox` around `ScreenContent`, driven by the stream's own
`freshnessSignal.isRefreshing`) with **no callers anywhere in the fork** — another
ported-but-unreachable primitive. Adopting it gives the action its dispatcher, the screen a
pull-to-refresh, and the primitive its first caller. `OnRetry` and `OnRefresh` collapsed into one
member, since it routes both the error-state retry and the pull gesture to the same callback.

### Account identity

A savings account is addressed by **(type, id)**: Fineract's path segment names the deposit kind
(`savingsaccounts` / `fixeddepositaccounts` / `recurringdepositaccounts`), so the same numeric id is
a different account under a different type. The route carries both, which is what will let the
recurring-deposit slice reuse this screen rather than clone it. The default comes from
`SavingAccountDepositTypeEntity.ServerTypes`, which already owns the id → endpoint mapping — **not**
`Constants.SAVINGS_ACCOUNT_TYPE`, which despite its name is a SavedStateHandle *key*
(`"savingsAccountType"`) and would request `/savingsAccountType/{id}`.

A search hit carries no deposit type, so it takes the plain-savings default; the client's savings
list (a later sub-slice) will pass `depositType.endpoint` explicitly.

27 tests (13 ViewModel + 14 render).

## S5b — the three writes

Ported from `savingsAccountApproval/` (266+44), `savingsAccountActivate/` (270+44) and
`savingsAccountTransaction/` (532+146). These are the three destinations S5a left as `null`, so the
summary's action buttons are now live.

### Approval and activation are ONE screen

The two originals are near-duplicates: a date picker, a submit button, a three-case UiState. The
only differences are the title, the command, and a note field approval has and activation does not.
They had already drifted — approval wrapped its date in a `SavingsApproval` object, activation
hand-built a `HashMap<String, String>` with three string keys, and only one of them named the locale
constant rather than hardcoding `"en"`.

### Both sent epoch milliseconds as the date

```kotlin
// approval
SavingsApproval(approvedOnDate = approvalDate.toString(), note = reasonForApproval)
// activation
hashMap["activatedOnDate"] = approvalDate.toString()
hashMap["dateFormat"] = ApiDateFormatter.DATE_FORMAT   // ← right next to the millis
```

`approvalDate` is a `Long` of epoch millis (it feeds `initialSelectedDateMillis`), so the request
carried `"1761000000000"` while declaring `dateFormat = "dd MMMM yyyy"`.

**The 6th and 7th occurrence of this class, and its worst variant.** The earlier five were a
mismatched *format* — `dd-MM-yyyy` sent as `dd MMM yyyy` — which at least parses to a date. This is
not a date at all. And it was invisible on screen: the same value was *displayed* through
`DateHelper.getDateAsStringFromLong` and rendered correctly. Only the request was wrong.

Fixed structurally, continuing the pattern from `LoanApproval` / `LoanDisburseInput` /
`AccountTransferRequest` / `LoanApplicationInput`: `SavingsApprovalInput`,
`SavingsActivationInput` and `SavingsTransactionInput` take `locale` and `dateFormat` as
**required** parameters, and `SavingsApproval` itself was tightened the same way — its two format
fields used to default to `DateConstants`, which is exactly what lets a caller set a date without
deciding how it will be parsed.

### Two command signatures could not express a correct request

- `activateSavings` took a **`HashMap<String, String>`** — three string keys assembled at the call
  site with nothing checking they were present or spelled right. The map is now built inside the
  impl from the typed input, since the endpoint's body genuinely is a string map.
- `processTransaction` took a nullable **Room entity** (`SavingsAccountTransactionRequestEntity?`,
  every field nullable, with an `errorMessage` column) as its outbound body. That entity is the wire
  type the API declares, so the mapping stayed — it just moved to the impl boundary, where the
  storage-only columns are left unset.

### The amount validation allowed negatives and used Float

```kotlin
if (amount.toFloat() == 0f) { showSnackbar(...); return false }
```

Two defects in one predicate. The only rejected value is exactly zero, so `"-500"` parses to
`-500f`, is not `0f`, and **submits a negative deposit**. And Float carries ~7 significant digits, so
a ten-million deposit became `1.0E7` and lost its cents. Now `toDoubleOrNull()` plus `> 0`, with the
officer's own string on the wire so no round-trip re-renders the figure they typed — and the three
error cases marked *on the field* rather than as one of three snackbars.

### The payment type was two unrelated values

`paymentTypeId` defaulted to the Int `0` and went out as `"0"` — an id naming no row — while the
payment-type *label* was a free text field the officer typed. The port reads the real options from
the transaction-template store (which existed with no caller) and sends the chosen option's id,
omitting it when unchosen. A failed template read is **stated but never a blocker**: Fineract accepts
a transaction with no payment type, and refusing to let an officer record cash they have already
taken because a reference list did not load is the worse failure. The notice itself is the retry.

### Also fixed

- **The review sheet's confirm button said "Review Transaction"** — on the sheet that *was* the
  review, so the tap that moves money asked the officer to review what they were already reviewing.
- `Logger.d("builtTransactionRequestAsJson", Throwable(json))` serialized the whole request — amount
  included — and logged it wrapped in a `Throwable` purely to pass a string through a signature that
  wanted one. Dropped.
- Success called `onBackPressed()` from a `LaunchedEffect` in the composition body with a snackbar
  alongside, so the screen vanished and the officer might not catch the message. A dialog states the
  outcome; leaving is their tap. On the transaction it carries Fineract's transaction id as a
  reference for the person whose cash was taken.
- Every failure replaced the FORM. All three now keep it — on the transaction the amount is the one
  value an officer cannot re-derive.
- All three date pickers allowed **future dates only**, so *today* was unselectable — on screens
  where today is almost always the answer. Same inverted rule as six other screens.
- `TransactionKind` briefly existed twice, in `summary` without the command value and in
  `transaction` with it — the same two-same-named-constants trap as
  `DateHelper.SHORT_MONTH` vs `DateFormatPattern.SHORT_MONTH`. Consolidated to one.

72 tests across the module (36 ViewModel + 36 render). The action-dispatch sweep caught **two** of
my own dead actions during this slice (`OnRefresh` in S5a, `OnRetryTemplate` here) — both became
real affordances rather than deletions.

## S5c — the create wizard, and the group path that was dropped

Ported from `savingsAccountv2/` (a 736-line ViewModel, a 374-line screen, four pages totalling 795
lines). The original ships two savings create screens — `savingsAccount/` and `savingsAccountv2/` —
the same duplicate shape the loan vertical had; `v2` is the live one.

### The group path was accepted by the route and discarded by the ViewModel

```kotlin
// the route
data class SavingsAccountRoute(val groupId: Int, val clientId: Int, val isGroupAccount: Boolean)

// the ViewModel, in full, on the subject
SavingsAccountState(clientId = savedStateHandle.toRoute<SavingsAccountRoute>().clientId)
```

`groupId` and `isGroupAccount` appear **nowhere else in the file**. `SavingsPayload` has a `groupId`
member and the builder never set it, so a group savings application posted with a client id and no
group. The original's group-detail caller passed `(groupId, 0, true)` — so that path created an
application against **client 0**.

Same accepted-then-discarded class as the group loan's External Id and the loan wizard's charges,
but on the *identity of the account being created*. Fixed by collapsing to `ownerId` + `isGroup`:
one id and one discriminator, so there is no second id to forget. The discriminator also picks which
Fineract **template endpoint** the form reads — the original read neither per-owner template, though
both exist in `core/store` and are cached, so its option lists came from the generic one.

### The Charges page is REAL here — unlike the loan wizard's

`SavingsPayload` carries `charges` and the original's builder mapped them, so this page reaches the
server and is ported. The loan wizard's identical-looking page had to be **removed**, because
`LoansPayload` has no charges member. The difference is entirely in the payload, not the UI — which
is why "does this input reach the server?" has to be checked per form rather than inferred from the
screen. (The original did leave each charge's `locale`/`dateFormat` null while setting both on the
application; the mapper supplies them from the same helper.)

### Also fixed

- **Occurrence eight of the date/format class** — `submittedOnDate` built by
  `DateHelper.getDateAsStringFromLong` (`dd-MM-yyyy`) against a declared `dd MMMM yyyy`. Back to the
  ordinary variant after the approval/activation screens' raw epoch millis.
- **No per-step validation at all** — `moveToNextStep` incremented with no checks, and
  `moveToPreviousStep` had no floor. Each `SavingsStep` now owns its fields; Submit re-validates
  every step and lands on the earliest failure.
- Eight parallel `Int = -1` dropdown indices → one keyed map. Read with `getOrNull`, so unlike the
  loan wizard it did not *crash* — but a stale index still survived a product change and selected a
  different option from the new product's list.
- The title said "Savings Account" for a client and a group alike, on the screen whose group path
  silently posted a client id. It now names the owner, and the preview states it.
- `HorizontalPager` synced to `currentStep` → the step is state and the content is a `when`, so a
  swipe cannot bypass validation.
- The date picker allowed **future dates only**, so today was unselectable — the eighth screen with
  that inverted rule.

### Caller

Group detail's **"Add Savings Account"** row, which S4k left absent pending this slice. It appeared
with no edit to the menu beyond its own flag — which is what that design was for. Group Clients and
Notes remain absent.

35 tests (23 ViewModel + 15 render, minus overlap).

## S5e — recurring deposit, and the caller my own S5c slice was missing

Ported from `6b66e8a43:feature/recurringDeposit/newRecurringDepositAccount/` — a 1,118-line
ViewModel, a 220-line screen and six pages totalling ~2,100 lines. The largest single item in the
migration.

### It lives in `feature/savings`, not its own module

A deliberate deviation from the original's separate `feature/recurringDeposit`. A recurring deposit
IS a savings-family account — `SavingAccountDepositTypeEntity.ServerTypes.RECURRING` — and the
savings summary already serves all three deposit types by `(type, id)`, so this wizard shares that
module's strings, components and account vocabulary. A module whose only content is one wizard is
the duplicate-scaffolding shape `G-IMPL-DUP` guards against.

### The product was read through an unguarded index

```kotlin
productId = state.template.productOptions?.get(state.recurringDepositAccountDetail.loanProductSelected)?.id
//                                        └─ get, not getOrNull …
val loanProductSelected: Int = -1        // ← … against this default
```

`get(-1)` throws, so submitting before choosing a product crashed. What makes it easy to miss is
that the file **reads as careful**: five other option reads in the same builder are wrapped in an
explicit `!= -1` and two use `getOrNull`. One unguarded `get` sat among them — and the six guarded
ones were only half-safe anyway, since `!= -1` catches the sentinel but not an index left over from
a previous template.

### The pre-closure penalty was sent even when unchecked

Both penalty fields went out with whatever had been typed, regardless of the checkbox. A penalty the
officer had *unchecked* still reached the server. They are now sent only when the penalty applies,
and only rendered then.

### `isCalendarInherited` was a constant dressed as a setting

The payload set it from `depositFrequencySameAsGroupCenterMeeting` — whether the deposit cadence
follows the GROUP or CENTRE meeting schedule. This screen is reached only for a client, a client has
no meeting schedule, and no control ever set the flag. Omitted rather than sent.

### The date pairing was correct here

`DateHelper.SHORT_MONTH` alongside a date from `DateHelper.getDateAsStringFromLong` — both
`dd-MM-yyyy`. Worth stating, because eight other screens got the same pairing wrong and two sent
raw epoch millis: this one was right by both halves happening to come from the same object. The port
keeps it right by construction instead.

### The caller — and a gap in my own S5c slice

S5c landed the savings wizard serving both owners but wired only group detail's row
(`isGroup = true`). **The entire client path was unreachable** — a code path with no way in, which
is the defect §6a's caller rule exists to prevent, introduced in my own slice and found by asking
the §6a question *after* shipping rather than before.

The lesson: the caller rule applies **per path, not per screen**. One screen with two owners needs
two callers, and a screen-level check passes with one.

Fixed by porting `clientApplyNewApplications` — the original's real entry point for all five account
types — which gives the savings wizard its client caller and the recurring-deposit wizard its only
caller. See `feature/client/MIGRATION.md#s5e`.

23 ViewModel tests. 125 tests in the module.

## Still to come in S5

`savingsAccountTransactionReceipt` — a multiplatform PDF viewer with four platform actuals.

## Charges entry on the summary (2026-10-23)

The summary's overflow menu gains **Charges**, opening the ONE charge module (`feature/charge`) for
`savingsaccounts/{accountId}/charges` — list, detail, add, edit, delete. The original had no
levied-charge screen for savings at all. The application wizard's Charges PAGE is unrelated (charges
chosen before the account exists) and is unchanged. See `feature/charge/MIGRATION.md`.

## Audit pass (2026-10-25)

The summary gains **More savings account info** (`feature/datatable`, `m_savings_account`). **Bug fixed:** a group savings account showed "Client name unavailable". Fineract sends `groupName` and a null `clientName`, so the store fetcher folds the group name in and the cached row keeps it. Verified on the device: Charges and Add charge open, and correctly report no charge types (the gateway has none for savings).

## Create drafts (2026-10-26)

Every create form here keeps a draft through core-base's `FormDraftSlot` (template: `submit/draft/`). An offline submit, or one that fails on the network, is kept automatically and replayed on reconnect. A refusal is kept only on **Save as draft**, as FAILED with the server's reason, and is not replayed. When the form opens on a slot that holds a draft, a Resume / Discard banner is shown. Drafts are listed in Settings › Sync & Drafts with an **Open** action that returns to the form.

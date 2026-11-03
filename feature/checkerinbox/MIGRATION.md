# feature/checkerinbox — migration ledger

**Source:** `6b66e8a43:feature/checker-inbox-task` — 10 `.kt`, 1 `strings.xml`, 5 drawables
**Landed:** 2026-10-24
**Status:** complete.

## 1. Original inventory

| original | migrated to | note |
|---|---|---|
| `checkerInboxTasks/*` (hub Screen, ViewModel, UiState) | `ui/CheckerInboxScreen.kt` tabs | five tiles → two tabs (§2) |
| `checkerInbox/*` (Screen 685 lines, ViewModel, UiState) | `ui/CheckerInboxScreen.kt` + `CheckerInboxViewModel.kt` | |
| `checkerInboxDialog/*` (filter dialog + its ViewModel) | `ui/CheckerInboxScreen.kt` (`FilterSheet`) | template read by the one ViewModel |
| `navigation/CheckerInboxTaskNavigation.kt` | `navigation/CheckerInboxNavigation.kt` | one route |
| `di/CheckerInboxTaskModule.kt` | `di/CheckerInboxModule.kt` | |
| `drawable/*` (5 tile icons) | — | the tiles are gone; `AppIcons` for actions |

Data: `CheckerInboxRepository` gained `rescheduleRequestsStream` (existing `rescheduleLoanTasks` store) and
`searchTemplate` (existing `checkerSearchTemplate` store). `RescheduleLoansTask` gained `loanId` and
defaults (§2).

## 2. Defects fixed, not carried

- **Four of the hub's five tiles did nothing.** Client Approval, Loan Approval and Loan Disbursal showed
  a hardcoded "0" with an empty `onClick`; Reschedule Loan showed a real count and an empty `onClick`.
  The hub is two tabs now: Approvals (the inbox) and Reschedules, whose rows open the loan's existing
  reschedules screen. Client / loan approvals under maker-checker already arrive in the inbox.
- **The pending-reschedule list could never decode.** `RescheduleLoansTask` required an `actionName`
  that `rescheduleloans?command=pending` does not send, and a non-null reason the server omits when none
  was given. Defaults now; tested against the response shape (`RescheduleLoansTaskDecodeTest`).
- **The filter was wrong three ways.** Action and entity were ignored unless a date was set; a From date
  alone returned NOTHING (`else -> false`); and both ends of the range were exclusive. Each criterion now
  applies on its own and the range is inclusive by day; an inverted range cannot be applied. Tested
  (`eachFilterCriterionAppliesOnItsOwn`).
- **A failed decision replaced the whole inbox with an error**, and a bulk decision reported nothing
  per task. Now each failure is listed with Fineract's own reason, the decided tasks leave the list, the
  failed ones stay selected for a retry, and going offline stops the batch and says so. Tested
  (`aBulkDecisionReportsEachFailureAndKeepsItSelected`, `goingOfflineStopsTheBatchAndSaysSo`).
- **A decision reloaded the whole inbox** to show its effect; the command clears the row through the
  store, so the list updates with no reload — and stays right offline.

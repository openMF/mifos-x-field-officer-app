# `feature/loan/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 93 tracked files — 66× `.kt`, 23× `.png`, 2× `.md`, 1× `.kts`, 1× `.xml`

**66 Kotlin files** — 12 screen(s), 12 ViewModel(s).

Screens: `GroupLoanApplicationScreen`, `LoanApprovalScreen`, `LoanDisbursementScreen`, `LoanProfileScreen`, `LoanRepaymentScreen`, `LoanRescheduleFormScreen`, `LoanReschedulesScreen`, `LoanScheduleScreen`, `LoanSummaryScreen`, `LoanTransactionsScreen`, `LoanTransferScreen`, `NewLoanApplicationScreen`

ViewModels: `GroupLoanApplicationViewModel`, `LoanApprovalViewModel`, `LoanDisbursementViewModel`, `LoanProfileViewModel`, `LoanRepaymentViewModel`, `LoanRescheduleFormViewModel`, `LoanReschedulesViewModel`, `LoanScheduleViewModel`, `LoanSummaryViewModel`, `LoanTransactionsViewModel`, `LoanTransferViewModel`, `NewLoanApplicationViewModel`

**Consumes:** `core/store/loan`, `core/store/recent`, `core/store/sync`, `core/store/testing`

### Docs in the tree

| Path | |
|---|---|
| `feature/loan/MIGRATION.md` | feature/loan — migration ledger |
| `feature/loan/README.md` | :feature:loan |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/loan sha=838cf5b0405f19e36039e9c469f4c01420f77253 -->
## API reference

_Generated from `feature/loan` at tree `838cf5b0405f` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/loan/src/commonMain/kotlin/kpt/feature/loan/di/LoanModule.kt`

```kotlin
val LoanModule = module
```
Collected into `GeneratedFeatureKoinBindings` by `:cmp-navigation:generateFeatureKoinBindings`, which scans each feature module's `di` package — no registry edit (spec §4).

### `feature/loan/src/commonMain/kotlin/kpt/feature/loan/navigation/LoanNavigation.kt`

```kotlin
data class LoanProfileRoute(val loanId: Int)
```
A loan account's profile. `loanId` is required — the original's route defaulted it, so a missing argument produced a screen that asked Fineract for loan `-1`.

```kotlin
data class LoanScheduleRoute(val loanId: Int)
```
The repayment schedule and the transaction history. Both carry only `loanId`: they are projections of the SAME `loans/{id}?associations=all` read the profile already cached, so there is nothing else to pass.

```kotlin
data class LoanTransactionsRoute(val loanId: Int)
```
_No KDoc at source._

```kotlin
data class LoanApprovalRoute(val loanId: Int)
```
Approve a pending loan. `loanId` only. The original route carried the whole `LoanWithAssociations` JSON-encoded through the back stack — see `kpt.feature.loan.approval.LoanApprovalViewModel`'s header for the three things that broke.

```kotlin
data class LoanRepaymentRoute(val loanId: Int)
```
Record a repayment.

```kotlin
data class LoanSummaryRoute(val loanId: Int)
```
A loan account's SUMMARY — the screen a loan hit in SEARCH and a loan on GROUP detail open in the original, as distinct from `LoanProfileRoute`, which the client's loan LIST opens.

```kotlin
data class LoanDisbursementRoute(val loanId: Int)
```
Disburse an approved loan. Reached only from the SUMMARY — `waitingForDisbursal` is one of the two states the loan profile collapses into "Unknown", so the profile never offers this action.

```kotlin
data class LoanTransferRoute(val loanId: Int)
```
Transfer funds out of a loan. `loanId` only, where the original's route carried FIVE fields — client id, office id, account number, currency and account id — each snapshotted by the caller from the loan it had just rendered.

```kotlin
data class LoanReschedulesRoute(val loanId: Int)
```
Every reschedule proposed on a loan, with approve and reject.

<details><summary>Used in the template — <code>feature/checkerinbox/src/commonMain/kotlin/kpt/feature/checkerinbox/navigation/CheckerInboxRoute.kt:33</code></summary>

```kotlin
            onNavigateBack = { navController.popBackStack() },
            // The loan's own reschedules screen approves and rejects — not duplicated here.
            onOpenLoanReschedules = { loanId -> navController.navigate(LoanReschedulesRoute(loanId)) },
        )
    }
}
```

</details>

```kotlin
data class LoanRescheduleFormRoute(val loanId: Int)
```
Propose a reschedule. Reached from the reschedules list's "+".

```kotlin
data class NewLoanApplicationRoute(val clientId: Int)
```
Apply for a new INDIVIDUAL loan. Reached from the client's loan-accounts list "add account" affordance, whose whole chain was already ported and `null`-gated — the original's `clientLoanAccountsDestination(createAccount = …)` edge.

```kotlin
fun NavController.navigateToNewLoanApplication(clientId: Int, navOptions: NavOptions? = null) =
```
_No KDoc at source._

```kotlin
data class GroupLoanApplicationRoute(val groupId: Int)
```
Apply for a GROUP loan. Reached from group detail's overflow menu — the original's `addGroupLoanAccount`. Keyed on `groupId`, not `loanId`: this is the one route in this file that CREATES a loan rather than opening one.

```kotlin
fun NavController.navigateToGroupLoanApplication(groupId: Int, navOptions: NavOptions? = null) =
```
_No KDoc at source._

```kotlin
fun NavController.navigateToLoanProfile(loanId: Int, navOptions: NavOptions? = null) =
```
_No KDoc at source._

```kotlin
fun NavController.navigateToLoanSummary(loanId: Int, navOptions: NavOptions? = null) =
```
_No KDoc at source._

```kotlin
fun NavController.navigateToLoanRepayment(loanId: Int, navOptions: NavOptions? = null) =
```
Record a repayment, reached directly rather than through a loan detail screen.

```kotlin
fun NavGraphBuilder.loanDestinations(navController: NavController)
```
`@FeatureDestination`, so the generator registers it ONCE — the same reasoning as the other shared drill-downs. The client's loan list and group detail both open it.

### `feature/loan/src/commonMain/kotlin/kpt/feature/loan/profile/components/LoanProfileActionItem.kt`

```kotlin
sealed class LoanProfileActionItem(
```
Ported verbatim from `6b66e8a43:feature/loan/loanAccountProfile/components/LoanAccountProfileActionItem.kt` — all thirteen items, same titles, subtitles and icons.

<details><summary>Used in the template — <code>feature/loan/src/commonMain/kotlin/kpt/feature/loan/navigation/LoanNavigation.kt:238</code></summary>

```kotlin
}

private fun NavController.navigateToProfileDetail(item: LoanProfileActionItem, loanId: Int) {
    when (item) {
        LoanProfileActionItem.RepaymentSchedule ->
            navigate(LoanScheduleRoute(loanId))
        LoanProfileActionItem.Transactions ->
```

</details>

---

_13 type(s), 7 function(s)/property(ies); 15 carry KDoc at source; 0 authored example(s); 2 live call site(s)._
<!-- api-docs:end -->

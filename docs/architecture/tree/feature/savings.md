# `feature/savings/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 43 tracked files — 29× `.kt`, 10× `.png`, 2× `.md`, 1× `.kts`, 1× `.xml`

**29 Kotlin files** — 5 screen(s), 5 ViewModel(s).

Screens: `RecurringDepositScreen`, `SavingsApplicationScreen`, `SavingsStatusChangeScreen`, `SavingsSummaryScreen`, `SavingsTransactionScreen`

ViewModels: `RecurringDepositViewModel`, `SavingsApplicationViewModel`, `SavingsStatusChangeViewModel`, `SavingsSummaryViewModel`, `SavingsTransactionViewModel`

**Consumes:** `core/store/recurringdeposit`, `core/store/savings`, `core/store/testing`

### Docs in the tree

| Path | |
|---|---|
| `feature/savings/MIGRATION.md` | `feature/savings` — migration notes |
| `feature/savings/README.md` | :feature:savings |

<!-- tree-scaffold:end -->
## Significance

_Authored below this marker; regeneration never touches it._

<!-- api-docs:begin module=feature/savings sha=2408f7fe68eb981d37224567725e5d15b275390c -->
## API reference

_Generated from `feature/savings` at tree `2408f7fe68eb` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/savings/src/commonMain/kotlin/kpt/feature/savings/di/SavingsModule.kt`

```kotlin
val SavingsModule = module
```
Collected into `GeneratedFeatureKoinBindings` by `:cmp-navigation:generateFeatureKoinBindings`, which scans each feature module's `di` package — no registry edit (spec §4).

### `feature/savings/src/commonMain/kotlin/kpt/feature/savings/navigation/SavingsNavigation.kt`

```kotlin
data class SavingsSummaryRoute(val accountType: String, val accountId: Int)
```
The savings vertical's destinations — ported from `6b66e8a43:feature/savings/navigation/SavingsNavigation.kt`.

```kotlin
data class SavingsStatusChangeRoute(val accountId: Int, val change: String)
```
Approve or activate — ONE route, because it is one screen (see `SavingsStatusChangeViewModel`).

```kotlin
data class SavingsApplicationRoute(val ownerId: Int, val isGroup: Boolean)
```
A new savings application, for a CLIENT or a GROUP. ONE owner id and ONE discriminator.

```kotlin
data class RecurringDepositApplicationRoute(val clientId: Int)
```
A new RECURRING-DEPOSIT application, for a client. `clientId` only: the recurring-deposit template endpoint is keyed on the client alone, and the original's own route carried only a client id too.

```kotlin
data class SavingsTransactionRoute(
```
Deposit or withdraw. Carries the account TYPE, since Fineract's path segment names it.

```kotlin
fun NavController.navigateToSavingsSummary(
```
Open one savings account's summary. `accountType` defaults to the plain-savings endpoint via `SavingAccountDepositType.ServerTypes`, which already owns the id -> path-segment mapping for all three deposit kinds.

```kotlin
fun NavController.navigateToSavingsApplication(
```
Opens the savings application for a client or a group.

```kotlin
fun NavController.navigateToRecurringDepositApplication(
```
Opens the recurring-deposit application for a client.

```kotlin
fun NavController.navigateToSavingsApproval(accountId: Int, navOptions: NavOptions? = null) =
```
Opens the approval of savings account `accountId`.

```kotlin
fun NavController.navigateToSavingsActivation(accountId: Int, navOptions: NavOptions? = null) =
```
Opens the activation of savings account `accountId`.

```kotlin
data class SavingsLinks(
```
Where the savings screens lead outside this feature — callbacks `ForkFeatureRegistry` fills with the owning feature's `navigateTo…` of the same shape, since a feature never depends on another.

```kotlin
fun NavGraphBuilder.savingsDestination(navController: NavController, links: SavingsLinks)
```
Registers every savings screen in the nav graph. Not `@FeatureDestination`: the summary opens charges, documents and datatables, so `ForkFeatureRegistry` registers it with `SavingsLinks`.

---

_6 type(s), 7 function(s)/property(ies); 13 carry KDoc at source; 0 authored example(s); 0 live call site(s)._
<!-- api-docs:end -->

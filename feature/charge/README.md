# feature/charge

The ONE charge module: list, detail, add, edit and delete the charges levied on a **client**, a
**loan** or a **savings account**. Callers pass the owner; nothing here knows who opened it.

```kotlin
navController.navigateToCharges(ChargeResource.LOAN, loanId)      // the list
navController.navigateToAddCharge(ChargeResource.CLIENT, clientId) // straight to the add form
```

| screen | route | reads / writes |
|---|---|---|
| `list/ChargeListScreen` | `ChargeListRoute(resourceType, resourceId)` | `ChargeRepository.chargesStream` · `ChargeCommandRepository.deleteCharge` |
| `detail/ChargeDetailScreen` | `ChargeDetailRoute(resourceType, resourceId, chargeId)` | the same stream, filtered to one charge |
| `form/ChargeFormScreen` | `ChargeFormRoute(resourceType, resourceId, chargeId = -1)` | `chargeTemplate` · `charge` (edit prefill) · `createCharge` / `updateCharge` |

Data path: `ChargeApi` → `Charges` table (keyed `resourceType, resourceId, id`) → `core/store`
`ChargeStore.kt` → `core/data` `ChargeRepository` / `ChargeCommandRepository` → ViewModels. Writes go
through `MutationGateway` (`OnlineRequired`) and record their effect through the stores, so the list
updates with no refetch.

Per-owner capabilities (Edit is loan + savings only — Fineract has no client-charge PUT) are on
`core/model` `ChargeResource`. History and what was consolidated: [`MIGRATION.md`](MIGRATION.md).

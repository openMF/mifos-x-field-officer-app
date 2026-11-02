# `feature/`

> **Kind:** Kotlin Multiplatform module group  
> **Measured:** 658 tracked files — 342× `.xml`, 255× `.kt`, 21× `.png`, 17× `.kts`, 16× `.md`

## Shape

| Path | Files |
|---|---:|
| `feature/settings/` | 76 |
| `feature/calculators/` | 49 |
| `feature/loans/` | 48 |
| `feature/macro/` | 44 |
| `feature/bills/` | 42 |
| `feature/rates/` | 38 |
| `feature/currency-rates/` | 37 |
| `feature/crypto/` | 36 |
| `feature/home/` | 36 |
| `feature/alerts/` | 35 |
| `feature/profile/` | 35 |
| `feature/amortization/` | 31 |
| `feature/emi-calculator/` | 31 |
| `feature/showcase/` | 31 |
| `feature/watchlist/` | 31 |
| `feature/cloudtodo/` | 30 |
| `feature/add-to-watchlist/` | 27 |
| (files at the root) | 1 |

## Modules

| | |
|---|---|
| `feature:add-to-watchlist` | 5 Kotlin files — [guide](feature/add-to-watchlist.md) |
| `feature:alerts` | 13 Kotlin files — [guide](feature/alerts.md) |
| `feature:amortization` | 9 Kotlin files — [guide](feature/amortization.md) |
| `feature:bills` | 20 Kotlin files — [guide](feature/bills.md) |
| `feature:calculators` | 27 Kotlin files — [guide](feature/calculators.md) |
| `feature:cloudtodo` | 9 Kotlin files — [guide](feature/cloudtodo.md) |
| `feature:crypto` | 14 Kotlin files — [guide](feature/crypto.md) |
| `feature:currency-rates` | 15 Kotlin files — [guide](feature/currency-rates.md) |
| `feature:emi-calculator` | 9 Kotlin files — [guide](feature/emi-calculator.md) |
| `feature:home` | 11 Kotlin files — [guide](feature/home.md) |
| `feature:loans` | 26 Kotlin files — [guide](feature/loans.md) |
| `feature:macro` | 22 Kotlin files — [guide](feature/macro.md) |
| `feature:profile` | 10 Kotlin files — [guide](feature/profile.md) |
| `feature:rates` | 16 Kotlin files — [guide](feature/rates.md) |
| `feature:settings` | 31 Kotlin files — [guide](feature/settings.md) |
| `feature:showcase` | 9 Kotlin files — [guide](feature/showcase.md) |
| `feature:watchlist` | 9 Kotlin files — [guide](feature/watchlist.md) |

## Subsections

| Unit | Files | Page |
|---|---:|---|
| `feature/add-to-watchlist/` | 27 | [add-to-watchlist](feature/add-to-watchlist.md) |
| `feature/alerts/` | 35 | [alerts](feature/alerts.md) |
| `feature/amortization/` | 31 | [amortization](feature/amortization.md) |
| `feature/bills/` | 42 | [bills](feature/bills.md) |
| `feature/calculators/` | 49 | [calculators](feature/calculators.md) |
| `feature/cloudtodo/` | 30 | [cloudtodo](feature/cloudtodo.md) |
| `feature/crypto/` | 36 | [crypto](feature/crypto.md) |
| `feature/currency-rates/` | 37 | [currency-rates](feature/currency-rates.md) |
| `feature/emi-calculator/` | 31 | [emi-calculator](feature/emi-calculator.md) |
| `feature/home/` | 36 | [home](feature/home.md) |
| `feature/loans/` | 48 | [loans](feature/loans.md) |
| `feature/macro/` | 44 | [macro](feature/macro.md) |
| `feature/profile/` | 35 | [profile](feature/profile.md) |
| `feature/rates/` | 38 | [rates](feature/rates.md) |
| `feature/settings/` | 76 | [settings](feature/settings.md) |
| `feature/showcase/` | 31 | [showcase](feature/showcase.md) |
| `feature/watchlist/` | 31 | [watchlist](feature/watchlist.md) |

## Docs in the tree

_Authored beside the code, where the module's own consumers read them._

| Path | |
|---|---|
| `feature/add-to-watchlist/README.md` | :feature:add-to-watchlist |
| `feature/alerts/README.md` | :feature:alerts |
| `feature/amortization/README.md` | :feature:amortization |
| `feature/bills/README.md` | :feature:bills |
| `feature/calculators/README.md` | :feature:calculators |
| `feature/crypto/README.md` | :feature:crypto |
| `feature/currency-rates/README.md` | :feature:currency-rates |
| `feature/emi-calculator/README.md` | :feature:emi-calculator |
| `feature/home/README.md` | :feature:home |
| `feature/loans/README.md` | :feature:loans |
| `feature/macro/README.md` | :feature:macro |
| `feature/profile/README.md` | :feature:profile |
| `feature/rates/README.md` | :feature:rates |
| `feature/settings/README.md` | :feature:settings |
| `feature/showcase/README.md` | :feature:showcase |
| `feature/watchlist/README.md` | :feature:watchlist |

## Deeper reading

_This page is the map; these are the depth. Both are authored, and linking them is what
stops two descriptions of one thing from drifting apart._

- [Demo showcase — feature ↔ archetype pairing](../../architecture/cross-cutting/demo-showcase.md)

<!-- tree-scaffold:end -->
## Significance

The app's actual screens. Every module here is **one user-facing capability** — its ViewModel, its
Compose screens, its navigation graph and its Koin module — and nothing else depends on it. That
one-way dependency is what makes a feature removable: `scripts/remove-demo.sh` strips the demo
features by deleting these directories and the aggregate regenerates without them.

**Why the modules look repetitive.** Each shipped feature is also the canonical demo of one framework
pattern — `loans` is the paging list plus draft-backed edit form, `cloudtodo` is the MUTABLE Store5
archetype with offline write-back, `macro` is the multi-source combine with independent per-card
state. `core/store/STORE_ARCHETYPES.yaml` names the pairing, and product-health fails the build when
an archetype loses its last showcase. Deleting a feature is therefore a deliberate act, not tidying.

**What a fork touches.** Add a directory, declare the feature's `store_archetype`, run codegen. The
registrations are DERIVED: `build-logic`'s `FeatureAggregateConventionPlugin` scans each module's
`di` and `navigation` packages and generates the Koin, destination and tab aggregates, so a new
feature needs no edit to `cmp-navigation`. Before that generator existed, adding a screen took an
import and a list entry in a registry, and forgetting either compiled cleanly while the feature
failed to resolve at runtime.

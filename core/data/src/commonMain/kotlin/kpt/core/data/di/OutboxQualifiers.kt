/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.di

/**
 * Named qualifiers for the app's `SubmitOutbox<*>` bindings.
 *
 * Why qualifiers?
 *
 * Koin indexes `single<T>` definitions by `T::class` (the raw type), not by the full
 * `KType`. So `single<SubmitOutbox<Loan>>` and `single<SubmitOutbox<PriceAlert>>` both
 * collide under `SubmitOutbox::class` — the LAST registered wins. Every consumer that
 * calls `get<SubmitOutbox<Foo>>()` actually receives whichever outbox was registered last,
 * regardless of the generic parameter. At first `.saveByUniqueKey(payload)` call, the
 * wrong serializer fires:
 *
 *   `ClassCastException: ...LoanCalcScenario cannot be cast to ...PriceAlert`
 *
 * Fix: every outbox `single<>` registration declares `qualifier = OutboxQualifiers.X`
 * and every consumer uses `get(qualifier = OutboxQualifiers.X)`. Then the lookup keys
 * are unambiguous: `(SubmitOutbox::class, "outbox.loan")` ≠
 * `(SubmitOutbox::class, "outbox.priceAlert")`.
 *
 * Add a new payload type? Add a new constant here, register its `single<>` with the
 * qualifier, and have the consuming feature module fetch with the same qualifier:
 * ```
 * val MyThing = named("outbox.myThing")
 * ```
 *
 * Intentionally EMPTY on the template (E1/C4). The demo showcase's qualifiers live in the
 * generated [kpt.core.data.config.AppOutboxQualifiers], derived from `@DataProvider(qualifier = …)`,
 * deletes them with the rest of the showcase and a template sync can blind-copy THIS file
 * without re-introducing them.
 */
/**
 * Koin qualifiers for this fork's `SubmitOutbox` instances — the FORK-OWNED seam.
 *
 * Empty today. The kmp-project-template filled it with `AppOutboxQualifiers.{Loan, BillReminder,
 * LoanCalcScenario, PriceAlert}` for its demo entities; `remove-demo.sh` stripped those at fork
 * standup, and `OutboxBindingVerifyTest` was deleted in S0b because it asserted their registration
 * and had not compiled since (found on the first-ever run of `:core:data:desktopTest`).
 *
 * The fork's 72 writes currently route through `MutationGateway` without per-entity outbox
 * qualifiers. When S8 migrates `feature/offline` and declares real ones, the binding-verification
 * test returns here asserting THOSE — not the template's.
 */
object OutboxQualifiers

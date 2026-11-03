/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.savings

/**
 * `savingsAccountType` is a path segment on Fineract ("savingsaccounts" / "recurringdepositaccounts"
 * / ...), so the same numeric id means different accounts under different types — it belongs in the
 * key, not as a caller-side constant.
 */
data class SavingsAccountKey(
    /** The savings account kind (`savingsaccounts`, `recurringdepositaccounts`, …). */
    val savingsAccountType: String,
    /** The savings account's server id. */
    val savingsAccountId: Int,
    /** The associations to include, comma-separated. */
    val associations: String? = "transactions",
)

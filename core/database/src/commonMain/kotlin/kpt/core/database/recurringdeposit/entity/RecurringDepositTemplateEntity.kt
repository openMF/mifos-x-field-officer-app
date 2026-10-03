/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.recurringdeposit.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kpt.core.base.database.annotation.DbEntity
import kpt.core.model.recurringdeposit.RecurringDepositAccountTemplate

/**
 * The recurring-deposit account-opening TEMPLATE, cached per client.
 *
 * `RecurringDepositAccountTemplate` is a form-options payload — ~20 nested objects
 * (`accountChart`, `chargeOptions`, `fieldOfficerOptions`, the interest/period enums…). It is
 * fetched whole and rendered whole; nothing ever queries an individual column. Decomposing it
 * into relational tables would buy no query capability and cost a large migration surface, so it
 * is stored as one serialized snapshot keyed by client — the same JSON-through-a-converter
 * approach this database already uses for nested lists.
 *
 * Caching it is what lets a field officer OPEN the account-creation form with the right product
 * and officer options while offline; the account creation itself is a write and stays online (D1).
 */
@DbEntity
@Entity(tableName = "recurring_deposit_templates")
data class RecurringDepositTemplateEntity(
    @PrimaryKey
    val clientId: Int,
    /** Serialized [kpt.core.model.recurringdeposit.RecurringDepositAccountTemplate]. */
    val payload: String,
    val fetchedAtMs: Long,
)

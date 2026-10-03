/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.loan.entity

import kotlinx.serialization.Serializable

/**
 * A COLUMN, not a table.
 *
 * This is embedded in its parent entity and persisted by a `@ColumnTypeConverter` as a JSON
 * string. It carried `@DbEntity` as well, so Room also generated a standalone table that no
 * DAO ever read or wrote — dead schema that counted against the generated
 * `onValidateSchema`, which at 85 tables exceeded the JVM 64KB method limit and broke every
 * JVM target.
 */
@Serializable
data class LoanStatusEntity(
    val id: Int? = null,

    val code: String? = null,

    val value: String? = null,

    val pendingApproval: Boolean? = null,

    val waitingForDisbursal: Boolean? = null,

    val active: Boolean? = null,

    val closedObligationsMet: Boolean? = null,

    val closedWrittenOff: Boolean? = null,

    val closedRescheduled: Boolean? = null,

    val closed: Boolean? = null,

    val overpaid: Boolean? = null,
)

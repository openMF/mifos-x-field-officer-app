/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.savings.entity

import androidx.room3.Ignore
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
data class SavingAccountDepositTypeEntity(
    /** The server id of this record. */
    val id: Int? = null,

    /**
     * The Fineract code that identifies this value (an enum code such as `loanStatusType.active`, or an ISO
     * currency code).
     */
    val code: String? = null,

    /** The human-readable label Fineract sends for this value. */
    val value: String? = null,
) {

    /** True for a recurring-deposit type. */
    @Ignore
    val isRecurring: Boolean
        get() = ServerTypes.RECURRING.id == id

    /** The API path segment for this deposit type (`savingsaccounts`, `fixeddepositaccounts`, …). */
    @Ignore
    val endpoint: String
        get() = ServerTypes.fromId(id).endpoint

    /** The deposit type as the server names it. */
    @Ignore
    val serverType: ServerTypes
        get() = ServerTypes.fromId(id)

    /**
     * Server types.
     *
     * @property code the Fineract code that identifies this value (an enum code such as `loanStatusType.active`, or an
     *   ISO currency code)
     * @property endpoint the API path segment for this deposit type (`savingsaccounts`, `fixeddepositaccounts`, …)
     * @property id the server id of this record
     */
    enum class ServerTypes(val id: Int, val code: String, val endpoint: String) {
        /** A savings account. */
        SAVINGS(100, "depositAccountType.savingsDeposit", "savingsaccounts"),

        /** A fixed-deposit account. */
        FIXED(200, "depositAccountType.fixedDeposit", "fixeddepositaccounts"),

        /** A recurring-deposit account. */
        RECURRING(300, "depositAccountType.recurringDeposit", "recurringdepositaccounts"),
        ;

        /** Constants and factories of `ServerTypes`. */
        companion object {
            /** The type with this server id; `SAVINGS` when no type matches. */
            fun fromId(id: Int?): ServerTypes {
                for (type in entries) {
                    if (type.id == id) {
                        return type
                    }
                }
                return SAVINGS
            }
        }
    }
}

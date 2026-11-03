/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.charge.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Ignore
import kotlinx.serialization.Serializable
import kpt.core.base.database.annotation.DbEntity
import kpt.core.database.client.entity.ClientDateEntity

/**
 * Created by nellyk on 2/15/2016.
 *
 * ## Keyed by OWNER + id, not by id
 *
 * Client, loan and savings charges are three different Fineract tables (`m_client_charge`,
 * `m_loan_charge`, `m_savings_account_charge`), so their ids coincide freely. Keyed on `id` alone a
 * loan charge 5 REPLACED client charge 5 on insert — one owner's list silently lost a row whenever
 * the other was fetched (found 2026-10-23). [resourceType] + [resourceId] + [id] is the identity
 * the API itself uses (`/{resourceType}/{resourceId}/charges/{id}`), and the owner is STAMPED by the
 * store on write because the list endpoints do not echo it consistently (loans send no `loanId`).
 *
 * `clientId` / `loanId` stay: they are what Fineract sends, and other readers deserialise them.
 */
@Serializable
@DbEntity
@Entity(
    tableName = "Charges",
    indices = [],
    inheritSuperIndices = false,
    primaryKeys = ["resourceType", "resourceId", "id"],
    ignoredColumns = [],
    foreignKeys = [],
)
data class ChargesEntity(
    /** The Fineract path segment of the owner — `clients`, `loans` or `savingsaccounts`. */
    @ColumnInfo(defaultValue = "''")
    val resourceType: String = "",

    /** The id of the resource the charge belongs to. */
    @ColumnInfo(defaultValue = "0")
    val resourceId: Int = 0,

    /** This levied instance's id, unique only within its owner. */
    val id: Int = 0,

    /** The server id of the client. */
    val clientId: Int? = null,

    /** The server id of the loan. */
    val loanId: Int? = null,

    /** The charge DEFINITION's id — which charge this is, repeated across every owner levying it. */
    val chargeId: Int? = null,

    /** The display name. */
    val name: String? = null,

    /** When the charge is applied (disbursement, specified due date, …). */
    val chargeTimeType: ChargeTimeTypeEntity? = null,

    /** The date the charge is due. */
    val chargeDueDate: ClientDateEntity? = null,

    /** The date the amount is due. */
    val dueDate: List<Int>? = null,

    /** How the charge amount is calculated (flat, percent of amount, …). */
    val chargeCalculationType: ChargeCalculationTypeEntity? = null,

    /** The currency the amounts are in. */
    val currency: ClientChargeCurrencyEntity? = null,

    /** The amount, in the record's currency. */
    val amount: Double? = null,

    /** The amount already paid. */
    val amountPaid: Double? = null,

    /** The amount waived. */
    val amountWaived: Double? = null,

    /** The amount written off. */
    val amountWrittenOff: Double? = null,

    /** The amount still outstanding. */
    val amountOutstanding: Double? = null,

    /** True when this charge is a penalty rather than a fee. */
    val penalty: Boolean? = null,

    /** True when the record is active. */
    val active: Boolean? = null,

    /** True when it has been paid. */
    val paid: Boolean? = null,

    /** True when it has been waived. */
    val waived: Boolean? = null,
) {

    /**
     * Derived, not stored — `@Ignore` keeps Room from treating it as a column.
     *
     * Without it Room emits `_item.formattedDueDate = ...` into the generated DAO impl, which does
     * not compile against a get-only property. Latent since the original app: it only surfaces once
     * the generated desktop impls are actually compiled.
     */
    @Ignore
    val formattedDueDate: String
        get() = if (dueDate?.size == 3) {
            "${dueDate[0]}-${dueDate[1]}-${dueDate[2]}"
        } else {
            "No Due Date"
        }
}

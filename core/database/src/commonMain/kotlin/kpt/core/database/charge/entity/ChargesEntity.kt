/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.charge.entity

import androidx.room3.Entity
import androidx.room3.Ignore
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable
import kpt.core.base.database.annotation.DbEntity
import kpt.core.database.client.entity.ClientDateEntity

/**
 * Created by nellyk on 2/15/2016.
 */
@Serializable
@DbEntity
@Entity(
    tableName = "Charges",
    indices = [],
    inheritSuperIndices = false,
    primaryKeys = [],
    ignoredColumns = [],
    foreignKeys = [],
)
data class ChargesEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val clientId: Int? = null,

    val loanId: Int? = null,

    val chargeId: Int? = null,

    val name: String? = null,

    val chargeTimeType: ChargeTimeTypeEntity? = null,

    val chargeDueDate: ClientDateEntity? = null,

    val dueDate: List<Int>? = null,

    val chargeCalculationType: ChargeCalculationTypeEntity? = null,

    val currency: ClientChargeCurrencyEntity? = null,

    val amount: Double? = null,

    val amountPaid: Double? = null,

    val amountWaived: Double? = null,

    val amountWrittenOff: Double? = null,

    val amountOutstanding: Double? = null,

    val penalty: Boolean? = null,

    val active: Boolean? = null,

    val paid: Boolean? = null,

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

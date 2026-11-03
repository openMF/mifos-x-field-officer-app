/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.payment.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable
import kpt.core.base.database.annotation.DbEntity

/** Room row for a payment type option. */
@DbEntity
@Entity(
    tableName = "PaymentTypeOption",
    indices = [],
    inheritSuperIndices = false,
    primaryKeys = [],
    foreignKeys = [],
    ignoredColumns = [],
)
@Serializable
data class PaymentTypeOptionEntity(
    /** The server id of this record. */
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    /** The display name. */
    val name: String = "",
    /** A free-text description. */
    val description: String? = null,
    /** True when the payment type is cash. */
    val isCashPayment: Boolean? = null,
    /** The sort position of this option. */
    val position: Int? = null,
)
//    : Comparable<PaymentTypeOptionEntity> {
//
//    override fun compareTo(other: PaymentTypeOptionEntity): Int {
//        return position?.compareTo(other.position ?: 0) ?: 0
//    }
//
//    override fun toString(): String {
//        return "PaymentTypeOption{" +
//                "id=$id, " +
//                "name='$name', " +
//                "description='$description', " +
//                "isCashPayment=$isCashPayment, " +
//                "position=$position" +
//                '}'
//    }
// }

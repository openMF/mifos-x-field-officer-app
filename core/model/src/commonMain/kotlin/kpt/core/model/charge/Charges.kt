/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.charge

import kotlinx.serialization.Serializable
import kpt.core.model.client.ClientDate

/**
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `ChargesEntity`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class Charges(
    /** The kind of resource the charge belongs to. */
    val resourceType: String = "",
    /** The server id of the resource the request created or changed. */
    val resourceId: Int = 0,
    /** The server id of this record. */
    val id: Int = 0,
    /** The server id of the client. */
    val clientId: Int? = null,
    /** The server id of the loan. */
    val loanId: Int? = null,
    /** The server id of the charge. */
    val chargeId: Int? = null,
    /** The display name. */
    val name: String? = null,
    /** When the charge is applied (disbursement, specified due date, …). */
    val chargeTimeType: ChargeTimeType? = null,
    /** The date the charge is due. */
    val chargeDueDate: ClientDate? = null,
    /** The date the amount is due. */
    val dueDate: List<Int>? = null,
    /** How the charge amount is calculated (flat, percent of amount, …). */
    val chargeCalculationType: ChargeCalculationType? = null,
    /** The currency the amounts are in. */
    val currency: ClientChargeCurrency? = null,
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
     * Derived, not stored — `` keeps Room from treating it as a column.
     *
     * Without it Room emits `_item.formattedDueDate = ...` into the generated DAO impl, which does
     * not compile against a get-only property. Latent since the original app: it only surfaces once
     * the generated desktop impls are actually compiled.
     */
    val formattedDueDate: String
        get() = if (dueDate?.size == 3) {
            "${dueDate[0]}-${dueDate[1]}-${dueDate[2]}"
        } else {
            "No Due Date"
        }
}

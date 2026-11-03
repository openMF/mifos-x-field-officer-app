/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.charge.ui

import androidx.lifecycle.SavedStateHandle
import kpt.core.common.utils.CurrencyFormatter
import kpt.core.common.utils.DateHelper
import kpt.core.model.charge.Charges
import kpt.core.model.objects.charge.ChargeResource

/**
 * One charge, fully formatted — the screens render strings and never reach for the entity.
 *
 * The original rendered `amount.toString()` / `chargeId.toString()` on nullable fields — the literal
 * text "null" on any charge Fineract sent without them — and a raw double for money. They are
 * formatted once here, with the charge's OWN currency.
 *
 * [id] is this levied instance (the row key). [definitionId] is the charge DEFINITION — what the
 * loan screen's "Charge ID" label showed, and what an officer compares against the catalogue.
 * [dueDate] is `null` when the charge has none; the screen labels that.
 */
internal data class ChargeUi(
    val id: Int,
    val definitionId: String,
    val name: String,
    val time: String,
    val calculation: String,
    val dueDate: String?,
    val amount: String,
    val amountPaid: String,
    val amountWaived: String,
    val outstanding: String,
    val penalty: Boolean,
)

internal fun Charges.toUi(): ChargeUi {
    fun money(value: Double?) = value?.let { CurrencyFormatter.format(it, currency?.code, currency?.decimalPlaces) }
        ?: PLACEHOLDER
    return ChargeUi(
        id = id,
        definitionId = chargeId?.toString() ?: PLACEHOLDER,
        name = name.orEmpty(),
        // Server-supplied display values (Fineract localises them), not app copy.
        time = chargeTimeType?.value ?: PLACEHOLDER,
        calculation = chargeCalculationType?.value ?: PLACEHOLDER,
        // `DateHelper.getDateAsString` ("14 Mar 2026"), not `formattedDueDate` ("2026-3-14", and
        // an English "No Due Date" literal from core/database). It indexes [2], so guard the size.
        dueDate = dueDate?.takeIf { it.size >= DATE_PARTS }?.let { DateHelper.getDateAsString(it) },
        amount = money(amount),
        amountPaid = money(amountPaid),
        amountWaived = money(amountWaived),
        outstanding = money(amountOutstanding),
        penalty = penalty == true,
    )
}

/**
 * The owner from a charge route. Every route is built from [ChargeResource.path] by
 * `ChargeNavigation`, so an unknown value is a programming error, not user input.
 */
internal fun SavedStateHandle.chargeResource(): ChargeResource =
    checkNotNull(ChargeResource.fromPath(get<String>(RESOURCE_TYPE_KEY))) {
        "charge route carried an unknown resourceType: ${get<String>(RESOURCE_TYPE_KEY)}"
    }

internal fun SavedStateHandle.chargeResourceId(): Int = get<Int>(RESOURCE_ID_KEY) ?: -1

/** Route argument names — they match the `ChargeNavigation` route properties. */
internal const val RESOURCE_TYPE_KEY = "resourceType"
internal const val RESOURCE_ID_KEY = "resourceId"
internal const val CHARGE_ID_KEY = "chargeId"

internal const val PLACEHOLDER = "—"
private const val DATE_PARTS = 3

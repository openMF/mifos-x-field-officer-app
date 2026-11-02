/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.common.utils

import java.text.NumberFormat
import java.util.Currency

actual object CurrencyFormatter {
    actual fun format(
        balance: Double?,
        currencyCode: String?,
        maximumFractionDigits: Int?,
    ): String {
        val numberFormat = NumberFormat.getCurrencyInstance()
        numberFormat.maximumFractionDigits = maximumFractionDigits ?: 0
        // A null/blank/unrecognised code keeps the locale's own currency rather than throwing —
        // see the common declaration for the two crashes this guard replaces.
        currencyCode.toCurrencyOrNull()?.let { numberFormat.currency = it }
        return numberFormat.format(balance ?: 0.0)
    }
}

/**
 * `Currency.getInstance` throws on a null argument (NPE) and on anything that is not a valid
 * ISO-4217 code (`IllegalArgumentException`) — including the empty string, which is what a loan
 * with no currency block projects to. Both are reachable from live Fineract data, so neither is a
 * programming error worth crashing a screen over.
 */
internal fun String?.toCurrencyOrNull(): Currency? =
    this?.takeIf { it.isNotBlank() }?.let { runCatching { Currency.getInstance(it) }.getOrNull() }

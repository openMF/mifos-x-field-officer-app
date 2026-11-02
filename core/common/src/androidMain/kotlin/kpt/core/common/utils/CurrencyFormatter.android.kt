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
        val balanceFormatter = NumberFormat.getCurrencyInstance()
        balanceFormatter.maximumFractionDigits = maximumFractionDigits ?: 0
        // Same guard as the desktop actual. This target already defaulted the AMOUNT (`?: 0`) but
        // not the CODE, so an unknown currency crashed here too.
        currencyCode.toCurrencyOrNull()?.let { balanceFormatter.currency = it }
        return balanceFormatter.format(balance ?: 0.0)
    }
}

/** See the desktop actual's copy — `Currency.getInstance` throws on null and on a non-ISO code. */
internal fun String?.toCurrencyOrNull(): Currency? =
    this?.takeIf { it.isNotBlank() }?.let { runCatching { Currency.getInstance(it) }.getOrNull() }

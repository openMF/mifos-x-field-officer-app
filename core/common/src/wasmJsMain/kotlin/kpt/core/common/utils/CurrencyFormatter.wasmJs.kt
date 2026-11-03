/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.common.utils

/** The wasmJs money formatter behind [CurrencyFormatter], over the browser's `Intl.NumberFormat`. */
actual object CurrencyFormatter {
    /**
     * Formats [balance] as money in [currencyCode] with at most [maximumFractionDigits] decimals, in the
     * browser's locale. A null balance formats as zero; a null or unusable code formats a plain number,
     * since the browser has no locale currency to fall back to.
     */
    actual fun format(
        balance: Double?,
        currencyCode: String?,
        maximumFractionDigits: Int?,
    ): String = moneyFormatter().format(balance ?: 0.0, currencyCode.orEmpty(), maximumFractionDigits ?: 0)
}

/** The browser-side formatter; the arguments cross into JS through this interface, not a `js()` string. */
private external interface MoneyFormatter : JsAny {
    fun format(amount: Double, code: String, digits: Int): String
}

// The minimum is clamped to the maximum: Intl throws when a currency's default minimum (2 for USD)
// exceeds the requested maximum, where the JVM formatter quietly lowers it. Each formatter is bound
// to a variable before `.format` is called: Kotlin/JS's `js()` parser reads `new X(...).format(...)`
// as `new (X(...).format)(...)`.
private fun moneyFormatter(): MoneyFormatter = js(
    """({
        format: function (amount, code, digits) {
            try {
                var probe = new Intl.NumberFormat(undefined, { style: "currency", currency: code });
                var min = Math.min(probe.resolvedOptions().minimumFractionDigits, digits);
                var money = new Intl.NumberFormat(undefined, {
                    style: "currency", currency: code, minimumFractionDigits: min, maximumFractionDigits: digits
                });
                return money.format(amount);
            } catch (e) {
                var plain = new Intl.NumberFormat(undefined, { maximumFractionDigits: digits });
                return plain.format(amount);
            }
        }
    })""",
)

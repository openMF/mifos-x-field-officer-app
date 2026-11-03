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

/**
 * Money, formatted for display.
 *
 * ## Every argument is nullable because every one of them is routinely absent
 *
 * Fineract omits a fee portion rather than sending zero, and a loan that has not been approved can
 * come back with no `currency` block at all. So all three actuals must TOLERATE nulls, and two of
 * them did not:
 *
 * - **desktop** called `format(balance)` with the nullable straight through, which throws
 *   `IllegalArgumentException: Cannot format given Object as a Number`. Android had guarded the
 *   amount (`?: 0`) from the start, so the same transaction list rendered on Android and crashed on
 *   desktop — a divergence nothing would have surfaced until the desktop build opened a loan.
 * - **both JVM targets** passed the code to `Currency.getInstance`, which throws on null and on
 *   anything that is not a valid ISO-4217 code — the empty string included. A loan with no
 *   currency therefore took the schedule screen down on every JVM platform.
 *
 * Found by the loan transaction-list and repayment-schedule tests, which build exactly those two
 * shapes. Both now fall back: a null amount formats as zero, an unusable code keeps the locale's
 * own currency.
 *
 * `js` and `wasmJs` are still `TODO()` and throw `NotImplementedError` on any money value — the web
 * target has no working currency formatting at all. Pre-existing and out of this slice's scope;
 * logged in CAPABILITY_GAPS as `currency-formatter-unimplemented-on-web`.
 */
expect object CurrencyFormatter {
    /**
     * Formats [balance] as money in [currencyCode] with at most [maximumFractionDigits] decimals; a null balance
     * formats as zero and an unusable code falls back to the locale's currency.
     */
    fun format(balance: Double?, currencyCode: String?, maximumFractionDigits: Int?): String
}

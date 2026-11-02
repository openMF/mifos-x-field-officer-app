/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.account.loan.transfer

import kotlinx.serialization.Serializable

/**
 * Move money from one account to another.
 *
 * ## Every field is REQUIRED, where all thirteen used to be nullable with null defaults
 *
 * Fineract needs all of them, so a nullable field here could only ever mean "the caller forgot" —
 * and the original's caller resolved four of them with `!!` at submit time precisely because the
 * payload would have accepted nulls silently. Requiring them moves the failure from a 400 (or an
 * NPE) to the compiler.
 *
 * `locale` and `dateFormat` are required for the same reason they are on
 * [kpt.core.model.objects.account.loan.LoanApproval] and
 * [kpt.core.model.objects.account.loan.loanDisburse.LoanDisburseInput]: a payload that defaults its
 * own format is how the date/format mismatch kept recurring in this port. Use
 * [kpt.core.model.utils.ApiDateFormatter].
 *
 * The original's caller happened to pair these correctly — `DateHelper.formattedFullDate` with
 * `Constants.DATE_FORMAT_LONG`, both "dd MMMM yyyy" — which is why this screen's dates worked and
 * four others did not. That was luck held in two unrelated constants, not a guarantee.
 */
@Serializable
data class AccountTransferRequest(
    val fromOfficeId: Int,
    val fromClientId: Int,
    val fromAccountType: Int,
    val fromAccountId: Int,
    val toOfficeId: Int,
    val toClientId: Int,
    val toAccountType: Int,
    val toAccountId: Int,
    val transferDate: String,
    val transferAmount: Double,
    val transferDescription: String,
    val dateFormat: String,
    val locale: String,
)

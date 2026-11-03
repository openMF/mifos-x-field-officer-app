/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.client

import kotlinx.serialization.Serializable
import kpt.core.model.loan.LoanAccount
import kpt.core.model.objects.account.share.ShareAccounts
import kpt.core.model.savings.SavingsAccount

/**
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `ClientAccounts`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class ClientAccounts(
    /** The loan accounts. */
    var loanAccounts: List<LoanAccount> = emptyList(),
    /** The savings accounts. */
    var savingsAccounts: List<SavingsAccount> = emptyList(),
    /** The share accounts. */
    var shareAccounts: List<ShareAccounts> = emptyList(),
) {
    private fun getSavingsAccounts(wantRecurring: Boolean): List<SavingsAccount> {
        val result: MutableList<SavingsAccount> = ArrayList()
        for (account in savingsAccounts) {
            if (account.depositType?.isRecurring == wantRecurring) {
                result.add(account)
            }
        }
        return result
    }

    /** The recurring-deposit savings accounts. */
    fun getRecurringSavingsAccounts(): List<SavingsAccount> {
        return getSavingsAccounts(true)
    }

    /** The savings accounts that are not recurring deposits. */
    fun getNonRecurringSavingsAccounts(): List<SavingsAccount> {
        return getSavingsAccounts(false)
    }
}

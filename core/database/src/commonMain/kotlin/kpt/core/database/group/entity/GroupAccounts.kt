/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.group.entity

import kotlinx.serialization.Serializable
import kpt.core.database.loan.entity.LoanAccountEntity
import kpt.core.database.savings.entity.SavingsAccountEntity
@Serializable
data class GroupAccounts(
    var loanAccounts: List<LoanAccountEntity> = emptyList(),

    var savingsAccounts: List<SavingsAccountEntity> = emptyList(),
) {

    private fun getSavingsAccounts(wantRecurring: Boolean): List<SavingsAccountEntity> {
        val result: MutableList<SavingsAccountEntity> = ArrayList()
        for (account in savingsAccounts) {
            if (account.depositType?.isRecurring == wantRecurring) {
                result.add(account)
            }
        }
        return result
    }

    fun getRecurringSavingsAccounts(): List<SavingsAccountEntity> {
        return getSavingsAccounts(true)
    }

    fun getNonRecurringSavingsAccounts(): List<SavingsAccountEntity> {
        return getSavingsAccounts(false)
    }
}

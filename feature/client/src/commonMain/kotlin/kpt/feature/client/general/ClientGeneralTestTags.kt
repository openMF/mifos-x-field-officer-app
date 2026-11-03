/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.general

import kpt.feature.client.general.components.ClientGeneralActionItem

/** Stable handles for UI tests and Maestro flows — see RULE-KMP-COMPOSE-UITEST-001. */
internal object ClientGeneralTestTags {
    const val SCREEN = "clientGeneral:screen"
    const val PERFORMANCE_CARD = "clientGeneral:performance"

    fun actionRow(item: ClientGeneralActionItem): String = "clientGeneral:action:" +
        when (item) {
            ClientGeneralActionItem.UpcomingCharges -> "upcomingCharges"
            ClientGeneralActionItem.LoanAccount -> "loanAccounts"
            ClientGeneralActionItem.SavingAccounts -> "savingAccounts"
            ClientGeneralActionItem.FixedDepositAccounts -> "fixedDeposit"
            ClientGeneralActionItem.RecurringDepositAccounts -> "recurringDeposit"
            ClientGeneralActionItem.SharesAccounts -> "shares"
            ClientGeneralActionItem.CollateralData -> "collateral"
        }
}

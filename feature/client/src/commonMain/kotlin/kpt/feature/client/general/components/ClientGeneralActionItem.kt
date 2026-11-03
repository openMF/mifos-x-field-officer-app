/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.general.components

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kpt.feature.client.generated.resources.Res
import kpt.feature.client.generated.resources.collateral_data
import kpt.feature.client.generated.resources.feature_client_general_collateral_subtitle
import kpt.feature.client.generated.resources.feature_client_general_collateral_title
import kpt.feature.client.generated.resources.feature_client_general_fixed_deposit_subtitle
import kpt.feature.client.generated.resources.feature_client_general_fixed_deposit_title
import kpt.feature.client.generated.resources.feature_client_general_loan_accounts_subtitle
import kpt.feature.client.generated.resources.feature_client_general_loan_accounts_title
import kpt.feature.client.generated.resources.feature_client_general_recurring_deposit_subtitle
import kpt.feature.client.generated.resources.feature_client_general_recurring_deposit_title
import kpt.feature.client.generated.resources.feature_client_general_saving_account_subtitle
import kpt.feature.client.generated.resources.feature_client_general_saving_account_title
import kpt.feature.client.generated.resources.feature_client_general_shares_subtitle
import kpt.feature.client.generated.resources.feature_client_general_shares_title
import kpt.feature.client.generated.resources.feature_client_general_upcoming_charges_subtitle
import kpt.feature.client.generated.resources.feature_client_general_upcoming_charges_title
import kpt.feature.client.generated.resources.fixed_deposit_accounts
import kpt.feature.client.generated.resources.loan_account
import kpt.feature.client.generated.resources.recurring_deposit_accounts
import kpt.feature.client.generated.resources.saving_account
import kpt.feature.client.generated.resources.shares_accounts
import kpt.feature.client.generated.resources.upcoming_charges
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

/**
 * Ported verbatim from `6b66e8a43:feature/client/clientGeneral/ClientProfileGeneralActionItem.kt`
 * — same seven items, same titles and subtitles, same drawables.
 *
 * The subtitles are the original's and several are plainly WRONG ("Loan Accounts" → "View or edit
 * the linked address", "Shares Accounts" and "Collateral Data" sharing "Check or update the account
 * notes"). §6a keeps them: copy is UI, and this is the kind of thing the Stitch pass fixes.
 * Recorded in CAPABILITY_GAPS rather than corrected here.
 */
sealed class ClientGeneralActionItem(
    /** The row's title. */
    val title: StringResource,
    /** The line under the title saying what the row opens. */
    val subTitle: StringResource,
    /** The row's leading icon. */
    val icon: DrawableResource,
) {
    /** Charges coming due on the client's accounts. */
    data object UpcomingCharges : ClientGeneralActionItem(
        title = Res.string.feature_client_general_upcoming_charges_title,
        subTitle = Res.string.feature_client_general_upcoming_charges_subtitle,
        icon = Res.drawable.upcoming_charges,
    )

    /** The client's loan accounts. */
    data object LoanAccount : ClientGeneralActionItem(
        title = Res.string.feature_client_general_loan_accounts_title,
        subTitle = Res.string.feature_client_general_loan_accounts_subtitle,
        icon = Res.drawable.loan_account,
    )

    /** The client's savings accounts. */
    data object SavingAccounts : ClientGeneralActionItem(
        title = Res.string.feature_client_general_saving_account_title,
        subTitle = Res.string.feature_client_general_saving_account_subtitle,
        icon = Res.drawable.saving_account,
    )

    /** The client's fixed-deposit accounts. */
    data object FixedDepositAccounts : ClientGeneralActionItem(
        title = Res.string.feature_client_general_fixed_deposit_title,
        subTitle = Res.string.feature_client_general_fixed_deposit_subtitle,
        icon = Res.drawable.fixed_deposit_accounts,
    )

    /** The client's recurring-deposit accounts. */
    data object RecurringDepositAccounts : ClientGeneralActionItem(
        title = Res.string.feature_client_general_recurring_deposit_title,
        subTitle = Res.string.feature_client_general_recurring_deposit_subtitle,
        icon = Res.drawable.recurring_deposit_accounts,
    )

    /** The client's share accounts. */
    data object SharesAccounts : ClientGeneralActionItem(
        title = Res.string.feature_client_general_shares_title,
        subTitle = Res.string.feature_client_general_shares_subtitle,
        icon = Res.drawable.shares_accounts,
    )

    /** The collateral the client has pledged. */
    data object CollateralData : ClientGeneralActionItem(
        title = Res.string.feature_client_general_collateral_title,
        subTitle = Res.string.feature_client_general_collateral_subtitle,
        icon = Res.drawable.collateral_data,
    )
}

/** The original's list and order. */
internal val clientGeneralActions: ImmutableList<ClientGeneralActionItem> = persistentListOf(
    ClientGeneralActionItem.UpcomingCharges,
    ClientGeneralActionItem.LoanAccount,
    ClientGeneralActionItem.SavingAccounts,
    ClientGeneralActionItem.FixedDepositAccounts,
    ClientGeneralActionItem.RecurringDepositAccounts,
    ClientGeneralActionItem.SharesAccounts,
    ClientGeneralActionItem.CollateralData,
)

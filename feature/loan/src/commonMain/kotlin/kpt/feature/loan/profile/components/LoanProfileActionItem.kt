/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loan.profile.components

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kpt.core.ui.generated.resources.account_details
import kpt.core.ui.generated.resources.autorenew
import kpt.core.ui.generated.resources.charges
import kpt.core.ui.generated.resources.collateral
import kpt.core.ui.generated.resources.dashboard
import kpt.core.ui.generated.resources.design_services
import kpt.core.ui.generated.resources.documents
import kpt.core.ui.generated.resources.notes
import kpt.core.ui.generated.resources.originators
import kpt.core.ui.generated.resources.repayment_schedule
import kpt.core.ui.generated.resources.reschedules
import kpt.core.ui.generated.resources.term_variations
import kpt.core.ui.generated.resources.transaction
import kpt.feature.loan.generated.resources.Res
import kpt.feature.loan.generated.resources.feature_loan_profile_item_account_details_subtitle
import kpt.feature.loan.generated.resources.feature_loan_profile_item_account_details_title
import kpt.feature.loan.generated.resources.feature_loan_profile_item_charges_subtitle
import kpt.feature.loan.generated.resources.feature_loan_profile_item_charges_title
import kpt.feature.loan.generated.resources.feature_loan_profile_item_collateral_subtitle
import kpt.feature.loan.generated.resources.feature_loan_profile_item_collateral_title
import kpt.feature.loan.generated.resources.feature_loan_profile_item_dashboard_subtitle
import kpt.feature.loan.generated.resources.feature_loan_profile_item_dashboard_title
import kpt.feature.loan.generated.resources.feature_loan_profile_item_documents_subtitle
import kpt.feature.loan.generated.resources.feature_loan_profile_item_documents_title
import kpt.feature.loan.generated.resources.feature_loan_profile_item_general_subtitle
import kpt.feature.loan.generated.resources.feature_loan_profile_item_general_title
import kpt.feature.loan.generated.resources.feature_loan_profile_item_notes_subtitle
import kpt.feature.loan.generated.resources.feature_loan_profile_item_notes_title
import kpt.feature.loan.generated.resources.feature_loan_profile_item_originators_subtitle
import kpt.feature.loan.generated.resources.feature_loan_profile_item_originators_title
import kpt.feature.loan.generated.resources.feature_loan_profile_item_repayment_schedule_subtitle
import kpt.feature.loan.generated.resources.feature_loan_profile_item_repayment_schedule_title
import kpt.feature.loan.generated.resources.feature_loan_profile_item_reschedules_subtitle
import kpt.feature.loan.generated.resources.feature_loan_profile_item_reschedules_title
import kpt.feature.loan.generated.resources.feature_loan_profile_item_standing_instructions_subtitle
import kpt.feature.loan.generated.resources.feature_loan_profile_item_standing_instructions_title
import kpt.feature.loan.generated.resources.feature_loan_profile_item_term_variations_subtitle
import kpt.feature.loan.generated.resources.feature_loan_profile_item_term_variations_title
import kpt.feature.loan.generated.resources.feature_loan_profile_item_transactions_subtitle
import kpt.feature.loan.generated.resources.feature_loan_profile_item_transactions_title
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import kpt.core.ui.generated.resources.Res as UiRes

/**
 * Ported verbatim from
 * `6b66e8a43:feature/loan/loanAccountProfile/components/LoanAccountProfileActionItem.kt` — all
 * thirteen items, same titles, subtitles and icons.
 *
 * The icons come from `core/ui`'s resources rather than this module's, exactly as the original
 * did (`UiRes.drawable.*`): they are the shared account-detail iconography that savings and
 * recurring-deposit profiles reuse when those slices land.
 */
sealed class LoanProfileActionItem(
    val title: StringResource,
    val subTitle: StringResource,
    val icon: DrawableResource,
) {
    data object General : LoanProfileActionItem(
        title = Res.string.feature_loan_profile_item_general_title,
        subTitle = Res.string.feature_loan_profile_item_general_subtitle,
        icon = UiRes.drawable.design_services,
    )

    data object Dashboard : LoanProfileActionItem(
        title = Res.string.feature_loan_profile_item_dashboard_title,
        subTitle = Res.string.feature_loan_profile_item_dashboard_subtitle,
        icon = UiRes.drawable.dashboard,
    )

    data object AccountDetails : LoanProfileActionItem(
        title = Res.string.feature_loan_profile_item_account_details_title,
        subTitle = Res.string.feature_loan_profile_item_account_details_subtitle,
        icon = UiRes.drawable.account_details,
    )

    data object RepaymentSchedule : LoanProfileActionItem(
        title = Res.string.feature_loan_profile_item_repayment_schedule_title,
        subTitle = Res.string.feature_loan_profile_item_repayment_schedule_subtitle,
        icon = UiRes.drawable.repayment_schedule,
    )

    data object Transactions : LoanProfileActionItem(
        title = Res.string.feature_loan_profile_item_transactions_title,
        subTitle = Res.string.feature_loan_profile_item_transactions_subtitle,
        icon = UiRes.drawable.transaction,
    )

    data object Charges : LoanProfileActionItem(
        title = Res.string.feature_loan_profile_item_charges_title,
        subTitle = Res.string.feature_loan_profile_item_charges_subtitle,
        icon = UiRes.drawable.charges,
    )

    data object Originators : LoanProfileActionItem(
        title = Res.string.feature_loan_profile_item_originators_title,
        subTitle = Res.string.feature_loan_profile_item_originators_subtitle,
        icon = UiRes.drawable.originators,
    )

    data object Collateral : LoanProfileActionItem(
        title = Res.string.feature_loan_profile_item_collateral_title,
        subTitle = Res.string.feature_loan_profile_item_collateral_subtitle,
        icon = UiRes.drawable.collateral,
    )

    data object TermVariations : LoanProfileActionItem(
        title = Res.string.feature_loan_profile_item_term_variations_title,
        subTitle = Res.string.feature_loan_profile_item_term_variations_subtitle,
        icon = UiRes.drawable.term_variations,
    )

    data object Reschedules : LoanProfileActionItem(
        title = Res.string.feature_loan_profile_item_reschedules_title,
        subTitle = Res.string.feature_loan_profile_item_reschedules_subtitle,
        icon = UiRes.drawable.reschedules,
    )

    data object Documents : LoanProfileActionItem(
        title = Res.string.feature_loan_profile_item_documents_title,
        subTitle = Res.string.feature_loan_profile_item_documents_subtitle,
        icon = UiRes.drawable.documents,
    )

    data object Notes : LoanProfileActionItem(
        title = Res.string.feature_loan_profile_item_notes_title,
        subTitle = Res.string.feature_loan_profile_item_notes_subtitle,
        icon = UiRes.drawable.notes,
    )

    data object StandingInstructions : LoanProfileActionItem(
        title = Res.string.feature_loan_profile_item_standing_instructions_title,
        subTitle = Res.string.feature_loan_profile_item_standing_instructions_subtitle,
        icon = UiRes.drawable.autorenew,
    )
}

/** The original's list and order — all thirteen. */
internal val loanProfileActionItems: ImmutableList<LoanProfileActionItem> = persistentListOf(
    LoanProfileActionItem.General,
    LoanProfileActionItem.Dashboard,
    LoanProfileActionItem.AccountDetails,
    LoanProfileActionItem.RepaymentSchedule,
    LoanProfileActionItem.Transactions,
    LoanProfileActionItem.Charges,
    LoanProfileActionItem.Originators,
    LoanProfileActionItem.Collateral,
    LoanProfileActionItem.TermVariations,
    LoanProfileActionItem.Reschedules,
    LoanProfileActionItem.Documents,
    LoanProfileActionItem.Notes,
    LoanProfileActionItem.StandingInstructions,
)

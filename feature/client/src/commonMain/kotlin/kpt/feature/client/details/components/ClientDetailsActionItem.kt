/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.details.components

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kpt.feature.client.generated.resources.Res
import kpt.feature.client.generated.resources.adf_scanner
import kpt.feature.client.generated.resources.create_new_folder
import kpt.feature.client.generated.resources.directions
import kpt.feature.client.generated.resources.feature_client_add_charge_subtitle
import kpt.feature.client.generated.resources.feature_client_add_charge_title
import kpt.feature.client.generated.resources.feature_client_apply_new_application_subtitle
import kpt.feature.client.generated.resources.feature_client_apply_new_application_title
import kpt.feature.client.generated.resources.feature_client_assign_staff_subtitle
import kpt.feature.client.generated.resources.feature_client_assign_staff_title
import kpt.feature.client.generated.resources.feature_client_client_screen_reports_subtitle
import kpt.feature.client.generated.resources.feature_client_client_screen_reports_title
import kpt.feature.client.generated.resources.feature_client_closure_application_subtitle
import kpt.feature.client.generated.resources.feature_client_closure_application_title
import kpt.feature.client.generated.resources.feature_client_create_collateral_subtitle
import kpt.feature.client.generated.resources.feature_client_create_collateral_title
import kpt.feature.client.generated.resources.feature_client_create_self_service_users_subtitle
import kpt.feature.client.generated.resources.feature_client_create_self_service_users_title
import kpt.feature.client.generated.resources.feature_client_create_standing_instructions_subtitle
import kpt.feature.client.generated.resources.feature_client_create_standing_instructions_title
import kpt.feature.client.generated.resources.feature_client_transfer_client_subtitle
import kpt.feature.client.generated.resources.feature_client_transfer_client_title
import kpt.feature.client.generated.resources.feature_client_update_default_account_subtitle
import kpt.feature.client.generated.resources.feature_client_update_default_account_title
import kpt.feature.client.generated.resources.feature_client_view_standing_instructions_subtitle
import kpt.feature.client.generated.resources.feature_client_view_standing_instructions_title
import kpt.feature.client.generated.resources.manage_accounts
import kpt.feature.client.generated.resources.meeting_room
import kpt.feature.client.generated.resources.person_remove
import kpt.feature.client.generated.resources.request_quote
import kpt.feature.client.generated.resources.room_preferences
import kpt.feature.client.generated.resources.switch_account
import kpt.feature.client.generated.resources.text_snippet
import kpt.feature.client.generated.resources.transfer_within_a_station
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

/**
 * Ported verbatim from
 * `6b66e8a43:feature/client/clientDetailsProfile/ClientProfileDetailsAction.kt`.
 *
 * Eleven items declared, EIGHT rendered. The original commented three out of the list
 * ([ClientScreenReports], [ViewStandingInstructions], [CreateSelfServiceUsers]) and their event
 * branches are empty `{}` — kept declared-but-unlisted, same as `FamilyMembers` on the profile
 * screen, so there is no dead row on screen.
 */
sealed class ClientDetailsActionItem(
    val title: StringResource,
    val subTitle: StringResource,
    val icon: DrawableResource,
) {
    data object ApplyNewApplication : ClientDetailsActionItem(
        title = Res.string.feature_client_apply_new_application_title,
        subTitle = Res.string.feature_client_apply_new_application_subtitle,
        icon = Res.drawable.text_snippet,
    )

    data object ClosureApplication : ClientDetailsActionItem(
        title = Res.string.feature_client_closure_application_title,
        subTitle = Res.string.feature_client_closure_application_subtitle,
        icon = Res.drawable.person_remove,
    )

    data object TransferClient : ClientDetailsActionItem(
        title = Res.string.feature_client_transfer_client_title,
        subTitle = Res.string.feature_client_transfer_client_subtitle,
        icon = Res.drawable.transfer_within_a_station,
    )

    data object AssignStaff : ClientDetailsActionItem(
        title = Res.string.feature_client_assign_staff_title,
        subTitle = Res.string.feature_client_assign_staff_subtitle,
        icon = Res.drawable.manage_accounts,
    )

    data object AddCharge : ClientDetailsActionItem(
        title = Res.string.feature_client_add_charge_title,
        subTitle = Res.string.feature_client_add_charge_subtitle,
        icon = Res.drawable.request_quote,
    )

    data object CreateCollateral : ClientDetailsActionItem(
        title = Res.string.feature_client_create_collateral_title,
        subTitle = Res.string.feature_client_create_collateral_subtitle,
        icon = Res.drawable.create_new_folder,
    )

    data object UpdateDefaultAccount : ClientDetailsActionItem(
        title = Res.string.feature_client_update_default_account_title,
        subTitle = Res.string.feature_client_update_default_account_subtitle,
        icon = Res.drawable.switch_account,
    )

    data object CreateStandingInstructions : ClientDetailsActionItem(
        title = Res.string.feature_client_create_standing_instructions_title,
        subTitle = Res.string.feature_client_create_standing_instructions_subtitle,
        icon = Res.drawable.meeting_room,
    )

    /** Declared, not rendered — commented out of the original's list. */
    data object ClientScreenReports : ClientDetailsActionItem(
        title = Res.string.feature_client_client_screen_reports_title,
        subTitle = Res.string.feature_client_client_screen_reports_subtitle,
        icon = Res.drawable.adf_scanner,
    )

    /** Declared, not rendered — commented out of the original's list. */
    data object ViewStandingInstructions : ClientDetailsActionItem(
        title = Res.string.feature_client_view_standing_instructions_title,
        subTitle = Res.string.feature_client_view_standing_instructions_subtitle,
        icon = Res.drawable.room_preferences,
    )

    /** Declared, not rendered — commented out of the original's list. */
    data object CreateSelfServiceUsers : ClientDetailsActionItem(
        title = Res.string.feature_client_create_self_service_users_title,
        subTitle = Res.string.feature_client_create_self_service_users_subtitle,
        icon = Res.drawable.directions,
    )
}

/** The original's list and order — the eight it actually renders. */
internal val clientDetailsActionItems: ImmutableList<ClientDetailsActionItem> = persistentListOf(
    ClientDetailsActionItem.ApplyNewApplication,
    ClientDetailsActionItem.ClosureApplication,
    ClientDetailsActionItem.TransferClient,
    ClientDetailsActionItem.AssignStaff,
    ClientDetailsActionItem.AddCharge,
    ClientDetailsActionItem.CreateCollateral,
    ClientDetailsActionItem.UpdateDefaultAccount,
    ClientDetailsActionItem.CreateStandingInstructions,
)

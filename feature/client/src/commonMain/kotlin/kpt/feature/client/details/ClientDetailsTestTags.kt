/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.details

import kpt.feature.client.details.components.ClientDetailsActionItem

/** Stable handles for UI tests and Maestro flows — see RULE-KMP-COMPOSE-UITEST-001. */
internal object ClientDetailsTestTags {
    const val ACTIVATE = "client-details:activate"
    const val SCREEN = "clientDetails:screen"
    const val HEADER = "clientDetails:header"
    const val UPDATE_PHOTO = "clientDetails:updatePhoto"
    const val UPDATE_SIGNATURE = "clientDetails:updateSignature"
    const val UPDATE_DETAILS = "clientDetails:updateDetails"
    const val STATUS_DIALOG = "clientDetails:statusDialog"

    fun actionRow(item: ClientDetailsActionItem): String = "clientDetails:action:" +
        when (item) {
            ClientDetailsActionItem.ApplyNewApplication -> "applyNew"
            ClientDetailsActionItem.ClosureApplication -> "closure"
            ClientDetailsActionItem.TransferClient -> "transfer"
            ClientDetailsActionItem.AssignStaff -> "assignStaff"
            ClientDetailsActionItem.AddCharge -> "addCharge"
            ClientDetailsActionItem.CreateCollateral -> "createCollateral"
            ClientDetailsActionItem.UpdateDefaultAccount -> "updateDefaultAccount"
            ClientDetailsActionItem.CreateStandingInstructions -> "standingInstructions"
            ClientDetailsActionItem.ClientScreenReports -> "screenReports"
            ClientDetailsActionItem.ViewStandingInstructions -> "viewStandingInstructions"
            ClientDetailsActionItem.CreateSelfServiceUsers -> "selfServiceUsers"
        }
}

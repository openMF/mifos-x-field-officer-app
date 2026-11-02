/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.profile

import kpt.feature.client.profile.components.ClientProfileActionItem

/** Stable handles for UI tests and Maestro flows — see RULE-KMP-COMPOSE-UITEST-001. */
internal object ClientProfileTestTags {
    const val SCREEN = "clientProfile:screen"
    const val PROFILE_CARD = "clientProfile:card"

    /** One per action row, so a test can assert WHICH rows render, not just how many. */
    fun actionRow(item: ClientProfileActionItem): String = "clientProfile:action:" +
        when (item) {
            ClientProfileActionItem.General -> "general"
            ClientProfileActionItem.Address -> "address"
            ClientProfileActionItem.Identifiers -> "identifiers"
            ClientProfileActionItem.Documents -> "documents"
            ClientProfileActionItem.Notes -> "notes"
            ClientProfileActionItem.PinpointLocation -> "pinpoint"
            ClientProfileActionItem.FamilyMembers -> "family"
        }
}

/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.transfer

/** Stable handles for UI tests and Maestro flows — see RULE-KMP-COMPOSE-UITEST-001. */
internal object ClientTransferTestTags {
    const val SCREEN = "clientTransfer:screen"
    const val OFFICE = "clientTransfer:office"
    const val DATE = "clientTransfer:date"
    const val NOTE = "clientTransfer:note"
    const val BACK = "clientTransfer:back"
    const val SUBMIT = "clientTransfer:submit"
    const val NO_OFFICES = "clientTransfer:noOffices"
    const val STATUS_DIALOG = "clientTransfer:statusDialog"
}

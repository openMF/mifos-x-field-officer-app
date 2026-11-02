/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.documents

/** Stable handles for UI tests and Maestro flows — see RULE-KMP-COMPOSE-UITEST-001. */
internal object ClientDocumentsTestTags {
    const val SCREEN = "clientDocuments:screen"
    const val LIST = "clientDocuments:list"
    const val COUNT = "clientDocuments:count"
    const val SEARCH_TOGGLE = "clientDocuments:searchToggle"
    const val SEARCH_BAR = "clientDocuments:searchBar"
    const val ADD = "clientDocuments:add"
    const val EMPTY = "clientDocuments:empty"
    const val DELETE_DIALOG = "clientDocuments:deleteDialog"
    const val WRITE_ERROR_DIALOG = "clientDocuments:writeError"
}

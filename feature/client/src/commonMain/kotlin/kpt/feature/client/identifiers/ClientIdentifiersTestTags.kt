/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.identifiers

/** Stable handles for UI tests and Maestro flows — see RULE-KMP-COMPOSE-UITEST-001. */
internal object ClientIdentifiersTestTags {
    const val SCREEN = "clientIdentifiers:screen"
    const val LIST = "clientIdentifiers:list"
    const val COUNT = "clientIdentifiers:count"
    const val ADD = "clientIdentifiers:add"
    const val SEARCH = "clientIdentifiers:search"
    const val EMPTY = "clientIdentifiers:empty"
    const val DELETE_DIALOG = "clientIdentifiers:deleteDialog"
    const val WRITE_ERROR_DIALOG = "clientIdentifiers:writeError"
}

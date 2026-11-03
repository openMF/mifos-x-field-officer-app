/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.list

/** Stable handles for the client list — asserted by UI tests, never shown to a user. */
object ClientListTestTags {
    /** The client list screen's root. */
    const val SCREEN = "client-list:screen"

    /** The search field. */
    const val SEARCH = "client-list:search"

    /** The list of clients. */
    const val LIST = "client-list:list"

    /** The pull-to-refresh container. */
    const val REFRESH = "client-list:refresh"

    /** The "new client" button. */
    const val CREATE = "client-list:create"

    /** The button that shows or hides the filters. */
    const val FILTER_TOGGLE = "client-list:filter-toggle"

    /** The filter panel. */
    const val FILTERS = "client-list:filters"

    /** The "clear filters" button. */
    const val CLEAR_FILTERS = "client-list:clear-filters"
}

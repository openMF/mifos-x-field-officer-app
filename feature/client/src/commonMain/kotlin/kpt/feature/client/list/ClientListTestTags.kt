/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.list

/** Stable handles for the client list — asserted by UI tests, never shown to a user. */
object ClientListTestTags {
    const val SCREEN = "client-list:screen"
    const val SEARCH = "client-list:search"
    const val LIST = "client-list:list"
    const val CREATE = "client-list:create"
    const val FILTER_TOGGLE = "client-list:filter-toggle"
    const val FILTERS = "client-list:filters"
    const val CLEAR_FILTERS = "client-list:clear-filters"
}

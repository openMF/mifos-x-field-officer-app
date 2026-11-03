/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.groups.list

/** Stable tags for UI tests and Maestro flows. Renaming one breaks both — treat as a contract. */
internal object GroupsListTestTags {
    const val SCREEN = "groups:list"
    const val SEARCH = "groups:list:search"
    const val FILTER_TOGGLE = "groups:list:filter-toggle"
    const val FILTERS = "groups:list:filters"
    const val ROW = "groups:list:row"
    const val CREATE = "GroupsListCreate"
}

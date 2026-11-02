/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.searchrecord.ui

/** Stable handles for UI tests and device flows. */
internal object SearchRecordTestTags {
    const val SCREEN = "search_record_screen"
    const val SEARCH_FIELD = "search_record_search_field"
    const val EMPTY = "search_record_empty"
    const val NO_RESULTS = "search_record_no_results"

    /** One result, by record type and id — a search can return an address and an identifier with the same id. */
    fun result(type: String, id: Int) = "search_record_result_${type}_$id"
}

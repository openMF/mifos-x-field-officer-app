/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.searchrecord.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.composableWithPushTransitions
import kpt.core.model.objects.searchrecord.GenericSearchRecord
import kpt.core.model.objects.searchrecord.RecordType
import kpt.feature.searchrecord.ui.SearchRecordScreen

/** [type] is a [RecordType] name — a String so the route stays serializable as the original's. */
@Serializable
data class SearchRecordRoute(val type: String)

/**
 * Ported from `6b66e8a43:feature/search-record/.../navigation/SearchRecordNavigation.kt`.
 *
 * Registered by the CLIENT graph, not the app shell as in the original: the screens that open it
 * (client addresses, identifiers) and the screens a result opens are both client destinations, and
 * feature/client is the hub that already owns those routes.
 */
fun NavGraphBuilder.searchRecordDestination(
    onBackClick: () -> Unit,
    onRecordSelected: (GenericSearchRecord) -> Unit,
) {
    composableWithPushTransitions<SearchRecordRoute> {
        SearchRecordScreen(
            onBackClick = onBackClick,
            onRecordSelected = onRecordSelected,
        )
    }
}

fun NavController.navigateToSearchRecord(type: RecordType) {
    navigate(SearchRecordRoute(type = type.name))
}

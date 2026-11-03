/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.collectionsheet.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.FeatureDestination
import kpt.core.base.ui.nav.composableWithPushTransitions
import kpt.feature.collectionsheet.ui.details.CollectionSheetDetailsScreen
import kpt.feature.collectionsheet.ui.newsheet.NewCollectionSheetScreen

/** The drawer's "Collection Sheet" — the original's `IndividualCollectionSheetScreenRoute`. */
@Serializable
data object IndividualCollectionSheetRoute

/**
 * One generated sheet, addressed by the REQUEST that selects it — never the sheet itself. The
 * original put the whole sheet (and later the whole payload) into the route as JSON; the details
 * screen now streams the sheet for these three values, so a process restart reopens it from cache.
 */
@Serializable
data class CollectionSheetDetailsRoute(
    /** The office the sheet is generated for. */
    val officeId: Int,
    /** The loan officer to narrow the sheet to; null for the whole office. */
    val staffId: Int? = null,
    /** The meeting date, `dd MMMM yyyy` as Fineract expects it. */
    val transactionDate: String,
)

/** Registers the collection-sheet search and details screens in the nav graph. */
@FeatureDestination
fun NavGraphBuilder.collectionSheetDestinations(navController: NavController) {
    composableWithPushTransitions<IndividualCollectionSheetRoute> {
        NewCollectionSheetScreen(
            onNavigateBack = { navController.popBackStack() },
            onOpenSheet = { officeId, staffId, date ->
                navController.navigate(CollectionSheetDetailsRoute(officeId, staffId, date))
            },
        )
    }
    composableWithPushTransitions<CollectionSheetDetailsRoute> {
        CollectionSheetDetailsScreen(onNavigateBack = { navController.popBackStack() })
    }
}

/** Opens the individual collection sheet search. */
fun NavController.navigateToIndividualCollectionSheet() = navigate(IndividualCollectionSheetRoute)

/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.datatable.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.FeatureDestination
import kpt.core.base.ui.nav.composableWithPushTransitions
import kpt.core.base.ui.nav.popBackStackSafely
import kpt.feature.datatable.list.DataTableListScreen
import kpt.feature.datatable.rows.DataTableRowsScreen

/**
 * The datatables registered against one application table, for one entity.
 *
 * `tableName` is the APPLICATION table (`m_client`, `m_group`) — it selects which entity kind's
 * datatables to list. `entityId` is the entity those datatables will be read for when one is
 * opened. The original's route carried only `tableName` and reconstructed the entity id from a
 * JSON nav argument one screen later.
 */
@Serializable
data class DataTableListRoute(val tableName: String, val entityId: Int)

/** One datatable's rows. `tableName` here is the DATATABLE's registered name, not the apptable. */
@Serializable
data class DataTableRowsRoute(val tableName: String, val entityId: Int)

fun NavController.navigateToDataTableList(
    appTable: String,
    entityId: Int,
    navOptions: NavOptions? = null,
) = navigate(DataTableListRoute(appTable, entityId), navOptions)

fun NavController.navigateToDataTableRows(
    tableName: String,
    entityId: Int,
    navOptions: NavOptions? = null,
) = navigate(DataTableRowsRoute(tableName, entityId), navOptions)

/**
 * `@FeatureDestination`, so the generator registers these ONCE — the same reasoning as
 * `activateDestination` and `documentDestinations`. Group detail opens the list today and client
 * will when create-client lands, so there is no single parent graph to nest it under.
 */
@FeatureDestination
fun NavGraphBuilder.dataTableDestinations(navController: NavController) {
    composableWithPushTransitions<DataTableListRoute> {
        DataTableListScreen(
            onBackPressed = navController::popBackStackSafely,
            onOpenTable = navController::navigateToDataTableRows,
        )
    }

    composableWithPushTransitions<DataTableRowsRoute> {
        DataTableRowsScreen(onBackPressed = navController::popBackStackSafely)
    }
}

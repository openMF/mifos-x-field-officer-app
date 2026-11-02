/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.report.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.FeatureDestination
import kpt.core.base.ui.nav.composableWithPushTransitions
import kpt.feature.report.ui.list.ReportListScreen
import kpt.feature.report.ui.run.ReportRunScreen

/** The drawer's "Run Reports" — the original's `RunReportScreenRoute`. */
@Serializable
data object ReportListRoute

/** One report, by name: its parameter form, then its result. */
@Serializable
data class ReportRunRoute(val reportName: String)

@FeatureDestination
fun NavGraphBuilder.reportDestinations(navController: NavController) {
    composableWithPushTransitions<ReportListRoute> {
        ReportListScreen(
            onNavigateBack = { navController.popBackStack() },
            onOpenReport = { navController.navigate(ReportRunRoute(it)) },
        )
    }
    composableWithPushTransitions<ReportRunRoute> {
        ReportRunScreen(onNavigateBack = { navController.popBackStack() })
    }
}

fun NavController.navigateToReports() = navigate(ReportListRoute)

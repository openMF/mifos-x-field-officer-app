/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.checkerinbox.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.composableWithPushTransitions
import kpt.feature.checkerinbox.ui.CheckerInboxScreen

/**
 * The drawer's "Checker Inbox & Tasks" — the original's `CheckerInboxTaskScreenRoute` hub +
 * `CheckerInboxScreenRoute`.
 */
@Serializable
data object CheckerInboxRoute

/**
 * Where the checker inbox leads outside this feature — callbacks `ForkFeatureRegistry` fills, since a
 * feature never depends on another.
 *
 * @property openLoanReschedules opens a loan's reschedules screen, which approves and rejects them
 */
data class CheckerInboxLinks(val openLoanReschedules: (loanId: Int) -> Unit)

/**
 * Registers the checker inbox and its task detail in the nav graph. Not `@FeatureDestination`: it
 * opens a loan's reschedules, so `ForkFeatureRegistry` registers it with [CheckerInboxLinks].
 */
fun NavGraphBuilder.checkerInboxDestinations(navController: NavController, links: CheckerInboxLinks) {
    composableWithPushTransitions<CheckerInboxRoute> {
        CheckerInboxScreen(
            onNavigateBack = { navController.popBackStack() },
            // The loan's own reschedules screen approves and rejects — not duplicated here.
            onOpenLoanReschedules = links.openLoanReschedules,
        )
    }
}

/** Opens the maker-checker inbox. */
fun NavController.navigateToCheckerInbox() = navigate(CheckerInboxRoute)

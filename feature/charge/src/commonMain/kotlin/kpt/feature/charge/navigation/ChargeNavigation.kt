/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.charge.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.FeatureDestination
import kpt.core.base.ui.nav.composableWithPushTransitions
import kpt.core.base.ui.nav.popBackStackSafely
import kpt.core.model.objects.charge.ChargeResource
import kpt.feature.charge.detail.ChargeDetailScreen
import kpt.feature.charge.form.ChargeFormScreen
import kpt.feature.charge.list.ChargeListScreen

/**
 * The charges levied on ONE resource. `resourceType` is [ChargeResource.path] — the original's
 * `ChargesRoute(resourceId, resourceType)` (`6b66e8a43:feature/client/charges/ChargesRoute.kt`),
 * which every caller then passed `"clients"`, so loans grew a second, separate charge screen.
 *
 * Neither argument has a default: the original defaulted both, so a missing argument asked
 * Fineract for the charges of resource `-1` under an empty type.
 *
 * @property resourceType the Fineract resource the charges belong to (`loans`, `savingsaccounts`, `clients`)
 * @property resourceId the id of that resource
 */
@Serializable
data class ChargeListRoute(val resourceType: String, val resourceId: Int)

/**
 * One charge, read from the table the list fills.
 *
 * @property resourceType the Fineract resource the charge belongs to
 * @property resourceId the id of that resource
 * @property chargeId the charge to show
 */
@Serializable
data class ChargeDetailRoute(val resourceType: String, val resourceId: Int, val chargeId: Int)

/**
 * The add / edit form. `chargeId` is `-1` for a CREATE: a nav argument cannot be absent, so the
 * sentinel is how "no charge yet" reaches the screen, and the ViewModel narrows it to `null`.
 */
@Serializable
data class ChargeFormRoute(
    /** The Fineract resource the charge belongs to (`loans`, `savingsaccounts`, `clients`). */
    val resourceType: String,
    /** The id of that resource. */
    val resourceId: Int,
    /** The charge to edit, or [CREATE_NEW] to add one. */
    val chargeId: Int = CREATE_NEW,
) {
    /** Sentinels for the route's arguments. */
    companion object {
        /** The [chargeId] that means "add a new charge" — a nav argument cannot be absent. */
        const val CREATE_NEW = -1
    }
}

/** Opens the charges of one resource. */
fun NavController.navigateToCharges(
    resource: ChargeResource,
    resourceId: Int,
    navOptions: NavOptions? = null,
) = navigate(ChargeListRoute(resource.path, resourceId), navOptions)

/** Straight to the add form — the client details screen's "Add Charge" opens this. */
fun NavController.navigateToAddCharge(
    resource: ChargeResource,
    resourceId: Int,
    navOptions: NavOptions? = null,
) = navigate(ChargeFormRoute(resource.path, resourceId), navOptions)

/**
 * `@FeatureDestination`, so the generator registers these ONCE on the authenticated graph — the
 * same reasoning as `documentDestinations`: client, loan and savings all open it, so there is no
 * single parent graph to nest it under.
 */
@FeatureDestination
fun NavGraphBuilder.chargeDestinations(navController: NavController) {
    composableWithPushTransitions<ChargeListRoute> {
        ChargeListScreen(
            onBackPressed = navController::popBackStackSafely,
            onAddCharge = { type, id -> navController.navigate(ChargeFormRoute(type, id)) },
            onOpenCharge = { type, id, chargeId -> navController.navigate(ChargeDetailRoute(type, id, chargeId)) },
            onEditCharge = { type, id, chargeId -> navController.navigate(ChargeFormRoute(type, id, chargeId)) },
        )
    }

    composableWithPushTransitions<ChargeDetailRoute> {
        ChargeDetailScreen(
            onBackPressed = navController::popBackStackSafely,
            onEditCharge = { type, id, chargeId -> navController.navigate(ChargeFormRoute(type, id, chargeId)) },
        )
    }

    composableWithPushTransitions<ChargeFormRoute> {
        // Back to wherever it was opened from. The list needs no callback: the write lands in the
        // table its stream observes.
        ChargeFormScreen(onBackPressed = navController::popBackStackSafely)
    }
}

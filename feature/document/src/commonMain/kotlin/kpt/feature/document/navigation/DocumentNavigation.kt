/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.document.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.FeatureDestination
import kpt.core.base.ui.nav.composableWithPushTransitions
import kpt.core.base.ui.nav.popBackStackSafely
import kpt.feature.document.list.DocumentListScreen
import kpt.feature.document.upload.DocumentUploadScreen

/**
 * Documents for any Fineract entity. `entityType` is the API path segment — "clients", "groups",
 * "loans" — and `entityId` the entity.
 *
 * Neither has a default. The original's route defaulted both, so a missing argument produced a
 * screen that asked Fineract for documents of entity `-1` under an empty type.
 */
@Serializable
data class DocumentListRoute(val entityType: String, val entityId: Int)

/**
 * The upload / replace form. `documentId` is `-1` for a CREATE: a nav argument cannot be absent, so
 * the sentinel is how "no document yet" reaches the screen, and the ViewModel narrows it to `null`.
 */
@Serializable
data class DocumentUploadRoute(
    val entityType: String,
    val entityId: Int,
    val documentId: Int = CREATE_NEW,
) {
    companion object {
        const val CREATE_NEW = -1
    }
}

fun NavController.navigateToDocumentList(
    entityType: String,
    entityId: Int,
    navOptions: NavOptions? = null,
) = navigate(DocumentListRoute(entityType, entityId), navOptions)

fun NavController.navigateToDocumentUpload(
    entityType: String,
    entityId: Int,
    documentId: Int = DocumentUploadRoute.CREATE_NEW,
    navOptions: NavOptions? = null,
) = navigate(DocumentUploadRoute(entityType, entityId, documentId), navOptions)

/**
 * Annotated `@FeatureDestination`, so the generator registers these ONCE on the authenticated
 * graph — the same reasoning as `activateDestination`. Group detail opens the list today and
 * loan / savings will, so there is no single parent graph to nest it under, and registering it in
 * each would declare the route twice in one NavHost.
 */
@FeatureDestination
fun NavGraphBuilder.documentDestinations(navController: NavController) {
    composableWithPushTransitions<DocumentListRoute> {
        DocumentListScreen(
            onBackPressed = navController::popBackStackSafely,
            onUpload = { entityType, entityId ->
                navController.navigateToDocumentUpload(entityType, entityId)
            },
        )
    }

    composableWithPushTransitions<DocumentUploadRoute> {
        DocumentUploadScreen(
            onBackPressed = navController::popBackStackSafely,
            // Back to the list, which re-reads — the upload invalidates nothing itself.
            onSaved = navController::popBackStackSafely,
        )
    }
}

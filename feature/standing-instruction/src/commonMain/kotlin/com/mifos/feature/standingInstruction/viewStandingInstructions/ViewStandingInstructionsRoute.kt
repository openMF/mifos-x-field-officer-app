/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-x-field-officer-app/blob/master/LICENSE.md
 */
package com.mifos.feature.standingInstructions.viewStandingInstructions

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.mifos.feature.standingInstruction.viewStandingInstructions.ViewStandingInstructionsScreen
import kotlinx.serialization.Serializable

@Serializable
data class ViewStandingInstructionsScreenRoute(
    val clientId: Long,
    val clientName: String,
    val fromAccountId: Long,
    val fromAccountType: Int,
    val currencyCode: String,
)

fun NavController.navigateToViewStandingInstructionsScreen(
    clientId: Long,
    clientName: String,
    fromAccountId: Long,
    fromAccountType: Int,
    currencyCode: String,
) {
    navigate(
        ViewStandingInstructionsScreenRoute(
            clientId = clientId,
            clientName = clientName,
            fromAccountId = fromAccountId,
            fromAccountType = fromAccountType,
            currencyCode = currencyCode,
        ),
    )
}

fun NavGraphBuilder.viewStandingInstructionsScreen(
    onBackPressed: () -> Unit,
) {
    composable<ViewStandingInstructionsScreenRoute> {
        ViewStandingInstructionsScreen(
            navigateBack = onBackPressed,
        )
    }
}

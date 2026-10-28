/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-x-field-officer-app/blob/master/LICENSE.md
 */
package com.mifos.feature.savings.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mifos.core.common.utils.Constants
import com.mifos.feature.savings.savingsAccount.SavingsAccountScreen
import com.mifos.feature.savings.savingsAccountActivate.SavingsAccountActivateScreen
import com.mifos.feature.savings.savingsAccountApproval.SavingsAccountApprovalScreen
import com.mifos.feature.savings.savingsAccountSummary.SavingsAccountSummaryScreen
import com.mifos.feature.savings.savingsAccountTransaction.SavingsAccountTransactionScreen
import com.mifos.feature.savings.savingsAccountv2.savingsAccountDestination
import com.mifos.room.entities.accounts.savings.SavingAccountDepositTypeEntity
import com.mifos.room.entities.accounts.savings.SavingsAccountWithAssociationsEntity
import kotlinx.serialization.Serializable

fun NavGraphBuilder.savingsDestination(
    navController: NavController,
    onBackPressed: () -> Unit,
    onFinish: () -> Unit,
    loadMoreSavingsAccountInfo: (String, Int) -> Unit,
    loadDocuments: (Int, String) -> Unit,
    onViewStandingInstructions: (clientId: Long, clientName: String, accountId: Long, accountType: Int, currencyCode: String) -> Unit,
) {
    savingsSummaryScreen(
        onBackPressed = navController::popBackStack,
        loadMoreSavingsAccountInfo = {
            loadMoreSavingsAccountInfo(
                Constants.DATA_TABLE_NAME_SAVINGS,
                it,
            )
        },
        loadDocuments = { loadDocuments(it, Constants.ENTITY_TYPE_SAVINGS) },
        onDepositClick = { savingsAccountWithAssociations, depositType ->
            navController.navigateToSavingsAccountTransactionScreen(
                savingsAccountWithAssociations = savingsAccountWithAssociations,
                depositType = depositType,
                transactionType = Constants.SAVINGS_ACCOUNT_TRANSACTION_DEPOSIT,
            )
        },
        onWithdrawButtonClicked = { savingsAccountWithAssociations, depositType ->
            navController.navigateToSavingsAccountTransactionScreen(
                savingsAccountWithAssociations = savingsAccountWithAssociations,
                depositType = depositType,
                transactionType = Constants.SAVINGS_ACCOUNT_TRANSACTION_WITHDRAWAL,
            )
        },
        approveSavings = { _, accountNumber ->
            navController.navigateToSavingsAccountApproval(
                accountNumber,
            )
        },
        activateSavings = { _, accountNumber ->
            navController.navigateToSavingsAccountActivate(
                accountNumber,
            )
        },
        onViewStandingInstructions = onViewStandingInstructions,
    )

    addSavingsAccountScreen {
        onBackPressed()
    }

    savingsAccountActivateScreen {
        onBackPressed()
    }

    savingsAccountApprovalScreen {
        onBackPressed()
    }

    savingsAccountTransactionScreen {
        onBackPressed()
    }

    savingsAccountDestination(
        navController = navController,
        onNavigateBack = onBackPressed,
        onFinish = onFinish,
    )
}

fun NavGraphBuilder.addSavingsAccountScreen(
    onBackPressed: () -> Unit,
) {
    composable<SavingsAccountRoute> {
        SavingsAccountScreen(
            navigateBack = onBackPressed,
        )
    }
}

fun NavGraphBuilder.savingsSummaryScreen(
    onBackPressed: () -> Unit,
    loadMoreSavingsAccountInfo: (Int) -> Unit,
    loadDocuments: (Int) -> Unit,
    onDepositClick: (SavingsAccountWithAssociationsEntity, SavingAccountDepositTypeEntity?) -> Unit,
    onWithdrawButtonClicked: (SavingsAccountWithAssociationsEntity, SavingAccountDepositTypeEntity?) -> Unit,
    approveSavings: (savingsAccountType: SavingAccountDepositTypeEntity?, savingsAccountNumber: Int) -> Unit,
    activateSavings: (savingsAccountType: SavingAccountDepositTypeEntity?, savingsAccountNumber: Int) -> Unit,
    onViewStandingInstructions: (clientId: Long, clientName: String, accountId: Long, accountType: Int, currencyCode: String) -> Unit,
) {
    composable(
        route = SavingsScreens.SavingsAccountSummary.route,
        arguments = listOf(
            navArgument(name = "arg", builder = { type = NavType.StringType }),
        ),
    ) {
        SavingsAccountSummaryScreen(
            navigateBack = onBackPressed,
            loadMoreSavingsAccountInfo = loadMoreSavingsAccountInfo,
            loadDocuments = loadDocuments,
            onDepositClick = onDepositClick,
            onWithdrawButtonClicked = onWithdrawButtonClicked,
            approveSavings = approveSavings,
            activateSavings = activateSavings,
            onViewStandingInstructions = onViewStandingInstructions,
        )
    }
}

fun NavGraphBuilder.savingsAccountActivateScreen(
    onBackPressed: () -> Unit,
) {
    composable<SavingsAccountActivate> {
        SavingsAccountActivateScreen(
            navigateBack = onBackPressed,
        )
    }
}

fun NavGraphBuilder.savingsAccountApprovalScreen(
    onBackPressed: () -> Unit,
) {
    composable<SavingsAccountApproval> {
        SavingsAccountApprovalScreen(
            navigateBack = onBackPressed,
        )
    }
}

fun NavGraphBuilder.savingsAccountTransactionScreen(
    onBackPressed: () -> Unit,
) {
    composable(
        route = SavingsScreens.SavingsAccountTransaction.route,
        arguments = listOf(
            navArgument(name = "arg", builder = { type = NavType.StringType }),
        ),
    ) {
        SavingsAccountTransactionScreen(
            navigateBack = onBackPressed,
        )
    }
}

@Serializable
data class SavingsAccountRoute(
    val groupId: Int,
    val clientId: Int,
    val isGroupAccount: Boolean,
)

fun NavController.navigateToAddSavingsAccount(
    groupId: Int,
    clientId: Int,
    isGroupAccount: Boolean,
) {
    navigate(SavingsAccountRoute(groupId, clientId, isGroupAccount))
}

@Serializable
data class SavingsAccountApproval(
    val savingsAccountId: Int,
)

fun NavController.navigateToSavingsAccountApproval(savingsAccountId: Int) {
    navigate(SavingsAccountApproval(savingsAccountId))
}

@Serializable
data class SavingsAccountActivate(
    val savingsAccountId: Int,
)

fun NavController.navigateToSavingsAccountActivate(savingsAccountId: Int) {
    navigate(SavingsAccountActivate(savingsAccountId))
}

fun NavController.navigateToSavingsAccountSummaryScreen(
    id: Int,
    type: SavingAccountDepositTypeEntity,
) {
    navigate(SavingsScreens.SavingsAccountSummary.argument(id, type))
}

fun NavController.navigateToSavingsAccountTransactionScreen(
    savingsAccountWithAssociations: SavingsAccountWithAssociationsEntity,
    transactionType: String,
    depositType: SavingAccountDepositTypeEntity?,
) {
    navigate(
        SavingsScreens.SavingsAccountTransaction.argument(
            savingsAccountWithAssociations,
            transactionType,
            depositType,
        ),
    )
}

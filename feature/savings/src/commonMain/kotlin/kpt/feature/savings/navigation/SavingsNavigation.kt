/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.savings.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.FeatureDestination
import kpt.core.base.ui.nav.composableWithPushTransitions
import kpt.core.base.ui.nav.popBackStackSafely
import kpt.core.common.utils.Constants
import kpt.core.model.objects.charge.ChargeResource
import kpt.core.model.savings.SavingAccountDepositType
import kpt.feature.charge.navigation.navigateToCharges
import kpt.feature.datatable.navigation.navigateToDataTableList
import kpt.feature.document.navigation.navigateToDocumentList
import kpt.feature.savings.application.SavingsApplicationScreen
import kpt.feature.savings.recurring.RecurringDepositScreen
import kpt.feature.savings.statuschange.SavingsStatusChange
import kpt.feature.savings.statuschange.SavingsStatusChangeScreen
import kpt.feature.savings.summary.SavingsSummaryScreen
import kpt.feature.savings.transaction.SavingsTransactionScreen

/**
 * The savings vertical's destinations — ported from
 * `6b66e8a43:feature/savings/navigation/SavingsNavigation.kt`.
 *
 * ## Addressed by (type, id), not by id
 *
 * Fineract's savings endpoints take a path segment naming the account KIND
 * (`savingsaccounts` / `recurringdepositaccounts` / `fixeddepositaccounts`), so the same numeric id
 * addresses different accounts under different types. [SavingsSummaryRoute] carries both, which is
 * also what will let the recurring-deposit slice reuse this screen rather than clone it.
 *
 * The original passed a JSON-encoded `SavingsSummaryData` blob as a single `arg` string and decoded
 * it in the ViewModel's constructor with an uncaught `Json.decodeFromString` — a malformed or
 * truncated argument crashed before the screen existed. Two typed route fields instead.
 *
 * ## This file MUST live in a `navigation` package
 *
 * The destination generator calls `scan(roots, "navigation")`, so an annotated destination anywhere
 * else is silently skipped — a clean build and a screen that does not exist in the app.
 *
 * ## What is NOT here yet
 *
 * The create wizard (`savingsAccountv2`) and recurring deposit land later in S5. Approval,
 * activation and deposit/withdraw arrived in S5b, so the summary's action buttons are now live.
 *
 * @property accountType the account's kind (savings, fixed or recurring deposit)
 * @property accountId the Fineract account id
 */
@Serializable
data class SavingsSummaryRoute(val accountType: String, val accountId: Int)

/**
 * Approve or activate — ONE route, because it is one screen (see `SavingsStatusChangeViewModel`).
 *
 * [change] is a [SavingsStatusChange] name rather than a boolean, so adding a third status command
 * (reject, withdraw-by-applicant) is an enum entry and not a second route.
 *
 * @property accountId the Fineract account id
 * @property change the `SavingsStatusChange` name to apply
 */
@Serializable
data class SavingsStatusChangeRoute(val accountId: Int, val change: String)

/**
 * A new savings application, for a CLIENT or a GROUP.
 *
 * ONE owner id and ONE discriminator. The original's route was
 * `SavingsAccountRoute(groupId: Int, clientId: Int, isGroupAccount: Boolean)` and its ViewModel
 * read only `clientId` — so `groupId` and `isGroupAccount` were accepted by the route and
 * discarded, and a group application posted with no group. Two ids where one is always wrong is
 * what made that possible.
 *
 * @property ownerId the client or group opening the account
 * @property isGroup true when [ownerId] is a group
 */
@Serializable
data class SavingsApplicationRoute(val ownerId: Int, val isGroup: Boolean)

/**
 * A new RECURRING-DEPOSIT application, for a client.
 *
 * `clientId` only: the recurring-deposit template endpoint is keyed on the client alone, and the
 * original's own route carried only a client id too. It lives in `feature/savings` rather than its
 * own module because a recurring deposit is a savings-family account — see
 * `RecurringDepositViewModel`.
 *
 * @property clientId the client opening the account
 */
@Serializable
data class RecurringDepositApplicationRoute(val clientId: Int)

/** Deposit or withdraw. Carries the account TYPE, since Fineract's path segment names it. */
@Serializable
data class SavingsTransactionRoute(
    /** The account's kind (savings, fixed or recurring deposit). */
    val accountType: String,
    /** The Fineract account id. */
    val accountId: Int,
    /** The transaction to make — a `TransactionKind` name: deposit or withdrawal. */
    val kind: String,
)

/**
 * Open one savings account's summary.
 *
 * [accountType] defaults to the plain-savings endpoint via
 * [SavingAccountDepositType.ServerTypes], which already owns the id -> path-segment mapping
 * for all three deposit kinds. NOT `Constants.SAVINGS_ACCOUNT_TYPE` — despite the name that is a
 * SavedStateHandle KEY (`"savingsAccountType"`), not a Fineract path segment, and passing it would
 * request `/savingsAccountType/{id}`.
 *
 * A caller that knows the account's deposit type (the client's savings list, group detail) passes
 * `account.depositType.endpoint` so a recurring-deposit row addresses the right endpoint.
 */
fun NavController.navigateToSavingsSummary(
    accountId: Int,
    accountType: String = SavingAccountDepositType.ServerTypes.SAVINGS.endpoint,
    navOptions: NavOptions? = null,
) = navigate(SavingsSummaryRoute(accountType, accountId), navOptions)

/** Opens the savings application for a client or a group. */
fun NavController.navigateToSavingsApplication(
    ownerId: Int,
    isGroup: Boolean,
    navOptions: NavOptions? = null,
) = navigate(SavingsApplicationRoute(ownerId, isGroup), navOptions)

/** Opens the recurring-deposit application for a client. */
fun NavController.navigateToRecurringDepositApplication(
    clientId: Int,
    navOptions: NavOptions? = null,
) = navigate(RecurringDepositApplicationRoute(clientId), navOptions)

/** Opens the approval of savings account [accountId]. */
fun NavController.navigateToSavingsApproval(accountId: Int, navOptions: NavOptions? = null) =
    navigate(SavingsStatusChangeRoute(accountId, SavingsStatusChange.APPROVE.name), navOptions)

/** Opens the activation of savings account [accountId]. */
fun NavController.navigateToSavingsActivation(accountId: Int, navOptions: NavOptions? = null) =
    navigate(SavingsStatusChangeRoute(accountId, SavingsStatusChange.ACTIVATE.name), navOptions)

/** Registers every savings screen in the nav graph. */
@FeatureDestination
fun NavGraphBuilder.savingsDestination(navController: NavController) {
    composableWithPushTransitions<SavingsSummaryRoute> {
        SavingsSummaryScreen(
            onBackPressed = navController::popBackStackSafely,
            // The generic document list, scoped to this savings account — the same entity-agnostic
            // screen group detail and the loan profile open.
            onDocuments = { accountId ->
                navController.navigateToDocumentList(Constants.ENTITY_TYPE_SAVINGS, accountId)
            },
            // The ONE charge module, scoped to this account — `savingsaccounts/{id}/charges`,
            // which deposit accounts share (they are savings accounts underneath in Fineract).
            onCharges = { accountId -> navController.navigateToCharges(ChargeResource.SAVINGS, accountId) },
            // The account's datatables — the original's "More savings account info".
            onMoreInfo = { accountId ->
                navController.navigateToDataTableList(Constants.DATA_TABLE_NAME_SAVINGS, accountId)
            },
            onApprove = navController::navigateToSavingsApproval,
            onActivate = navController::navigateToSavingsActivation,
            onTransaction = { accountType, accountId, kind ->
                navController.navigate(SavingsTransactionRoute(accountType, accountId, kind.name))
            },
        )
    }

    composableWithPushTransitions<SavingsStatusChangeRoute> {
        SavingsStatusChangeScreen(onBackPressed = navController::popBackStackSafely)
    }

    composableWithPushTransitions<SavingsTransactionRoute> {
        SavingsTransactionScreen(onBackPressed = navController::popBackStackSafely)
    }

    composableWithPushTransitions<SavingsApplicationRoute> {
        SavingsApplicationScreen(onBackPressed = navController::popBackStackSafely)
    }

    composableWithPushTransitions<RecurringDepositApplicationRoute> {
        RecurringDepositScreen(onBackPressed = navController::popBackStackSafely)
    }
}

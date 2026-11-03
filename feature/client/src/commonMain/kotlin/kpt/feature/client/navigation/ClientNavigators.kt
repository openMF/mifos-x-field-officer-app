/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptions

/** Opens the client list. */
fun NavController.navigateToClientList(navOptions: NavOptions? = null) =
    navigate(ClientListRoute, navOptions)

/** Opens the clients graph (the tab's destination). */
fun NavController.navigateToClients(navOptions: NavOptions? = null) =
    navigate(ClientDestination, navOptions)

/** Opens client [clientId]'s profile. */
fun NavController.navigateToClientProfile(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientProfileRoute(clientId), navOptions)

/** Opens the assign-loan-officer screen for client [clientId]. */
fun NavController.navigateToClientStaff(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientStaffRoute(clientId), navOptions)

/** Opens the closure form for client [clientId]. */
fun NavController.navigateToClientClosure(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientClosureRoute(clientId), navOptions)

/** Opens the office-transfer form for client [clientId]. */
fun NavController.navigateToClientTransfer(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientTransferRoute(clientId), navOptions)

/** Opens the pledge-collateral form for client [clientId]. */
fun NavController.navigateToClientCollateral(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientCollateralRoute(clientId), navOptions)

/** Opens client [clientId]'s general view. */
fun NavController.navigateToClientGeneral(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientGeneralRoute(clientId), navOptions)

/** Opens client [clientId]'s identity documents. */
fun NavController.navigateToClientIdentifiers(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientIdentifiersRoute(clientId), navOptions)

/** Opens the apply-for-a-new-account chooser for client [clientId]. */
fun NavController.navigateToClientApplyNew(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientApplyNewRoute(clientId), navOptions)

/** Opens client [clientId]'s detail fields and actions. */
fun NavController.navigateToClientDetails(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientDetailsRoute(clientId), navOptions)

/** Opens client [clientId]'s addresses. */
fun NavController.navigateToClientAddress(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientAddressRoute(clientId), navOptions)

/** Opens the collateral client [clientId] has pledged. */
fun NavController.navigateToClientCollateralDetails(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientCollateralDetailsRoute(clientId), navOptions)

/** Opens client [clientId]'s loan accounts. */
fun NavController.navigateToClientLoanAccounts(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientLoanAccountsRoute(clientId), navOptions)

/** Opens the new-client form. */
fun NavController.navigateToCreateNewClient(navOptions: NavOptions? = null) =
    navigate(CreateNewClientRoute, navOptions)

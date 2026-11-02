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

fun NavController.navigateToClientList(navOptions: NavOptions? = null) =
    navigate(ClientListRoute, navOptions)

fun NavController.navigateToClients(navOptions: NavOptions? = null) =
    navigate(ClientDestination, navOptions)

fun NavController.navigateToClientProfile(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientProfileRoute(clientId), navOptions)

fun NavController.navigateToClientStaff(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientStaffRoute(clientId), navOptions)

fun NavController.navigateToClientClosure(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientClosureRoute(clientId), navOptions)

fun NavController.navigateToClientTransfer(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientTransferRoute(clientId), navOptions)

fun NavController.navigateToClientCollateral(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientCollateralRoute(clientId), navOptions)

fun NavController.navigateToClientGeneral(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientGeneralRoute(clientId), navOptions)

fun NavController.navigateToClientIdentifiers(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientIdentifiersRoute(clientId), navOptions)

fun NavController.navigateToClientApplyNew(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientApplyNewRoute(clientId), navOptions)

fun NavController.navigateToClientDetails(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientDetailsRoute(clientId), navOptions)

fun NavController.navigateToClientAddress(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientAddressRoute(clientId), navOptions)

fun NavController.navigateToClientCollateralDetails(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientCollateralDetailsRoute(clientId), navOptions)

fun NavController.navigateToClientLoanAccounts(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientLoanAccountsRoute(clientId), navOptions)

fun NavController.navigateToCreateNewClient(navOptions: NavOptions? = null) =
    navigate(CreateNewClientRoute, navOptions)

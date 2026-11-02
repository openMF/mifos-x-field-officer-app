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
import kpt.feature.client.depositaccounts.DepositAccountKind

fun NavController.navigateToClientEditDetails(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientEditDetailsRoute(clientId), navOptions)

fun NavController.navigateToClientEditProfile(
    clientId: Int,
    clientName: String,
    accountNo: String,
    navOptions: NavOptions? = null,
) = navigate(ClientEditProfileRoute(clientId, clientName, accountNo), navOptions)

fun NavController.navigateToClientDefaultAccount(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientDefaultAccountRoute(clientId), navOptions)

fun NavController.navigateToClientAddDocument(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientAddDocumentRoute(clientId), navOptions)

fun NavController.navigateToClientAddAddress(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientAddAddressRoute(clientId), navOptions)

fun NavController.navigateToClientAddIdentifier(clientId: Long, navOptions: NavOptions? = null) =
    navigate(ClientAddIdentifierRoute(clientId), navOptions)

fun NavController.navigateToClientShareAccounts(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientShareAccountsRoute(clientId), navOptions)

fun NavController.navigateToShareApplication(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ShareApplicationRoute(clientId), navOptions)

fun NavController.navigateToFixedDepositApplication(clientId: Int, navOptions: NavOptions? = null) =
    navigate(FixedDepositApplicationRoute(clientId), navOptions)

fun NavController.navigateToClientDepositAccounts(
    clientId: Int,
    kind: DepositAccountKind,
    navOptions: NavOptions? = null,
) = navigate(ClientDepositAccountsRoute(clientId, kind.name), navOptions)

fun NavController.navigateToClientSignature(
    clientId: Int,
    clientName: String,
    accountNo: String,
    navOptions: NavOptions? = null,
) = navigate(ClientSignatureRoute(clientId, clientName, accountNo), navOptions)

fun NavController.navigateToClientDocuments(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientDocumentsRoute(clientId), navOptions)

fun NavController.navigateToClientDocumentPreview(route: ClientDocumentPreviewRoute) = navigate(route)

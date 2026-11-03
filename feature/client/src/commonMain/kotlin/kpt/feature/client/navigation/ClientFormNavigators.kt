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

/** Opens the edit form for client [clientId]'s details. */
fun NavController.navigateToClientEditDetails(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientEditDetailsRoute(clientId), navOptions)

/** Opens the profile-photo screen for a client. */
fun NavController.navigateToClientEditProfile(
    clientId: Int,
    clientName: String,
    accountNo: String,
    navOptions: NavOptions? = null,
) = navigate(ClientEditProfileRoute(clientId, clientName, accountNo), navOptions)

/** Opens the default-savings-account picker for client [clientId]. */
fun NavController.navigateToClientDefaultAccount(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientDefaultAccountRoute(clientId), navOptions)

/** Opens the attach-document form for client [clientId]. */
fun NavController.navigateToClientAddDocument(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientAddDocumentRoute(clientId), navOptions)

/** Opens the add-address form for client [clientId]. */
fun NavController.navigateToClientAddAddress(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientAddAddressRoute(clientId), navOptions)

/** Opens the add-identifier form for client [clientId]. */
fun NavController.navigateToClientAddIdentifier(clientId: Long, navOptions: NavOptions? = null) =
    navigate(ClientAddIdentifierRoute(clientId), navOptions)

/** Opens client [clientId]'s share accounts. */
fun NavController.navigateToClientShareAccounts(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientShareAccountsRoute(clientId), navOptions)

/** Opens the share-account application for client [clientId]. */
fun NavController.navigateToShareApplication(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ShareApplicationRoute(clientId), navOptions)

/** Opens the fixed-deposit application for client [clientId]. */
fun NavController.navigateToFixedDepositApplication(clientId: Int, navOptions: NavOptions? = null) =
    navigate(FixedDepositApplicationRoute(clientId), navOptions)

/** Opens a client's deposit accounts of one kind. */
fun NavController.navigateToClientDepositAccounts(
    clientId: Int,
    kind: DepositAccountKind,
    navOptions: NavOptions? = null,
) = navigate(ClientDepositAccountsRoute(clientId, kind.name), navOptions)

/** Opens the signature screen for a client. */
fun NavController.navigateToClientSignature(
    clientId: Int,
    clientName: String,
    accountNo: String,
    navOptions: NavOptions? = null,
) = navigate(ClientSignatureRoute(clientId, clientName, accountNo), navOptions)

/** Opens client [clientId]'s attached documents. */
fun NavController.navigateToClientDocuments(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ClientDocumentsRoute(clientId), navOptions)

/** Opens one saved document in the in-app viewer. */
fun NavController.navigateToClientDocumentPreview(route: ClientDocumentPreviewRoute) = navigate(route)

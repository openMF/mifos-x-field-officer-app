/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.di

import kpt.feature.client.addaddress.ClientAddAddressViewModel
import kpt.feature.client.adddocument.ClientAddDocumentViewModel
import kpt.feature.client.addidentifier.ClientAddIdentifierViewModel
import kpt.feature.client.address.ClientAddressViewModel
import kpt.feature.client.applynew.ClientApplyNewViewModel
import kpt.feature.client.closure.ClientClosureViewModel
import kpt.feature.client.collateral.ClientCollateralViewModel
import kpt.feature.client.collateraldetails.ClientCollateralDetailsViewModel
import kpt.feature.client.createnew.CreateNewClientViewModel
import kpt.feature.client.defaultaccount.ClientDefaultAccountViewModel
import kpt.feature.client.depositaccounts.ClientDepositAccountsViewModel
import kpt.feature.client.details.ClientDetailsViewModel
import kpt.feature.client.documentpreview.ClientDocumentPreviewViewModel
import kpt.feature.client.documents.ClientDocumentsViewModel
import kpt.feature.client.editdetails.ClientEditDetailsViewModel
import kpt.feature.client.editprofile.ClientEditProfileViewModel
import kpt.feature.client.fixeddepositapplication.FixedDepositApplicationViewModel
import kpt.feature.client.general.ClientGeneralViewModel
import kpt.feature.client.identifiers.ClientIdentifiersViewModel
import kpt.feature.client.list.ClientListViewModel
import kpt.feature.client.loanaccounts.ClientLoanAccountsViewModel
import kpt.feature.client.profile.ClientProfileViewModel
import kpt.feature.client.shareaccounts.ClientShareAccountsViewModel
import kpt.feature.client.shareapplication.ShareApplicationViewModel
import kpt.feature.client.signature.ClientSignatureViewModel
import kpt.feature.client.staff.ClientStaffViewModel
import kpt.feature.client.transfer.ClientTransferViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Collected into `GeneratedFeatureKoinBindings` by `:cmp-navigation:generateFeatureKoinBindings`,
 * which scans each feature module's `di` package — no registry edit (spec §4).
 */
val ClientModule = module {
    viewModelOf(::ClientApplyNewViewModel)
    viewModelOf(::ClientListViewModel)
    viewModelOf(::ClientProfileViewModel)
    viewModelOf(::ClientIdentifiersViewModel)
    viewModelOf(::ClientStaffViewModel)
    viewModelOf(::ClientClosureViewModel)
    viewModelOf(::ClientTransferViewModel)
    viewModelOf(::ClientCollateralViewModel)
    viewModelOf(::ClientGeneralViewModel)
    viewModelOf(::ClientDetailsViewModel)
    viewModelOf(::ClientAddressViewModel)
    viewModelOf(::ClientDocumentsViewModel)
    viewModelOf(::ClientDocumentPreviewViewModel)
    viewModelOf(::ClientCollateralDetailsViewModel)
    viewModelOf(::ClientSignatureViewModel)
    viewModelOf(::ClientLoanAccountsViewModel)
    viewModelOf(::ClientDepositAccountsViewModel)
    viewModelOf(::CreateNewClientViewModel)
    viewModelOf(::ClientEditDetailsViewModel)
    viewModelOf(::ClientEditProfileViewModel)
    viewModelOf(::ClientDefaultAccountViewModel)
    viewModelOf(::ClientAddDocumentViewModel)
    viewModelOf(::ClientAddAddressViewModel)
    viewModelOf(::ClientAddIdentifierViewModel)
    viewModelOf(::ClientShareAccountsViewModel)
    viewModelOf(::ShareApplicationViewModel)
    viewModelOf(::FixedDepositApplicationViewModel)
}

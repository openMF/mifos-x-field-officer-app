/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-x-field-officer-app/blob/master/LICENSE.md
 */
package com.mifos.core.data.di

import com.mifos.core.common.network.MifosDispatchers
import com.mifos.core.data.datasource.SearchRecordLocalDataSource
import com.mifos.core.data.datasource.SearchRecordLocalDataSourceImpl
import com.mifos.core.data.repository.ActivateRepository
import com.mifos.core.data.repository.AmountTransferRepository
import com.mifos.core.data.repository.AppLockRepository
import com.mifos.core.data.repository.CenterDetailsRepository
import com.mifos.core.data.repository.CenterListRepository
import com.mifos.core.data.repository.ChargeRepository
import com.mifos.core.data.repository.CheckerInboxRepository
import com.mifos.core.data.repository.CheckerInboxTasksRepository
import com.mifos.core.data.repository.ClientDetailsEditRepository
import com.mifos.core.data.repository.ClientDetailsRepository
import com.mifos.core.data.repository.ClientIdentifiersRepository
import com.mifos.core.data.repository.ClientListRepository
import com.mifos.core.data.repository.CreateNewCenterRepository
import com.mifos.core.data.repository.CreateNewClientRepository
import com.mifos.core.data.repository.CreateNewGroupRepository
import com.mifos.core.data.repository.DataTableDataRepository
import com.mifos.core.data.repository.DataTableListRepository
import com.mifos.core.data.repository.DataTableRepository
import com.mifos.core.data.repository.DataTableRowDialogRepository
import com.mifos.core.data.repository.DocumentCreateUpdateRepository
import com.mifos.core.data.repository.DocumentListRepository
import com.mifos.core.data.repository.FixedDepositRepository
import com.mifos.core.data.repository.GenerateCollectionSheetRepository
import com.mifos.core.data.repository.GroupDetailsRepository
import com.mifos.core.data.repository.GroupListRepository
import com.mifos.core.data.repository.GroupLoanAccountRepository
import com.mifos.core.data.repository.GroupsListRepository
import com.mifos.core.data.repository.IndividualCollectionSheetDetailsRepository
import com.mifos.core.data.repository.LoanAccountGeneralRepository
import com.mifos.core.data.repository.LoanRejectRepository
import com.mifos.core.data.repository.LoginRepository
import com.mifos.core.data.repository.NewIndividualCollectionSheetRepository
import com.mifos.core.data.repository.NoteRepository
import com.mifos.core.data.repository.OfflineDashboardRepository
import com.mifos.core.data.repository.PathTrackingRepository
import com.mifos.core.data.repository.PinPointClientRepository
import com.mifos.core.data.repository.RecurringAccountRepository
import com.mifos.core.data.repository.ReportCategoryRepository
import com.mifos.core.data.repository.ReportDetailRepository
import com.mifos.core.data.repository.SavingsAccountActivateRepository
import com.mifos.core.data.repository.SavingsAccountApprovalRepository
import com.mifos.core.data.repository.SavingsAccountRepository
import com.mifos.core.data.repository.SavingsAccountSummaryRepository
import com.mifos.core.data.repository.SavingsAccountTransactionReceiptRepository
import com.mifos.core.data.repository.SavingsAccountTransactionRepository
import com.mifos.core.data.repository.SearchRecordRepository
import com.mifos.core.data.repository.SearchRepository
import com.mifos.core.data.repository.ShareAccountRepository
import com.mifos.core.data.repository.SignatureRepository
import com.mifos.core.data.repository.StandingInstructionsRepository
import com.mifos.core.data.repository.SurveyListRepository
import com.mifos.core.data.repository.SurveySubmitRepository
import com.mifos.core.data.repository.SyncCenterPayloadsRepository
import com.mifos.core.data.repository.SyncCentersDialogRepository
import com.mifos.core.data.repository.SyncClientPayloadsRepository
import com.mifos.core.data.repository.SyncClientsDialogRepository
import com.mifos.core.data.repository.SyncGroupPayloadsRepository
import com.mifos.core.data.repository.SyncGroupsDialogRepository
import com.mifos.core.data.repository.SyncLoanRepaymentTransactionRepository
import com.mifos.core.data.repository.SyncSavingsAccountTransactionRepository
import com.mifos.core.data.repository.UserVerificationRepository
import com.mifos.core.data.repository.loan.LoanAccountApprovalRepository
import com.mifos.core.data.repository.loan.LoanAccountRepository
import com.mifos.core.data.repository.loan.LoanAccountSummaryRepository
import com.mifos.core.data.repository.loan.LoanChargeFormRepository
import com.mifos.core.data.repository.loan.LoanChargeOffRepository
import com.mifos.core.data.repository.loan.LoanChargeRepository
import com.mifos.core.data.repository.loan.LoanCreateGuarantorRepository
import com.mifos.core.data.repository.loan.LoanDisburseRepository
import com.mifos.core.data.repository.loan.LoanOfficerRepository
import com.mifos.core.data.repository.loan.LoanRepaymentRepository
import com.mifos.core.data.repository.loan.LoanRepaymentScheduleRepository
import com.mifos.core.data.repository.loan.LoanReschedulesRepository
import com.mifos.core.data.repository.loan.LoanTransactionsRepository
import com.mifos.core.data.repositoryImp.ActivateRepositoryImp
import com.mifos.core.data.repositoryImp.AmountTransferRepositoryImp
import com.mifos.core.data.repositoryImp.AppLockRepositoryImpl
import com.mifos.core.data.repositoryImp.BiometricStorageAdapterImpl
import com.mifos.core.data.repositoryImp.CenterDetailsRepositoryImp
import com.mifos.core.data.repositoryImp.CenterListRepositoryImp
import com.mifos.core.data.repositoryImp.ChargeRepositoryImp
import com.mifos.core.data.repositoryImp.CheckerInboxRepositoryImp
import com.mifos.core.data.repositoryImp.CheckerInboxTasksRepositoryImp
import com.mifos.core.data.repositoryImp.ClientDetailsEditRepositoryImpl
import com.mifos.core.data.repositoryImp.ClientDetailsRepositoryImp
import com.mifos.core.data.repositoryImp.ClientIdentifiersRepositoryImp
import com.mifos.core.data.repositoryImp.ClientListRepositoryImp
import com.mifos.core.data.repositoryImp.CreateNewCenterRepositoryImp
import com.mifos.core.data.repositoryImp.CreateNewClientRepositoryImp
import com.mifos.core.data.repositoryImp.CreateNewGroupRepositoryImp
import com.mifos.core.data.repositoryImp.DataTableDataRepositoryImp
import com.mifos.core.data.repositoryImp.DataTableListRepositoryImp
import com.mifos.core.data.repositoryImp.DataTableRepositoryImp
import com.mifos.core.data.repositoryImp.DataTableRowDialogRepositoryImp
import com.mifos.core.data.repositoryImp.DocumentCreateUpdateRepositoryImp
import com.mifos.core.data.repositoryImp.DocumentListRepositoryImp
import com.mifos.core.data.repositoryImp.FixedDepositRepositoryImpl
import com.mifos.core.data.repositoryImp.GenerateCollectionSheetRepositoryImp
import com.mifos.core.data.repositoryImp.GroupDetailsRepositoryImp
import com.mifos.core.data.repositoryImp.GroupListRepositoryImp
import com.mifos.core.data.repositoryImp.GroupLoanAccountRepositoryImp
import com.mifos.core.data.repositoryImp.GroupsListRepositoryImpl
import com.mifos.core.data.repositoryImp.IndividualCollectionSheetDetailsRepositoryImp
import com.mifos.core.data.repositoryImp.LoanAccountGeneralRepositoryImp
import com.mifos.core.data.repositoryImp.LoginRepositoryImp
import com.mifos.core.data.repositoryImp.NewIndividualCollectionSheetRepositoryImp
import com.mifos.core.data.repositoryImp.NoteRepositoryImp
import com.mifos.core.data.repositoryImp.OfflineDashboardRepositoryImp
import com.mifos.core.data.repositoryImp.PasscodeStorageAdapterImpl
import com.mifos.core.data.repositoryImp.PathTrackingRepositoryImp
import com.mifos.core.data.repositoryImp.PinPointClientRepositoryImp
import com.mifos.core.data.repositoryImp.RecurringAccountRepositoryImp
import com.mifos.core.data.repositoryImp.ReportCategoryRepositoryImp
import com.mifos.core.data.repositoryImp.ReportDetailRepositoryImp
import com.mifos.core.data.repositoryImp.SavingsAccountActivateRepositoryImp
import com.mifos.core.data.repositoryImp.SavingsAccountApprovalRepositoryImp
import com.mifos.core.data.repositoryImp.SavingsAccountRepositoryImp
import com.mifos.core.data.repositoryImp.SavingsAccountSummaryRepositoryImp
import com.mifos.core.data.repositoryImp.SavingsAccountTransactionReceiptRepositoryImpl
import com.mifos.core.data.repositoryImp.SavingsAccountTransactionRepositoryImp
import com.mifos.core.data.repositoryImp.SearchRecordRepositoryImpl
import com.mifos.core.data.repositoryImp.SearchRepositoryImp
import com.mifos.core.data.repositoryImp.ShareAccountRepositoryImpl
import com.mifos.core.data.repositoryImp.SignatureRepositoryImp
import com.mifos.core.data.repositoryImp.StandingInstructionsRepositoryImp
import com.mifos.core.data.repositoryImp.SurveyListRepositoryImp
import com.mifos.core.data.repositoryImp.SurveySubmitRepositoryImp
import com.mifos.core.data.repositoryImp.SyncCenterPayloadsRepositoryImp
import com.mifos.core.data.repositoryImp.SyncCentersDialogRepositoryImp
import com.mifos.core.data.repositoryImp.SyncClientPayloadsRepositoryImp
import com.mifos.core.data.repositoryImp.SyncClientsDialogRepositoryImp
import com.mifos.core.data.repositoryImp.SyncGroupPayloadsRepositoryImp
import com.mifos.core.data.repositoryImp.SyncGroupsDialogRepositoryImp
import com.mifos.core.data.repositoryImp.SyncLoanRepaymentTransactionRepositoryImp
import com.mifos.core.data.repositoryImp.SyncSavingsAccountTransactionRepositoryImp
import com.mifos.core.data.repositoryImp.UserVerificationRepositoryImpl
import com.mifos.core.data.repositoryImp.loan.LoanAccountApprovalRepositoryImp
import com.mifos.core.data.repositoryImp.loan.LoanAccountRepositoryImp
import com.mifos.core.data.repositoryImp.loan.LoanAccountSummaryRepositoryImp
import com.mifos.core.data.repositoryImp.loan.LoanChargeFormRepositoryImp
import com.mifos.core.data.repositoryImp.loan.LoanChargeOffRepositoryImpl
import com.mifos.core.data.repositoryImp.loan.LoanChargeRepositoryImp
import com.mifos.core.data.repositoryImp.loan.LoanCreateGuarantorRepositoryImp
import com.mifos.core.data.repositoryImp.loan.LoanDisburseRepositoryImpl
import com.mifos.core.data.repositoryImp.loan.LoanOfficerRepositoryImpl
import com.mifos.core.data.repositoryImp.loan.LoanRejectRepositoryImpl
import com.mifos.core.data.repositoryImp.loan.LoanRepaymentRepositoryImp
import com.mifos.core.data.repositoryImp.loan.LoanRepaymentScheduleRepositoryImp
import com.mifos.core.data.repositoryImp.loan.LoanReschedulesRepositoryImpl
import com.mifos.core.data.repositoryImp.loan.LoanTransactionsRepositoryImp
import com.mifos.core.data.util.NetworkMonitor
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.mifos.authenticator.biometrics.BiometricStorageAdapter
import org.mifos.authenticator.passcode.PasscodeStorageAdapter

val RepositoryModule = module {
    single<CoroutineDispatcher> { get(named(MifosDispatchers.IO.name)) }

    singleOf(::LoginRepositoryImp) bind LoginRepository::class
    singleOf(::SearchRepositoryImp) bind SearchRepository::class

    // Client
    singleOf(::ClientDetailsRepositoryImp) bind ClientDetailsRepository::class
    singleOf(::ClientListRepositoryImp) bind ClientListRepository::class
    singleOf(::ChargeRepositoryImp) bind ChargeRepository::class
    singleOf(::ClientIdentifiersRepositoryImp) bind ClientIdentifiersRepository::class
    singleOf(::CreateNewClientRepositoryImp) bind CreateNewClientRepository::class
    singleOf(::ClientDetailsEditRepositoryImpl) bind ClientDetailsEditRepository::class
    singleOf(::PinPointClientRepositoryImp) bind PinPointClientRepository::class

    // Center
    singleOf(::CenterDetailsRepositoryImp) bind CenterDetailsRepository::class
    singleOf(::CenterListRepositoryImp) bind CenterListRepository::class
    singleOf(::CreateNewCenterRepositoryImp) bind CreateNewCenterRepository::class
    singleOf(::GroupsListRepositoryImpl) bind GroupsListRepository::class

    // Group
    singleOf(::GroupDetailsRepositoryImp) bind GroupDetailsRepository::class
    singleOf(::GroupListRepositoryImp) bind GroupListRepository::class
    singleOf(::GroupLoanAccountRepositoryImp) bind GroupLoanAccountRepository::class
    singleOf(::CreateNewGroupRepositoryImp) bind CreateNewGroupRepository::class

    // Loan
    singleOf(::LoanAccountRepositoryImp) bind LoanAccountRepository::class
    singleOf(::LoanAccountApprovalRepositoryImp) bind LoanAccountApprovalRepository::class
    singleOf(::LoanAccountGeneralRepositoryImp) bind LoanAccountGeneralRepository::class
    singleOf(::LoanAccountSummaryRepositoryImp) bind LoanAccountSummaryRepository::class
    singleOf(::LoanChargeFormRepositoryImp) bind LoanChargeFormRepository::class
    singleOf(::LoanChargeRepositoryImp) bind LoanChargeRepository::class
    singleOf(::LoanRepaymentRepositoryImp) bind LoanRepaymentRepository::class
    singleOf(::LoanRepaymentScheduleRepositoryImp) bind LoanRepaymentScheduleRepository::class
    singleOf(::LoanTransactionsRepositoryImp) bind LoanTransactionsRepository::class
    singleOf(::LoanReschedulesRepositoryImpl) bind LoanReschedulesRepository::class
    singleOf(::LoanChargeOffRepositoryImpl) bind LoanChargeOffRepository::class
    singleOf(::LoanDisburseRepositoryImpl) bind LoanDisburseRepository::class
    singleOf(::LoanCreateGuarantorRepositoryImp) bind LoanCreateGuarantorRepository::class
    singleOf(::LoanOfficerRepositoryImpl) bind LoanOfficerRepository::class
    singleOf(::LoanRejectRepositoryImpl) bind LoanRejectRepository::class

    // Account Transfer
    singleOf(::AmountTransferRepositoryImp) bind AmountTransferRepository::class

    // Savings
    singleOf(::SavingsAccountRepositoryImp) bind SavingsAccountRepository::class
    singleOf(::SavingsAccountActivateRepositoryImp) bind SavingsAccountActivateRepository::class
    singleOf(::SavingsAccountApprovalRepositoryImp) bind SavingsAccountApprovalRepository::class
    singleOf(::SavingsAccountSummaryRepositoryImp) bind SavingsAccountSummaryRepository::class
    singleOf(::SavingsAccountTransactionRepositoryImp) bind SavingsAccountTransactionRepository::class
    singleOf(::SavingsAccountTransactionReceiptRepositoryImpl) bind SavingsAccountTransactionReceiptRepository::class

    // Standing Instructions
    singleOf(::StandingInstructionsRepositoryImp) bind StandingInstructionsRepository::class

    // Sync
    singleOf(::SyncCenterPayloadsRepositoryImp) bind SyncCenterPayloadsRepository::class
    singleOf(::SyncCentersDialogRepositoryImp) bind SyncCentersDialogRepository::class
    singleOf(::SyncClientPayloadsRepositoryImp) bind SyncClientPayloadsRepository::class
    singleOf(::SyncClientsDialogRepositoryImp) bind SyncClientsDialogRepository::class
    singleOf(::SyncGroupPayloadsRepositoryImp) bind SyncGroupPayloadsRepository::class
    singleOf(::SyncGroupsDialogRepositoryImp) bind SyncGroupsDialogRepository::class
    singleOf(::SyncLoanRepaymentTransactionRepositoryImp) bind SyncLoanRepaymentTransactionRepository::class
    singleOf(::SyncSavingsAccountTransactionRepositoryImp) bind SyncSavingsAccountTransactionRepository::class

    // Others
    singleOf(::ActivateRepositoryImp) bind ActivateRepository::class
    singleOf(::CheckerInboxRepositoryImp) bind CheckerInboxRepository::class
    singleOf(::CheckerInboxTasksRepositoryImp) bind CheckerInboxTasksRepository::class
    singleOf(::DataTableDataRepositoryImp) bind DataTableDataRepository::class
    singleOf(::DataTableListRepositoryImp) bind DataTableListRepository::class
    singleOf(::DataTableRepositoryImp) bind DataTableRepository::class
    singleOf(::DataTableRowDialogRepositoryImp) bind DataTableRowDialogRepository::class
    singleOf(::DocumentCreateUpdateRepositoryImp) bind DocumentCreateUpdateRepository::class
    singleOf(::DocumentListRepositoryImp) bind DocumentListRepository::class
    singleOf(::IndividualCollectionSheetDetailsRepositoryImp) bind IndividualCollectionSheetDetailsRepository::class
    singleOf(::NewIndividualCollectionSheetRepositoryImp) bind NewIndividualCollectionSheetRepository::class
    singleOf(::GenerateCollectionSheetRepositoryImp) bind GenerateCollectionSheetRepository::class
    singleOf(::NoteRepositoryImp) bind NoteRepository::class
    singleOf(::OfflineDashboardRepositoryImp) bind OfflineDashboardRepository::class
    singleOf(::PathTrackingRepositoryImp) bind PathTrackingRepository::class
    singleOf(::ReportCategoryRepositoryImp) bind ReportCategoryRepository::class
    singleOf(::ReportDetailRepositoryImp) bind ReportDetailRepository::class
    singleOf(::SearchRepositoryImp) bind SearchRepository::class
    singleOf(::SignatureRepositoryImp) bind SignatureRepository::class
    singleOf(::SurveyListRepositoryImp) bind SurveyListRepository::class
    singleOf(::SurveySubmitRepositoryImp) bind SurveySubmitRepository::class
    singleOf(::SignatureRepositoryImp) bind SignatureRepository::class
    singleOf(::SearchRecordRepositoryImpl) bind SearchRecordRepository::class
    singleOf(::SearchRecordLocalDataSourceImpl) bind SearchRecordLocalDataSource::class

    singleOf(::RecurringAccountRepositoryImp) bind RecurringAccountRepository::class
    singleOf(::ShareAccountRepositoryImpl) bind ShareAccountRepository::class
    singleOf(::FixedDepositRepositoryImpl) bind FixedDepositRepository::class

    singleOf(::AppLockRepositoryImpl) bind(AppLockRepository::class)
    singleOf(::PasscodeStorageAdapterImpl) bind(PasscodeStorageAdapter::class)
    singleOf(::BiometricStorageAdapterImpl) bind(BiometricStorageAdapter::class)
    singleOf(::UserVerificationRepositoryImpl) bind(UserVerificationRepository::class)

    includes(platformModule)
    single<PlatformDependentDataModule> { getPlatformDataModule }
    single<NetworkMonitor> { getPlatformDataModule.networkMonitor }
}

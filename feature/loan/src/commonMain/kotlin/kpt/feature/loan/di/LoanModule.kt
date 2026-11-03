/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.loan.di

import kpt.feature.loan.approval.LoanApprovalViewModel
import kpt.feature.loan.disbursement.LoanDisbursementViewModel
import kpt.feature.loan.grouploan.GroupLoanApplicationViewModel
import kpt.feature.loan.newloan.NewLoanApplicationViewModel
import kpt.feature.loan.profile.LoanProfileViewModel
import kpt.feature.loan.repayment.LoanRepaymentViewModel
import kpt.feature.loan.rescheduleform.LoanRescheduleFormViewModel
import kpt.feature.loan.reschedules.LoanReschedulesViewModel
import kpt.feature.loan.schedule.LoanScheduleViewModel
import kpt.feature.loan.summary.LoanSummaryViewModel
import kpt.feature.loan.transactions.LoanTransactionsViewModel
import kpt.feature.loan.transfer.LoanTransferViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Collected into `GeneratedFeatureKoinBindings` by `:cmp-navigation:generateFeatureKoinBindings`,
 * which scans each feature module's `di` package — no registry edit (spec §4).
 */
val LoanModule = module {
    viewModelOf(::LoanProfileViewModel)
    viewModelOf(::LoanScheduleViewModel)
    viewModelOf(::LoanTransactionsViewModel)
    viewModelOf(::LoanApprovalViewModel)
    viewModelOf(::LoanRepaymentViewModel)
    viewModelOf(::LoanSummaryViewModel)
    viewModelOf(::LoanDisbursementViewModel)
    viewModelOf(::LoanTransferViewModel)
    viewModelOf(::LoanReschedulesViewModel)
    viewModelOf(::LoanRescheduleFormViewModel)
    viewModelOf(::GroupLoanApplicationViewModel)
    viewModelOf(::NewLoanApplicationViewModel)
}

/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.savings.di

import kpt.feature.savings.application.SavingsApplicationViewModel
import kpt.feature.savings.recurring.RecurringDepositViewModel
import kpt.feature.savings.statuschange.SavingsStatusChangeViewModel
import kpt.feature.savings.summary.SavingsSummaryViewModel
import kpt.feature.savings.transaction.SavingsTransactionViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Collected into `GeneratedFeatureKoinBindings` by `:cmp-navigation:generateFeatureKoinBindings`,
 * which scans each feature module's `di` package — no registry edit (spec §4).
 */
val SavingsModule = module {
    viewModelOf(::SavingsSummaryViewModel)
    viewModelOf(::SavingsStatusChangeViewModel)
    viewModelOf(::SavingsTransactionViewModel)
    viewModelOf(::SavingsApplicationViewModel)
    viewModelOf(::RecurringDepositViewModel)
}

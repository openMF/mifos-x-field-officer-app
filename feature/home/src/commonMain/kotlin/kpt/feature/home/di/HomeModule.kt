/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.home.di

import kpt.feature.home.dashboard.HomeViewModel
import kpt.feature.home.dashboard.LoanQueueViewModel
import kpt.feature.home.search.SearchViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Koin bindings for the home feature's view models: dashboard, search and loan queue. */
val HomeModule = module {
    viewModelOf(::SearchViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::LoanQueueViewModel)
}

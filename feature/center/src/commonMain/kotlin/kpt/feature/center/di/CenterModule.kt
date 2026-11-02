/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.center.di

import kpt.feature.center.create.CreateCenterViewModel
import kpt.feature.center.detail.CenterDetailViewModel
import kpt.feature.center.list.CenterListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Collected into `GeneratedFeatureKoinBindings` by `:cmp-navigation:generateFeatureKoinBindings`,
 * which scans each feature module's `di` package — no registry edit.
 */
val CenterModule = module {
    viewModel { CenterListViewModel(repository = get(), downloads = get()) }
    viewModelOf(::CenterDetailViewModel)
    viewModelOf(::CreateCenterViewModel)
}

/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.groups.di

import kpt.feature.groups.create.CreateGroupViewModel
import kpt.feature.groups.detail.GroupsDetailViewModel
import kpt.feature.groups.list.GroupsListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Collected into `GeneratedFeatureKoinBindings` by `:cmp-navigation:generateFeatureKoinBindings`,
 * which scans each feature module's `di` package — no registry edit.
 */
val GroupsModule = module {
    viewModel { GroupsListViewModel(repository = get(), downloads = get()) }
    viewModelOf(::CreateGroupViewModel)
    viewModelOf(::GroupsDetailViewModel)
}

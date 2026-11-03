/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.offlinesync.di

import kpt.feature.offlinesync.ui.OfflineSyncViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** Collected by `:cmp-navigation:generateFeatureKoinBindings` — the `di` package is load-bearing. */
val OfflineSyncModule = module {
    viewModel {
        OfflineSyncViewModel(
            repository = get(),
            centers = get(),
            groups = get(),
            clients = get(),
            networkMonitor = get(),
        )
    }
}

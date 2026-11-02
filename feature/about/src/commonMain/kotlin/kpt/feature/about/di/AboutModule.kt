/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.about.di

import kpt.feature.about.ui.AboutViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** Collected by `:cmp-navigation:generateFeatureKoinBindings` — the `di` package is load-bearing. */
val AboutModule = module {
    // A lambda, not viewModelOf: the constructor's only parameter is a defaulted AboutConfig,
    // which Koin would otherwise try to resolve as a dependency.
    viewModel { AboutViewModel() }
}

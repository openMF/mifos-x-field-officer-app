/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.searchrecord.di

import kpt.feature.searchrecord.ui.SearchRecordViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Collected by `:cmp-navigation:generateFeatureKoinBindings` — the `di` package is load-bearing. */
val SearchRecordModule = module {
    viewModelOf(::SearchRecordViewModel)
}

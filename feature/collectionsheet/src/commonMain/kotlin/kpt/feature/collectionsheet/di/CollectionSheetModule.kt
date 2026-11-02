/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.collectionsheet.di

import kpt.feature.collectionsheet.ui.details.CollectionSheetDetailsViewModel
import kpt.feature.collectionsheet.ui.newsheet.NewCollectionSheetViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Collected by `:cmp-navigation:generateFeatureKoinBindings` — the `di` package is load-bearing. */
val CollectionSheetModule = module {
    viewModelOf(::NewCollectionSheetViewModel)
    viewModelOf(::CollectionSheetDetailsViewModel)
}

/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.profile.di

import kpt.feature.profile.identity.ProfileViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Profile backbone DI.
 *
 * The template's demo profile body (`kpt.feature.profile.demo.**`) and its
 * `core/store/profile` read port are `owner: demo` and were removed by
 * `scripts/remove-demo.sh`. This fork supplies its own profile content in the
 * feature port (epic `template-port-fresh`, Phase 06), at which point its
 * ViewModel + sources are bound here.
 */
val ProfileModule = module {
    viewModelOf(::ProfileViewModel)
}

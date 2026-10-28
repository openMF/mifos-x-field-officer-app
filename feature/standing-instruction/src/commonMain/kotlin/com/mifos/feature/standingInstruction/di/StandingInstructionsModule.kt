/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-x-field-officer-app/blob/master/LICENSE.md
 */
package com.mifos.feature.standingInstruction.di

import com.mifos.feature.standingInstruction.viewStandingInstructions.ViewStandingInstructionsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val StandingInstructionsModule = module {
    viewModelOf(::ViewStandingInstructionsViewModel)
}

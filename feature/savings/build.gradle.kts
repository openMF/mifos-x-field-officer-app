/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-x-field-officer-app/blob/master/LICENSE.md
 */
plugins {
    alias(libs.plugins.cmp.feature.convention)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.mifos.feature.savings"
}

kotlin{
    sourceSets{
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.ui)
            implementation(libs.kotlinx.serialization.json)
            implementation(projects.feature.standingInstruction)
        }
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}
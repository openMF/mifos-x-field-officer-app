/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
plugins {
    alias(libs.plugins.cmp.feature.convention)
}

kotlin {
    sourceSets {
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
        commonMain.dependencies {
            implementation(projects.core.data)
            implementation(projects.core.model)
            // koinNavViewModel(), imported aliased as `retainedKoinViewModel` — same contract
            // as feature/home's demo body.
            implementation(libs.koin.compose.navigation)

            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}

compose {
    resources {
        packageOfResClass = "kpt.feature.profile.generated.resources"
    }
}

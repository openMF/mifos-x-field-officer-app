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
            // Real preference repositories over in-memory MapSettings, as feature/signin does —
            // a fake would test a reimplementation of the thing under test.
            implementation(projects.coreBase.common)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.test)
            implementation(libs.turbine)
            // The fake AuthCommandRepository must implement `authenticate`, whose request/response
            // DTOs live in core/network (as in feature/signin's test).
            implementation(projects.core.network)
        }
        commonMain.dependencies {
            implementation(projects.core.data)
            // Sign-out is the Settings dialog + ViewModel — one implementation, two entry points.
            implementation(projects.feature.settings)
            implementation(projects.core.datastore)
            implementation(projects.core.model)
            implementation(projects.core.store)
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

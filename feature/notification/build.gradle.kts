/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
plugins {
    alias(libs.plugins.cmp.feature.convention)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // kotlinx-datetime for "6d ago", via core/common's api.
            implementation(projects.core.common)
            implementation(projects.core.data)
            implementation(projects.core.model)
            implementation(projects.core.ui)
            // A notification opens the record it is about — each record's screen lives in its feature.
            implementation(projects.feature.loan)
            implementation(projects.feature.client)
            implementation(projects.feature.groups)
            implementation(projects.feature.center)
            implementation(projects.feature.savings)
            implementation(libs.koin.compose.navigation)
            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            // screenDataStreamForTesting, behind the core/data fakes.
            implementation(projects.coreBase.store)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
}

compose {
    resources {
        packageOfResClass = "kpt.feature.notification.generated.resources"
    }
}

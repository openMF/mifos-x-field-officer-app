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
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // DateHelper (date fields) + kotlinx-datetime.
            implementation(projects.core.common)
            implementation(projects.core.data)
            implementation(projects.core.model)
            implementation(projects.core.ui)
            implementation(libs.koin.compose.navigation)
            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.kotlinx.serialization.json)
            // Export CSV through the platform's own save dialog (Android: a document picker, no
            // storage permission) — the original wrote to a hardcoded path on Android and TODO()'d elsewhere.
            implementation(libs.filekit.core)
            implementation(libs.filekit.dialog.compose)
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
        packageOfResClass = "kpt.feature.report.generated.resources"
    }
}

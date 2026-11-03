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
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kermit.logging)
            // DateHelper — the date field's display format, shared with the other forms.
            implementation(projects.core.common)
            implementation(projects.core.data)
            implementation(projects.core.model)
            // NavigationItem + @FeatureTab + toObjectNavigationRoute — the tab declaration seam.
            implementation(projects.core.ui)
            // The shared activate screen (client / centre / group all reach it).
            implementation(projects.feature.activate)
            // Group rows in a center open the group detail, owned by feature/groups (no reverse edge).
            implementation(projects.feature.groups)
            implementation(projects.feature.savings)
            implementation(libs.koin.compose.navigation)
            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
        }
        commonTest.dependencies {
            implementation(projects.coreBase.store)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
}

compose {
    resources {
        packageOfResClass = "kpt.feature.center.generated.resources"
    }
}

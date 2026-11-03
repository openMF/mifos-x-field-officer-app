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
            implementation(projects.core.common)
            implementation(projects.core.data)
            implementation(projects.core.model)
            implementation(projects.core.ui)
            // feature/document — the summary's "Documents" row opens the GENERIC document list
            // (`DocumentListRoute(entityType, entityId)`), already ported and entity-agnostic. The
            // original passed `Constants.ENTITY_TYPE_SAVINGS` to the same screen, so this is a
            // wiring edge rather than a new screen. One-directional: document knows nothing about
            // savings.
            implementation(projects.feature.document)
            // feature/charge — the summary's "Charges" entry opens the ONE charge module, scoped
            // to the account. One-directional, like document.
            implementation(projects.feature.charge)
            implementation(projects.feature.datatable)
            implementation(libs.koin.compose.navigation)
            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(compose.material3)
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
        packageOfResClass = "kpt.feature.savings.generated.resources"
    }
}

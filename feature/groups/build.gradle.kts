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
            implementation(projects.core.data)
            // Utilities (date helpers, constants) — core/common is not a data layer, so it is declared here
            // rather than reached through core/network or core/database as it used to be.
            implementation(projects.core.common)
            implementation(projects.core.model)
            // NavigationItem + @FeatureTab + toObjectNavigationRoute — the tab declaration seam.
            implementation(projects.core.ui)
            // The shared activate screen (client / centre / group all reach it). Same
            // feature-depends-on-feature edge `feature/client` has on `feature/note`.
            implementation(projects.feature.activate)
            // The generic document list, opened from group detail (the original's
            // `groupNavGraph(loadDocumentList = …)` edge).
            implementation(projects.feature.document)
            implementation(projects.feature.datatable)
            // feature/loan — group detail's "Add Loan Account" opens the group loan application
            // form, which lives in feature/loan because it is loan-shaped (product template,
            // terms, amortization). The original has the same edge
            // (`6b66e8a43:feature/groups/.../GroupNavGraph.kt` -> addGroupLoanAccount).
            implementation(projects.feature.loan)
            // feature/savings — group detail's "Add Savings Account" opens the savings application
            // as a GROUP application (S5c). Same edge as the original's `addSavingsAccount`.
            implementation(projects.feature.savings)
            implementation(projects.feature.note)
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
        packageOfResClass = "kpt.feature.groups.generated.resources"
    }
}

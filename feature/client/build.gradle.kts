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
            // For `PostClientAddressRequest`, which the add-address form must construct.
            //
            // That DTO is the ODD ONE OUT: its sibling command payloads (`AssignStaffRequest`,
            // `IdentifierPayload`) live in core/model, which is why no other client screen needs
            // this dependency. `ClientCommandRepository.createClientAddress` therefore exposes a
            // core/network type in a core/data contract, and every caller inherits the leak.
            // `feature/{activate,note,signin}` already carry the same dependency for the same
            // reason. Moving the two address DTOs to core/model is the real fix — 11 references
            // across 6 files — and is tracked as `address-dtos-live-in-core-network`.
            // NavigationItem + @FeatureTab + toObjectNavigationRoute — the tab declaration seam.
            implementation(projects.core.ui)
            // Client is the hub that navigates into its drill-down features. The original declared
            // the same edge (`6b66e8a43:feature/client/build.gradle.kts` → projects.feature.note,
            // plus dataTable/savings/loan/document/recurringDeposit/groups as those slices land).
            implementation(projects.feature.note)
            // Record search over cached addresses / identifiers — opened from, and returning to,
            // client screens, so the client graph registers it (the note pattern above).
            implementation(projects.feature.searchrecord)
            // feature/charge — "Upcoming Charges" and "Add Charge" open the ONE charge module.
            implementation(projects.feature.charge)
            // feature/loan — the client's loan-accounts list opens the loan PROFILE and the
            // REPAYMENT form directly, which is the same edge the original had
            // (`6b66e8a43:feature/client/.../ClientNavigation.kt` imports
            // `navigateToLoanAccountProfileScreen` + `navigateToLoanRepaymentScreen`).
            //
            // One-directional: `feature/loan` has no dependency on `feature/client`, so there is
            // no cycle. The loan screens take `onX` callbacks and know nothing about who opened
            // them.
            implementation(projects.feature.loan)
            // feature/savings — the apply-new chooser opens the savings and recurring-deposit
            // applications (S5e). Same edges as the original's `clientApplyNewApplicationRoute`.
            implementation(projects.feature.savings)
            implementation(projects.feature.groups)
            // The pending client's "Activate Client" button (the original's `activateClient`).
            implementation(projects.feature.activate)
            implementation(libs.koin.compose.navigation)
            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            // Signature screen: the file picker that supplies an image to crop. krop itself comes
            // through core/ui's `api` dependency, because AppImageCropperDialog's signature
            // names its types.
            implementation(libs.filekit.compose)
            implementation(libs.filekit.core)
        }
        commonTest.dependencies {
            // screenDataStreamForTesting — the sanctioned test factory, so the fake repository hands
            // back real ScreenDataStreams rather than a parallel fake of the state machine.
            implementation(projects.coreBase.store)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
}

compose {
    resources {
        packageOfResClass = "kpt.feature.client.generated.resources"
    }
}

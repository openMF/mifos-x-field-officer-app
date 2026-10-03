/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import org.convention.forkProp

plugins {
    alias(libs.plugins.cmp.feature.convention)
    alias(libs.plugins.buildkonfig)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kermit.logging)
            implementation(projects.core.data)
            implementation(projects.core.domain)
            implementation(projects.core.model)
            implementation(projects.core.network)
            implementation(projects.core.datastore)
            // MifosProgressIndicatorOverlay — the modal submit overlay the v1 screen rendered
            // (ui.yaml#components.progress_overlay, RE-EXTRACT D4) and the stitch mockup's
            // #progress_overlay. Lives in core/ui, which the module did not previously depend on,
            // which is part of why the port shipped an inline spinner instead.
            implementation(projects.core.ui)
            implementation(libs.koin.compose.navigation)
            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
        }
        commonTest.dependencies {
            implementation(projects.coreBase.common)
            implementation(projects.coreBase.store)
            implementation(libs.multiplatform.settings.test)
        }
    }
}

// The stitch mockup renders the app name as the screen's headline ("Mifos X Field Officer").
// Read from `gradle/fork.properties#app.display.name` — the build-bridge syncForkConfig generates
// from the SoT `app-profile/app.yaml#identity.app_name` — rather than committed as a literal in
// strings.xml. Same reasoning as feature/settings' About footer: a brand string duplicated into
// per-locale resources drifts from app-profile silently, and a fork rebrands in ONE place.
val signinAppDisplayName = forkProp("app.display.name", "App")

buildkonfig {
    packageName = "kpt.feature.signin"
    defaultConfigs {
        buildConfigField(
            STRING,
            "APP_DISPLAY_NAME",
            signinAppDisplayName,
        )
    }
}

compose {
    resources {
        packageOfResClass = "kpt.feature.signin.generated.resources"
    }
}

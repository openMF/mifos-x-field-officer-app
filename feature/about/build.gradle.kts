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
import org.convention.dynamicVersion
import org.convention.forkProp

plugins {
    alias(libs.plugins.cmp.feature.convention)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.buildkonfig)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // kotlinx-datetime (the copyright year), via core/common's api.
            implementation(projects.core.common)
            // AppOrgLinks — the org links declared in app-profile/app.yaml#org.links (community, product).
            implementation(projects.core.model)
            implementation(libs.koin.compose.navigation)
            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
}

compose {
    resources {
        packageOfResClass = "kpt.feature.about.generated.resources"
    }
}

// The fork's identity, from the SoT `app-profile/app.yaml` (identity.app_name + org.*) via the
// generated build-bridge `gradle/fork.properties`. About renders these instead of string
// resources, so a rebrand is an app-profile edit — the original hardcoded "MifosXDroid",
// "App Version 1.0.1", "©2016–2022" and five URLs, and every one of them went stale.
buildkonfig {
    packageName = "kpt.feature.about"
    defaultConfigs {
        buildConfigField(STRING, "APP_DISPLAY_NAME", forkProp("app.display.name", "App"))
        // The SAME value the Android build stamps as versionName (cmp-android/build.gradle.kts).
        buildConfigField(STRING, "VERSION_NAME", System.getenv("VERSION") ?: project.dynamicVersion)
        buildConfigField(STRING, "ORG_NAME", forkProp("org.name", ""))
        buildConfigField(STRING, "ORG_EMAIL", forkProp("org.email", ""))
        buildConfigField(STRING, "WEBSITE_URL", forkProp("org.marketing.url", ""))
        buildConfigField(STRING, "SOURCE_URL", forkProp("org.support.url", ""))
        // The license TEXT. MPL-2.0 unless a fork declares otherwise (`org.license.url`).
        buildConfigField(STRING, "LICENSE_URL", forkProp("org.license.url", "https://mozilla.org/MPL/2.0/"))
    }
}

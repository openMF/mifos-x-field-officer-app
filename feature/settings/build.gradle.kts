/*
 * Copyright 2025 Mifos Initiative
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
    // Module-local BuildKonfig so the About/version footer can show the fork's app display name
    // WITHOUT a hardcoded string resource (S9/T10 white-label seam). Same mechanism core/network uses.
    alias(libs.plugins.buildkonfig)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(projects.core.data)
            implementation(projects.core.model)
            implementation(projects.core.ui)
            // Firebase analytics (AnalyticsHelper + Compose TrackScreenView/rememberAnalyticsHelper) via core/firebase.
            implementation(projects.core.firebase)

            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
        }
    }
}

compose {
    resources {
        packageOfResClass = "kpt.feature.settings.generated.resources"
    }
}

// Fork app display name → `kpt.feature.settings.BuildKonfig.APP_DISPLAY_NAME`, read from
// `gradle/fork.properties#app.display.name` — the build-bridge that syncForkConfig generates from the
// SoT `app-profile/app.yaml#identity.app_name`. SettingsScreen's footer renders this instead of a
// hardcoded string resource, so a fork rebrands in app-profile, not 7 locale strings.xml files (S9/T10).
// Read through the ONE Gradle-side reader (org.convention.KptAppProfileProperties) rather than a local
// Properties().load() — see that file for why independent parsers are the defect class.
val settingsAppDisplayName = forkProp("app.display.name", "App")

buildkonfig {
    packageName = "kpt.feature.settings"
    defaultConfigs {
        buildConfigField(
            STRING,
            "APP_DISPLAY_NAME",
            settingsAppDisplayName,
        )
    }
}

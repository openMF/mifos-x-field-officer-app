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
            implementation(projects.core.datastore)
            implementation(projects.core.data)
            implementation(projects.core.model)
            implementation(projects.core.ui)
            // Sync & Drafts screen consumes the DraftInventory seam. Depend on core/store (the fork
            // seam), which `api`-re-exposes core-base/store — features never depend on core-base directly.
            implementation(projects.core.store)
            // Firebase analytics (AnalyticsHelper + Compose TrackScreenView/rememberAnalyticsHelper) via core/firebase.
            implementation(projects.core.firebase)
            // AppReviewManager + the generated AppReviewConfig for the "Rate this app" row. Same rule
            // as core/store above: depend on core/platform, which `api`-re-exposes core-base/platform.
            implementation(projects.core.platform)

            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
        }
        commonTest.dependencies {
            // ServerConfigViewModelTest builds a real ProjectPreferencesRepository over MapSettings
            // and needs DispatcherManager — the same fakes feature/signin's SignInViewModelTest uses.
            implementation(projects.coreBase.common)
            implementation(projects.core.datastore)
            // `Settings` itself, not just MapSettings: core/datastore declares
            // multiplatform-settings as `implementation`, so the interface in
            // ProjectPreferencesRepositoryImpl's signature is not exposed transitively.
            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.test)
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
// Read through the ONE Gradle-side reader (org.convention.ForkProperties) rather than a local
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

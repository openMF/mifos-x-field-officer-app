/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-x-field-officer-app/blob/master/LICENSE.md
 */
plugins {
    alias(libs.plugins.cmp.feature.convention)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.mifos.feature.client"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.data)
            implementation(projects.core.domain)
            implementation(projects.core.datastore)
            implementation(projects.core.designsystem)
            implementation(projects.core.ui)
            implementation(projects.feature.dataTable)
            implementation(projects.feature.note)
            implementation(projects.feature.savings)
            implementation(projects.feature.loan)
            implementation(projects.feature.document)
            implementation(projects.feature.recurringDeposit)
            implementation(projects.feature.groups)
            implementation(projects.feature.standingInstruction)

            implementation(compose.ui)
            implementation(compose.material3)
            implementation(compose.foundation)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.paging.common)
            implementation(libs.androidx.paging.compose)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.coil.kt.compose)
            implementation(libs.coil.network.ktor)
            implementation(libs.ui.backhandler)

            implementation(libs.filekit.core)
            implementation(libs.filekit.compose)
            implementation(libs.filekit.dialog.compose)
            implementation(libs.crop.krop.ui)

        }
        
        androidMain.dependencies {
            implementation(libs.maps.compose)
            implementation(libs.accompanist.permission)
            implementation(libs.ktor.client.android)

            implementation(libs.coil.network.okhttp)
            implementation(libs.play.services.location)
            implementation(libs.kotlinx.coroutines.play.services)
        }


        desktopMain.dependencies {
            implementation(libs.pdfbox)
        }
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}
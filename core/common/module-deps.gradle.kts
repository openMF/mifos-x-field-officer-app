/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */

// FORK-OWNED dependency seam (white-label). `build.gradle.kts` is template-owned and
// FULL-COPIES on sync; fork deps live HERE so they survive it.
// Ported from the old tree's build.gradle.kts at dev@44d92532f (epic template-port-fresh).

import org.gradle.accessors.dm.LibrariesForLibs
val libs = the<LibrariesForLibs>()

dependencies {
    "commonMainApi"(libs.coil.kt)
    "commonMainApi"(libs.coil.core)
    "commonMainApi"(libs.coil.svg)
    "commonMainApi"(libs.coil.network.ktor)
    "commonMainApi"(libs.squareup.okio)
    "commonMainApi"(libs.jb.kotlin.stdlib)
    "commonMainImplementation"(libs.filekit.core)
    "commonMainImplementation"(libs.filekit.coil)
    // FileKit key remap: the template catalog is on a newer FileKit where the old
    // `filekit-compose` artifact is gone. dev's filekit-dialogs        -> filekit-dialog-compose
    //                                    dev's filekit-dialog-compose -> filekit-compose
    "commonMainImplementation"(libs.filekit.compose)        // io.github.vinceglb:filekit-dialogs-compose
    "commonMainImplementation"(libs.filekit.dialog.compose) // io.github.vinceglb:filekit-dialogs
    "commonMainImplementation"(libs.ktor.client.core)
    "androidMainImplementation"(libs.kotlinx.coroutines.android)
    "androidMainImplementation"(libs.koin.android)
    "desktopMainImplementation"(libs.kotlinx.coroutines.swing)
    "desktopMainImplementation"(libs.kotlin.reflect)
    "jsMainApi"(libs.jb.kotlin.stdlib.js)
    "jsMainApi"(libs.jb.kotlin.dom)
    "commonTestImplementation"(libs.kotlinx.coroutines.test)
}

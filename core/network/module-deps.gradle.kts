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
    "commonMainApi"(project(":core:database"))
    "commonMainApi"(project(":core:datastore"))
    "commonMainApi"(libs.kotlinx.datetime)
}

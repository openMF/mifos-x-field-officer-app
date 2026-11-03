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
    "commonMainApi"(project(":core:common"))
    "commonMainApi"(project(":core:model"))

    // Android at-rest encryption (kpt.core.database.encryption). SQLCipher's androidx.sqlite driver
    // plus the `Settings` type of the Keystore-backed `named("secure")` store the passphrase lives in.
    // Catalog lookup by name so versions stay in libs.versions.toml (type-safe `libs.` accessors are
    // not generated for an applied script plugin; `findLibrary` is).
    val catalog = project.extensions.getByType(VersionCatalogsExtension::class.java).named("libs")
    "androidMainImplementation"(catalog.findLibrary("sqlcipher-android").get())
    "androidMainImplementation"(catalog.findLibrary("multiplatform-settings").get())
}

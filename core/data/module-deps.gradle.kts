/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */

// module-deps.gradle.kts — the FORK-OWNED dependency seam for `core/data` (white-label).
//
// `core/data/build.gradle.kts` is template-owned and FULL-COPIES on `/kmp-project-template-sync`.
// Add this module's fork-specific dependencies HERE instead, and they survive every sync.
//
// Use string configuration notation — type-safe `libs.`/`projects.` accessors are not generated
// for applied script plugins:
//
//     dependencies {
//         "commonMainImplementation"(project(":core:common"))
//         "commonMainImplementation"("com.example:some-lib:1.2.3")
//     }
//
// Empty on the template — this is yours to fill.

dependencies {
    val libs = project.extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

    // worker-kmp core — Settings → Offline sync runs as a durable background job
    // (kpt.core.data.offline.work.OfflineSyncWorker), registered in cmp-shared/ForkWorkerDeclarations.kt.
    "commonMainImplementation"(libs.findLibrary("worker-kmp").get())
    // Kermit, for the command repositories' and UserLogoutManagerImpl's diagnostic logging.
    "commonMainImplementation"(libs.findLibrary("kermit-logging").get())

    // In-memory `MapSettings` for core/data tests that exercise the REAL `core/datastore`
    // preferences (AuthCommandRepositoryTest: the session a sign-in persists). Resolved through the
    // catalog so the version stays in one place.
    "commonTestImplementation"(
        project.extensions.getByType(VersionCatalogsExtension::class.java)
            .named("libs").findLibrary("multiplatform-settings-test").get(),
    )
}

/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */

// module-deps.gradle.kts — the FORK-OWNED dependency seam for `core/ui` (white-label).
//
// `core/ui/build.gradle.kts` is template-owned and FULL-COPIES on `/kmp-project-template-sync`.
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
    // kpt.core.ui.util.ShareUtils (androidMain) launches OssLicensesMenuActivity. The oss-licenses
    // GRADLE PLUGIN is already on the root classpath; this is the runtime artifact that provides the
    // activity itself, and it was never declared — so `:core:ui:compileAndroidMain` could not resolve
    // `com.google.android.gms.oss.licenses`.
    //
    // DEBT, recorded as capability #10 in FEATURE_LAYER_MIGRATION_SPEC.md §3a: ShareUtils is an
    // `actual object` of platform code sitting in core/ui, which is Compose-only. It belongs in
    // core/platform (and its sharing half largely duplicates core-base/platform's ShareManager).
    // Declared here to unblock the baseline; relocated in the slice that owns `about`/`settings`.
    "androidMainImplementation"("com.google.android.gms:play-services-oss-licenses:17.5.1")
}

/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */

// feature-deps.gradle.kts — the FORK-OWNED feature-dependency seam (white-label, S7/F4).
//
// `cmp-navigation/build.gradle.kts` (template-owned) applies this script via `apply(from = ...)`. A fork
// adds/removes a feature module dependency by editing ONLY this file — it never touches the template build
// file, so a template sync full-copies `cmp-navigation/build.gradle.kts` while these deps survive. This is
// the build-graph twin of the `cmp-navigation/registry` runtime seams (FeatureRegistry etc.); adding a
// feature is: include it in `settings.gradle.kts`, add its dep here, and register its module + routes in
// `FeatureRegistry`.
//
// String `project(":feature:x")` notation (not the type-safe `projects.feature.x` accessors) is used
// deliberately: type-safe project accessors are NOT generated for `apply(from = ...)` script plugins. The
// `commonMainImplementation` configuration is the KMP-created implementation configuration for the
// commonMain source set; it exists by the time this script is applied (after the `kotlin { }` block).

dependencies {
    // feature/signin — login. Included in settings.local.gradle.kts and picked up by
    // :cmp-navigation:generateFeatureKoinBindings (which emits `includes(SignInModule)`), but without
    // this dependency cmp-navigation cannot resolve the symbol the generated aggregate references:
    //   GeneratedFeatureKoinBindings.kt: Unresolved reference 'signin' / 'SignInModule'
    //
    // The codegen derives the REGISTRATION from source; it cannot derive the BUILD EDGE. Both halves
    // are required for every fork feature.
    "commonMainImplementation"(project(":feature:signin"))

    // feature/client — the client vertical (S3). Registration is three steps: this build
    // edge, the settings.local include, and @FeatureDestination on the graph builder.
    "commonMainImplementation"(project(":feature:client"))
}

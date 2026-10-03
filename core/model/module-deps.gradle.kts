/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */

// module-deps.gradle.kts — the FORK-OWNED dependency seam for `core/model` (white-label).
//
// `core/model/build.gradle.kts` is template-owned and FULL-COPIES on `/kmp-project-template-sync`.
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

/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */

// module-deps.gradle.kts — the FORK-OWNED dependency seam for `core/platform` (white-label).
//
// `core/platform/build.gradle.kts` is template-owned and FULL-COPIES on `/kmp-project-template-sync`.
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
    // ── core-base/security re-export (added 2026-10-05, signin v2) ────────────────────────────
    // `api`, not `implementation`: feature modules must see these symbols TRANSITIVELY.
    //
    // Why here. `core-base/security` ships BiometricAuthenticator (android/ios/desktop/js actuals),
    // SecurityPolicy, FailedAttemptTracker, SecureAuthManager and SensitiveString — and before this
    // line, NOTHING outside `cmp-navigation` and `core-base/network` could reach any of it. The
    // capabilities had shipped with the template and sat unused because no `core/*` seam exposed
    // them, so every feature that wanted biometric unlock or failed-attempt throttling had no path
    // to it.
    //
    // Why THIS module. `core/designsystem` already `api`-exposes `core/platform` to every feature
    // (RULE-IMPLEMENT-STORE5-001 S5-FEATURE-DEP-REDUNDANT), so re-exporting here reaches the whole
    // feature layer without any feature declaring a new dependency. It mirrors exactly what
    // `core/store` does for `core-base/store` and what line 25 of build.gradle.kts already does for
    // `core-base/platform` — the encapsulation-compliant boundary (G-CORE-BASE-ENCAP): features
    // reach core-base THROUGH core/, never directly.
    //
    // Why this file and not build.gradle.kts. That file is `owner: template` and FULL-COPIES on
    // `/kmp-project-template-sync`; a dep added there is one sync from silent deletion. This seam
    // exists precisely so a fork can extend the module without touching template-owned files, so
    // no upstream PR is required for this change.
    "commonMainApi"(project(":core-base:security"))

    // kpt.core.platform.permission.PermissionRequester — suspendCancellableCoroutine bridges the
    // Android ActivityResult callback to a suspend fn. core-base/platform declares coroutines as
    // `implementation`, so it is not visible transitively here.
    "commonMainImplementation"("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")

    // Android permission mechanics: ComponentActivity + activityResultRegistry +
    // ActivityResultContracts come from activity-ktx; ContextCompat.checkSelfPermission and
    // ActivityCompat.shouldShowRequestPermissionRationale come from core-ktx.
    //
    // These live HERE, not in `core/designsystem`, because the permission surface moved to this
    // module. designsystem is Compose-only and must not carry android platform deps.
    "androidMainImplementation"("androidx.activity:activity-ktx:1.13.0")
    "androidMainImplementation"("androidx.core:core-ktx:1.19.0")
}

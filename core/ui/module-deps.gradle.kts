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

    // AppDocumentViewer — in-app PDF rendering (PDFium) on every target. Resolved through the
    // catalog by name so the version lives in ONE place (`pdfViewer` in libs.versions.toml); the
    // type-safe `libs.` accessor is not generated for an applied script plugin, `findLibrary` is.
    "commonMainImplementation"(
        project.extensions.getByType(VersionCatalogsExtension::class.java)
            .named("libs").findLibrary("pdf-viewer").get(),
    )

    // Image cropper + signature canvas, for the client signature screen. krop is `api`:
    // AppImageCropperDialog's signature names its `CropState` / `AspectRatio`, so a feature calling it
    // needs those types on its own compile classpath. compose-signature is internal to the dialog.
    val libs = project.extensions.getByType(VersionCatalogsExtension::class.java).named("libs")
    "commonMainApi"(libs.findLibrary("crop-krop-ui").get())
    "commonMainImplementation"(libs.findLibrary("compose-signature").get())

    // Keep androidx material3 OFF the 1.5.0-alpha line. The androidx Compose BOM upgrades it to
    // 1.5.0-alpha17, which was compiled against a different androidx.compose.foundation than the
    // 1.12.0 CMP resolves; the mismatch is a RUNTIME AbstractMethodError in
    // OutlinedTextFieldDefaults that takes down any screen with an OutlinedTextField. `strictly`
    // (not a plain constraint, which loses to the higher request) pins the highest stable version.
    constraints {
        "androidMainImplementation"("androidx.compose.material3:material3") {
            version { strictly("1.4.0") }
            because("material3 1.5.0-alpha is binary-incompatible with foundation 1.12.0")
        }
    }
}

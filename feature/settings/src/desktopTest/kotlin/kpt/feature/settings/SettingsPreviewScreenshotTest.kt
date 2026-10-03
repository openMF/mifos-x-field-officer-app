/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.settings

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runDesktopComposeUiTest
import io.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import sergio.sastre.composable.preview.scanner.common.CommonComposablePreviewScanner
import kotlin.test.assertTrue

/**
 * Device-free CMP render tier (SCREENSHOT_TEST.md CMP-PRIMARY). Auto-discovers every commonMain
 * `@Preview` in this feature via `CommonComposablePreviewScanner`, then renders each one off
 * `desktopTest` (JVM — no emulator, no Robolectric) and captures via roborazzi-compose-desktop.
 * Drives `recordRoborazziDesktop` / `verifyRoborazziDesktop`.
 *
 * NB (desktop path): the `roborazzi-compose-preview-scanner-support` bridge (`ComposablePreview
 * .captureRoboImage()`) is Android/Robolectric-only (published `aar`, no JVM variant), so the
 * device-free desktop path renders each discovered preview manually inside its own
 * `runDesktopComposeUiTest` block (fresh composition per preview → no `setContent`-once clash).
 *
 * This is the template's demonstrator; every fork gets the same tier automatically via
 * `CMPFeatureConventionPlugin`, so `kmp-screen-gen`'s emitted previews become CI-renderable
 * with zero per-feature wiring.
 */
@OptIn(ExperimentalTestApi::class)
class SettingsPreviewScreenshotTest {

    @Test
    fun captureAllPreviews() {
        val previews = CommonComposablePreviewScanner()
            .scanPackageTrees("kpt.feature.settings")
            .getPreviews()

        // Names must be unique BEFORE anything is written. Two previews resolving to one file would
        // otherwise have the second silently overwrite the first: the golden count stays plausible,
        // `verifyRoborazziDesktop` stays green (it re-renders the same winner), and one preview is
        // simply no longer covered. Fail loudly and make the author rename.
        val names = previews.map { it.methodName }
        val duplicates = names.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
        assertTrue(
            duplicates.isEmpty(),
            "Preview function names must be unique across this feature — goldens are keyed by name. " +
                "Duplicated: $duplicates",
        )

        previews.forEach { preview ->
            runDesktopComposeUiTest {
                setContent { preview() }
                // Golden under src/desktopTest/resources (committed) so CI `verifyRoborazziDesktop`
                // has a baseline to compare a fresh render against — not a throwaway build/ dir.
                //
                // Keyed by the preview FUNCTION NAME, not its scan index. Index keying made every
                // golden positional: adding one @Preview shifted all later previews down a slot, so
                // a one-preview change rewrote 19 files and the diff said nothing about what
                // actually changed. A rename here renames one file; a new preview adds one file.
                onRoot().captureRoboImage(
                    "src/desktopTest/resources/screenshots/settings/${preview.methodName}.png",
                )
            }
        }
    }
}

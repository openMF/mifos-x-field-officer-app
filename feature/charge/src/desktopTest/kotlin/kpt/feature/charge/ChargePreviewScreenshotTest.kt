/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.charge

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.runDesktopComposeUiTest
import io.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import sergio.sastre.composable.preview.scanner.common.CommonComposablePreviewScanner
import kotlin.test.assertTrue

/**
 * Device-free CMP render tier — every commonMain `@Preview` in this feature, rendered off
 * `desktopTest` and captured by roborazzi-compose-desktop. Same harness as
 * `NotePreviewScreenshotTest`: an empty scan FAILS (a `private` preview is invisible to the scanner),
 * and names must be unique because goldens are keyed by preview function name.
 */
@OptIn(ExperimentalTestApi::class)
class ChargePreviewScreenshotTest {

    @Test
    fun captureAllPreviews() {
        val previews = CommonComposablePreviewScanner()
            .scanPackageTrees("kpt.feature.charge")
            .getPreviews()

        assertTrue(
            previews.isNotEmpty(),
            "no @Preview found — the render tier is silently disabled. Previews must be `internal` or public.",
        )

        val duplicates = previews.map { it.methodName }.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
        assertTrue(
            duplicates.isEmpty(),
            "Preview function names must be unique — goldens are keyed by name. Duplicated: $duplicates",
        )

        previews.forEach { preview ->
            runDesktopComposeUiTest {
                setContent { preview() }
                // A preview that opens a dialog has a SECOND root (the dialog's window), so `onRoot()`
                // would fail. Capture each root: the screen as the plain name, every dialog beside it.
                val roots = onAllNodes(isRoot())
                val count = roots.fetchSemanticsNodes().size
                for (i in 0 until count) {
                    val suffix = if (i == 0) "" else "-$i"
                    roots[i].captureRoboImage(
                        "src/desktopTest/resources/screenshots/charge/${preview.methodName}$suffix.png",
                    )
                }
            }
        }
    }
}

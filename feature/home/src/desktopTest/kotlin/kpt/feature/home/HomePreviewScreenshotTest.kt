/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.home

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.runDesktopComposeUiTest
import io.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import sergio.sastre.composable.preview.scanner.common.CommonComposablePreviewScanner
import kotlin.test.assertTrue

/**
 * Device-free render tier for the Home dashboard: one golden per Stitch state preview
 * (`mockups/home/stitch/0{1..5}-home-*`), rendered phone-width and tall enough to hold the whole
 * scrolling dashboard so each golden compares against its `screen.png` whole.
 */
@OptIn(ExperimentalTestApi::class)
class HomePreviewScreenshotTest {

    @Test
    fun captureAllPreviews() {
        val previews = CommonComposablePreviewScanner()
            .scanPackageTrees("kpt.feature.home")
            .getPreviews()

        assertTrue(previews.isNotEmpty(), "no @Preview found — previews must be internal or public, never private")
        val duplicates = previews.map { it.methodName }.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
        assertTrue(duplicates.isEmpty(), "Preview names must be unique — goldens are keyed by name. Duplicated: $duplicates")

        previews.forEach { preview ->
            runDesktopComposeUiTest(width = PHONE_WIDTH, height = PHONE_TALL) {
                setContent { preview() }
                val roots = onAllNodes(isRoot())
                for (i in 0 until roots.fetchSemanticsNodes().size) {
                    roots[i].captureRoboImage("src/desktopTest/resources/screenshots/home/${preview.methodName}${if (i == 0) "" else "-$i"}.png")
                }
            }
        }
    }

    private companion object {
        const val PHONE_WIDTH = 412
        const val PHONE_TALL = 2200
    }
}

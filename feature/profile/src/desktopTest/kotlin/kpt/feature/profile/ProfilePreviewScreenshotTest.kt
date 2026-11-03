/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.profile

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.runDesktopComposeUiTest
import io.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import sergio.sastre.composable.preview.scanner.common.CommonComposablePreviewScanner
import kotlin.test.assertTrue

/**
 * Device-free render tier for Profile: every commonMain `@Preview` in this feature, rendered off
 * `desktopTest` and captured by name. Same harness as `OfflineSyncPreviewScreenshotTest`.
 */
@OptIn(ExperimentalTestApi::class)
class ProfilePreviewScreenshotTest {

    @Test
    fun captureAllPreviews() {
        val previews = CommonComposablePreviewScanner()
            .scanPackageTrees("kpt.feature.profile")
            .getPreviews()

        // An empty scan would capture nothing and pass — previews must be internal or public.
        assertTrue(previews.isNotEmpty(), "no @Preview found — previews must be internal or public, never private")
        val duplicates = previews.map { it.methodName }.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
        assertTrue(
            duplicates.isEmpty(),
            "Preview names must be unique — goldens are keyed by name. Duplicated: $duplicates",
        )

        previews.forEach { preview ->
            runDesktopComposeUiTest(width = PHONE_WIDTH, height = PHONE_TALL) {
                setContent { preview() }
                val roots = onAllNodes(isRoot())
                for (i in 0 until roots.fetchSemanticsNodes().size) {
                    val suffix = if (i == 0) "" else "-$i"
                    roots[i].captureRoboImage(
                        "src/desktopTest/resources/screenshots/profile/${preview.methodName}$suffix.png",
                    )
                }
            }
        }
    }

    private companion object {
        const val PHONE_WIDTH = 412
        const val PHONE_TALL = 1100
    }
}

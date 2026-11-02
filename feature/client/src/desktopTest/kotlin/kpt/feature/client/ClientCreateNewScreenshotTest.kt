/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.runDesktopComposeUiTest
import io.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import sergio.sastre.composable.preview.scanner.common.CommonComposablePreviewScanner
import kotlin.test.assertTrue

/**
 * Device-free render tier for the document preview — same harness as `NotePreviewScreenshotTest`.
 *
 * Scoped to `kpt.feature.client.documentpreview` on purpose: feature/client had no render tier
 * before this, and scanning the whole module would commit goldens for ~30 screens in a change that
 * touched one. Widen the scan when the module's render tier is adopted as a whole.
 */
@OptIn(ExperimentalTestApi::class)
class ClientCreateNewScreenshotTest {

    @Test
    fun captureAllPreviews() {
        val previews = CommonComposablePreviewScanner()
            .scanPackageTrees("kpt.feature.client.createnew")
            .getPreviews()

        // An empty scan must FAIL — see NotePreviewScreenshotTest for the silent-green it prevents.
        assertTrue(previews.isNotEmpty(), "no @Preview found in kpt.feature.client.documentpreview")

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
                        "src/desktopTest/resources/screenshots/client/${preview.methodName}$suffix.png",
                    )
                }
            }
        }
    }
}

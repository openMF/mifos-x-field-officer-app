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

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Waits, in REAL time, until [text] is on screen — for text that comes from string resources.
 *
 * The browser fetches a module's resources asynchronously, so the first frames render those strings
 * empty. `waitUntil` cannot cover that on JS: it loops on the virtual clock without yielding, so the
 * fetch never completes. Each check here suspends on a real delay, which lets the event loop run.
 * On the JVM the strings are there on the first frame and this returns at once.
 */
@OptIn(ExperimentalTestApi::class)
internal suspend fun ComposeUiTest.awaitResourceText(text: String, timeoutMillis: Long = 10_000) {
    val step = 50L
    repeat((timeoutMillis / step).toInt()) {
        if (onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()) return
        withContext(Dispatchers.Default) { delay(step) }
        mainClock.advanceTimeBy(step)
        awaitIdle()
    }
}

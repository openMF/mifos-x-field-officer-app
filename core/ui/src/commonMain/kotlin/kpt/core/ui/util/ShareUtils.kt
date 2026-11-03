/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui.util

import androidx.compose.ui.graphics.ImageBitmap

/** Platform share, dial, mail and browser actions. */
expect object ShareUtils {

    /** Shares `text` through the platform share sheet. */
    fun shareText(text: String)

    /** Shares an image through the platform share sheet, or saves it where there is none. */
    suspend fun shareImage(title: String, image: ImageBitmap)

    /** Shares an image through the platform share sheet, or saves it where there is none. */
    suspend fun shareImage(title: String, byte: ByteArray)

    /** Opens this app's page in the system settings. */
    fun openAppInfo()

    /** Shares a link to this app's store listing. */
    fun shareApp()

    /** Opens the dialler on the support helpline. */
    fun callHelpline()

    /** Opens a mail draft to support. */
    fun mailHelpline()

    /** Opens `url` in the browser. */
    fun openUrl(url: String)

    /** Opens the open-source licences screen. */
    fun ossLicensesMenuActivity()

    /** Restarts the app. */
    fun restartApplication()
}

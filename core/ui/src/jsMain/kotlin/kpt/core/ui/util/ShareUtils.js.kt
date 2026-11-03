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
import androidx.compose.ui.graphics.asSkiaBitmap
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.download
import kotlinx.browser.window

/** Browser actions: sharing downloads the file instead. */
actual object ShareUtils {
    /** Does nothing on this platform. */
    actual fun shareText(text: String) {
    }

    /** Downloads the image. */
    actual suspend fun shareImage(title: String, image: ImageBitmap) {
        image.asSkiaBitmap().readPixels()
            ?.let { FileKit.download(bytes = it, fileName = "AppQrCode") }
    }

    /** Downloads the image. */
    actual suspend fun shareImage(title: String, byte: ByteArray) {
        FileKit.download(bytes = byte, fileName = "AppQrCode")
    }

    /** Shows the helpline number in an alert: browsers cannot dial. */
    actual fun callHelpline() {
        window.alert("Calling is not supported on Web. Please contact support at 8000000000.")
    }

    /** Opens a mail draft to support. */
    actual fun mailHelpline() {
        val url =
            "mailto:support@example.com?subject=Help%20Request&body=Hello,%20I%20need%20assistance%20with..."
        window.open(url)
    }

    /** Does nothing on this platform. */
    actual fun openAppInfo() {
    }

    /** Does nothing on this platform. */
    actual fun shareApp() {
    }

    /** Opens `url` in a new tab. */
    actual fun openUrl(url: String) {
        window.open(url)
    }

    /** Does nothing on this platform. */
    actual fun ossLicensesMenuActivity() {
    }

    /** Does nothing on this platform. */
    actual fun restartApplication() {
    }
}

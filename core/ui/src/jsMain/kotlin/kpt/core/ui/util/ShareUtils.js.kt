/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui.util

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.download
import kotlinx.browser.window

actual object ShareUtils {
    actual fun shareText(text: String) {
    }

    actual suspend fun shareImage(title: String, image: ImageBitmap) {
        image.asSkiaBitmap().readPixels()
            ?.let { FileKit.download(bytes = it, fileName = "MifosQrCode") }
    }

    actual suspend fun shareImage(title: String, byte: ByteArray) {
        FileKit.download(bytes = byte, fileName = "MifosQrCode")
    }

    actual fun callHelpline() {
        window.alert("Calling is not supported on Web. Please contact support at 8000000000.")
    }

    actual fun mailHelpline() {
        val url =
            "mailto:support@example.com?subject=Help%20Request&body=Hello,%20I%20need%20assistance%20with..."
        window.open(url)
    }

    actual fun openAppInfo() {
    }

    actual fun shareApp() {
    }

    actual fun openUrl(url: String) {
    }

    actual fun ossLicensesMenuActivity() {
    }

    actual fun restartApplication() {
    }
}

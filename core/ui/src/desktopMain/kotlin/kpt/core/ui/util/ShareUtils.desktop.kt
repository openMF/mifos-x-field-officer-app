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
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.write
import java.awt.Desktop
import java.net.URI

/** Desktop actions through `java.awt.Desktop`; sharing saves the file instead. */
actual object ShareUtils {
    /** Does nothing on this platform. */
    actual fun shareText(text: String) {
    }

    /** Saves the image as a PNG where the user picks. */
    actual suspend fun shareImage(title: String, image: ImageBitmap) {
        val newFile = FileKit.openFileSaver(
            suggestedName = "AppQrCode",
            extension = "png",
        )
        image.asSkiaBitmap().readPixels()?.let { newFile?.write(it) }
    }

    /** Saves the image as a PNG where the user picks. */
    actual suspend fun shareImage(title: String, byte: ByteArray) {
        val newFile = FileKit.openFileSaver(
            suggestedName = "AppQrCode",
            extension = "png",
        )
        newFile?.write(byte)
    }

    /** Opens a `tel:` link to the helpline, where the desktop supports it. */
    actual fun callHelpline() {
        if (Desktop.isDesktopSupported()) {
            val uri = URI("tel:8000000000")
            try {
                Desktop.getDesktop().browse(uri)
            } catch (e: Exception) {
                println("Calling is not supported on Desktop.")
            }
        } else {
            println("Calling is not supported on this platform.")
        }
    }

    /** Opens a mail draft to support, where the desktop supports it. */
    actual fun mailHelpline() {
        val uri =
            URI("mailto:support@example.com?subject=Help%20Request&body=Hello,%20I%20need%20assistance%20with...")
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.MAIL)) {
            Desktop.getDesktop().mail(uri)
        }
    }

    /** Does nothing on this platform. */
    actual fun openAppInfo() {
    }

    /** Does nothing on this platform. */
    actual fun shareApp() {
    }

    /** Opens `url` in the default browser, where the desktop supports it. */
    actual fun openUrl(url: String) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop()
                    .isSupported(Desktop.Action.BROWSE)
            ) {
                Desktop.getDesktop().browse(URI(url))
            }
        } catch (e: Exception) {
            println("Error opening URL: ${e.message}")
        }
    }

    /** Does nothing on this platform. */
    actual fun ossLicensesMenuActivity() {
    }

    /** Does nothing on this platform. */
    actual fun restartApplication() {
    }
}

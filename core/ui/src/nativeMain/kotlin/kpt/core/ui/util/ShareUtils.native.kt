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
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.dialogs.shareFile
import io.github.vinceglb.filekit.write
import kpt.core.ui.util.ImageUtil.compressImage
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual object ShareUtils {
    actual fun shareText(text: String) {
    }

    actual suspend fun shareImage(title: String, image: ImageBitmap) {
    }

    actual suspend fun shareImage(title: String, byte: ByteArray) {
        try {
            val compressedBytes =
                compressImage(
                    byte,
                )

            val fileToShare = saveFile(data = compressedBytes, fileName = title)
            FileKit.shareFile(fileToShare)
        } catch (e: Exception) {
            println("Failed to share file: ${e.message}")
        }
    }

    /**
     * Saves a byte array as a file inside the iOS app's cache directory.
     *
     * Converts the resulting file path to a properly scoped [NSURL],
     * which is necessary for iOS to allow sharing via `UIActivityViewController`.
     *
     * @param data The file content to write.
     * @param fileName The name of the file to create.
     * @return A [PlatformFile] backed by a scoped `NSURL`, ready for sharing.
     */
    private suspend fun saveFile(data: ByteArray, fileName: String): PlatformFile {
        val tempFile = PlatformFile(FileKit.cacheDir, fileName)
        tempFile.write(data)

        /**
         * iOS requires file URLs used in `UIActivityViewController` to be created
         * with `NSURL.fileURLWithPath(...)` to ensure they have proper sandbox access.
         *
         * If the file is created from a raw path string, the system may reject it
         * with a sandbox extension error (e.g., "Cannot issue sandbox extension for URL").
         *
         * Wrapping the path in `NSURL` ensures the file is treated as a valid
         * security-scoped resource.
         */
        val nsUrl = NSURL.fileURLWithPath(tempFile.absolutePath())
        return PlatformFile(nsUrl)
    }

    actual fun callHelpline() {
        val url = NSURL.URLWithString("tel://8000000000")
        if (url?.let { UIApplication.sharedApplication.canOpenURL(it) } == true) {
            UIApplication.sharedApplication.openURL(url)
        }
    }

    actual fun mailHelpline() {
        val url =
            "mailto:support@example.com?subject=Help%20Request&body=Hello,%20I%20need%20assistance%20with..."
        val mailUrl = NSURL.URLWithString(url)

        if (mailUrl?.let { UIApplication.sharedApplication.canOpenURL(it) } == true) {
            UIApplication.sharedApplication.openURL(mailUrl)
        }
    }

    actual fun openAppInfo() {
    }

    actual fun shareApp() {
    }

    actual fun openUrl(url: String) {
        val nsUrl = NSURL.URLWithString(url)
        if (nsUrl != null) {
            UIApplication.sharedApplication.openURL(nsUrl)
        }
    }

    actual fun ossLicensesMenuActivity() {
    }

    actual fun restartApplication() {
    }
}

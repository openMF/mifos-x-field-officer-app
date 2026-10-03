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

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
fun imageToByteArray(image: String?): ByteArray? {
    if (image.isNullOrBlank()) return null
    val base64String = image.substringAfter(",")
    val cleanBase64 = base64String
        .replace("\\s".toRegex(), "")
        .replace("[^A-Za-z0-9+/=]".toRegex(), "")

    try {
        val decodedBytes = Base64.decode(cleanBase64)
        val decodedBitmap = ImageUtil.compressImage(decodedBytes)
        return decodedBitmap
    } catch (e: Exception) {
        return null
    }
}

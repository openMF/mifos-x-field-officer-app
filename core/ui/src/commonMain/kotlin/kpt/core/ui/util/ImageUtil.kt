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

/** Image resizing for uploads. */
expect object ImageUtil {
    /** Widest an image is scaled to, in pixels. */
    val DEFAULT_MAX_WIDTH: Float

    /** Tallest an image is scaled to, in pixels. */
    val DEFAULT_MAX_HEIGHT: Float

    /** Scales the image down to fit `maxWidth` × `maxHeight` and re-encodes it. */
    fun compressImage(
        decodedBytes: ByteArray,
        maxWidth: Float = DEFAULT_MAX_WIDTH,
        maxHeight: Float = DEFAULT_MAX_HEIGHT,
    ): ByteArray
}

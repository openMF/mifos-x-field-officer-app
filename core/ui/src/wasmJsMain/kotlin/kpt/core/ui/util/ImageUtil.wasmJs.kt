/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui.util

/** Image helpers for the browser, which has no bitmap re-encoder: images pass through unchanged. */
actual object ImageUtil {
    /** Widest an image is scaled to, in pixels, where scaling is supported. */
    actual val DEFAULT_MAX_WIDTH: Float = 816f

    /** Tallest an image is scaled to, in pixels, where scaling is supported. */
    actual val DEFAULT_MAX_HEIGHT: Float = 612f

    /** Returns [decodedBytes] unchanged: the browser target has no codec to re-encode with. */
    actual fun compressImage(
        decodedBytes: ByteArray,
        maxWidth: Float,
        maxHeight: Float,
    ): ByteArray = decodedBytes
}

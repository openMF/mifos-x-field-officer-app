/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui.components

/** What a document's bytes can be shown as. */
enum class DocumentKind {
    /** A PDF document. */
    Pdf,

    /** An image. */
    Image,

    /** Any other file type. */
    Unsupported,
}

/**
 * Decides how to show a document.
 *
 * The CONTENT is asked first — a file's leading bytes cannot be mislabelled, while a Fineract
 * `fileName` or `type` is whatever the uploading client sent. The original keyed on the extension
 * alone (`if (extension == "pdf")`), so a scan uploaded as `IMG_0042` rendered as nothing. The
 * declared MIME type and the extension are the fallbacks for a format the signatures below do not
 * cover.
 */
fun documentKindOf(bytes: ByteArray, fileName: String? = null, mimeType: String? = null): DocumentKind {
    val mime = mimeType?.lowercase().orEmpty()
    val extension = fileName?.substringAfterLast('.', missingDelimiterValue = "")?.lowercase().orEmpty()
    return kindFromSignature(bytes) ?: when {
        mime == "application/pdf" || extension == "pdf" -> DocumentKind.Pdf
        mime.startsWith("image/") || extension in IMAGE_EXTENSIONS -> DocumentKind.Image
        else -> DocumentKind.Unsupported
    }
}

private fun kindFromSignature(bytes: ByteArray): DocumentKind? {
    fun startsWith(vararg signature: Int) =
        bytes.size >= signature.size && signature.indices.all { bytes[it] == signature[it].toByte() }

    val isWebp = startsWith(0x52, 0x49, 0x46, 0x46) && bytes.size >= 12 &&
        bytes.copyOfRange(8, 12).decodeToString() == "WEBP"
    return when {
        startsWith(0x25, 0x50, 0x44, 0x46) -> DocumentKind.Pdf // %PDF
        startsWith(0x89, 0x50, 0x4E, 0x47) -> DocumentKind.Image // PNG
        startsWith(0xFF, 0xD8, 0xFF) -> DocumentKind.Image // JPEG
        startsWith(0x47, 0x49, 0x46, 0x38) -> DocumentKind.Image // GIF8
        isWebp -> DocumentKind.Image
        else -> null
    }
}

private val IMAGE_EXTENSIONS = setOf("png", "jpg", "jpeg", "gif", "webp", "bmp")

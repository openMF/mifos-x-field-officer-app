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

import kotlin.test.Test
import kotlin.test.assertEquals

class DocumentKindTest {

    private fun bytes(vararg values: Int) = ByteArray(values.size) { values[it].toByte() }

    private val pdf = "%PDF-1.7".encodeToByteArray()
    private val png = bytes(0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A)
    private val jpeg = bytes(0xFF, 0xD8, 0xFF, 0xE0)
    private val webp = "RIFF\u0000\u0000\u0000\u0000WEBPVP8 ".encodeToByteArray()
    private val unknown = "hello".encodeToByteArray()

    @Test
    fun theContentDecidesBeforeTheName() {
        // The original keyed on the extension alone, so a scan uploaded as `IMG_0042` (no
        // extension) rendered as nothing, and a PDF named `.jpg` went to the image path.
        assertEquals(DocumentKind.Pdf, documentKindOf(pdf, fileName = "IMG_0042"))
        assertEquals(DocumentKind.Pdf, documentKindOf(pdf, fileName = "scan.jpg", mimeType = "image/jpeg"))
        assertEquals(DocumentKind.Image, documentKindOf(png, fileName = "statement.pdf"))
    }

    @Test
    fun eachImageSignatureIsRecognised() {
        assertEquals(DocumentKind.Image, documentKindOf(png))
        assertEquals(DocumentKind.Image, documentKindOf(jpeg))
        assertEquals(DocumentKind.Image, documentKindOf("GIF89a".encodeToByteArray()))
        assertEquals(DocumentKind.Image, documentKindOf(webp))
    }

    @Test
    fun theDeclaredTypeIsTheFallback() {
        assertEquals(DocumentKind.Pdf, documentKindOf(unknown, mimeType = "application/PDF"))
        assertEquals(DocumentKind.Image, documentKindOf(unknown, mimeType = "image/heic"))
        assertEquals(DocumentKind.Pdf, documentKindOf(unknown, fileName = "deed.PDF"))
        assertEquals(DocumentKind.Image, documentKindOf(unknown, fileName = "photo.JPEG"))
    }

    @Test
    fun anythingElseIsUnsupportedRatherThanGuessed() {
        assertEquals(DocumentKind.Unsupported, documentKindOf(unknown, fileName = "report.docx"))
        assertEquals(DocumentKind.Unsupported, documentKindOf(ByteArray(0)))
        // A RIFF container that is not WebP (e.g. a WAV) must not render as an image.
        assertEquals(DocumentKind.Unsupported, documentKindOf("RIFF\u0000\u0000\u0000\u0000WAVE".encodeToByteArray()))
    }
}

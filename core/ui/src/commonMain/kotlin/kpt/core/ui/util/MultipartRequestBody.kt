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

import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.name
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.InternalAPI
import kpt.core.model.objects.noncoreobjects.DocumentUpload
import kotlin.text.substringBeforeLast

/** Multipart form body carrying `file`, plus a description when given. */
@OptIn(InternalAPI::class)
fun multipartRequestBody(
    file: ByteArray,
    name: String,
    extension: String,
    description: String? = null,
): MultiPartFormDataContent {
    val fileName = "${name.substringBeforeLast(".")}.${extension.lowercase()}"
    val mimeType = getMimeTypeFromPlatformFile(extension)
    return MultiPartFormDataContent(
        formData {
            append(
                "file",
                file,
                Headers.build {
                    append(HttpHeaders.ContentType, mimeType)
                    append(HttpHeaders.ContentDisposition, "filename=\"${fileName}\"")
                },
            )
            append("name", name)
            description?.let { append("description", it) }
        },
    )
}

/**
 * What the officer picked, as a [DocumentUpload] — the document write path's input. The multipart
 * encoding lives in `core/network` now; the UI only names the file it chose.
 */
fun documentUpload(
    file: ByteArray,
    name: String,
    extension: String,
    description: String? = null,
): DocumentUpload = DocumentUpload(
    name = name,
    fileName = "${name.substringBeforeLast(".")}.${extension.lowercase()}",
    mimeType = getMimeTypeFromPlatformFile(extension),
    bytes = file,
    description = description,
)

/** MIME type for a file extension. */
fun getMimeTypeFromPlatformFile(extension: String): String {
    return when (extension.lowercase()) {
        "jpeg", "jpg" -> "image/jpeg"
        "png" -> "image/png"
        "pdf" -> "application/pdf"
        "doc" -> "application/msword"
        "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        "xls" -> "application/vnd.ms-excel"
        "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        else -> "application/octet-stream"
    }
}

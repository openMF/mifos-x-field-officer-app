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

import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.name
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.InternalAPI
import kotlin.text.substringBeforeLast

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

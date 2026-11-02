/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.document

import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kpt.core.model.objects.noncoreobjects.DocumentUpload

/**
 * The wire encoding of a [DocumentUpload] for Fineract's `POST|PUT {entity}/{id}/documents`.
 *
 * Moved here from `core/ui` (`multipartRequestBody`): HTTP encoding is the network layer's job, and
 * leaving it in the UI layer meant every write handed `core/data` an opaque body it could not
 * persist. Same parts, same headers, byte for byte.
 */
fun DocumentUpload.toMultipart(): MultiPartFormDataContent = MultiPartFormDataContent(
    formData {
        append(
            "file",
            bytes,
            Headers.build {
                append(HttpHeaders.ContentType, mimeType)
                append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
            },
        )
        append("name", name)
        description?.let { append("description", it) }
    },
)

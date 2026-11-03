/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.noncoreobjects

/**
 * A document to attach to a Fineract entity — what the officer picked, in domain terms.
 *
 * The write path used to take a pre-built `MultiPartFormDataContent` from `core/ui`, so the data
 * layer could send the file but never knew WHAT it sent, and could not record the new document
 * locally. With this, `core/data` hands the upload to `core/network` for encoding AND writes the
 * same name / file / bytes to the local stores once the server accepts it.
 *
 * @param name        the document's display name (Fineract `name`).
 * @param fileName    the file name sent in the multipart part, extension included.
 * @param mimeType    the part's content type.
 * @param description Fineract `description` — for an identifier scan, the lookup key.
 */
class DocumentUpload(
    val name: String,
    val fileName: String,
    val mimeType: String,
    /** The file's content. */
    val bytes: ByteArray,
    val description: String? = null,
)

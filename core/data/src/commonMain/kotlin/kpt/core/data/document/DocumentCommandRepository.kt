/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.document

import kpt.core.base.store.mutation.MutationResult
import kpt.core.model.objects.noncoreobjects.DocumentUpload
import kpt.core.model.objects.responses.SaveResponse
import kpt.core.model.shared.GenericResponse

/**
 * Every document write, as an explicit outcome.
 *
 * Each returns [MutationResult] rather than `Unit`: a field officer has to be told whether the
 * change reached the server, was refused because the device is offline, or failed — a distinction
 * that cannot be reconstructed above this layer.
 *
 * Writes go the same way reads do — network → store → database: the command reaches Fineract, and
 * the server's acceptance is recorded through the document stores, so every document stream already
 * shows it when the call returns. Offline CAPTURE is a different operation with its own explicit
 * queue ([kpt.core.data.sync.OfflineQueueRepository]); it is never a silent fallback from here.
 */
interface DocumentCommandRepository {

    /** [SaveResponse.resourceId] is the new document's id. */
    suspend fun createDocument(entityType: String, entityId: Int, upload: DocumentUpload): MutationResult<SaveResponse>

    /** Deletes a document of an entity. */
    suspend fun removeDocument(entityType: String, entityId: Int, documentId: Int): MutationResult<GenericResponse>

    /** Replaces a document of an entity. */
    suspend fun updateDocument(
        entityType: String,
        entityId: Int,
        documentId: Int,
        upload: DocumentUpload,
    ): MutationResult<GenericResponse>
}

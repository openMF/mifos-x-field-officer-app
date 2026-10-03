/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.document

import io.ktor.client.request.forms.MultiPartFormDataContent
import kpt.core.base.store.mutation.MutationResult
import kpt.core.model.shared.GenericResponse

/**
 * Every document write, as an explicit outcome.
 *
 * Each returns [MutationResult] rather than `Unit`: a field officer has to be told whether the
 * change reached the server, was refused because the device is offline, or failed — a distinction
 * that cannot be reconstructed above this layer.
 *
 * These are commands, so they carry no Store and no cache. Offline CAPTURE is a different
 * operation with its own explicit queue ([kpt.core.data.sync.OfflineQueueRepository]); it is never
 * a silent fallback from here.
 */
interface DocumentCommandRepository {

    suspend fun createDocument(entityType: String, entityId: Int, request: MultiPartFormDataContent): MutationResult<GenericResponse>

    suspend fun removeDocument(entityType: String, entityId: Int, documentId: Int): MutationResult<GenericResponse>

    suspend fun updateDocument(entityType: String, entityId: Int, documentId: Int, request: MultiPartFormDataContent): MutationResult<GenericResponse>
}

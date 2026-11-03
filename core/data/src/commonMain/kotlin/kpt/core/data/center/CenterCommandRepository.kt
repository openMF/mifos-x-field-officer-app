/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.center

import kpt.core.base.store.mutation.MutationResult
import kpt.core.model.center.CenterPayload
import kpt.core.model.center.PostCentersCenterIdRequest
import kpt.core.model.center.PostCentersCenterIdResponse
import kpt.core.model.collectionsheet.payload.CollectionSheetPayload
import kpt.core.model.objects.clients.ActivatePayload
import kpt.core.model.objects.responses.SaveResponse
import kpt.core.model.shared.GenericResponse

/**
 * Every center write, as an explicit outcome.
 *
 * Each returns [MutationResult] rather than `Unit`: a field officer has to be told whether the
 * change reached the server, was refused because the device is offline, or failed — a distinction
 * that cannot be reconstructed above this layer.
 *
 * They run online-only, and a write the server accepts is recorded locally through the center
 * stores, so the reads see it. Offline CAPTURE is a different operation with its own explicit queue
 * ([kpt.core.data.sync.OfflineQueueRepository]); it is never a silent fallback from here.
 *
 * Generating a collection sheet is a READ, so it lives on [CenterRepository.collectionSheet].
 */
interface CenterCommandRepository {

    /** Runs a Fineract `command` (close, activate, …) on a centre. */
    suspend fun activate2(
        centerId: Long,
        postCentersCenterIdRequest: PostCentersCenterIdRequest,
        command: String? = null,
    ): MutationResult<PostCentersCenterIdResponse>

    /** Saves a centre's collection sheet. */
    suspend fun saveCollectionSheet(
        centerId: Int,
        collectionSheetPayload: CollectionSheetPayload?,
    ): MutationResult<SaveResponse>

    /** Saves a centre's collection sheet through the endpoint that returns the raw response. */
    suspend fun saveCollectionSheetAsync(
        centerId: Int,
        collectionSheetPayload: CollectionSheetPayload?,
    ): MutationResult<SaveResponse>

    /** Creates a centre. */
    suspend fun createCenter(centerPayload: CenterPayload?): MutationResult<SaveResponse>

    /** Activates a centre. */
    suspend fun activateCenter(centerId: Int, activatePayload: ActivatePayload?): MutationResult<GenericResponse>
}

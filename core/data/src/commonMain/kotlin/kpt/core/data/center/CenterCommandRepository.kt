/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.center

import kpt.core.base.store.mutation.MutationResult
import kpt.core.database.center.entity.CenterPayloadEntity
import kpt.core.model.objects.clients.ActivatePayload
import kpt.core.model.objects.databaseobjects.CollectionSheet
import kpt.core.model.objects.responses.SaveResponse
import kpt.core.model.shared.GenericResponse
import kpt.core.model.shared.Payload
import kpt.core.network.mifos.center.dto.PostCentersCenterIdRequest
import kpt.core.network.mifos.center.dto.PostCentersCenterIdResponse
import kpt.core.network.mifos.collectionsheet.dto.CollectionSheetPayload

/**
 * Every center write, as an explicit outcome.
 *
 * Each returns [MutationResult] rather than `Unit`: a field officer has to be told whether the
 * change reached the server, was refused because the device is offline, or failed — a distinction
 * that cannot be reconstructed above this layer.
 *
 * These are commands, so they carry no Store and no cache. Offline CAPTURE is a different
 * operation with its own explicit queue ([kpt.core.data.sync.OfflineQueueRepository]); it is never
 * a silent fallback from here.
 */
interface CenterCommandRepository {

    suspend fun activate2(centerId: Long, postCentersCenterIdRequest: PostCentersCenterIdRequest, command: String? = null): MutationResult<PostCentersCenterIdResponse>

    suspend fun getCollectionSheet(centerId: Long, payload: Payload?): MutationResult<CollectionSheet>

    suspend fun saveCollectionSheet(centerId: Int, collectionSheetPayload: CollectionSheetPayload?): MutationResult<SaveResponse>

    suspend fun saveCollectionSheetAsync(centerId: Int, collectionSheetPayload: CollectionSheetPayload?): MutationResult<SaveResponse>

    suspend fun createCenter(centerPayload: CenterPayloadEntity?): MutationResult<SaveResponse>

    suspend fun activateCenter(centerId: Int, activatePayload: ActivatePayload?): MutationResult<GenericResponse>
}

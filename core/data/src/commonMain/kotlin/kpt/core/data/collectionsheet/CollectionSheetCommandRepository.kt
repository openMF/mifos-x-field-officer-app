/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.collectionsheet

import kpt.core.base.store.mutation.MutationResult
import kpt.core.database.collectionsheet.entity.CollectionSheetResponse
import kpt.core.database.collectionsheet.entity.IndividualCollectionSheet
import kpt.core.model.collectionsheet.CollectionSheetPayload
import kpt.core.model.collectionsheet.ProductiveCollectionSheetPayload
import kpt.core.model.objects.collectionsheets.CollectionSheetRequestPayload
import kpt.core.model.shared.GenericResponse
import kpt.core.network.mifos.collectionsheet.dto.IndividualCollectionSheetPayload
import kpt.core.network.mifos.collectionsheet.dto.RequestCollectionSheetPayload

/**
 * Every collectionsheet write, as an explicit outcome.
 *
 * Each returns [MutationResult] rather than `Unit`: a field officer has to be told whether the
 * change reached the server, was refused because the device is offline, or failed — a distinction
 * that cannot be reconstructed above this layer.
 *
 * These are commands, so they carry no Store and no cache. Offline CAPTURE is a different
 * operation with its own explicit queue ([kpt.core.data.sync.OfflineQueueRepository]); it is never
 * a silent fallback from here.
 */
interface CollectionSheetCommandRepository {

    suspend fun getIndividualCollectionSheet(payload: RequestCollectionSheetPayload?): MutationResult<IndividualCollectionSheet>

    suspend fun saveIndividualCollectionSheet(payload: IndividualCollectionSheetPayload?): MutationResult<GenericResponse>

    suspend fun fetchProductiveSheet(centerId: Int, payload: CollectionSheetRequestPayload?): MutationResult<CollectionSheetResponse>

    suspend fun submitProductiveSheet(centerId: Int, payload: ProductiveCollectionSheetPayload?): MutationResult<GenericResponse>

    suspend fun fetchCollectionSheet(groupId: Int, payload: CollectionSheetRequestPayload?): MutationResult<CollectionSheetResponse>

    suspend fun submitCollectionSheet(groupId: Int, payload: CollectionSheetPayload?): MutationResult<GenericResponse>
}

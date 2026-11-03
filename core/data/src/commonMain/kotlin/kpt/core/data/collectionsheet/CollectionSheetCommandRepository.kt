/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.collectionsheet

import kpt.core.base.store.mutation.MutationResult
import kpt.core.model.collectionsheet.CollectionSheetPayload
import kpt.core.model.collectionsheet.IndividualCollectionSheetPayload
import kpt.core.model.collectionsheet.ProductiveCollectionSheetPayload
import kpt.core.model.collectionsheet.RequestCollectionSheetPayload
import kpt.core.model.shared.GenericResponse

/**
 * Every collectionsheet write, as an explicit outcome.
 *
 * Each returns [MutationResult] rather than `Unit`: a field officer has to be told whether the
 * change reached the server, was refused because the device is offline, or failed — a distinction
 * that cannot be reconstructed above this layer.
 *
 * They run online-only — financial writes reach Fineract or they are not real — and a save the
 * server accepts re-reads the sheet it saved through the sheet stores, so a later read never serves
 * the pre-save sheet. Offline CAPTURE is a different operation with its own explicit queue
 * ([kpt.core.data.sync.OfflineQueueRepository]); it is never a silent fallback from here.
 *
 * GENERATING a sheet is a read (POST-shaped in Fineract), so those live on [CollectionSheetRepository].
 */
interface CollectionSheetCommandRepository {

    /**
     * [request] is the generate request the sheet came from: the save body carries no office or
     * staff, so without it the saved sheet could not be found again to record the outcome against.
     */
    suspend fun saveIndividualCollectionSheet(
        request: RequestCollectionSheetPayload,
        payload: IndividualCollectionSheetPayload?,
    ): MutationResult<GenericResponse>

    /** Submits a centre's productive collection sheet. */
    suspend fun submitProductiveSheet(
        centerId: Int,
        payload: ProductiveCollectionSheetPayload?,
    ): MutationResult<GenericResponse>

    /** Submits a group's collection sheet. */
    suspend fun submitCollectionSheet(groupId: Int, payload: CollectionSheetPayload?): MutationResult<GenericResponse>
}

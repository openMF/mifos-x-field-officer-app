/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.testing

import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.collectionsheet.CollectionSheetCommandRepository
import kpt.core.model.collectionsheet.CollectionSheetPayload
import kpt.core.model.collectionsheet.IndividualCollectionSheetPayload
import kpt.core.model.collectionsheet.ProductiveCollectionSheetPayload
import kpt.core.model.collectionsheet.RequestCollectionSheetPayload
import kpt.core.model.shared.GenericResponse

/** Records each individual-sheet save — what was sent, for which request — and answers [result]. */
@ExperimentalDataTestingApi
class FakeCollectionSheetCommandRepository(
    private val result: () -> MutationResult<GenericResponse> = {
        MutationResult.Applied(
            GenericResponse(),
            synced = true,
        )
    },
) : CollectionSheetCommandRepository {

    data class Save(val request: RequestCollectionSheetPayload, val payload: IndividualCollectionSheetPayload?)

    val saved = mutableListOf<Save>()

    override suspend fun saveIndividualCollectionSheet(
        request: RequestCollectionSheetPayload,
        payload: IndividualCollectionSheetPayload?,
    ): MutationResult<GenericResponse> {
        saved += Save(
            request.copy(),
            payload?.copy(bulkRepaymentTransactions = ArrayList(payload.bulkRepaymentTransactions)),
        )
        return result()
    }

    override suspend fun submitProductiveSheet(centerId: Int, payload: ProductiveCollectionSheetPayload?) = result()

    override suspend fun submitCollectionSheet(groupId: Int, payload: CollectionSheetPayload?) = result()
}

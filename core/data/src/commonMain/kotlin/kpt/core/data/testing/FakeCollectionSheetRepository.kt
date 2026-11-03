/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(kpt.core.base.store.screen.ExperimentalScreenDataStreamTestingApi::class)

package kpt.core.data.testing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.data.collectionsheet.CollectionSheetRepository
import kpt.core.model.collectionsheet.CenterDetail
import kpt.core.model.collectionsheet.CollectionSheetResponse
import kpt.core.model.collectionsheet.IndividualCollectionSheet
import kpt.core.model.collectionsheet.RequestCollectionSheetPayload
import kpt.core.model.objects.collectionsheets.CollectionSheetRequestPayload

/**
 * The individual sheet a test drives through [sheet]; [requested] records every request streamed.
 * The center/group reads are not exercised by any feature yet and answer "nothing".
 */
@ExperimentalDataTestingApi
class FakeCollectionSheetRepository(
    initial: ScreenState<IndividualCollectionSheet> = ScreenState.Loading,
) : CollectionSheetRepository {

    /** The collection sheet the stream emits. */
    val sheet = MutableStateFlow(initial)

    /** The sheet requests, in order. */
    val requested = mutableListOf<RequestCollectionSheetPayload>()

    override fun individualCollectionSheetStream(request: RequestCollectionSheetPayload, scope: CoroutineScope) =
        screenDataStreamForTesting(sheet).also { requested += request }

    override fun centerDetailsStream(officeId: Int, staffId: Int, meetingDate: String, scope: CoroutineScope) =
        screenDataStreamForTesting(flowOf<ScreenState<List<CenterDetail>>>(ScreenState.Empty))

    override suspend fun individualCollectionSheet(
        request: RequestCollectionSheetPayload,
    ): IndividualCollectionSheet? = null

    override suspend fun productiveSheet(
        centerId: Int,
        request: CollectionSheetRequestPayload,
    ): CollectionSheetResponse? = null

    override suspend fun groupCollectionSheet(
        groupId: Int,
        request: CollectionSheetRequestPayload,
    ): CollectionSheetResponse? = null
}

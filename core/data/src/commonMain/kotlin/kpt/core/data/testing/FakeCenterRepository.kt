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
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.data.center.CenterRepository
import kpt.core.model.center.Center
import kpt.core.model.center.CenterWithAssociations
import kpt.core.model.objects.databaseobjects.CollectionSheet
import kpt.core.model.objects.groups.CenterInfo
import kpt.core.model.shared.Payload

/** Scripted centres: [centers] is the list; [withMeeting] holds each centre's calendar by id. */
@ExperimentalDataTestingApi
class FakeCenterRepository(
    centers: List<Center> = emptyList(),
    withMeeting: Map<Int, CenterWithAssociations> = emptyMap(),
) : CenterRepository {

    /** The centres the stream emits. */
    val centers = MutableStateFlow<ScreenState<List<Center>>>(ScreenState.Content(centers))
    private val withMeeting = withMeeting

    override fun centersStream(scope: CoroutineScope) = screenDataStreamForTesting(centers)

    override fun centerWithMeetingStream(centerId: Int, scope: CoroutineScope) =
        screenDataStreamForTesting(
            MutableStateFlow<ScreenState<CenterWithAssociations>>(
                ScreenState.Content(withMeeting[centerId] ?: CenterWithAssociations(id = centerId)),
            ),
        )

    override fun centerSummaryStream(centerId: Int, scope: CoroutineScope) =
        screenDataStreamForTesting(MutableStateFlow<ScreenState<List<CenterInfo>>>(ScreenState.Content(emptyList())))

    override suspend fun collectionSheet(centerId: Long, payload: Payload): CollectionSheet? = null
}

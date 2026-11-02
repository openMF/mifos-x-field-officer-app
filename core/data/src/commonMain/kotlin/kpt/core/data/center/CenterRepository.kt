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

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.model.center.Center
import kpt.core.model.center.CenterWithAssociations
import kpt.core.model.objects.databaseobjects.CollectionSheet
import kpt.core.model.objects.groups.CenterInfo
import kpt.core.model.shared.Payload

interface CenterRepository {
    /** Centers the officer services. Replaces the legacy CenterList + CenterDetails pair. */
    fun centersStream(scope: CoroutineScope): ScreenDataStream<List<Center>>

    /**
     * One center with its group members and collection-meeting calendar — the detail's meeting rows
     * (next meeting, frequency) and activation date.
     */
    fun centerWithMeetingStream(centerId: Int, scope: CoroutineScope): ScreenDataStream<CenterWithAssociations>

    /**
     * The center's summary counts (`runreports/GroupSummaryCounts`) — active clients, loans and
     * borrowers, overdue loans. Its own stream: a role without report access (Fineract 403) fails
     * this section only, never the center's details.
     */
    fun centerSummaryStream(centerId: Int, scope: CoroutineScope): ScreenDataStream<List<CenterInfo>>

    /**
     * The center's collection sheet for the meeting [payload] selects. Fineract's
     * `?command=generateCollectionSheet` is POST-shaped but a read; it was misfiled as a command.
     * Network first; offline, the last sheet generated for that meeting. `null` when neither exists.
     */
    suspend fun collectionSheet(centerId: Long, payload: Payload): CollectionSheet?
}

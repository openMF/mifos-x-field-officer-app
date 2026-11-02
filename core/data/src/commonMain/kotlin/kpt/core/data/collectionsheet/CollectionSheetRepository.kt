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

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.model.collectionsheet.CenterDetail
import kpt.core.model.collectionsheet.CollectionSheetResponse
import kpt.core.model.collectionsheet.IndividualCollectionSheet
import kpt.core.model.collectionsheet.RequestCollectionSheetPayload
import kpt.core.model.objects.collectionsheets.CollectionSheetRequestPayload

interface CollectionSheetRepository {
    /**
     * The centre sheet for one (office, staff, meeting date).
     *
     * A sheet is only meaningful for that triple, so all three are part of the address — a date
     * change must re-fetch rather than serve the previous meeting's sheet.
     */
    fun centerDetailsStream(
        officeId: Int,
        staffId: Int,
        meetingDate: String,
        scope: CoroutineScope,
    ): ScreenDataStream<List<CenterDetail>>

    /**
     * The individual collection sheet for one (office, staff, date) request — network first, and
     * offline the last sheet generated for it. A sheet with no loan due is still CONTENT here; a screen
     * that shows "nothing due" applies `emptyIfContent` itself.
     */
    fun individualCollectionSheetStream(
        request: RequestCollectionSheetPayload,
        scope: CoroutineScope,
    ): ScreenDataStream<IndividualCollectionSheet>

    /*
     * Generated sheets. Fineract's `?command=generateCollectionSheet` is POST-shaped but a read —
     * these were misfiled as commands. Each is network first and, offline, the last sheet generated
     * for that request; `null` when neither exists.
     */

    suspend fun individualCollectionSheet(request: RequestCollectionSheetPayload): IndividualCollectionSheet?

    suspend fun productiveSheet(centerId: Int, request: CollectionSheetRequestPayload): CollectionSheetResponse?

    suspend fun groupCollectionSheet(groupId: Int, request: CollectionSheetRequestPayload): CollectionSheetResponse?
}

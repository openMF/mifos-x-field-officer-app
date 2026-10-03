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

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.database.collectionsheet.entity.CenterDetail

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
}

/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.collectionsheet.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kpt.core.base.database.annotation.DbEntity

/**
 * A staff member's centre-meeting roster — the ONE cacheable read on the collection-sheet API.
 *
 * The rest of that api is `?command=generateCollectionSheet` / `saveCollectionSheet`: a sheet is
 * GENERATED server-side against live balances and then SAVED, so neither is cacheable and both
 * stay online-only (D1 — financial writes reach Fineract or they are not real).
 *
 * What a field officer genuinely needs offline is WHICH centres meet and when, so that is what is
 * stored. `meetingFallCenters` persists as JSON through `CollectionSheetTypeConverters`, matching
 * how the rest of this database already holds nested lists.
 */
@DbEntity
@Entity(tableName = "collection_sheet_center_details")
data class CenterDetailEntity(
    @PrimaryKey
    val staffId: Int,
    val staffName: String? = null,
    val meetingFallCenters: List<MeetingFallCalendar>? = null,
)

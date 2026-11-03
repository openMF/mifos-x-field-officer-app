/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.collectionsheet.converter

import androidx.room3.ColumnTypeConverter
import kotlinx.serialization.json.Json
import kpt.core.base.database.annotation.DbConverters
import kpt.core.database.collectionsheet.entity.MeetingFallCalendar

/** Room column converters for collection sheet fields. */
@DbConverters
class CollectionSheetTypeConverters {

    /** Stores a `List<MeetingFallCalendar>?` in a JSON column. */
    @ColumnTypeConverter
    fun fromMeetingFallCalendarList(meetings: List<MeetingFallCalendar>?): String =
        Json.encodeToString(meetings ?: emptyList())

    /** Reads a `List<MeetingFallCalendar>` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toMeetingFallCalendarList(json: String): List<MeetingFallCalendar> =
        if (json.isBlank()) emptyList() else Json.decodeFromString(json)
}

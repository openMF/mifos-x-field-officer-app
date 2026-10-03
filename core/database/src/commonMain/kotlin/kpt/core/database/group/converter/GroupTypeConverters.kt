/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.group.converter

import androidx.room3.ColumnTypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kpt.core.base.database.annotation.DbConverters
import kpt.core.database.group.entity.GroupDateEntity
import kpt.core.database.group.entity.GroupEntity
import kpt.core.model.shared.Timeline

/**
 * Created by Pronay Sarker on 17/02/2025 (7:45 AM)
 */
@DbConverters
class GroupTypeConverters {

    @ColumnTypeConverter
    fun fromGroupDate(date: GroupDateEntity?): String? {
        return date?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
    fun toGroupDate(json: String?): GroupDateEntity? {
        return json?.let { Json.decodeFromString(it) }
    }

    @ColumnTypeConverter
    fun fromTimeline(timeline: Timeline?): String? {
        return timeline?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
    fun toTimeline(json: String?): Timeline? {
        return json?.let { Json.decodeFromString(it) }
    }

    @ColumnTypeConverter
    fun fromListGroup(date: List<GroupEntity>): String {
        return date.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
    fun toListGroup(json: String): List<GroupEntity> {
        return json.let { Json.decodeFromString(it) }
    }
}

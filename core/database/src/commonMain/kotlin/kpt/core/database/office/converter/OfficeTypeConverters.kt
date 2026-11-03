/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.office.converter

import androidx.room3.ColumnTypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kpt.core.base.database.annotation.DbConverters
import kpt.core.database.office.entity.OfficeOpeningDateEntity

/** Room column converters for office fields. */
@DbConverters
class OfficeTypeConverters {
    /** Stores a `List<Int?>?` in a JSON column. */
    @ColumnTypeConverter
    fun fromOpeningDateList(list: List<Int?>?): String {
        return Json.encodeToString(list ?: emptyList())
    }

    /** Stores a `OfficeOpeningDateEntity?` in a JSON column. */
    @ColumnTypeConverter
    fun fromOfficeOpeningDate(status: OfficeOpeningDateEntity?): String? {
        return status?.let { Json.encodeToString(it) }
    }

    /** Reads a `OfficeOpeningDateEntity?` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toOfficeOpeningDate(json: String?): OfficeOpeningDateEntity? {
        return json?.let { Json.decodeFromString(it) }
    }
}

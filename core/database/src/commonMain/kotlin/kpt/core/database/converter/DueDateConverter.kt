/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.converter

import androidx.room3.ColumnTypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kpt.core.base.database.annotation.DbConverters

/** Room column converter for due dates. */
@DbConverters
class DueDateConverter {
    /** Stores a `List<Int>` in a JSON column. */
    @ColumnTypeConverter
    fun fromListToString(dueDate: List<Int>): String {
        return Json.encodeToString(dueDate)
    }

    /** Reads a `List<Int>` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun fromStringToList(dueDateString: String): List<Int> {
        return Json.decodeFromString(dueDateString)
    }
}

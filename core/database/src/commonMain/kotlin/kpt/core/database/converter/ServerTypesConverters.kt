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
import kpt.core.base.database.annotation.DbConverters
import kpt.core.database.savings.entity.SavingAccountDepositTypeEntity.ServerTypes

/** Room column converters for server-type enums. */
@DbConverters
class ServerTypesConverters {
    /** Converts a `Int?` column value to `ServerTypes?`. */
    @ColumnTypeConverter
    fun toServerTypes(id: Int?): ServerTypes? {
        return id?.let { ServerTypes.fromId(it) }
    }

    /** Converts a `ServerTypes?` column value to `Int?`. */
    @ColumnTypeConverter
    fun fromServerTypes(serverTypes: ServerTypes?): Int? {
        return serverTypes?.id
    }
}

/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.converter

import androidx.room3.ColumnTypeConverter
import kpt.core.base.database.annotation.DbConverters
import kpt.core.database.savings.entity.SavingAccountDepositTypeEntity.ServerTypes

@DbConverters
class ServerTypesConverters {
    @ColumnTypeConverter
    fun toServerTypes(id: Int?): ServerTypes? {
        return id?.let { ServerTypes.fromId(it) }
    }

    @ColumnTypeConverter
    fun fromServerTypes(serverTypes: ServerTypes?): Int? {
        return serverTypes?.id
    }
}

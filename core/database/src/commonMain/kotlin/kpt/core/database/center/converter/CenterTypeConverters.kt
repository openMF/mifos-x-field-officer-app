/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.center.converter

import androidx.room3.ColumnTypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kpt.core.base.database.annotation.DbConverters
import kpt.core.database.center.entity.CenterDateEntity
import kpt.core.database.savings.entity.SavingAccountDepositTypeEntity

/** Room column converters for center fields. */
@DbConverters
class CenterTypeConverters {

    /** Stores a `CenterDateEntity?` in a JSON column. */
    @ColumnTypeConverter
    fun fromCenterDate(centerDate: CenterDateEntity?): String? {
        return centerDate?.let { Json.encodeToString(it) }
    }

    /** Reads a `CenterDateEntity?` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toCenterDate(json: String?): CenterDateEntity? {
        return json?.let { Json.decodeFromString(it) }
    }

    /** Stores a `SavingAccountDepositTypeEntity?` in a JSON column. */
    @ColumnTypeConverter
    fun fromDepositType(type: SavingAccountDepositTypeEntity?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    /** Reads a `SavingAccountDepositTypeEntity?` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toDepositType(json: String?): SavingAccountDepositTypeEntity? {
        return json?.let { Json.decodeFromString(it) }
    }
}

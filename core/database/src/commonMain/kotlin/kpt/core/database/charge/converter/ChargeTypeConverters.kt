/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.charge.converter

import androidx.room3.ColumnTypeConverter
import kotlinx.serialization.json.Json
import kpt.core.base.database.annotation.DbConverters
import kpt.core.database.charge.entity.ChargeCalculationTypeEntity
import kpt.core.database.charge.entity.ChargeTimeTypeEntity
import kpt.core.database.charge.entity.ClientChargeCurrencyEntity

/** Room column converters for charge fields. */
@DbConverters
object ChargeTypeConverters {

    /** Stores a `ChargeTimeTypeEntity?` in a JSON column. */
    @ColumnTypeConverter
    fun fromChargeTimeType(type: ChargeTimeTypeEntity?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    /** Reads a `ChargeTimeTypeEntity?` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toChargeTimeType(json: String?): ChargeTimeTypeEntity? {
        return json?.let { Json.decodeFromString(it) }
    }

    /** Stores a `ChargeCalculationTypeEntity?` in a JSON column. */
    @ColumnTypeConverter
    fun fromChargeCalculationType(type: ChargeCalculationTypeEntity?): String? {
        return type?.let { Json.encodeToString(it) }
    }

    /** Reads a `ChargeCalculationTypeEntity?` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toChargeCalculationType(json: String?): ChargeCalculationTypeEntity? {
        return json?.let { Json.decodeFromString(it) }
    }

    /** Stores a `ClientChargeCurrencyEntity?` in a JSON column. */
    @ColumnTypeConverter
    fun fromClientChargeCurrency(currency: ClientChargeCurrencyEntity?): String? {
        return currency?.let { Json.encodeToString(it) }
    }

    /** Reads a `ClientChargeCurrencyEntity?` back from the JSON column it is stored in. */
    @ColumnTypeConverter
    fun toClientChargeCurrency(json: String?): ClientChargeCurrencyEntity? {
        return json?.let { Json.decodeFromString(it) }
    }
}

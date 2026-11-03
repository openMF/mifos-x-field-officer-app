/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.charge.entity

import androidx.room3.ColumnInfo
import kotlinx.serialization.Serializable

/**
 * A COLUMN, not a table.
 *
 * This is embedded in its parent entity and persisted by a `@ColumnTypeConverter` as a JSON
 * string. It carried `@DbEntity` as well, so Room also generated a standalone table that no
 * DAO ever read or wrote — dead schema that counted against the generated
 * `onValidateSchema`, which at 85 tables exceeded the JVM 64KB method limit and broke every
 * JVM target.
 */
@Serializable
data class ClientChargeCurrencyEntity(
    /** The server id of this record. */
    @ColumnInfo(
        index = true,
        name = ColumnInfo.INHERIT_FIELD_NAME,
        typeAffinity = ColumnInfo.UNDEFINED,
        collate = ColumnInfo.UNSPECIFIED,
        defaultValue = ColumnInfo.VALUE_UNSPECIFIED,
    )
    val id: Int = 0,

    /**
     * The Fineract code that identifies this value (an enum code such as `loanStatusType.active`, or an ISO
     * currency code).
     */
    val code: String? = null,

    /** The display name. */
    val name: String? = null,

    /** The number of decimal places amounts in this currency carry. */
    val decimalPlaces: Int? = null,

    /** The unit amounts in this currency are rounded to. */
    val inMultiplesOf: Int? = null,

    /** The currency symbol to display (e.g. `$`). */
    val displaySymbol: String? = null,

    /** The currency's name code used in translations (e.g. `currency.USD`). */
    val nameCode: String? = null,

    /** The currency's display label (name and symbol). */
    val displayLabel: String? = null,
)

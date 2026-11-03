/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.shared

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 *
 *
 * @param booleanDisplayType
 * @param codeLookupDisplayType
 * @param codeValueDisplayType
 * @param columnCode
 * @param columnDisplayType
 * @param columnLength
 * @param columnName
 * @param columnType
 * @param columnValues
 * @param dateDisplayType
 * @param dateTimeDisplayType
 * @param decimalDisplayType
 * @param integerDisplayType
 * @param isColumnIndexed
 * @param isColumnNullable
 * @param isColumnPrimaryKey
 * @param isColumnUnique
 * @param mandatory
 * @param stringDisplayType
 * @param textDisplayType
 * @param timeDisplayType
 */

@Serializable
data class ResultsetColumnHeaderData(

    val booleanDisplayType: Boolean? = null,

    val codeLookupDisplayType: Boolean? = null,

    val codeValueDisplayType: Boolean? = null,

    val columnCode: String? = null,

    val columnDisplayType: ColumnDisplayType? = null,

    val columnLength: Long? = null,

    val columnName: String? = null,

    val columnType: ColumnType? = null,

    val columnValues: List<@Contextual Any>? = null,

    val dateDisplayType: Boolean? = null,

    val dateTimeDisplayType: Boolean? = null,

    val decimalDisplayType: Boolean? = null,

    val integerDisplayType: Boolean? = null,

    val isColumnIndexed: Boolean? = null,

    val isColumnNullable: Boolean? = null,

    val isColumnPrimaryKey: Boolean? = null,

    val isColumnUnique: Boolean? = null,

    val mandatory: Boolean? = null,

    val stringDisplayType: Boolean? = null,

    val textDisplayType: Boolean? = null,

    val timeDisplayType: Boolean? = null,

) {

    /**
     *
     *
     * Values: TEXT,STRING,INTEGER,FLOAT,DECIMAL,DATE,TIME,DATETIME,BOOLEAN,BINARY,CODELOOKUP,CODEVALUE
     *
     * @property value the display type's name as Fineract sends it
     */
    @Serializable
    enum class ColumnDisplayType(val value: String) {
        /** Free text. */
        @SerialName("TEXT")
        TEXT("TEXT"),

        /** A text value. */
        @SerialName("STRING")
        STRING("STRING"),

        /** A whole number. */
        @SerialName("INTEGER")
        INTEGER("INTEGER"),

        /** A floating-point number. */
        @SerialName("FLOAT")
        FLOAT("FLOAT"),

        /** A decimal number. */
        @SerialName("DECIMAL")
        DECIMAL("DECIMAL"),

        /** A date. */
        @SerialName("DATE")
        DATE("DATE"),

        /** A time of day. */
        @SerialName("TIME")
        TIME("TIME"),

        /** A date and time. */
        @SerialName("DATETIME")
        DATETIME("DATETIME"),

        /** A yes/no value. */
        @SerialName("BOOLEAN")
        BOOLEAN("BOOLEAN"),

        /** Binary data. */
        @SerialName("BINARY")
        BINARY("BINARY"),

        /** A value picked from a code list, shown by its label. */
        @SerialName("CODELOOKUP")
        CODELOOKUP("CODELOOKUP"),

        /** A value picked from a code list. */
        @SerialName("CODEVALUE")
        CODEVALUE("CODEVALUE"),
    }

    /**
     *
     *
     * Values: BIT, BOOLEAN, SMALLINT, TINYINT, INTEGER, MEDIUMINT, BIGINT, REAL, FLOAT, DOUBLE, NUMERIC, DECIMAL,
     * SERIAL, SMALLSERIAL, BIGSERIAL, MONEY, CHAR, VARCHAR, LONGVARCHAR, TEXT, TINYTEXT, MEDIUMTEXT, LONGTEXT, JSON,
     * DATE, TIME, TIME_WITH_TIMEZONE, TIMESTAMP, DATETIME, TIMESTAMP_WITH_TIMEZONE, INTERVAL, BINARY, VARBINARY,
     * LONGVARBINARY, BYTEA, BLOB, TINYBLOB, MEDIUMBLOB, LONGBLOB
     *
     * @property value the column type's name as Fineract sends it
     */
    @Serializable
    enum class ColumnType(val value: String) {
        /** The `BIT` SQL column type. */
        @SerialName("BIT")
        BIT("BIT"),

        /** The `BOOLEAN` SQL column type. */
        @SerialName("BOOLEAN")
        BOOLEAN("BOOLEAN"),

        /** The `SMALLINT` SQL column type. */
        @SerialName("SMALLINT")
        SMALLINT("SMALLINT"),

        /** The `TINYINT` SQL column type. */
        @SerialName("TINYINT")
        TINYINT("TINYINT"),

        /** The `INTEGER` SQL column type. */
        @SerialName("INTEGER")
        INTEGER("INTEGER"),

        /** The `MEDIUMINT` SQL column type. */
        @SerialName("MEDIUMINT")
        MEDIUMINT("MEDIUMINT"),

        /** The `BIGINT` SQL column type. */
        @SerialName("BIGINT")
        BIGINT("BIGINT"),

        /** The `REAL` SQL column type. */
        @SerialName("REAL")
        REAL("REAL"),

        /** The `FLOAT` SQL column type. */
        @SerialName("FLOAT")
        FLOAT("FLOAT"),

        /** The `DOUBLE` SQL column type. */
        @SerialName("DOUBLE")
        DOUBLE("DOUBLE"),

        /** The `NUMERIC` SQL column type. */
        @SerialName("NUMERIC")
        NUMERIC("NUMERIC"),

        /** The `DECIMAL` SQL column type. */
        @SerialName("DECIMAL")
        DECIMAL("DECIMAL"),

        /** The `SERIAL` SQL column type. */
        @SerialName("SERIAL")
        SERIAL("SERIAL"),

        /** The `SMALLSERIAL` SQL column type. */
        @SerialName("SMALLSERIAL")
        SMALLSERIAL("SMALLSERIAL"),

        /** The `BIGSERIAL` SQL column type. */
        @SerialName("BIGSERIAL")
        BIGSERIAL("BIGSERIAL"),

        /** The `MONEY` SQL column type. */
        @SerialName("MONEY")
        MONEY("MONEY"),

        /** The `CHAR` SQL column type. */
        @SerialName("CHAR")
        CHAR("CHAR"),

        /** The `VARCHAR` SQL column type. */
        @SerialName("VARCHAR")
        VARCHAR("VARCHAR"),

        /** The `LONGVARCHAR` SQL column type. */
        @SerialName("LONGVARCHAR")
        LONGVARCHAR("LONGVARCHAR"),

        /** The `TEXT` SQL column type. */
        @SerialName("TEXT")
        TEXT("TEXT"),

        /** The `TINYTEXT` SQL column type. */
        @SerialName("TINYTEXT")
        TINYTEXT("TINYTEXT"),

        /** The `MEDIUMTEXT` SQL column type. */
        @SerialName("MEDIUMTEXT")
        MEDIUMTEXT("MEDIUMTEXT"),

        /** The `LONGTEXT` SQL column type. */
        @SerialName("LONGTEXT")
        LONGTEXT("LONGTEXT"),

        /** The `JSON` SQL column type. */
        @SerialName("JSON")
        JSON("JSON"),

        /** The `DATE` SQL column type. */
        @SerialName("DATE")
        DATE("DATE"),

        /** The `TIME` SQL column type. */
        @SerialName("TIME")
        TIME("TIME"),

        /** The `TIME_WITH_TIMEZONE` SQL column type. */
        @SerialName("TIME_WITH_TIMEZONE")
        TIME_WITH_TIMEZONE("TIME_WITH_TIMEZONE"),

        /** The `TIMESTAMP` SQL column type. */
        @SerialName("TIMESTAMP")
        TIMESTAMP("TIMESTAMP"),

        /** The `DATETIME` SQL column type. */
        @SerialName("DATETIME")
        DATETIME("DATETIME"),

        /** The `TIMESTAMP_WITH_TIMEZONE` SQL column type. */
        @SerialName("TIMESTAMP_WITH_TIMEZONE")
        TIMESTAMP_WITH_TIMEZONE("TIMESTAMP_WITH_TIMEZONE"),

        /** The `INTERVAL` SQL column type. */
        @SerialName("INTERVAL")
        INTERVAL("INTERVAL"),

        /** The `BINARY` SQL column type. */
        @SerialName("BINARY")
        BINARY("BINARY"),

        /** The `VARBINARY` SQL column type. */
        @SerialName("VARBINARY")
        VARBINARY("VARBINARY"),

        /** The `LONGVARBINARY` SQL column type. */
        @SerialName("LONGVARBINARY")
        LONGVARBINARY("LONGVARBINARY"),

        /** The `BYTEA` SQL column type. */
        @SerialName("BYTEA")
        BYTEA("BYTEA"),

        /** The `BLOB` SQL column type. */
        @SerialName("BLOB")
        BLOB("BLOB"),

        /** The `TINYBLOB` SQL column type. */
        @SerialName("TINYBLOB")
        TINYBLOB("TINYBLOB"),

        /** The `MEDIUMBLOB` SQL column type. */
        @SerialName("MEDIUMBLOB")
        MEDIUMBLOB("MEDIUMBLOB"),

        /** The `LONGBLOB` SQL column type. */
        @SerialName("LONGBLOB")
        LONGBLOB("LONGBLOB"),
    }
}

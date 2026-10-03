/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
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
     */
    @Serializable
    enum class ColumnDisplayType(val value: String) {
        @SerialName("TEXT")
        TEXT("TEXT"),

        @SerialName("STRING")
        STRING("STRING"),

        @SerialName("INTEGER")
        INTEGER("INTEGER"),

        @SerialName("FLOAT")
        FLOAT("FLOAT"),

        @SerialName("DECIMAL")
        DECIMAL("DECIMAL"),

        @SerialName("DATE")
        DATE("DATE"),

        @SerialName("TIME")
        TIME("TIME"),

        @SerialName("DATETIME")
        DATETIME("DATETIME"),

        @SerialName("BOOLEAN")
        BOOLEAN("BOOLEAN"),

        @SerialName("BINARY")
        BINARY("BINARY"),

        @SerialName("CODELOOKUP")
        CODELOOKUP("CODELOOKUP"),

        @SerialName("CODEVALUE")
        CODEVALUE("CODEVALUE"),
    }

    /**
     *
     *
     * Values: BIT,BOOLEAN,SMALLINT,TINYINT,INTEGER,MEDIUMINT,BIGINT,REAL,FLOAT,DOUBLE,NUMERIC,DECIMAL,SERIAL,SMALLSERIAL,BIGSERIAL,MONEY,CHAR,VARCHAR,LONGVARCHAR,TEXT,TINYTEXT,MEDIUMTEXT,LONGTEXT,JSON,DATE,TIME,TIME_WITH_TIMEZONE,TIMESTAMP,DATETIME,TIMESTAMP_WITH_TIMEZONE,INTERVAL,BINARY,VARBINARY,LONGVARBINARY,BYTEA,BLOB,TINYBLOB,MEDIUMBLOB,LONGBLOB
     */
    @Serializable
    enum class ColumnType(val value: String) {
        @SerialName("BIT")
        BIT("BIT"),

        @SerialName("BOOLEAN")
        BOOLEAN("BOOLEAN"),

        @SerialName("SMALLINT")
        SMALLINT("SMALLINT"),

        @SerialName("TINYINT")
        TINYINT("TINYINT"),

        @SerialName("INTEGER")
        INTEGER("INTEGER"),

        @SerialName("MEDIUMINT")
        MEDIUMINT("MEDIUMINT"),

        @SerialName("BIGINT")
        BIGINT("BIGINT"),

        @SerialName("REAL")
        REAL("REAL"),

        @SerialName("FLOAT")
        FLOAT("FLOAT"),

        @SerialName("DOUBLE")
        DOUBLE("DOUBLE"),

        @SerialName("NUMERIC")
        NUMERIC("NUMERIC"),

        @SerialName("DECIMAL")
        DECIMAL("DECIMAL"),

        @SerialName("SERIAL")
        SERIAL("SERIAL"),

        @SerialName("SMALLSERIAL")
        SMALLSERIAL("SMALLSERIAL"),

        @SerialName("BIGSERIAL")
        BIGSERIAL("BIGSERIAL"),

        @SerialName("MONEY")
        MONEY("MONEY"),

        @SerialName("CHAR")
        CHAR("CHAR"),

        @SerialName("VARCHAR")
        VARCHAR("VARCHAR"),

        @SerialName("LONGVARCHAR")
        LONGVARCHAR("LONGVARCHAR"),

        @SerialName("TEXT")
        TEXT("TEXT"),

        @SerialName("TINYTEXT")
        TINYTEXT("TINYTEXT"),

        @SerialName("MEDIUMTEXT")
        MEDIUMTEXT("MEDIUMTEXT"),

        @SerialName("LONGTEXT")
        LONGTEXT("LONGTEXT"),

        @SerialName("JSON")
        JSON("JSON"),

        @SerialName("DATE")
        DATE("DATE"),

        @SerialName("TIME")
        TIME("TIME"),

        @SerialName("TIME_WITH_TIMEZONE")
        TIME_WITH_TIMEZONE("TIME_WITH_TIMEZONE"),

        @SerialName("TIMESTAMP")
        TIMESTAMP("TIMESTAMP"),

        @SerialName("DATETIME")
        DATETIME("DATETIME"),

        @SerialName("TIMESTAMP_WITH_TIMEZONE")
        TIMESTAMP_WITH_TIMEZONE("TIMESTAMP_WITH_TIMEZONE"),

        @SerialName("INTERVAL")
        INTERVAL("INTERVAL"),

        @SerialName("BINARY")
        BINARY("BINARY"),

        @SerialName("VARBINARY")
        VARBINARY("VARBINARY"),

        @SerialName("LONGVARBINARY")
        LONGVARBINARY("LONGVARBINARY"),

        @SerialName("BYTEA")
        BYTEA("BYTEA"),

        @SerialName("BLOB")
        BLOB("BLOB"),

        @SerialName("TINYBLOB")
        TINYBLOB("TINYBLOB"),

        @SerialName("MEDIUMBLOB")
        MEDIUMBLOB("MEDIUMBLOB"),

        @SerialName("LONGBLOB")
        LONGBLOB("LONGBLOB"),
    }
}

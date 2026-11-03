/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.runreport

import kotlinx.serialization.Serializable

/**
 * Created by Tarun on 03-08-17.
 */
@Serializable
// Defaults on every field: Fineract's result-set header JSON varies by version (`columnDisplayType`
// and `columnValues` are not always present), and a missing field must not fail the WHOLE report.
data class ColumnHeader(
    /** How the column is displayed (text, date, code lookup, …). */
    var columnDisplayType: String = "",
    /** The column's name. */
    var columnName: String = "",
    /** The column's database type. */
    var columnType: String = "",
    /** The values the column may take. */
    var columnValues: List<String> = emptyList(),
    /** True when the column may be empty. */
    var isColumnNullable: Boolean = true,
    /** True when the column is the primary key. */
    var isColumnPrimaryKey: Boolean = false,
)

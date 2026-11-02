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
    var columnDisplayType: String = "",
    var columnName: String = "",
    var columnType: String = "",
    var columnValues: List<String> = emptyList(),
    var isColumnNullable: Boolean = true,
    var isColumnPrimaryKey: Boolean = false,
)

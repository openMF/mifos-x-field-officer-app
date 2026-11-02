/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.createnew

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.model.datatable.ColumnHeader
import kpt.core.model.datatable.ColumnValue
import kpt.core.model.datatable.DataTable
import org.jetbrains.compose.ui.tooling.preview.Preview
import kpt.core.designsystem.theme.KptTheme as AppTheme

// i18n:skip — preview fixtures (a tenant's datatable — its names come from the server)
private val previewTable = DataTable(
    registeredTableName = "client_household",
    columnHeaderData = listOf(
        ColumnHeader(dataTableColumnName = "client_id", columnDisplayType = "INTEGER", columnPrimaryKey = true),
        ColumnHeader(dataTableColumnName = "household_size", columnDisplayType = "INTEGER", columnNullable = false),
        ColumnHeader(dataTableColumnName = "monthly_income", columnDisplayType = "DECIMAL", columnNullable = true),
        ColumnHeader(dataTableColumnName = "joined_on", columnDisplayType = "DATE", columnNullable = true),
        ColumnHeader(dataTableColumnName = "owns_home", columnDisplayType = "BOOLEAN", columnNullable = false),
        ColumnHeader(
            dataTableColumnName = "occupation",
            columnDisplayType = "CODELOOKUP",
            columnValues = listOf(ColumnValue(id = 41, value = "Farmer"), ColumnValue(id = 42, value = "Trader")),
        ),
    ),
)

/** The client datatable section as create-client renders it — with a refused required field. */
@Preview
@Composable
internal fun ClientDataTableSectionsPreview() {
    AppTheme {
        Surface {
            Column(Modifier.padding(KptTheme.spacing.md)) {
                DataTableSections(
                    tables = listOf(previewTable),
                    state = CreateNewClientState(
                        dataTableValues = mapOf(
                            DataTableField("client_household", "monthly_income") to "1250.50",
                            DataTableField("client_household", "owns_home") to "true",
                            DataTableField("client_household", "occupation") to "42",
                        ),
                        dataTableErrors = mapOf(
                            DataTableField("client_household", "household_size") to DataTableFieldError.REQUIRED,
                        ),
                    ),
                    onAction = {},
                )
            }
        }
    }
}

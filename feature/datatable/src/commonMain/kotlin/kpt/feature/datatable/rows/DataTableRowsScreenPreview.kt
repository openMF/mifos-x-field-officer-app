/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.datatable.rows

import androidx.compose.runtime.Composable
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.theme.KptTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier: `CommonComposablePreviewScanner`
 * discovers these from commonMain and renders them off `desktopTest`. The Koin-bound screens are
 * not previewed; their stateless `*Content` halves are. Fixture values are server content, not
 * app copy — `// i18n:skip`.
 */

// i18n:skip — preview fixtures
private val previewRows = listOf(
    DataTableRow(id = 1, cells = listOf("Household size" to "5", "Monthly income" to "18,000", "Owns home" to "Yes")),
    DataTableRow(id = 2, cells = listOf("Household size" to "3", "Monthly income" to "9,500", "Owns home" to "No")),
)

@Preview
@Composable
internal fun DataTableRowsContentPreview() {
    KptTheme {
        DataTableRowsContent(
            // i18n:skip — preview fixture
            state = DataTableRowsState(tableName = "Household Details", entityId = 42),
            rows = ScreenState.Content(previewRows),
            onAction = {},
            onBackPressed = {},
        )
    }
}

@Preview
@Composable
internal fun DataTableRowsContentOfflineNoticePreview() {
    // A delete attempted offline: the rows stay and the notice says why nothing changed.
    KptTheme {
        DataTableRowsContent(
            // i18n:skip — preview fixture
            state = DataTableRowsState(tableName = "Household Details", entityId = 42, notice = RowNotice.Offline),
            rows = ScreenState.Content(previewRows),
            onAction = {},
            onBackPressed = {},
        )
    }
}

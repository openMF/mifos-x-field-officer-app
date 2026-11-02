/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.datatable.list

import androidx.compose.runtime.Composable
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.datatable.DataTable
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier: `CommonComposablePreviewScanner`
 * discovers these from commonMain and renders them off `desktopTest`. The Koin-bound screens are
 * not previewed; their stateless `*Content` halves are. Fixture values are server content, not
 * app copy — `// i18n:skip`.
 */

@Preview
@Composable
internal fun DataTableListContentPreview() {
    KptTheme {
        DataTableListContent(
            dataTables = ScreenState.Content(
                listOf(
                    // i18n:skip — preview fixtures
                    DataTable(applicationTableName = "m_client", registeredTableName = "Household Details"),
                    DataTable(applicationTableName = "m_client", registeredTableName = "Business Profile"),
                ),
            ),
            onAction = {},
            onBackPressed = {},
        )
    }
}

@Preview
@Composable
internal fun DataTableListContentEmptyPreview() {
    KptTheme {
        DataTableListContent(dataTables = ScreenState.Empty, onAction = {}, onBackPressed = {})
    }
}

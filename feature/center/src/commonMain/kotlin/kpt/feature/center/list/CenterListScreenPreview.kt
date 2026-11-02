/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.center.list

import androidx.compose.runtime.Composable
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.center.Center
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier: `CommonComposablePreviewScanner`
 * discovers these from commonMain and renders them off `desktopTest`. The Koin-bound
 * `CenterListScreen` is not previewed; its stateless `CenterListContent` is.
 *
 * Names and account numbers are PREVIEW FIXTURE DATA (server content, not app copy), hence
 * `// i18n:skip`.
 */

// i18n:skip — preview fixtures
internal val previewCenters = listOf(
    Center(id = 1, accountNo = "000000001", name = "Ward 4 Centre", officeName = "Head Office", active = true),
    Center(id = 2, accountNo = "000000002", name = "Riverside Centre", officeName = "Branch A", active = true),
    Center(id = 3, accountNo = "000000003", name = "Market Road Centre", officeName = "Branch A", active = false),
)

@Preview
@Composable
internal fun CenterListContentPreview() {
    KptTheme {
        CenterListContent(ui = CenterListState(), rows = ScreenState.Content(previewCenters), onAction = {})
    }
}

@Preview
@Composable
internal fun CenterListContentFilteredPreview() {
    // Filter chips visible with one office selected — the state the chips themselves render.
    KptTheme {
        CenterListContent(
            ui = CenterListState(selectedOffices = setOf("Branch A"), filtersVisible = true),
            rows = ScreenState.Content(previewCenters.filter { it.officeName == "Branch A" }),
            onAction = {},
        )
    }
}

@Preview
@Composable
internal fun CenterListContentEmptyPreview() {
    KptTheme {
        CenterListContent(ui = CenterListState(), rows = ScreenState.Empty, onAction = {})
    }
}

/** Selection mode (two centers long-pressed) — the selection bar and highlighted rows. */
@Preview
@Composable
internal fun CenterListSelectionPreview() {
    KptTheme {
        CenterListContent(
            ui = CenterListState(selected = setOf(1, 2)),
            rows = ScreenState.Content(previewCenters),
            onAction = {},
        )
    }
}

/** A finished download that could not fetch one record — the failure is named, with the server's reason. */
@Preview
@Composable
internal fun CenterListDownloadPreview() {
    KptTheme {
        CenterListContent(
            ui = CenterListState(
                // i18n:skip — preview fixture
                download = kpt.core.model.offline.DownloadProgress(
                    completed = 2,
                    total = 2,
                    records = 37,
                    finished = true,
                    failures = listOf(
                        kpt.core.model.offline.DownloadFailure(
                            "Riverside Centre · loan #42",
                            "Loan with identifier 42 does not exist",
                        ),
                    ),
                ),
            ),
            rows = ScreenState.Content(previewCenters.map { it.copy(sync = true) }),
            onAction = {},
        )
    }
}

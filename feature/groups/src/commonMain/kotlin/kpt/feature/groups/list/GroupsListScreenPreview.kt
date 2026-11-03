/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.groups.list

import androidx.compose.runtime.Composable
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.group.Group
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier: `CommonComposablePreviewScanner`
 * discovers these from commonMain and renders them off `desktopTest`. The Koin-bound screens are
 * not previewed; their stateless `*Content` halves are. Fixture names are server content, not app
 * copy — `// i18n:skip`.
 */

// i18n:skip — preview fixtures
internal val previewGroups = listOf(
    Group(id = 11, accountNo = "000000011", name = "Sunrise Group", officeName = "Head Office", active = true),
    Group(id = 12, accountNo = "000000012", name = "Lotus Group", officeName = "Branch A", active = true),
    Group(id = 13, accountNo = "000000013", name = "Harvest Group", officeName = "Branch A", active = false),
)

@Preview
@Composable
internal fun GroupsListContentPreview() {
    KptTheme {
        GroupsListContent(ui = GroupsListState(), rows = ScreenState.Content(previewGroups), onAction = {})
    }
}

@Preview
@Composable
internal fun GroupsListContentEmptyPreview() {
    KptTheme {
        GroupsListContent(ui = GroupsListState(), rows = ScreenState.Empty, onAction = {})
    }
}

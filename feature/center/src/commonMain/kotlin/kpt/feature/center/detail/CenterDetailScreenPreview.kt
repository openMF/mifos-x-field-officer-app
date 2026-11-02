/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.center.detail

import androidx.compose.runtime.Composable
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.center.Center
import kpt.core.model.group.Group
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier (see CenterListScreenPreview). Fixture
 * names are server content, not app copy — `// i18n:skip`.
 */

// i18n:skip — preview fixtures
private val previewCenter = Center(
    id = 1,
    accountNo = "000000001",
    name = "Ward 4 Centre",
    officeName = "Head Office",
    staffName = "Nadia Islam",
    active = true,
)

// i18n:skip — preview fixtures
private val previewGroups = listOf(
    Group(id = 11, accountNo = "000000011", name = "Sunrise Group", centerId = 1, active = true),
    Group(id = 12, accountNo = "000000012", name = "Lotus Group", centerId = 1, active = true),
)

@Preview
@Composable
internal fun CenterDetailContentPreview() {
    KptTheme {
        CenterDetailContent(
            center = ScreenState.Content(previewCenter),
            groups = ScreenState.Content(previewGroups),
            onAction = {},
        )
    }
}

@Preview
@Composable
internal fun CenterDetailContentPendingActivationPreview() {
    // An inactive centre offers Activate, and its group list is still loading.
    KptTheme {
        CenterDetailContent(
            center = ScreenState.Content(previewCenter.copy(active = false)),
            groups = ScreenState.Loading,
            onAction = {},
            canActivate = true,
        )
    }
}

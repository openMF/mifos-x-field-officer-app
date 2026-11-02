/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.groups.detail

import androidx.compose.runtime.Composable
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.group.Group
import org.jetbrains.compose.ui.tooling.preview.Preview

/* @Preview siblings for the device-free CMP render tier (see GroupsListScreenPreview). */

// i18n:skip — preview fixture
private val previewGroup = Group(
    id = 11,
    accountNo = "000000011",
    name = "Sunrise Group",
    officeName = "Head Office",
    centerName = "Ward 4 Centre",
    staffName = "Nadia Islam",
    active = true,
)

@Preview
@Composable
internal fun GroupsDetailContentPreview() {
    // Every action the detail can offer, so the full action list renders.
    KptTheme {
        GroupsDetailContent(
            group = ScreenState.Content(previewGroup),
            canActivate = false,
            canOpenDocuments = true,
            canOpenDataTables = true,
            canAddLoanAccount = true,
            canAddSavingsAccount = true,
            onAction = {},
        )
    }
}

@Preview
@Composable
internal fun GroupsDetailContentPendingActivationPreview() {
    KptTheme {
        GroupsDetailContent(
            group = ScreenState.Content(previewGroup.copy(active = false)),
            canActivate = true,
            onAction = {},
        )
    }
}

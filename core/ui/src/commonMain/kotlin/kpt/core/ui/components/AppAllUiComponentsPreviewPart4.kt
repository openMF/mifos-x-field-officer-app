/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kpt.core.base.designsystem.KptTheme
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.AppTheme
import kpt.core.ui.generated.resources.Res
import kpt.core.ui.generated.resources.core_ui_ic_centers_24dp
import kpt.core.ui.generated.resources.core_ui_no_internet
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun AppTextTitleDescSingleLinePreview(
    modifier: Modifier = Modifier,
) {
    AppTheme {
        AppTextTitleDescSingleLine(
            title = "AppTextTitleDescSingleLine Title",
            description = "AppTextTitleDescSingleLine Description",
            modifier = modifier,
        )
    }
}

@Preview
@Composable
private fun AppTextTitleDescDoubleLinePreview(
    modifier: Modifier = Modifier,
) {
    AppTheme {
        AppTextTitleDescDoubleLine(
            title = "AppTextTitleDescDoubleLine Title",
            description = "AppTextTitleDescDoubleLine Description",
            descriptionStyle = KptTheme.typography.bodyMedium,
            modifier = modifier,
        )
    }
}

@Preview
@Composable
private fun AppTextTitleDescDrawableSingleLinePreview(
    modifier: Modifier = Modifier,
) {
    AppTheme {
        AppTextTitleDescDrawableSingleLine(
            title = "AppTextTitleDescDrawableSingleLine Title",
            description = "AppTextTitleDescDrawableSingleLine Description",
            imageResId = Res.drawable.core_ui_ic_centers_24dp,
            modifier = modifier,
        )
    }
}

@Preview
@Composable
private fun AppTitleDescSingleLineEqualPreview(
    modifier: Modifier = Modifier,
) {
    AppTheme {
        AppTitleDescSingleLineEqual(
            title = "AppTitleDescSingleLineEqual Title",
            description = "AppTitleDescSingleLineEqual Description",
            modifier = modifier,
        )
    }
}

@Preview
@Composable
private fun AppTextUserImagePreview(
    modifier: Modifier = Modifier,
) {
    AppTheme {
        AppTextUserImage(
            text = "A",
            modifier = modifier,
        )
    }
}

@Preview
@Composable
private fun SelectionModeTopAppBarPreview() {
    AppTheme {
        SelectionModeTopAppBar(
            itemCount = 3,
            resetSelectionMode = {},
        )
    }
}

@Composable
@Preview
private fun AppTwoButtonRowPreview() {
    AppTheme {
        AppTwoButtonRow(
            firstBtnText = "First Button",
            secondBtnText = "Second Button",
            onFirstBtnClick = {},
            onSecondBtnClick = {},
        )
    }
}

@Preview
@Composable
private fun AppUserImagePreview(
    modifier: Modifier = Modifier,
) {
    AppTheme {
        AppUserImage(
            bitmap = null,
            modifier = modifier,
        )
    }
}

@Preview
@Composable
private fun SelectionModeTopAppBar() {
    AppTheme {
        SelectionModeTopAppBar(
            itemCount = 3,
            resetSelectionMode = {},
        )
    }
}

@Preview
@Composable
private fun SelectionModeTopAppBarWithActionsPreview() {
    AppTheme {
        SelectionModeTopAppBar(
            itemCount = 5,
            resetSelectionMode = {},
            actions = {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = AppIcons.Delete,
                        contentDescription = "delete",
                    )
                }
            },
        )
    }
}

@Preview
@Composable
private fun NoInternetPreview(
    modifier: Modifier = Modifier,
) {
    AppTheme {
        NoInternet(
            error = Res.string.core_ui_no_internet,
            modifier = modifier,
            isRetryEnabled = true,
            retry = {},
        )
    }
}

@Preview
@Composable
private fun ChargeDialogPreview() {
    val sampleChargeOptions = listOf("Bank Fee", "Overdue Fee", "Processing Fee")

    AppTheme {
        AddChargeBottomSheet(
            title = "Add Charge",
            confirmText = "Confirm",
            dismissText = "Cancel",
            showDatePicker = false,
            selectedChargeName = sampleChargeOptions[0],
            selectedDate = "2025-09-06",
            chargeAmount = "1500",
            chargeType = "Flat",
            chargeCollectedOn = "2025-09-06",
            chargeOptions = sampleChargeOptions,
            onConfirm = { },
            onDismiss = { },
            onChargeSelected = { _, _ -> },
            onDatePick = { },
            onDateChange = { },
            onAmountChange = {},
        )
    }
}

@Preview
@Composable
private fun AppActionsChargeListingComponentPreview() {
    AppTheme {
        AppActionsChargeListingComponent(
            chargeTitle = "Bank Fee",
            type = "Flat",
            date = "06-09-2025",
            collectedOn = "06-09-2025",
            amount = "1500 UGX",
            onActionClicked = { },
            isExpanded = true,
            onExpandToggle = {},
        )
    }
}

@Preview
@Composable
private fun PreviewAppActionsShareListingComponent() {
    val sampleMenu = listOf(
        Actions.Edit(),
        Actions.Delete(),
    )

    AppTheme {
        AppActionsShareListingComponent(
            accountNo = "AC123456",
            shareProductName = "Gold Savings Plan",
            pendingForApprovalShares = 5,
            approvedShares = 10,
            isExpanded = true,
            onClick = {},
            menuList = sampleMenu,
            onActionClicked = { },
        )
    }
}

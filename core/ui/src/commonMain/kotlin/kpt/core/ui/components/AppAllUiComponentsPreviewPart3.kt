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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kpt.core.base.designsystem.KptTheme
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.theme.AppTheme
import kpt.core.designsystem.theme.AppTypography
import kpt.core.ui.generated.resources.Res
import kpt.core.ui.generated.resources.core_ui_core_common_working
import kpt.core.ui.generated.resources.core_ui_ic_group_black_24dp
import kpt.core.ui.util.TextUtil
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun PreviewAppHeader() {
    KptTheme {
        AppListingRowItemHeader(
            text = "Hello",
        )
    }
}

@Preview
@Composable
private fun AppProgressIndicatorPreview() {
    AppTheme {
        AppProgressIndicator()
    }
}

@Preview
@Composable
private fun AppProgressIndicatorOverlayPreview() {
    AppTheme {
        AppProgressIndicatorOverlay()
    }
}

@Preview
@Composable
private fun PreviewRadioButtonDialog() {
    AppTheme {
        AppRadioButtonDialog(
            titleResId = Res.string.core_ui_core_common_working,
            items = arrayOf("1", "2", "3"),
            selectedItem = "1",
            onDismissRequest = { },
            selectItem = { _, _ -> },
        )
    }
}

@Preview
@Composable
private fun AppRoundIconPreview(
    modifier: Modifier = Modifier,
) {
    AppTheme {
        AppRoundIcon(
            iconId = Res.drawable.core_ui_ic_group_black_24dp,
            modifier = modifier,
        )
    }
}

@Preview
@Composable
private fun AppRowTextWithButton() {
    AppTheme {
        AppRowTextWithButton(
            title = "Title",
            description = "Description",
            size = "Size",
            btnText = "Button",
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun PreviewAppRowCard() {
    AppTheme {
        AppRowCard(
            title = "Customer Info",
            leftValues = listOf(
                TextUtil(text = "Name", style = AppTypography.labelMediumEmphasized),
                TextUtil(text = "Age", style = AppTypography.labelMedium),
                TextUtil(text = "City", style = AppTypography.labelMedium),
            ),
            rightValues = listOf(
                TextUtil(text = "453", style = AppTypography.labelMedium, color = Color.Green),
                TextUtil(text = "455", style = AppTypography.labelMedium),
            ),
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun PreviewAppRowCardWithImage() {
    AppTheme {
        AppRowCard(
            title = "Customer Info",
            leftValues = listOf(
                TextUtil(text = "Name", style = AppTypography.labelMediumEmphasized),
                TextUtil(text = "Age", style = AppTypography.labelMedium),
                TextUtil(text = "City", style = AppTypography.labelMedium),
            ),
            rightValues = listOf(
                TextUtil(text = "Closed", style = AppTypography.labelMedium, color = Color.Red),
                TextUtil(text = "455", style = AppTypography.labelMedium),
            ),
            byteArray = null,
        )
    }
}

@Preview
@Composable
private fun AppRowWithLabelsPreview() {
    AppTheme {
        AppRowWithLabels(
            title = "Title",
            subTitle = "Sub Title",
            label1 = TextUtil("label1", color = Color.Green),
            label2 = TextUtil("label2"),
            onClick = {},
            byteArray = null,
        )
    }
}

@Preview
@Composable
private fun AppRowDropDownPreview() {
    AppTheme {
        AppDropDownRow(
            isActive = true,
            title = "John Wault",
            accountNo = "Acc No. 838272",
            office = "Head Office",
            date = "20-04-2020",
            onClick = {},
        )
    }
}

@Composable
@Preview
private fun AppRowWithTextAndButtonPreview() {
    AppTheme {
        AppRowWithTextAndButton(
            onBtnClick = {},
            btnText = "View",
            text = "2 Collaterals",
        )
    }
}

@Preview
@Composable
private fun AppSearchBarPreview() {
    var text by remember { mutableStateOf("") }

    AppSearchBar(
        query = text,
        onQueryChange = { text = it },
        onBackClick = { },
        onSearchClick = { println("Searching for $it") },
    )
}

@Composable
@Preview
private fun AppSuccessStatusDialogPreview() {
    AppTheme {
        AppStatusDialog(
            status = ResultStatus.SUCCESS,
            onConfirm = {},
            btnText = "OK",
            successTitle = "Success",
            successMessage = "Operation Successful",
            failureTitle = "Failure",
            failureMessage = "Operation Failed",
        )
    }
}

@Composable
@Preview
private fun AppFailureStatusDialogPreview() {
    AppTheme {
        AppStatusDialog(
            status = ResultStatus.FAILURE,
            onConfirm = {},
            btnText = "OK",
            successTitle = "Success",
            successMessage = "Operation Successful",
            failureTitle = "Failure",
            failureMessage = "Operation Failed",
        )
    }
}

@Preview
@Composable
private fun AppStepperDemo() {
    val steps = listOf(
        Step("Details") { Text("Step 1: Details Content") },
        Step("Terms") { Text("Step 2: Terms Content") },
        Step("Charges") { Text("Step 3: Charges Content") },
        Step("Schedule") { Text("Step 4: Schedule Content") },
        Step("Preview") { Text("Step 5: Preview Content") },
    )

    AppStepper(
        steps = steps,
        currentIndex = 2,
        onStepChange = { },
        modifier = Modifier
            .fillMaxWidth(),
    )
}

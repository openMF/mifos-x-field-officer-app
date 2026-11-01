/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.designsystem.fieldofficer.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kpt.core.designsystem.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun AppSweetErrorPreview() {
    AppTheme {
        AppSweetError(
            message = "Something went wrong. Please check your network.",
            buttonText = "Retry",
            onclick = {},
        )
    }
}

@Preview
@Composable
private fun AppSweetError_NoRetryPreview() {
    AppTheme {
        AppSweetError(
            message = "Failed to fetch data.",
            isRetryEnabled = false,
        )
    }
}

@Preview
@Composable
private fun AppPaginationSweetErrorPreview() {
    AppTheme {
        AppPaginationSweetError(
            onclick = {},
        )
    }
}

@Preview
@Composable
private fun AppTextFieldDropdownPreview() {
    val options = listOf("Option 1", "Option 2", "Option 3")
    var selectedValue by remember { mutableStateOf("") }

    AppTheme {
        AppTextFieldDropdown(
            value = selectedValue,
            onValueChanged = { selectedValue = it },
            onOptionSelected = { _, option -> selectedValue = option },
            options = options,
            label = "Select Option",
        )
    }
}

@Preview
@Composable
private fun AppTopBarPreview() {
    AppTheme {
        AppTopBar(
            topBarTitle = "Sample Title",
            backPress = {},
        )
    }
}

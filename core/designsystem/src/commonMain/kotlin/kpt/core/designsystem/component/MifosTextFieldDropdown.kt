/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(ExperimentalMaterial3Api::class)

package kpt.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.theme.DesignToken
import kpt.core.designsystem.theme.MifosTheme
import kpt.core.designsystem.theme.MifosTypography
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MifosTextFieldDropdown(
    enabled: Boolean = true,
    value: String,
    onValueChanged: (String) -> Unit,
    onOptionSelected: (Int, String) -> Unit,
    options: List<String>,
    modifier: Modifier = Modifier
        .clip(DesignToken.shapes.medium)
        .fillMaxWidth(),
    label: String? = null,
    readOnly: Boolean = true,
    errorMessage: String? = null,
) {
    var isExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = isExpanded && enabled,
        onExpandedChange = { isExpanded = !isExpanded },
    ) {
        OutlinedTextField(
            enabled = enabled,
            isError = errorMessage != null,
            supportingText = {
                errorMessage?.let {
                    Text(
                        text = it,
                        color = KptTheme.colorScheme.error,
                    )
                }
            },
            value = value,
            onValueChange = onValueChanged,
            label = {
                label?.let {
                    Text(
                        text = it,
                    )
                }
            },
            modifier = modifier
                .menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = enabled)
                .clickable(enabled = readOnly && enabled) { isExpanded = true },
            shape = DesignToken.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = KptTheme.colorScheme.secondaryContainer,
                unfocusedBorderColor = KptTheme.colorScheme.secondaryContainer,
            ),
            maxLines = 1,
            textStyle = MifosTypography.bodyLarge,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
            },
            readOnly = readOnly,
        )

        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
        ) {
            options.forEachIndexed { index, item ->
                DropdownMenuItem(
                    text = { Text(text = item) },
                    onClick = {
                        isExpanded = false
                        onOptionSelected(index, item)
                    },
                )
            }
        }
    }
}

@Preview
@Composable
private fun MifosTextFieldDropdownPreview() {
    val options = listOf("Option 1", "Option 2", "Option 3")
    var selectedValue by remember { mutableStateOf("") }

    MifosTheme {
        MifosTextFieldDropdown(
            value = selectedValue,
            onValueChanged = { selectedValue = it },
            onOptionSelected = { _, option -> selectedValue = option },
            options = options,
            label = "Select Option",
        )
    }
}

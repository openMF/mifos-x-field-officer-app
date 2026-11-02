/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(ExperimentalMaterial3Api::class)

package kpt.core.designsystem.fieldofficer.component

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
import kpt.core.designsystem.theme.AppTheme
import kpt.core.designsystem.theme.AppTypography
import kpt.core.designsystem.theme.DesignToken
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTextFieldDropdown(
    value: String,
    onOptionSelected: (Int, String) -> Unit,
    options: List<String>,
    modifier: Modifier = Modifier
        .clip(DesignToken.shapes.medium)
        .fillMaxWidth(),
    enabled: Boolean = true,
    // Only an EDITABLE field (`readOnly = false`) ever calls this; a read-only dropdown changes only
    // through [onOptionSelected]. Required, it made every read-only caller pass a do-nothing `{}`
    // (40 of them, each a dead-clickable finding), so it is optional.
    onValueChanged: (String) -> Unit = {},
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
                // The anchor type must MATCH `readOnly`, or the field takes focus instead of
                // opening.
                //
                // This was hardcoded to `PrimaryEditable` while `readOnly` defaults to `true`, so
                // every read-only dropdown in the app — 22 screens, from the loan forms to client
                // transfer to every wizard — focused on tap and never expanded. M3 treats
                // `PrimaryEditable` as "tap to type": it claims the gesture for the text field.
                //
                // The `.clickable` below was presumably meant to compensate, but it is chained
                // AFTER `menuAnchor`, so the anchor's own pointer handling consumes the tap first
                // and it never runs. It is kept for the editable case, where the anchor does not
                // claim a body tap.
                //
                // Found on device 2026-10-22: the Office field on Create Group showed a focus ring
                // and a chevron and did nothing. Nothing in the build or the test suite catches
                // this — a dropdown that never opens still composes, still renders, and its
                // ViewModel still passes its own tests.
                .menuAnchor(
                    type = if (readOnly) {
                        MenuAnchorType.PrimaryNotEditable
                    } else {
                        MenuAnchorType.PrimaryEditable
                    },
                    enabled = enabled,
                )
                .clickable(enabled = readOnly && enabled) { isExpanded = true },
            shape = DesignToken.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = KptTheme.colorScheme.secondaryContainer,
                unfocusedBorderColor = KptTheme.colorScheme.secondaryContainer,
            ),
            maxLines = 1,
            textStyle = AppTypography.bodyLarge,
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

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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun AppCard() {
    AppTheme {
        AppCard {
            Column(
                modifier = Modifier.padding(KptTheme.spacing.md),
            ) {
                Text("Simple Card")
                Text("This is the card content.")
            }
        }
    }
}

@Preview
@Composable
private fun AppMenuDropDownItem() {
    AppTheme {
        DropdownMenu(
            expanded = true,
            onDismissRequest = {},
        ) {
            AppMenuDropDownItem(
                option = "Profile",
                onClick = {},
            )
            AppMenuDropDownItem(
                option = "Settings",
                onClick = {},
            )
            AppMenuDropDownItem(
                option = "Logout",
                onClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun AppOutlinedTextField_ValuePreview() {
    var text by remember { mutableStateOf(TextFieldValue("Hello")) }
    AppTheme {
        AppOutlinedTextField(
            value = text,
            onValueChanged = { text = it },
            label = "Username",
            icon = Icons.Default.Person,
        )
    }
}

@Preview
@Composable
private fun AppOutlinedTextField_PasswordPreview() {
    var text by remember { mutableStateOf("secret123") }
    var passwordVisible by remember { mutableStateOf(false) }

    AppTheme {
        AppOutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = "Password",
            keyboardType = KeyboardType.Password,
            passwordToggle = AppPasswordToggle(
                isVisible = passwordVisible,
                onClick = { passwordVisible = it },
            ),
            showClearIcon = true,
            onClickClearIcon = { text = "" },
        )
    }
}

@Preview
@Composable
private fun AppOutlinedTextField_ErrorPreview() {
    var text by remember { mutableStateOf("") }
    AppTheme {
        AppOutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = "Email",
            error = "Invalid email address",
            icon = Icons.Default.Email,
        )
    }
}

@Preview
@Composable
private fun AppDatePickerTextFieldPreview() {
    AppTheme {
        AppDatePickerTextField(
            value = "2025-08-18",
            label = "Date of Birth",
            openDatePicker = { /* open picker */ },
        )
    }
}

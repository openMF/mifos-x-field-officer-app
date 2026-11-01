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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.theme.AppTheme
import kpt.core.designsystem.theme.DesignToken
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter

@Preview
@Composable
private fun AppDialogBoxWithMessage() {
    AppTheme {
        AppDialogBox(
            title = "Delete Item",
            showDialogState = true,
            confirmButtonText = "Yes",
            dismissButtonText = "No",
            onConfirm = {},
            onDismiss = {},
            message = "Are you sure you want to delete this item?",
        )
    }
}

@Preview
@Composable
private fun AppDialogBoxWithComposableMessage() {
    AppTheme {
        AppDialogBox(
            title = "Info",
            showDialogState = true,
            confirmButtonText = "Okay",
            dismissButtonText = "Cancel",
            onConfirm = {},
            onDismiss = {},
            message = {
                Column {
                    Text("This is a custom composable inside the dialog.")
                    Spacer(modifier = Modifier.height(KptTheme.spacing.sm))
                    Text("You can place anything here, like a list or input field.")
                }
            },
        )
    }
}

@Preview
@Composable
private fun AppRadioButtonDialog() {
    AppTheme {
        AppRadioButtonDialog(
            title = "Choose Option",
            selectedItem = "Option 2",
            items = arrayOf("Option 1", "Option 2", "Option 3", "Option 4"),
            selectItem = { _, _ -> },
            onDismissRequest = {},
        )
    }
}

@Preview
@Composable
private fun AppCustomDialog() {
    AppTheme {
        AppCustomDialog(
            onDismiss = {},
            content = {
                Card(
                    modifier = Modifier.padding(KptTheme.spacing.md),
                ) {
                    Column(modifier = Modifier.padding(KptTheme.spacing.md)) {
                        Text("This is a custom dialog.")
                        Spacer(modifier = Modifier.height(KptTheme.spacing.sm))
                        Text("You can fully customize this content.")
                    }
                }
            },
        )
    }
}

@Preview
@Composable
private fun AppBasicDialog(
    @PreviewParameter(BasicDialogStatePreviewProvider::class)
    state: BasicDialogState,
) {
    AppTheme {
        AppBasicDialog(
            visibilityState = state,
            onConfirm = {},
            onDismissRequest = {},
        )
    }
}

@Preview
@Composable
private fun AppBasicDialog() {
    AppTheme {
        AppBasicDialog(
            visibilityState = BasicDialogState.Shown(
                title = "An error has occurred.",
                message = "Username or password is incorrect. Try again.",
            ),
            onDismissRequest = {},
        )
    }
}

@Preview
@Composable
private fun AppBottomSheet() {
    AppTheme {
        AppBottomSheet(
            content = {
                Box(
                    Modifier.height(DesignToken.sizes.imageDp100),
                ) {
                }
            },
            onDismiss = {},
        )
    }
}

@Preview
@Composable
private fun AppButton() {
    AppTheme {
        AppButton(
            onClick = {},
        ) {
            Text("Default Button")
        }
    }
}

@Preview
@Composable
private fun AppButtonWithText() {
    AppTheme {
        AppButton(
            onClick = {},
            text = { Text("Text Button") },
        )
    }
}

@Preview
@Composable
private fun AppOutlinedButton() {
    AppTheme {
        AppOutlinedButton(
            onClick = {},
        ) {
            Text("Outlined Button")
        }
    }
}

@Preview
@Composable
private fun AppTextButton() {
    AppTheme {
        AppTextButton(
            onClick = {},
        ) {
            Text("Text Button")
        }
    }
}

@Preview
@Composable
private fun AppTextButtonWithIcon() {
    AppTheme {
        AppTextButton(
            text = { Text("With Icon") },
            onClick = {},
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                )
            },
        )
    }
}

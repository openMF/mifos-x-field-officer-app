/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.theme.MifosTheme
import kpt.core.ui.util.DevicePreview

@Composable
fun MifosAlertDialog(
    dialogTitle: String? = null,
    dialogText: String,
    dismissText: String? = "Cancel",
    confirmationText: String = "Ok",
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    AlertDialog(
        icon = {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null)
            }
        },
        title = { if (dialogTitle != null) Text(text = dialogTitle) },
        text = { Text(text = dialogText) },
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onConfirmation) {
                Text(text = confirmationText, color = KptTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            if (dismissText != null) {
                TextButton(onClick = onDismissRequest) {
                    Text(text = dismissText, color = KptTheme.colorScheme.error)
                }
            }
        },
    )
}

@DevicePreview
@Composable
private fun MifosAlertDialogPreview(
    modifier: Modifier = Modifier,
) {
    MifosTheme {
        MifosAlertDialog(
            dialogTitle = "Dialog Title",
            dialogText = "Dialog Text",
            dismissText = "Dismiss",
            confirmationText = "Confirm",
            onDismissRequest = {},
            onConfirmation = {},
            modifier = modifier,
        )
    }
}

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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.fieldofficer.component.AppButton
import kpt.core.designsystem.theme.AppTheme
import kpt.core.designsystem.theme.AppTypography
import kpt.core.designsystem.theme.DesignToken
import kpt.core.ui.generated.resources.Res
import kpt.core.ui.generated.resources.ic_icon_error
import kpt.core.ui.generated.resources.ic_icon_success
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun AppStatusDialog(
    status: ResultStatus,
    onConfirm: () -> Unit,
    btnText: String,
    successTitle: String,
    successMessage: String,
    failureTitle: String,
    failureMessage: String,
    modifier: Modifier = Modifier,
    showButton: Boolean = true,
    showAsDialog: Boolean = false,
    onDismissRequest: () -> Unit = {},
    /** A second, quieter action under the button — e.g. "Save as draft" on a failed create. */
    secondaryBtnText: String? = null,
    onSecondary: (() -> Unit)? = null,
    /** A line under the message — e.g. "Saved as a draft …" once it was kept. */
    note: String? = null,
) {
    val (title, message, icon) = when (status) {
        ResultStatus.SUCCESS -> Triple(
            successTitle,
            successMessage,
            painterResource(Res.drawable.ic_icon_success),
        )
        ResultStatus.FAILURE -> Triple(
            failureTitle,
            failureMessage,
            painterResource(Res.drawable.ic_icon_error),
        )
    }

    val content: @Composable () -> Unit = {
        Column(
            modifier = if (showAsDialog) {
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(DesignToken.padding.large)
            } else {
                // Inline it is drawn OVER the screen it reports on (callers pass fillMaxSize), so it
                // needs its own surface — without one the form's table showed through the text
                // (loan application preview, device 2026-10-26).
                modifier.background(KptTheme.colorScheme.background)
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(DesignToken.padding.largeIncreasedExtra)
            },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DesignToken.spacing.large),
        ) {
            Image(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(
                    if (showAsDialog) DesignToken.sizes.iconExtraLarge else DesignToken.sizes.profile,
                ),
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(DesignToken.spacing.small),
            ) {
                Text(
                    text = title,
                    style = AppTypography.titleLargeEmphasized,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = message,
                    style = AppTypography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }

            note?.let {
                Text(
                    text = it,
                    style = AppTypography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = DesignToken.padding.small).testTag(AppStatusDialogTestTags.NOTE),
                )
            }

            if (showButton) {
                AppButton(
                    onClick = onConfirm,
                    text = {
                        Text(
                            text = btnText,
                            style = AppTypography.labelLarge,
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = DesignToken.padding.small),
                )
                if (secondaryBtnText != null && onSecondary != null) {
                    TextButton(
                        onClick = onSecondary,
                        modifier = Modifier.fillMaxWidth().testTag(AppStatusDialogTestTags.SECONDARY),
                    ) { Text(secondaryBtnText, style = AppTypography.labelLarge) }
                }
            } else {
                Spacer(modifier = Modifier.height(DesignToken.spacing.large))
            }
        }
    }

    if (showAsDialog) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = {},
            text = content,
            containerColor = KptTheme.colorScheme.surfaceContainerHigh,
            modifier = modifier,
        )
    } else {
        content()
    }
}

enum class ResultStatus {
    SUCCESS,
    FAILURE,
}

@Composable
@Preview
private fun AppSuccessStatusDialogPreview() {
    AppTheme {
        AppStatusDialog(
            status = ResultStatus.SUCCESS,
            onConfirm = {},
            btnText = "Continue",
            successTitle = "Success",
            successMessage = "Profile photo updated successfully",
            failureTitle = "Failure",
            failureMessage = "Operation Failed",
        )
    }
}

object AppStatusDialogTestTags {
    const val SECONDARY = "status-dialog:secondary"
    const val NOTE = "status-dialog:note"
}

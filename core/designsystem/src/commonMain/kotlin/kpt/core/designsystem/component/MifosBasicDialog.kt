/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextAlign
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.theme.DesignToken
import kpt.core.designsystem.theme.MifosTheme
import kpt.core.designsystem.theme.MifosTypography
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter
import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider

@Composable
fun MifosBasicDialog(
    visibilityState: BasicDialogState,
    onDismissRequest: () -> Unit,
): Unit = when (visibilityState) {
    BasicDialogState.Hidden -> Unit
    is BasicDialogState.Shown -> {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = {
                MifosTextButton(
                    content = {
                        Text(text = "Ok")
                    },
                    onClick = onDismissRequest,
                    modifier = Modifier.testTag("AcceptAlertButton"),
                )
            },
            title = visibilityState.title.let {
                {
                    Text(
                        text = it,
                        style = KptTheme.typography.headlineSmall,
                        modifier = Modifier.testTag("AlertTitleText"),
                    )
                }
            },
            text = {
                Text(
                    text = visibilityState.message,
                    style = KptTheme.typography.bodyMedium,
                    modifier = Modifier.testTag("AlertContentText"),
                )
            },
            containerColor = KptTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.semantics {
                testTag = "AlertPopup"
            },
        )
    }
}

@Composable
fun MifosBasicDialog(
    visibilityState: BasicDialogState,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    confirmText: String = "Ok",
    dismissText: String = "Cancel",
    icon: @Composable (() -> Unit)? = null,
) {
    when (visibilityState) {
        BasicDialogState.Hidden -> Unit
        is BasicDialogState.Shown -> {
            AlertDialog(
                onDismissRequest = onDismissRequest,
                confirmButton = {
                    Text(
                        text = confirmText,
                        style = MifosTypography.labelLarge,
                        color = KptTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(horizontal = DesignToken.padding.large)
                            .clickable {
                                onConfirm()
                            },
                    )
                },
                dismissButton = {
                    Text(
                        text = dismissText,
                        style = MifosTypography.labelLarge,
                        color = KptTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(horizontal = DesignToken.padding.large)
                            .clickable {
                                onDismissRequest()
                            },
                    )
                },
                icon = icon,
                title = {
                    Text(
                        text = visibilityState.title,
                        style = KptTheme.typography.headlineSmall,
                        modifier = Modifier.fillMaxWidth().testTag("AlertTitleText"),
                        textAlign = TextAlign.Center,
                    )
                },
                text = {
                    Text(
                        text = visibilityState.message,
                        style = KptTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth().testTag("AlertContentText"),
                    )
                },
                containerColor = KptTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.semantics {
                    testTag = "AlertPopup"
                },
            )
        }
    }
}

@Composable
fun MifosBasicDialog(
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    confirmText: String = "Ok",
    dismissText: String = "Cancel",
    isConfirmEnabled: Boolean = true,
    title: String,
    content: @Composable () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                MifosOutlinedButton(
                    onClick = { onDismissRequest() },
                    text = {
                        Text(
                            text = dismissText,
                            color = KptTheme.colorScheme.primary,
                            style = MifosTypography.labelLarge,
                        )
                    },
                    modifier = Modifier.weight(1f),
                )

                Spacer(modifier = Modifier.width(KptTheme.spacing.sm))

                MifosTextButton(
                    onClick = { onConfirm() },
                    text = {
                        Text(
                            text = confirmText,
                            style = MifosTypography.labelLarge,
                        )
                    },
                    modifier = Modifier.weight(1f),
                    enabled = isConfirmEnabled,
                )
            }
        },
        title = {
            Text(
                text = title,
                style = MifosTypography.titleMediumEmphasized,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("AlertTitleText"),
            )
        },
        text = { content() },
        containerColor = KptTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.semantics { testTag = "AlertPopup" },
    )
}

@Preview
@Composable
private fun MifosBasicDialog_preview() {
    MifosTheme {
        MifosBasicDialog(
            visibilityState = BasicDialogState.Shown(
                title = "An error has occurred.",
                message = "Username or password is incorrect. Try again.",
            ),
            onDismissRequest = {},
        )
    }
}

/**
 * Models display of a [MifosBasicDialog].
 */
sealed class BasicDialogState {

    data object Hidden : BasicDialogState()

    data class Shown(
        val message: String,
        val title: String = "An Error Occurred!",
    ) : BasicDialogState()
}

class BasicDialogStatePreviewProvider : PreviewParameterProvider<BasicDialogState> {
    override val values: Sequence<BasicDialogState> = sequenceOf(
        BasicDialogState.Shown(
            title = "An error has occurred.",
            message = "Username or password is incorrect. Try again.",
        ),
        BasicDialogState.Shown(
            title = "Confirm Action",
            message = "Are you sure you want to continue?",
        ),
    )
}

@Preview
@Composable
private fun MifosBasicDialogPreview(
    @PreviewParameter(BasicDialogStatePreviewProvider::class)
    state: BasicDialogState,
) {
    MifosTheme {
        MifosBasicDialog(
            visibilityState = state,
            onConfirm = {},
            onDismissRequest = {},
        )
    }
}

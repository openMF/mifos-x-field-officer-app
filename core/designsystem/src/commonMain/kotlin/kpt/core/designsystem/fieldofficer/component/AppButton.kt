/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.designsystem.fieldofficer.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.theme.AppColors
import kpt.core.designsystem.theme.AppTheme
import kpt.core.designsystem.theme.DesignToken
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * App button with generic content slot. Wraps Material 3 [Button].
 *
 * @param onClick Will be called when the user clicks the button.
 * @param modifier Modifier to be applied to the button.
 * @param enabled Controls the enabled state of the button. When `false`, this button will not be
 * clickable and will appear disabled to accessibility services.
 * @param contentPadding The spacing values to apply internally between the container and the
 * content.
 * @param content The button content.
 */
@Composable
fun AppButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = DesignToken.shapes.medium,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = KptTheme.colorScheme.primary,
        contentColor = KptTheme.colorScheme.onPrimary,
        disabledContainerColor = KptTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        disabledContentColor = KptTheme.colorScheme.onSurface.copy(alpha = 0.38f),
    ),
    content: @Composable RowScope.() -> Unit = {},
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(DesignToken.sizes.imageDp48),
        enabled = enabled,
        colors = colors,
        shape = shape,
        elevation = elevation,
        contentPadding = contentPadding,
        content = content,
    )
}

/**
 * App button with generic content slot. Wraps Material 3 [Button].
 *
 * @param onClick Will be called when the user clicks the button.
 * @param modifier Modifier to be applied to the button.
 * @param enabled Controls the enabled state of the button. When `false`, this button will not be
 * clickable and will appear disabled to accessibility services.
 * @param contentPadding The spacing values to apply internally between the container and the
 * content.
 * @param content The button content.
 */
@Composable
fun AppButton(
    onClick: () -> Unit,
    text: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = DesignToken.shapes.medium,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = KptTheme.colorScheme.primary,
        contentColor = AppColors.customWhite,
    ),
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(DesignToken.sizes.imageDp48),
        enabled = enabled,
        colors = colors,
        shape = shape,
        elevation = elevation,
        contentPadding = contentPadding,
        content = {
            text()
        },
    )
}

/**
 * App outlined button with generic content slot. Wraps Material 3 [OutlinedButton].
 *
 * @param onClick Will be called when the user clicks the button.
 * @param modifier Modifier to be applied to the button.
 * @param enabled Controls the enabled state of the button. When `false`, this button will not be
 * clickable and will appear disabled to accessibility services.
 * @param contentPadding The spacing values to apply internally between the container and the
 * content.
 * @param content The button content.
 */
@Composable
fun AppOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.outlinedShape,
    border: BorderStroke? = ButtonDefaults.outlinedButtonBorder(enabled),
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit = {},
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .height(DesignToken.sizes.imageDp48),
        enabled = enabled,
        shape = shape,
        colors = colors,
        border = border,
        contentPadding = contentPadding,
        content = content,
    )
}

/**
 * App outlined button with text and optional leading icon.
 *
 * @param onClick Will be called when the user clicks the button.
 * @param modifier Modifier to be applied to the button.
 * @param enabled Controls the enabled state of the button. When `false`, this button will not be
 * clickable and will appear disabled to accessibility services.
 * @param text The button text label content.
 * @param leadingIcon The button leading icon content. Pass `null` here for no leading icon.
 */
@Composable
fun AppOutlinedButton(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    shape: Shape = DesignToken.shapes.medium,
    border: BorderStroke = BorderStroke(
        width = AppButtonDefaults.OutlinedButtonBorderWidth,
        color = KptTheme.colorScheme.secondaryContainer,
    ),
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(
        contentColor = KptTheme.colorScheme.primary,
    ),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(DesignToken.sizes.imageDp48),
        enabled = enabled,
        shape = shape,
        colors = colors,
        border = border,
        contentPadding = contentPadding,
    ) {
        AppButtonContent(
            text = text,
            leadingIcon = leadingIcon,
        )
    }
}

/**
 * App text button with generic content slot. Wraps Material 3 [TextButton].
 *
 * @param onClick Will be called when the user clicks the button.
 * @param modifier Modifier to be applied to the button.
 * @param enabled Controls the enabled state of the button. When `false`, this button will not be
 * clickable and will appear disabled to accessibility services.
 * @param content The button content.
 */
@Composable
fun AppTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit = {},
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.height(DesignToken.sizes.imageDp48),
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            containerColor = KptTheme.colorScheme.primary,
            contentColor = KptTheme.colorScheme.onPrimary,
            disabledContainerColor = KptTheme.colorScheme.onSurface.copy(
                alpha = .12f,
            ),
            disabledContentColor = KptTheme.colorScheme.onSurface.copy(
                .5f,
            ),
        ),
        content = content,
        shape = DesignToken.shapes.medium,
    )
}

/**
 * App text button with text and icon content slots.
 *
 * @param onClick Will be called when the user clicks the button.
 * @param modifier Modifier to be applied to the button.
 * @param enabled Controls the enabled state of the button. When `false`, this button will not be
 * clickable and will appear disabled to accessibility services.
 * @param text The button text label content.
 * @param leadingIcon The button leading icon content. Pass `null` here for no leading icon.
 */
@Composable
fun AppTextButton(
    text: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    AppTextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    ) {
        AppButtonContent(
            text = text,
            leadingIcon = leadingIcon,
        )
    }
}

/**
 * Internal App button content layout for arranging the text label and leading icon.
 *
 * @param text The button text label content.
 * @param leadingIcon The button leading icon content. Default is `null` for no leading icon.Ï
 */
@Composable
private fun AppButtonContent(
    text: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            Box(Modifier.sizeIn(maxHeight = ButtonDefaults.IconSize)) {
                leadingIcon()
            }
        }
        Box(
            Modifier
                .padding(
                    start = if (leadingIcon != null) {
                        ButtonDefaults.IconSpacing
                    } else {
                        DesignToken.padding.none
                    },
                ),
        ) {
            text()
        }
    }
}

/**
 * App button default values.
 */
@Suppress("ForbiddenComment")
object AppButtonDefaults {
    // TODO: File bug
    // OutlinedButton default border width isn't exposed via ButtonDefaults
    /** Border width of an outlined button. */
    val OutlinedButtonBorderWidth = 1.dp
}

@Preview
@Composable
private fun AppButtonPreview() {
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
private fun AppButtonWithTextPreview() {
    AppTheme {
        AppButton(
            onClick = {},
            text = { Text("Text Button") },
        )
    }
}

@Preview
@Composable
private fun AppOutlinedButtonPreview() {
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
private fun AppTextButtonPreview() {
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
private fun AppTextButtonWithIconPreview() {
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

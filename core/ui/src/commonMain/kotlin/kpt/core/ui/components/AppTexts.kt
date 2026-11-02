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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.theme.AppTheme
import kpt.core.designsystem.theme.DesignToken
import kpt.core.ui.generated.resources.Res
import kpt.core.ui.generated.resources.core_ui_ic_centers_24dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun AppTextTitleDescSingleLine(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            style = KptTheme.typography.labelMedium,
            text = title,
            modifier = Modifier
                .alpha(0.7f),
        )

        Text(
            style = KptTheme.typography.bodyMedium,
            text = description,
        )
    }
}

@Composable
fun AppTextTitleDescDoubleLine(
    title: String,
    description: String,
    descriptionStyle: TextStyle,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = KptTheme.typography.labelMedium,
            modifier = Modifier
                .alpha(0.7f)
                .fillMaxWidth(),
        )
        Text(
            text = description,
            style = descriptionStyle,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun AppTextTitleDescDrawableSingleLine(
    title: String,
    description: String,
    imageResId: DrawableResource,
    modifier: Modifier = Modifier,
    imageSize: Dp = DesignToken.sizes.imageDp14,
    // Null = the image is not tappable. A do-nothing default drew a tap target with no action.
    onDrawableClick: (() -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            style = KptTheme.typography.labelMedium,
            text = title,
            modifier = Modifier
                .weight(1f)
                .alpha(0.7f),
        )
        Text(
            style = KptTheme.typography.bodyMedium,
            text = description,
        )
        Spacer(modifier = Modifier.width(DesignToken.spacing.dp5))
        Image(
            painter = painterResource(imageResId),
            contentDescription = "Image",
            modifier = Modifier
                .size(imageSize)
                .then(if (onDrawableClick != null) Modifier.clickable(onClick = onDrawableClick) else Modifier),
        )
    }
}

@Composable
fun AppTitleDescSingleLineEqual(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            style = KptTheme.typography.labelMedium,
            text = title,
            modifier = Modifier
                .alpha(0.7f)
                .weight(1f),
        )

        Text(
            style = KptTheme.typography.bodyMedium,
            text = description,
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview
@Composable
private fun AppTextTitleDescSingleLinePreview(
    modifier: Modifier = Modifier,
) {
    AppTheme {
        AppTextTitleDescSingleLine(
            title = "AppTextTitleDescSingleLine Title",
            description = "AppTextTitleDescSingleLine Description",
            modifier = modifier,
        )
    }
}

@Preview
@Composable
private fun AppTextTitleDescDoubleLinePreview(
    modifier: Modifier = Modifier,
) {
    AppTheme {
        AppTextTitleDescDoubleLine(
            title = "AppTextTitleDescDoubleLine Title",
            description = "AppTextTitleDescDoubleLine Description",
            descriptionStyle = KptTheme.typography.bodyMedium,
            modifier = modifier,
        )
    }
}

@Preview
@Composable
private fun AppTextTitleDescDrawableSingleLinePreview(
    modifier: Modifier = Modifier,
) {
    AppTheme {
        AppTextTitleDescDrawableSingleLine(
            title = "AppTextTitleDescDrawableSingleLine Title",
            description = "AppTextTitleDescDrawableSingleLine Description",
            imageResId = Res.drawable.core_ui_ic_centers_24dp,
            modifier = modifier,
        )
    }
}

@Preview
@Composable
private fun AppTitleDescSingleLineEqualPreview(
    modifier: Modifier = Modifier,
) {
    AppTheme {
        AppTitleDescSingleLineEqual(
            title = "AppTitleDescSingleLineEqual Title",
            description = "AppTitleDescSingleLineEqual Description",
            modifier = modifier,
        )
    }
}

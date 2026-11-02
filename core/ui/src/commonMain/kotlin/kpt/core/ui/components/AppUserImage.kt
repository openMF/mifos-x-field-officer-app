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
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.ImageLoader
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.theme.AppTheme
import kpt.core.designsystem.theme.DesignToken
import kpt.core.ui.generated.resources.Res
import kpt.core.ui.generated.resources.profile
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun AppUserImage(
    bitmap: ByteArray?,
    modifier: Modifier = Modifier,
    hasBorder: Boolean = false,
) {
    val context = LocalPlatformContext.current

    val painter = if (bitmap != null) {
        rememberAsyncImagePainter(
            model = bitmap,
            imageLoader = ImageLoader(context),
        )
    } else {
        painterResource(Res.drawable.profile)
    }

    val imageModifier = modifier
        .clip(CircleShape)
        .then(
            if (hasBorder) {
                Modifier.border(
                    width = DesignToken.strokes.dp2,
                    color = KptTheme.colorScheme.primary,
                    shape = CircleShape,
                )
            } else {
                Modifier
            },
        )

    Image(
        modifier = imageModifier,
        painter = painter,
        contentDescription = "Profile Image",
        contentScale = if (bitmap != null) ContentScale.Crop else ContentScale.Fit,
    )
}

@Preview
@Composable
private fun AppUserImagePreview(
    modifier: Modifier = Modifier,
) {
    AppTheme {
        AppUserImage(
            bitmap = null,
            modifier = modifier,
        )
    }
}

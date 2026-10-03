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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.ImageLoader
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.theme.DesignToken
import kpt.core.designsystem.theme.MifosTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun MifosUserSignatureImage(
    bitmap: ByteArray?,
    emptyMessage: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalPlatformContext.current

    Box(
        modifier = modifier
            .background(
                color = KptTheme.colorScheme.surfaceVariant,
                shape = DesignToken.shapes.medium,
            )
            .size(width = DesignToken.sizes.imageDp256, height = DesignToken.sizes.imageDp128)
            .border(
                width = DesignToken.strokes.thin,
                color = KptTheme.colorScheme.primary,
                shape = DesignToken.shapes.medium,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                modifier = Modifier.fillMaxSize()
                    .clip(DesignToken.shapes.medium),
                painter = rememberAsyncImagePainter(
                    model = bitmap,
                    imageLoader = ImageLoader(context),
                ),
                contentDescription = "Signature Image",
                contentScale = ContentScale.Crop,
            )
        } else {
            Text(
                text = emptyMessage,
                style = KptTheme.typography.bodySmall,
                color = KptTheme.colorScheme.secondary,
            )
        }
    }
}

@Preview
@Composable
private fun MifosUserSignaturePreview(
    modifier: Modifier = Modifier,
) {
    MifosTheme {
        MifosUserImage(
            bitmap = null,
            modifier = modifier,
        )
    }
}

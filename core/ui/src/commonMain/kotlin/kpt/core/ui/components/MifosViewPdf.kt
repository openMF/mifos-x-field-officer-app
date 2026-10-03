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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.sp
import coil3.SingletonImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.theme.DesignToken

@Composable
fun <T> MifosViewPdf(
    bitmaps: List<T>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
    ) {
        itemsIndexed(bitmaps) { index, bmp ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(KptTheme.colorScheme.onPrimary),
            ) {
                AsyncImage(
                    model = bmp,
                    contentDescription = null,
                    // This project does not include coil's singleton artifact, so the loader is
                    // passed explicitly rather than resolved from a global.
                    imageLoader = SingletonImageLoader.get(LocalPlatformContext.current),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )

                Text(
                    text = "Page ${index + 1} of ${bitmaps.size}",
                    fontSize = 5.sp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = DesignToken.padding.medium, bottom = DesignToken.padding.small),
                )
            }

            if (bitmaps.size != 1) {
                HorizontalDivider()
            }
        }
    }
}

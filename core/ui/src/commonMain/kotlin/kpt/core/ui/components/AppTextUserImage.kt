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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.theme.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.min

@Composable
fun AppTextUserImage(
    text: String,
    modifier: Modifier = Modifier,
    color: Color? = null,
) {
    var boxSize by remember { mutableStateOf(Size.Zero) }
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                color ?: KptTheme.colorScheme.primary,
            )
            .onGloballyPositioned { coordinates ->
                boxSize = Size(
                    coordinates.size.width.toFloat(),
                    coordinates.size.height.toFloat(),
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            fontSize = with(LocalDensity.current) {
                (min(boxSize.width, boxSize.height) / 2).toSp()
            },
        )
    }
}

@Preview
@Composable
private fun AppTextUserImagePreview(
    modifier: Modifier = Modifier,
) {
    AppTheme {
        AppTextUserImage(
            text = "A",
            modifier = modifier,
        )
    }
}

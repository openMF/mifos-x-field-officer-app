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

import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.attafitamim.krop.core.crop.AspectRatio
import com.attafitamim.krop.core.crop.CropState
import com.attafitamim.krop.core.crop.RectCropShape
import com.attafitamim.krop.core.crop.cropperStyle
import com.attafitamim.krop.ui.ImageCropperDialog
import kpt.core.designsystem.fieldofficer.component.AppTopBar
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.DesignToken

@Composable
fun AppImageCropperDialog(
    state: CropState,
    aspectRatio: AspectRatio,
    modifier: Modifier = Modifier,
) {
    ImageCropperDialog(
        state = state,
        style = cropperStyle(
            shapes = listOf(
                RectCropShape,
            ),
            aspects = listOf(
                aspectRatio,
            ),
        ),
        topBar = {
            AppTopBar(
                modifier = modifier.height(DesignToken.sizes.topBarStandardHeight),
                topBarTitle = "",
                backPress = {
                    state.done(accept = false)
                },
                actions = {
                    IconButton(
                        onClick = {
                            state.reset()
                        },
                    ) {
                        Icon(AppIcons.Refresh, null)
                    }
                    IconButton(
                        onClick = {
                            state.done(accept = true)
                        },
                        enabled = !state.accepted,
                    ) {
                        Icon(AppIcons.Check, null)
                    }
                },
            )
        },
    )
}

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import kpt.core.designsystem.theme.AppTheme
import kpt.core.designsystem.theme.DesignToken
import kpt.core.ui.generated.resources.Res
import kpt.core.ui.generated.resources.core_ui_ic_group_black_24dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/** Round icon image. */
@Composable
fun AppRoundIcon(
    iconId: DrawableResource,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .clip(CircleShape),
    ) {
        Image(
            modifier = Modifier.padding(all = DesignToken.padding.dp6),
            painter = painterResource(iconId),
            contentDescription = "Icon",
        )
    }
}

@Preview
@Composable
private fun AppRoundIconPreview(
    modifier: Modifier = Modifier,
) {
    AppTheme {
        AppRoundIcon(
            iconId = Res.drawable.core_ui_ic_group_black_24dp,
            modifier = modifier,
        )
    }
}

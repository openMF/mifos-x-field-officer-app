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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.fieldofficer.component.AppOutlinedButton
import kpt.core.designsystem.theme.AppTheme
import kpt.core.designsystem.theme.DesignToken
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun AppRowWithTextAndButton(
    onBtnClick: () -> Unit,
    btnText: String,
    text: String,
    modifier: Modifier = Modifier,
    btnEnabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = DesignToken.shapes.medium)
            .border(
                DesignToken.strokes.thin,
                color = KptTheme.colorScheme.secondaryContainer,
                shape = DesignToken.shapes.medium,
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = KptTheme.typography.bodyLarge,
            fontFamily = FontFamily.SansSerif,
            modifier = Modifier.padding(
                start = DesignToken.padding.large,
                top = DesignToken.padding.large,
                bottom = DesignToken.padding.large,
            )
                .weight(.6f),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
        )

        AppOutlinedButton(
            onClick = {
                onBtnClick()
            },
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = KptTheme.colorScheme.onPrimary,
                contentColor = KptTheme.colorScheme.primary,
            ),
            shape = DesignToken.shapes.small,
            border = BorderStroke(
                DesignToken.strokes.thin,
                color = KptTheme.colorScheme.secondaryContainer,
            ),
            modifier = Modifier
                .padding(end = DesignToken.padding.large)
                .height(DesignToken.sizes.iconExtraLarge)
                .wrapContentWidth(),
            enabled = btnEnabled,
        ) {
            Text(
                text = btnText,
                style = KptTheme.typography.labelLarge,
                fontFamily = FontFamily.SansSerif,
            )
        }
    }
}

@Composable
@Preview
private fun AppRowWithTextAndButtonPreview() {
    AppTheme {
        AppRowWithTextAndButton(
            onBtnClick = {},
            btnText = "View",
            text = "2 Collaterals",
        )
    }
}

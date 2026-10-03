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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.theme.DesignToken
import kpt.core.designsystem.theme.MifosTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun MifosTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = KptTheme.colorScheme.primary,
    unselectedColor: Color = KptTheme.colorScheme.primaryContainer,
) {
    Tab(
        text = {
            Text(text = text)
        },
        selected = selected,
        onClick = onClick,
        selectedContentColor = contentColorFor(selectedColor),
        unselectedContentColor = contentColorFor(unselectedColor),
        modifier = modifier
            .clip(DesignToken.shapes.dp25)
            .background(if (selected) selectedColor else unselectedColor)
            .padding(horizontal = DesignToken.padding.largeIncreased),
    )
}

@Preview
@Composable
private fun MifosTabPreview() {
    MifosTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
            modifier = Modifier.padding(KptTheme.spacing.md),
        ) {
            MifosTab(
                text = "Selected Tab",
                selected = true,
                onClick = {},
            )
            MifosTab(
                text = "Unselected Tab",
                selected = false,
                onClick = {},
            )
        }
    }
}

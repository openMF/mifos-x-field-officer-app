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

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.KptTheme
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.theme.DesignToken
import kpt.core.designsystem.theme.MifosTypography
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun MifosGeneralCardComponentOutline(
    modifier: Modifier = Modifier,
    borderCorner: Dp = DesignToken.sizes.iconMiny,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .border(
                width = DesignToken.strokes.thin,
                shape = RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 12.dp,
                    bottomStart = borderCorner,
                    bottomEnd = borderCorner,
                ),
                color = KptTheme.colorScheme.secondaryContainer,
            ),
    ) {
        content()
    }
}

@Composable
fun MifosGeneralRowItem(
    keyContent: @Composable () -> Unit,
    valueContent: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart,
        ) { keyContent() }

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterEnd,
        ) { valueContent() }
    }
}

@Composable
fun MifosGeneralCard(
    modifier: Modifier = Modifier,
    contentMap: Map<String, String>,
    separator: String = " : ",
) {
    MifosGeneralCardComponentOutline {
        Column(
            modifier = modifier.padding(DesignToken.padding.large),
        ) {
            contentMap.entries.forEachIndexed { index, map ->
                MifosGeneralRowItem(
                    keyContent = { Text(text = map.key + separator, style = MifosTypography.labelMediumEmphasized) },
                    valueContent = { Text(text = map.value, style = MifosTypography.labelMediumEmphasized) },
                )
                if (index < contentMap.size - 1) {
                    Spacer(modifier = Modifier.height(DesignToken.padding.small))
                }
            }
        }
    }
}

@Preview
@Composable
fun MifosPreviewGeneralCard() {
    KptTheme {
        MifosGeneralCard(
            contentMap = mapOf(
                "title" to "answer",
                "title1" to "ans1",
                "title2" to "ans2",
                "title3" to "ans3",
                "title4" to "ans4",
            ),
        )
    }
}

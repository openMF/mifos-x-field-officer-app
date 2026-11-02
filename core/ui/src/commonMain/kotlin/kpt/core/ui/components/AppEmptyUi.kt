/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.AppTheme
import kpt.core.designsystem.theme.DesignToken
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun AppEmptyUi(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = AppIcons.Info,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .semantics {
                contentDescription = "AppEmptyUi"
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(DesignToken.padding.dp18)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DesignToken.spacing.largeIncreased, Alignment.CenterVertically),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text + icon.name,
                modifier = Modifier.size(DesignToken.sizes.profile),
            )

            Text(
                text = text,
                style = KptTheme.typography.bodyMedium,
            )
        }
    }
}

@Preview
@Composable
private fun AppEmptyUiPreview() {
    AppTheme {
        AppEmptyUi(
            text = "No data available",
            icon = AppIcons.Info,
        )
    }
}

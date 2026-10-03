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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.theme.DesignToken
import kpt.core.designsystem.theme.MifosTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun MifosErrorContent(
    message: String,
    modifier: Modifier = Modifier,
    isRefreshEnabled: Boolean = false,
    imageVector: ImageVector? = null,
    onRefresh: () -> Unit = {},
    refreshButtonText: String = "",
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = imageVector ?: Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(DesignToken.sizes.imageDp48),
        )
        if (isRefreshEnabled) {
            Text(
                text = message,
                modifier = Modifier.padding(vertical = KptTheme.spacing.md),
            )
            Button(onClick = onRefresh) {
                Text(text = refreshButtonText)
            }
        }
    }
}

@Preview
@Composable
private fun MifosErrorContentPreview() {
    MifosTheme {
        MifosErrorContent(
            message = "Something went wrong. Please try again later.",
        )
    }
}

@Preview
@Composable
private fun MifosErrorContentWithRefreshPreview() {
    MifosTheme {
        MifosErrorContent(
            message = "Failed to load data.",
            isRefreshEnabled = true,
            refreshButtonText = "Retry",
            onRefresh = { /* Handle refresh */ },
        )
    }
}

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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.theme.DesignToken
import kpt.core.designsystem.theme.MifosTheme
import kpt.core.ui.util.DevicePreview

@Composable
fun MifosLinkText(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isUnderlined: Boolean = true,
) {
    Text(
        text = text,
        style = KptTheme.typography.bodyMedium.copy(
            textDecoration = if (isUnderlined) TextDecoration.Underline else null,
        ),
        modifier = modifier
            .padding(vertical = DesignToken.padding.extraExtraSmall)
            .clickable {
                onClick()
            },
    )
}

@DevicePreview
@Composable
private fun MifosLinkTextPreview(
    modifier: Modifier = Modifier,
) {
    MifosTheme {
        MifosLinkText(
            text = "Link Text",
            onClick = {},
            modifier = modifier,
        )
    }
}

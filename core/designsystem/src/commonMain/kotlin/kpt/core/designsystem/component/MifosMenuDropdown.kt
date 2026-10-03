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

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.theme.DesignToken
import kpt.core.designsystem.theme.MifosTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun MifosMenuDropDownItem(
    option: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DropdownMenuItem(
        text = {
            Text(
                modifier = Modifier.padding(DesignToken.padding.dp6),
                text = option,
                style = KptTheme.typography.bodyLarge,
            )
        },
        onClick = { onClick() },
        modifier = modifier,
    )
}

@Preview
@Composable
private fun MifosMenuDropDownItemPreview() {
    MifosTheme {
        DropdownMenu(
            expanded = true,
            onDismissRequest = {},
        ) {
            MifosMenuDropDownItem(
                option = "Profile",
                onClick = {},
            )
            MifosMenuDropDownItem(
                option = "Settings",
                onClick = {},
            )
            MifosMenuDropDownItem(
                option = "Logout",
                onClick = {},
            )
        }
    }
}

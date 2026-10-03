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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kpt.core.designsystem.component.MifosButton
import kpt.core.designsystem.theme.DesignToken
import kpt.core.designsystem.theme.MifosTheme
import kpt.core.designsystem.theme.MifosTypography
import kpt.core.ui.generated.resources.Res
import kpt.core.ui.generated.resources.core_ui_click_here_to_view_filled_state
import kpt.core.ui.generated.resources.core_ui_click_to_add_new
import kpt.core.ui.generated.resources.core_ui_no_item_found
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun MifosEmptyCard(
    msg: String? = stringResource(Res.string.core_ui_click_here_to_view_filled_state),
    title: String = stringResource(Res.string.core_ui_no_item_found),
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    isButtonPresent: Boolean = false,
) {
    MifosListingComponentOutline {
        Column(modifier.fillMaxWidth()) {
            Text(
                text = title,
                style = MifosTypography.titleSmallEmphasized,
            )
            if (msg != null) {
                Spacer(Modifier.height((DesignToken.padding.medium)))
                Text(
                    text = msg,
                    style = MifosTypography.bodySmall,
                )
            }
            if (isButtonPresent) {
                Spacer(Modifier.height((DesignToken.padding.medium)))
                MifosButton(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(Res.string.core_ui_click_to_add_new))
                }
            }
        }
    }
}

@Preview
@Composable
private fun MifosEmptyCardPreview() {
    MifosTheme {
        MifosEmptyCard("Add any to show", isButtonPresent = false)
    }
}

@Preview
@Composable
private fun MifosEmptyCardWithButtonPreview() {
    MifosTheme {
        MifosEmptyCard("Add any to show", isButtonPresent = true)
    }
}

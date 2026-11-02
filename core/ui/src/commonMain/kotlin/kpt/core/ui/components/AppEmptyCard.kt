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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kpt.core.designsystem.fieldofficer.component.AppButton
import kpt.core.designsystem.theme.AppTheme
import kpt.core.designsystem.theme.AppTypography
import kpt.core.designsystem.theme.DesignToken
import kpt.core.ui.generated.resources.Res
import kpt.core.ui.generated.resources.core_ui_click_here_to_view_filled_state
import kpt.core.ui.generated.resources.core_ui_click_to_add_new
import kpt.core.ui.generated.resources.core_ui_no_item_found
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun AppEmptyCard(
    modifier: Modifier = Modifier,
    msg: String? = stringResource(Res.string.core_ui_click_here_to_view_filled_state),
    title: String = stringResource(Res.string.core_ui_no_item_found),
    onClick: () -> Unit = {},
    isButtonPresent: Boolean = false,
) {
    AppListingComponentOutline {
        Column(modifier.fillMaxWidth()) {
            Text(
                text = title,
                style = AppTypography.titleSmallEmphasized,
            )
            if (msg != null) {
                Spacer(Modifier.height((DesignToken.padding.medium)))
                Text(
                    text = msg,
                    style = AppTypography.bodySmall,
                )
            }
            if (isButtonPresent) {
                Spacer(Modifier.height((DesignToken.padding.medium)))
                AppButton(
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
private fun AppEmptyCardPreview() {
    AppTheme {
        AppEmptyCard(msg = "Add any to show", isButtonPresent = false)
    }
}

@Preview
@Composable
private fun AppEmptyCardWithButtonPreview() {
    AppTheme {
        AppEmptyCard(msg = "Add any to show", isButtonPresent = true)
    }
}

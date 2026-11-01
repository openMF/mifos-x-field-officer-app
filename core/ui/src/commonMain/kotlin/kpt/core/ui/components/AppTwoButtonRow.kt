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

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.fieldofficer.component.AppOutlinedButton
import kpt.core.designsystem.fieldofficer.component.AppTextButton
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.AppTheme
import kpt.core.designsystem.theme.AppTypography
import kpt.core.designsystem.theme.DesignToken
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * The cancel / confirm button pair used by every form in this app.
 *
 * ## Why the two buttons take their own test tags
 *
 * `modifier` reaches the ROW, and a Row carries no enabled semantics — so a test or a Maestro flow
 * that tags the row cannot assert whether the CONFIRM button is live, and asserting on the button's
 * label text finds the `Text` inside it rather than the clickable. Without a handle on the button
 * itself, "is the submit affordance disabled?" is unanswerable from outside, which is exactly the
 * question this codebase's absent-never-inert rule turns on.
 *
 * Both default to no tag, so existing call sites are unchanged.
 */
@Composable
fun AppTwoButtonRow(
    firstBtnText: String,
    secondBtnText: String,
    onFirstBtnClick: () -> Unit,
    onSecondBtnClick: () -> Unit,
    modifier: Modifier = Modifier,
    isButtonIconVisible: Boolean = true,
    isFirstButtonEnabled: Boolean = true,
    isSecondButtonEnabled: Boolean = true,
    firstBtnTestTag: String? = null,
    secondBtnTestTag: String? = null,
) {
    Row(modifier = modifier.fillMaxWidth()) {
        AppOutlinedButton(
            onClick = {
                onFirstBtnClick()
            },
            leadingIcon = {
                if (isButtonIconVisible) {
                    Icon(
                        imageVector = AppIcons.ChevronLeft,
                        contentDescription = null,
                        modifier = Modifier.size(DesignToken.sizes.iconAverage),
                        tint = KptTheme.colorScheme.primary,
                    )
                }
            },
            text = {
                Text(
                    text = firstBtnText,
                    color = KptTheme.colorScheme.primary,
                    style = AppTypography.labelLarge,
                )
            },
            modifier = Modifier
                .weight(1f)
                .height(DesignToken.sizes.buttonHeightMedium)
                .then(firstBtnTestTag?.let { Modifier.testTag(it) } ?: Modifier),
            enabled = isFirstButtonEnabled,
        )
        Spacer(Modifier.padding(DesignToken.padding.small))
        AppTextButton(
            onClick = {
                onSecondBtnClick()
            },
            leadingIcon = {
                if (isButtonIconVisible) {
                    Icon(
                        imageVector = AppIcons.Check,
                        contentDescription = null,
                        modifier = Modifier.size(DesignToken.sizes.iconAverage),
                    )
                }
            },
            text = {
                Text(
                    text = secondBtnText,
                    style = AppTypography.labelLarge,
                )
            },
            modifier = Modifier
                .weight(1f)
                .height(DesignToken.sizes.buttonHeightMedium)
                .then(secondBtnTestTag?.let { Modifier.testTag(it) } ?: Modifier),
            enabled = isSecondButtonEnabled,
        )
    }
}

@Composable
@Preview
private fun AppTwoButtonRowPreview() {
    AppTheme {
        AppTwoButtonRow(
            firstBtnText = "First Button",
            secondBtnText = "Second Button",
            onFirstBtnClick = {},
            onSecondBtnClick = {},
        )
    }
}

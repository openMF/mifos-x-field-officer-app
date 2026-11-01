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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.fieldofficer.component.AppButton
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.AppColors
import kpt.core.designsystem.theme.AppTheme
import kpt.core.designsystem.theme.AppTypography
import kpt.core.designsystem.theme.DesignToken
import kpt.core.ui.util.TextUtil
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRowCard(
    title: String,
    leftValues: List<TextUtil>,
    rightValues: List<TextUtil>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppListingComponentOutline {
        Row(
            modifier
                .fillMaxWidth()
                .clickable { onClick() },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(DesignToken.padding.extraExtraSmall),
            ) {
                PrintTextUtil(
                    TextUtil(
                        text = title,
                        style = AppTypography.titleSmallEmphasized,
                    ),
                )
                leftValues.forEach {
                    PrintTextUtil(it)
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(DesignToken.padding.extraExtraSmall),
                ) {
                    rightValues.forEach {
                        PrintTextUtil(it)
                    }
                }
                Icon(
                    imageVector = AppIcons.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(DesignToken.sizes.iconSmall),
                )
            }
        }
    }
}

@Composable
fun AppRowCard(
    title: String,
    leftValues: List<TextUtil>,
    rightValues: List<TextUtil>,
    modifier: Modifier = Modifier,
    byteArray: ByteArray? = null,
    imageVector: DrawableResource? = null,
) {
    Row(
        modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (imageVector == null) {
            AppUserImage(
                bitmap = byteArray,
                modifier = Modifier.size(DesignToken.sizes.iconExtraLarge),
            )
        } else {
            Icon(
                painter = painterResource(imageVector),
                modifier = Modifier
                    .size(DesignToken.sizes.iconExtraLarge)
                    .background(
                        color = KptTheme.colorScheme.surfaceBright,
                        shape = CircleShape,
                    )
                    .padding(DesignToken.padding.small),
                contentDescription = null,
            )
        }
        Spacer(Modifier.width(DesignToken.padding.medium))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(DesignToken.padding.extraExtraSmall),
        ) {
            PrintTextUtil(
                TextUtil(
                    text = title,
                    style = AppTypography.titleSmallEmphasized,
                    color = KptTheme.colorScheme.onSurface,
                ),
            )
            leftValues.forEach {
                PrintTextUtil(
                    it,
                )
            }
        }
        Spacer(Modifier.width(DesignToken.padding.medium))
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(DesignToken.padding.extraExtraSmall),
                horizontalAlignment = Alignment.End,
            ) {
                rightValues.forEach {
                    PrintTextUtil(
                        it,
                    )
                }
            }
            Icon(
                imageVector = AppIcons.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(DesignToken.sizes.iconMiny),
            )
        }
    }
}

@Composable
fun AppRowTextWithButton(
    title: String,
    description: String,
    size: String,
    btnText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppListingComponentOutline {
        Row(modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(DesignToken.padding.extraExtraSmall),
            ) {
                Text(
                    text = title,
                    style = AppTypography.labelSmallEmphasized,
                )
                Text(
                    text = description,
                    style = AppTypography.bodySmallEmphasized,
                )
                Text(
                    text = size,
                    style = AppTypography.labelSmall,
                )
            }
            AppButton(
                onClick = onClick,
            ) {
                Text(
                    text = btnText,
                    style = AppTypography.labelMediumEmphasized,
                    color = KptTheme.colorScheme.primary,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRowWithLabels(
    byteArray: ByteArray?,
    title: String,
    subTitle: String,
    label1: TextUtil,
    label2: TextUtil,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppListingComponentOutline {
        Row(
            modifier
                .fillMaxWidth()
                .padding(DesignToken.padding.extraSmall)
                .clickable { onClick() },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppUserImage(
                bitmap = byteArray,
                modifier = Modifier.size(DesignToken.sizes.iconMedium),
            )
            Row(
                Modifier.padding(start = DesignToken.padding.small).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(DesignToken.padding.extraExtraSmall),
                ) {
                    PrintTextUtil(
                        TextUtil(
                            text = title,
                            style = AppTypography.titleSmallEmphasized,
                        ),
                    )
                    PrintTextUtil(
                        TextUtil(
                            text = subTitle,
                            style = AppTypography.bodySmall,
                        ),
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(DesignToken.padding.extraExtraSmall),
                    ) {
                        PrintTextUtil(label1)
                        PrintTextUtil(label2)
                    }
                    Icon(
                        imageVector = AppIcons.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(DesignToken.sizes.iconSmall),
                    )
                }
            }
        }
    }
}

@Composable
fun AppDropDownRow(
    isActive: Boolean,
    title: String,
    accountNo: String,
    office: String,
    date: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(DesignToken.padding.extraSmall)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppTextUserImage(
            text = if (isActive)"Active" else "Closed",
            color = if (isActive) AppColors.customEnable else AppColors.lightRed,
            modifier = Modifier.size(DesignToken.sizes.iconExtraLarge),
        )
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = DesignToken.padding.extraSmall),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(DesignToken.padding.extraExtraSmall),
            ) {
                PrintTextUtil(
                    TextUtil(
                        text = title,
                        style = AppTypography.titleSmallEmphasized,
                    ),
                )
                PrintTextUtil(
                    TextUtil(
                        text = accountNo,
                        style = AppTypography.bodySmall,
                    ),
                )
                PrintTextUtil(
                    TextUtil(
                        text = office,
                        style = AppTypography.bodySmall,
                    ),
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PrintTextUtil(
                    TextUtil(
                        text = date,
                        style = AppTypography.labelSmall,
                    ),
                )
                Icon(
                    imageVector = AppIcons.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(DesignToken.sizes.iconSmall),
                )
            }
        }
    }
}

@Preview
@Composable
private fun AppRowTextWithButton() {
    AppTheme {
        AppRowTextWithButton(
            title = "Title",
            description = "Description",
            size = "Size",
            btnText = "Button",
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun PreviewAppRowCard() {
    AppTheme {
        AppRowCard(
            title = "Customer Info",
            leftValues = listOf(
                TextUtil(text = "Name", style = AppTypography.labelMediumEmphasized),
                TextUtil(text = "Age", style = AppTypography.labelMedium),
                TextUtil(text = "City", style = AppTypography.labelMedium),
            ),
            rightValues = listOf(
                TextUtil(text = "453", style = AppTypography.labelMedium, color = Color.Green),
                TextUtil(text = "455", style = AppTypography.labelMedium),
            ),
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun PreviewAppRowCardWithImage() {
    AppTheme {
        AppRowCard(
            title = "Customer Info",
            leftValues = listOf(
                TextUtil(text = "Name", style = AppTypography.labelMediumEmphasized),
                TextUtil(text = "Age", style = AppTypography.labelMedium),
                TextUtil(text = "City", style = AppTypography.labelMedium),
            ),
            rightValues = listOf(
                TextUtil(text = "Closed", style = AppTypography.labelMedium, color = Color.Red),
                TextUtil(text = "455", style = AppTypography.labelMedium),
            ),
            byteArray = null,
        )
    }
}

@Preview
@Composable
private fun AppRowWithLabelsPreview() {
    AppTheme {
        AppRowWithLabels(
            title = "Title",
            subTitle = "Sub Title",
            label1 = TextUtil("label1", color = Color.Green),
            label2 = TextUtil("label2"),
            onClick = {},
            byteArray = null,
        )
    }
}

@Preview
@Composable
private fun AppRowDropDownPreview() {
    AppTheme {
        AppDropDownRow(
            isActive = true,
            title = "John Wault",
            accountNo = "Acc No. 838272",
            office = "Head Office",
            date = "20-04-2020",
            onClick = {},
        )
    }
}

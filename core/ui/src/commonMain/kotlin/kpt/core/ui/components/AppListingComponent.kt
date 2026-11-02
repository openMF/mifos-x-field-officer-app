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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import kpt.core.base.designsystem.KptTheme
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.AppColors
import kpt.core.designsystem.theme.AppTypography
import kpt.core.designsystem.theme.DesignToken
import kpt.core.ui.generated.resources.Res
import kpt.core.ui.generated.resources.core_ui_account_no
import kpt.core.ui.generated.resources.core_ui_action
import kpt.core.ui.generated.resources.core_ui_amount_paid
import kpt.core.ui.generated.resources.core_ui_balance
import kpt.core.ui.generated.resources.core_ui_calculation_type
import kpt.core.ui.generated.resources.core_ui_cancelled
import kpt.core.ui.generated.resources.core_ui_completed
import kpt.core.ui.generated.resources.core_ui_description
import kpt.core.ui.generated.resources.core_ui_document_id
import kpt.core.ui.generated.resources.core_ui_document_key
import kpt.core.ui.generated.resources.core_ui_document_type
import kpt.core.ui.generated.resources.core_ui_due
import kpt.core.ui.generated.resources.core_ui_due_as_of
import kpt.core.ui.generated.resources.core_ui_effective_from
import kpt.core.ui.generated.resources.core_ui_end_date
import kpt.core.ui.generated.resources.core_ui_identify_documents
import kpt.core.ui.generated.resources.core_ui_last_active
import kpt.core.ui.generated.resources.core_ui_loan_balance
import kpt.core.ui.generated.resources.core_ui_loan_product
import kpt.core.ui.generated.resources.core_ui_name
import kpt.core.ui.generated.resources.core_ui_original_loan
import kpt.core.ui.generated.resources.core_ui_outstanding
import kpt.core.ui.generated.resources.core_ui_owner_external_id
import kpt.core.ui.generated.resources.core_ui_paid
import kpt.core.ui.generated.resources.core_ui_pending
import kpt.core.ui.generated.resources.core_ui_purchase_price_ratio
import kpt.core.ui.generated.resources.core_ui_quantity
import kpt.core.ui.generated.resources.core_ui_savings_product
import kpt.core.ui.generated.resources.core_ui_settlement_date
import kpt.core.ui.generated.resources.core_ui_start_date
import kpt.core.ui.generated.resources.core_ui_status
import kpt.core.ui.generated.resources.core_ui_total_collateral_value
import kpt.core.ui.generated.resources.core_ui_total_value
import kpt.core.ui.generated.resources.core_ui_transfer_external_id
import kpt.core.ui.generated.resources.core_ui_type
import kpt.core.ui.generated.resources.core_ui_waived
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AppListingComponentOutline(
    modifier: Modifier = Modifier,
    borderColor: Color = KptTheme.colorScheme.secondaryContainer,
    backgroundColor: Color = KptTheme.colorScheme.surface,
    shape: Shape = DesignToken.shapes.medium,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(DesignToken.padding.extraExtraSmall)
            .border(
                width = DesignToken.strokes.thin,
                shape = shape,
                color = borderColor,
            )
            .background(color = backgroundColor, shape = shape)
            .padding(DesignToken.padding.large),
    ) {
        content()
    }
}

@Composable
fun AppListingRowItem(
    keyContent: @Composable () -> Unit,
    valueContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
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
}

@Composable
fun AppListingColumnItem(
    keyContent: @Composable () -> Unit,
    valueContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        keyContent()
        valueContent()
    }
}

@Composable
fun AppListingColumnItem(
    key: String,
    value: String,
    modifier: Modifier = Modifier,
    keyStyle: TextStyle = AppTypography.labelSmall,
    keyColor: Color = KptTheme.colorScheme.secondary,
    valueStyle: TextStyle = AppTypography.bodySmall,
    valueColor: Color = KptTheme.colorScheme.onSurface,
) {
    AppListingColumnItem(
        modifier = modifier,
        keyContent = {
            Text(
                text = key,
                style = keyStyle.copy(color = keyColor),
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )
        },
        valueContent = {
            Text(
                text = value,
                style = valueStyle.copy(color = valueColor),
                overflow = TextOverflow.Clip,
                maxLines = 1,
            )
        },
    )
}

@Composable
fun AppListingRowItem(
    key: String,
    value: String,
    modifier: Modifier = Modifier,
    keyStyle: TextStyle = AppTypography.labelMediumEmphasized,
    valueStyle: TextStyle = AppTypography.labelMedium,
    valueColor: Color = KptTheme.colorScheme.onSurface,
) {
    AppListingRowItem(
        modifier = modifier,
        keyContent = {
            if (key.isNotBlank()) {
                Text(
                    text = "$key:",
                    style = keyStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        valueContent = {
            Text(
                text = value,
                style = valueStyle.copy(color = valueColor),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        },
    )
}

@Composable
fun AppListingRowItemHeader(
    text: String,
    modifier: Modifier = Modifier,
    keyStyle: TextStyle = AppTypography.labelMediumEmphasized,
) {
    AppListingRowItem(
        modifier = modifier,
        keyContent = {
            if (text.isNotBlank()) {
                Text(text = text, style = keyStyle)
            }
        },
        valueContent = {
            Icon(
                imageVector = AppIcons.MoreHoriz,
                contentDescription = "More vert",
            )
        },
    )
}

@Composable
fun AppDefaultListingComponent(
    data: Map<String, String>,
    modifier: Modifier = Modifier,
    color: Color = KptTheme.colorScheme.primary.copy(alpha = 0.5f),
) {
    AppListingComponentOutline(
        modifier = modifier,
        borderColor = color,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(DesignToken.padding.extraExtraSmall),
        ) {
            data.forEach { (key, value) ->
                AppListingRowItem(
                    key = key,
                    value = value,
                )
            }
        }
    }
}

@Composable
fun AppDefaultListingComponentFromStringResources(
    data: Map<StringResource, String>,
    modifier: Modifier = Modifier,
    borderColor: Color = KptTheme.colorScheme.primary.copy(alpha = 0.5f),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(DesignToken.padding.extraExtraSmall),
    backgroundColor: Color = KptTheme.colorScheme.surface,
) {
    AppListingComponentOutline(
        modifier = modifier,
        borderColor = borderColor,
        backgroundColor = backgroundColor,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = verticalArrangement,
        ) {
            data.forEach { (key, value) ->
                AppListingRowItem(
                    key = stringResource(key),
                    value = value,
                )
            }
        }
    }
}

@Composable
fun AppClientFeeListingComponent(
    name: String,
    dueAsOf: String,
    due: String,
    paid: String,
    waived: String,
    outstanding: String,
    modifier: Modifier = Modifier,
) {
    AppListingComponentOutline(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            AppListingRowItem(
                key = stringResource(Res.string.core_ui_name),
                value = name,
                keyStyle = AppTypography.titleSmallEmphasized,
                valueStyle = AppTypography.titleSmall,
            )
            Spacer(Modifier.height(DesignToken.padding.large))
            AppListingRowItem(
                key = stringResource(Res.string.core_ui_due_as_of),
                value = dueAsOf,
            )
            Spacer(Modifier.height(DesignToken.padding.medium))
            Column(
                verticalArrangement = Arrangement.spacedBy(DesignToken.padding.extraExtraSmall),
            ) {
                AppListingRowItem(
                    key = stringResource(Res.string.core_ui_due),
                    value = due,
                    valueColor = KptTheme.colorScheme.error,
                )
                AppListingRowItem(
                    key = stringResource(Res.string.core_ui_paid),
                    value = paid,
                    valueColor = AppColors.customEnable,
                )
                AppListingRowItem(
                    key = stringResource(Res.string.core_ui_waived),
                    value = waived,
                    valueColor = AppColors.customYellow,
                )
            }
            Spacer(Modifier.height(DesignToken.padding.medium))
            AppListingRowItem(
                key = stringResource(Res.string.core_ui_outstanding),
                value = outstanding,
            )
        }
    }
}

@Composable
fun AppLoanListingComponent(
    accountNo: String,
    loanProduct: String,
    originalLoan: String,
    amountPaid: String,
    loanBalance: String,
    type: String,
    modifier: Modifier = Modifier,
) {
    AppListingComponentOutline(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            AppListingRowItem(
                key = stringResource(Res.string.core_ui_account_no),
                value = accountNo,
                keyStyle = AppTypography.titleSmallEmphasized,
                valueStyle = AppTypography.titleSmall,
            )
            Spacer(Modifier.height(DesignToken.padding.large))
            AppListingRowItem(
                key = stringResource(Res.string.core_ui_loan_product),
                value = loanProduct,
            )
            Spacer(Modifier.height(DesignToken.padding.medium))
            Column(
                verticalArrangement = Arrangement.spacedBy(DesignToken.padding.extraExtraSmall),
            ) {
                AppListingRowItem(
                    key = stringResource(Res.string.core_ui_original_loan),
                    value = originalLoan,
                    valueColor = KptTheme.colorScheme.primary,
                )
                AppListingRowItem(
                    key = stringResource(Res.string.core_ui_amount_paid),
                    value = amountPaid,
                    valueColor = AppColors.customEnable,
                )
                AppListingRowItem(
                    key = stringResource(Res.string.core_ui_loan_balance),
                    value = loanBalance,
                    valueColor = AppColors.customYellow,
                )
            }
            Spacer(Modifier.height(DesignToken.padding.medium))
            AppListingRowItem(
                key = stringResource(Res.string.core_ui_type),
                value = type,
            )
        }
    }
}

@Composable
fun AppSavingsListingComponent(
    accountNo: String,
    savingsProduct: String,
    lastActive: String,
    balance: String,
    modifier: Modifier = Modifier,
) {
    AppListingComponentOutline(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            AppListingRowItem(
                key = stringResource(Res.string.core_ui_account_no),
                value = accountNo,
                keyStyle = AppTypography.titleSmallEmphasized,
                valueStyle = AppTypography.titleSmall,
            )
            Spacer(Modifier.height(DesignToken.padding.large))
            AppListingRowItem(
                key = stringResource(Res.string.core_ui_savings_product),
                value = savingsProduct,
            )
            Spacer(Modifier.height(DesignToken.padding.medium))
            Column(
                verticalArrangement = Arrangement.spacedBy(DesignToken.padding.extraExtraSmall),
            ) {
                AppListingRowItem(
                    key = stringResource(Res.string.core_ui_last_active),
                    value = lastActive,
                )
                AppListingRowItem(
                    key = stringResource(Res.string.core_ui_balance),
                    value = balance,
                    valueColor = KptTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
fun AppCollateralDataListingComponent(
    name: String,
    quantity: String,
    totalValue: String,
    totalCollateralValue: String,
    modifier: Modifier = Modifier,
) {
    AppListingComponentOutline(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            AppListingRowItem(
                key = "Name",
                value = name,
                keyStyle = AppTypography.titleSmallEmphasized,
                valueStyle = AppTypography.titleSmall,
            )
            Spacer(Modifier.height(DesignToken.padding.large))
            Column(
                verticalArrangement = Arrangement.spacedBy(DesignToken.padding.extraExtraSmall),
            ) {
                AppListingRowItem(
                    key = stringResource(Res.string.core_ui_quantity),
                    value = quantity,
                )
                AppListingRowItem(
                    key = stringResource(Res.string.core_ui_total_value),
                    value = totalValue,
                )
            }
            Spacer(Modifier.height(DesignToken.padding.medium))
            AppListingRowItem(
                key = stringResource(Res.string.core_ui_total_collateral_value),
                value = totalCollateralValue,
                valueColor = KptTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
fun AppIdentifierListingComponent(
    type: String,
    id: String,
    key: String,
    status: Status,
    description: String,
    identifyDocuments: String,
    modifier: Modifier = Modifier,
) {
    AppListingComponentOutline(modifier = modifier) {
        Column {
            AppListingRowItem(
                key = stringResource(Res.string.core_ui_document_type),
                value = type,
                keyStyle = AppTypography.titleSmallEmphasized,
                valueStyle = AppTypography.titleSmall,
            )
            Spacer(Modifier.height(DesignToken.padding.large))
            Column(
                verticalArrangement = Arrangement.spacedBy(DesignToken.padding.medium),
            ) {
                AppListingRowItem(
                    key = stringResource(Res.string.core_ui_document_id),
                    value = id,
                )
                AppListingRowItem(
                    key = stringResource(Res.string.core_ui_document_key),
                    value = key,
                )
                AppListingRowItem(
                    key = stringResource(Res.string.core_ui_status),
                    value = status.name,
                    valueColor = status.color,
                )
                AppListingRowItem(
                    key = stringResource(Res.string.core_ui_description),
                    value = description,
                )
                AppListingRowItem(
                    key = stringResource(Res.string.core_ui_identify_documents),
                    value = identifyDocuments,
                )
            }
        }
    }
}

enum class Status(val color: Color) {
    Inactive(AppColors.lightRed),
    Pending(AppColors.customYellow),
    Active(AppColors.customEnable),
}

@Composable
fun AppDelinquencyListingComponent(
    date: String,
    time: String,
    isPause: Boolean,
    startDate: String,
    endDate: String,
    modifier: Modifier = Modifier,
) {
    AppListingComponentOutline(modifier = modifier) {
        Column {
            AppListingRowItem(
                value = "$date; $time",
                key = "",
                valueStyle = AppTypography.titleSmallEmphasized,
            )
            Spacer(Modifier.height(DesignToken.padding.large))
            Column(
                verticalArrangement = Arrangement.spacedBy(DesignToken.padding.medium),
            ) {
                AppListingRowItem(
                    key = stringResource(Res.string.core_ui_action),
                    value = if (isPause) "Pause" else "Resume",
                    valueColor = if (isPause) AppColors.customYellow else AppColors.customEnable,
                )
                AppListingRowItem(
                    key = stringResource(Res.string.core_ui_start_date),
                    value = startDate,
                )
                AppListingRowItem(
                    key = stringResource(Res.string.core_ui_end_date),
                    value = endDate,
                )
            }
        }
    }
}

@Composable
fun AppChargesListingComponent(
    title: String,
    type: String,
    dueAsOf: String,
    calculationType: String,
    due: String,
    paid: String,
    waived: String,
    outstanding: String,
    modifier: Modifier = Modifier,
) {
    AppListingComponentOutline(modifier = modifier) {
        Column {
            AppListingRowItem(
                key = "",
                value = title,
                valueStyle = AppTypography.titleSmallEmphasized,
            )
            Spacer(Modifier.height(DesignToken.padding.large))
            Column(
                verticalArrangement = Arrangement.spacedBy(DesignToken.padding.medium),
            ) {
                AppListingRowItem(
                    key = stringResource(Res.string.core_ui_type),
                    value = type,
                )
                AppListingRowItem(
                    key = stringResource(Res.string.core_ui_due_as_of),
                    value = dueAsOf,
                )
                AppListingRowItem(
                    key = stringResource(Res.string.core_ui_calculation_type),
                    value = calculationType,
                )
            }
            Spacer(Modifier.height(DesignToken.padding.medium))
            Column(
                verticalArrangement = Arrangement.spacedBy(DesignToken.padding.medium),
            ) {
                AppListingRowItem(
                    key = stringResource(Res.string.core_ui_due),
                    value = due,
                    valueColor = AppColors.lightRed,
                )
                AppListingRowItem(
                    key = stringResource(Res.string.core_ui_paid),
                    value = paid,
                    valueColor = AppColors.customEnable,
                )
                AppListingRowItem(
                    key = stringResource(Res.string.core_ui_waived),
                    value = waived,
                    valueColor = AppColors.customYellow,
                )
            }
            Spacer(Modifier.height(DesignToken.padding.medium))
            AppListingRowItem(
                key = stringResource(Res.string.core_ui_outstanding),
                value = outstanding,
            )
        }
    }
}

@Composable
fun AppSellLoanListingComponent(
    status: Status,
    effectiveForm: String,
    ownerExternalId: String,
    transferExternalId: String,
    settlementDate: String,
    purchasePriceRatio: String,
    modifier: Modifier = Modifier,
) {
    AppListingComponentOutline(modifier = modifier) {
        Column {
            AppListingRowItem(
                value = when (status) {
                    Status.Inactive -> stringResource(Res.string.core_ui_cancelled)
                    Status.Pending -> stringResource(Res.string.core_ui_pending)
                    Status.Active -> stringResource(Res.string.core_ui_completed)
                },
                key = "",
                valueStyle = AppTypography.titleSmallEmphasized,
                valueColor = status.color,
            )
            Spacer(Modifier.height(DesignToken.padding.large))
            Column(
                verticalArrangement = Arrangement.spacedBy(DesignToken.padding.medium),
            ) {
                AppListingRowItem(
                    key = stringResource(Res.string.core_ui_effective_from),
                    value = effectiveForm,
                )
                AppListingRowItem(
                    key = stringResource(Res.string.core_ui_owner_external_id),
                    value = ownerExternalId,
                )
                AppListingRowItem(
                    key = stringResource(Res.string.core_ui_transfer_external_id),
                    value = transferExternalId,
                )
                AppListingRowItem(
                    key = stringResource(Res.string.core_ui_settlement_date),
                    value = settlementDate,
                )
                AppListingRowItem(
                    key = stringResource(Res.string.core_ui_purchase_price_ratio),
                    value = purchasePriceRatio,
                )
            }
        }
    }
}

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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.AppColors
import kpt.core.designsystem.theme.AppTheme
import kpt.core.designsystem.theme.AppTypography
import kpt.core.designsystem.theme.DesignToken
import kpt.core.designsystem.utils.onClick
import kpt.core.ui.generated.resources.Res
import kpt.core.ui.generated.resources.client_documents_component_header_subtitle_description
import kpt.core.ui.generated.resources.client_documents_component_header_subtitle_file_name
import kpt.core.ui.generated.resources.client_share_accounts_approved_shares
import kpt.core.ui.generated.resources.client_share_accounts_pending_for_approval_shares
import kpt.core.ui.generated.resources.client_share_accounts_share_product
import kpt.core.ui.generated.resources.core_ui_account_no
import kpt.core.ui.generated.resources.core_ui_amount_paid
import kpt.core.ui.generated.resources.core_ui_approve_account
import kpt.core.ui.generated.resources.core_ui_balance
import kpt.core.ui.generated.resources.core_ui_delete
import kpt.core.ui.generated.resources.core_ui_delete_document
import kpt.core.ui.generated.resources.core_ui_description
import kpt.core.ui.generated.resources.core_ui_document_id
import kpt.core.ui.generated.resources.core_ui_document_key
import kpt.core.ui.generated.resources.core_ui_due
import kpt.core.ui.generated.resources.core_ui_due_as_of
import kpt.core.ui.generated.resources.core_ui_edit
import kpt.core.ui.generated.resources.core_ui_identify_documents
import kpt.core.ui.generated.resources.core_ui_last_active
import kpt.core.ui.generated.resources.core_ui_loan_balance
import kpt.core.ui.generated.resources.core_ui_loan_product
import kpt.core.ui.generated.resources.core_ui_make_repayment
import kpt.core.ui.generated.resources.core_ui_note_createdBy
import kpt.core.ui.generated.resources.core_ui_note_date
import kpt.core.ui.generated.resources.core_ui_note_note
import kpt.core.ui.generated.resources.core_ui_original_loan
import kpt.core.ui.generated.resources.core_ui_outstanding
import kpt.core.ui.generated.resources.core_ui_paid
import kpt.core.ui.generated.resources.core_ui_quantity
import kpt.core.ui.generated.resources.core_ui_status
import kpt.core.ui.generated.resources.core_ui_total_collateral_value
import kpt.core.ui.generated.resources.core_ui_total_value
import kpt.core.ui.generated.resources.core_ui_type
import kpt.core.ui.generated.resources.core_ui_upload_again
import kpt.core.ui.generated.resources.core_ui_view_account
import kpt.core.ui.generated.resources.core_ui_view_details
import kpt.core.ui.generated.resources.core_ui_view_document
import kpt.core.ui.generated.resources.core_ui_waived
import kpt.core.ui.generated.resources.listing_component_identifier_not_available
import kpt.core.ui.generated.resources.name
import kpt.core.ui.util.TextUtil
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
private fun ActionMenuRow(
    menuItem: Actions,
    onActionClicked: (Actions) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .height(DesignToken.sizes.avatarMedium)
            .clickable {
                onActionClicked(menuItem)
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        Icon(
            modifier = Modifier.padding(horizontal = DesignToken.padding.large),
            imageVector = menuItem.icon,
            contentDescription = null,
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(menuItem.text),
            color = KptTheme.colorScheme.onSurface,
            fontSize = KptTheme.typography.bodyLarge.fontSize,
        )
    }
}

/** Card frame of an actions listing; the content is clipped while collapsed. */
@Composable
fun AppActionsListingComponentOutline(
    modifier: Modifier = Modifier,
    isExpanded: Boolean = true,
    content: @Composable () -> Unit,
) {
    val bottomCorner by animateDpAsState(
        if (isExpanded) 0.dp else 12.dp,
        animationSpec = spring(),
    )

    Box(
        modifier = modifier
            .border(
                width = DesignToken.strokes.thin,
                shape = RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 12.dp,
                    bottomStart = bottomCorner,
                    bottomEnd = bottomCorner,
                ),
                color = KptTheme.colorScheme.secondaryContainer,
            ),
    ) {
        content()
    }
}

/** Identifier card with its actions menu. */
@Composable
fun AppActionsIdentifierListingComponent(
    type: String,
    id: String,
    key: String,
    status: Status?,
    description: String,
    identifyDocuments: String,
    isExpanded: Boolean,
    menuList: List<Actions>,
    onActionClicked: (Actions) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current

    Column(modifier = modifier) {
        AppActionsListingComponentOutline(
            isExpanded = isExpanded,
        ) {
            Column(modifier = Modifier.padding(DesignToken.padding.large).onClick { onClick() }) {
                AppListingRowItemHeader(
                    text = type,
                    keyStyle = AppTypography.titleSmallEmphasized,
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
                        value = status?.name
                            ?: stringResource(Res.string.listing_component_identifier_not_available),
                        valueColor = if (status?.name != null) status.color else Color.Red,
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
        AnimatedVisibility(
            visible = isExpanded,
            enter = slideInVertically {
                with(density) { -40.dp.roundToPx() }
            } + expandVertically(
                expandFrom = Alignment.Top,
            ) + fadeIn(
                initialAlpha = 0.3f,
            ),
            exit = slideOutVertically() + shrinkVertically() + fadeOut(),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(
                    bottomStart = DesignToken.padding.medium,
                    bottomEnd = DesignToken.padding.medium,
                ),
                color = KptTheme.colorScheme.surfaceContainer,
            ) {
                Column(
                    modifier = Modifier.padding(
                        vertical = DesignToken.padding.small,
                    ),
                ) {
                    menuList.map { menuItem ->
                        ActionMenuRow(menuItem, onActionClicked)
                    }
                }
            }
        }
    }
}

/** Collateral card with its actions menu. */
@Composable
fun AppActionsCollateralDataListingComponent(
    name: String,
    quantity: String,
    totalValue: String,
    totalCollateralValue: String,
    menuList: List<Actions>,
    onActionClicked: (Actions) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppActionsListingComponentOutline(modifier = modifier, isExpanded = false) {
        Column {
            Column(
                modifier = Modifier.padding(DesignToken.padding.large),
            ) {
                AppListingRowItem(
                    key = stringResource(Res.string.name),
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

            if (menuList.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(
                        bottomStart = DesignToken.padding.medium,
                        bottomEnd = DesignToken.padding.medium,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(
                            vertical = DesignToken.padding.small,
                        ),
                    ) {
                        menuList.map { menuItem ->
                            ActionMenuRow(menuItem, onActionClicked)
                        }
                    }
                }
            }
        }
    }
}

/** Client document card with its actions menu. */
@Composable
fun AppActionsClientDocumentListingComponent(
    documentName: String,
    documentDescription: String,
    fileName: String,
    isExpanded: Boolean,
    menuList: List<Actions>,
    onClick: () -> Unit,
    onActionClicked: (Actions) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    Column(modifier = modifier) {
        AppActionsListingComponentOutline(
            isExpanded = isExpanded,
        ) {
            Column(
                modifier = Modifier
                    .clickable { onClick() }
                    .padding(DesignToken.padding.large),
            ) {
                AppListingRowItemHeader(
                    text = documentName,
                    keyStyle = AppTypography.titleSmallEmphasized,
                )
                Spacer(Modifier.height(DesignToken.padding.large))
                Column(
                    verticalArrangement = Arrangement.spacedBy(DesignToken.padding.medium),
                ) {
                    AppListingRowItem(
                        key = stringResource(Res.string.client_documents_component_header_subtitle_description),
                        value = documentDescription,
                    )
                    AppListingRowItem(
                        key = stringResource(Res.string.client_documents_component_header_subtitle_file_name),
                        value = fileName,
                    )
                }
            }
        }
        AnimatedVisibility(
            visible = isExpanded,
            enter = slideInVertically {
                with(density) { -40.dp.roundToPx() }
            } + expandVertically(
                expandFrom = Alignment.Top,
            ) + fadeIn(
                initialAlpha = 0.3f,
            ),
            exit = slideOutVertically() + shrinkVertically() + fadeOut(),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(
                    bottomStart = DesignToken.padding.medium,
                    bottomEnd = DesignToken.padding.medium,
                ),
                color = KptTheme.colorScheme.surfaceContainer,
            ) {
                Column(
                    modifier = Modifier.padding(
                        vertical = DesignToken.padding.small,
                    ),
                ) {
                    menuList.map { menuItem ->
                        ActionMenuRow(menuItem, onActionClicked)
                    }
                }
            }
        }
    }
}

/** Loan account card with its actions menu. */
@Composable
fun AppActionsLoanListingComponent(
    accountNo: String,
    loanProduct: String,
    originalLoan: String,
    amountPaid: String,
    loanBalance: String,
    type: String,
    status: String,
    menuList: List<Actions>,
    onActionClicked: (Actions) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isActive by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier) {
        AppActionsListingComponentOutline(
            isExpanded = isActive,
        ) {
            Column(
                modifier = Modifier.padding(DesignToken.padding.large)
                    .onClick { isActive = !isActive },
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
                Spacer(Modifier.height(DesignToken.padding.medium))
                AppListingRowItem(
                    key = stringResource(Res.string.core_ui_status),
                    value = status,
                )
            }
        }
        if (isActive) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(
                    bottomStart = DesignToken.padding.medium,
                    bottomEnd = DesignToken.padding.medium,
                ),
                color = KptTheme.colorScheme.surfaceContainer,
            ) {
                Column(
                    modifier = Modifier.padding(
                        vertical = DesignToken.padding.small,
                    ),
                ) {
                    menuList.map { menuItem ->
                        ActionMenuRow(menuItem, onActionClicked)
                    }
                }
            }
        }
    }
}

/** Share account card with its actions menu. */
@Composable
fun AppActionsShareListingComponent(
    accountNo: String,
    shareProductName: String,
    pendingForApprovalShares: Int?,
    approvedShares: Int?,
    isExpanded: Boolean,
    onClick: () -> Unit,
    menuList: List<Actions>,
    onActionClicked: (Actions) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppActionsListingComponentOutline(modifier = modifier) {
        Column(
            modifier = Modifier.clickable { onClick() },
        ) {
            Column(
                modifier = Modifier.padding(DesignToken.padding.large),
            ) {
                AppListingRowItemHeader(
                    text = accountNo,
                    keyStyle = AppTypography.titleSmallEmphasized,
                )

                Spacer(Modifier.height(DesignToken.padding.large))
                AppListingRowItem(
                    key = stringResource(Res.string.client_share_accounts_share_product),
                    value = shareProductName,
                )
                Spacer(Modifier.height(DesignToken.padding.medium))
                Column(
                    verticalArrangement = Arrangement.spacedBy(DesignToken.padding.extraExtraSmall),
                ) {
                    AppListingRowItem(
                        key = stringResource(Res.string.client_share_accounts_pending_for_approval_shares),
                        value = (
                            pendingForApprovalShares
                                ?: stringResource(Res.string.listing_component_identifier_not_available)
                            ).toString(),
                    )
                    AppListingRowItem(
                        key = stringResource(Res.string.client_share_accounts_approved_shares),
                        value = (
                            approvedShares
                                ?: stringResource(Res.string.listing_component_identifier_not_available)
                            ).toString(),
                    )
                }
            }

            if (isExpanded) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(
                        bottomStart = DesignToken.padding.medium,
                        bottomEnd = DesignToken.padding.medium,
                    ),
                    color = KptTheme.colorScheme.surfaceContainer,
                ) {
                    Column(
                        modifier = Modifier.padding(
                            vertical = DesignToken.padding.small,
                        ),
                    ) {
                        menuList.map { menuItem ->
                            ActionMenuRow(menuItem, onActionClicked)
                        }
                    }
                }
            }
        }
    }
}

/** Charge card with its actions menu, expandable for the details. */
@Composable
fun AppActionsChargeListingComponent(
    chargeTitle: String,
    type: String,
    collectedOn: String,
    onActionClicked: (Actions) -> Unit,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    modifier: Modifier = Modifier,
    date: String? = null,
    amount: String? = null,
    menuList: List<Actions> = listOf<Actions>(
        Actions.Edit(),
        Actions.Delete(),
    ),
) {
    Column(
        modifier.fillMaxWidth(),
    ) {
        AppActionsListingComponentOutline(
            isExpanded = isExpanded,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable(
                    onClick = { onExpandToggle() },
                ).padding(
                    horizontal = DesignToken.padding.large,
                    vertical = DesignToken.padding.medium,
                ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,

            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(DesignToken.padding.extraExtraSmall),
                ) {
                    PrintTextUtil(
                        TextUtil(
                            text = chargeTitle,
                            style = AppTypography.titleSmallEmphasized,
                        ),
                    )
                    PrintTextUtil(
                        TextUtil(
                            text = type,
                        ),
                    )
                    if (
                        date != null
                    ) {
                        PrintTextUtil(
                            TextUtil(
                                text = date,
                            ),
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(DesignToken.padding.extraExtraSmall),
                        horizontalAlignment = Alignment.End,
                    ) {
                        Text(
                            text = collectedOn,
                            color = AppColors.customEnable,
                            style = AppTypography.bodySmall,
                            textAlign = TextAlign.End,
                        )
                        if (amount != null) {
                            Text(
                                text = amount,
                                style = AppTypography.labelMedium,
                                textAlign = TextAlign.End,
                            )
                        }
                    }
                    Icon(
                        modifier = Modifier
                            .size(DesignToken.sizes.iconSmall)
                            .padding(horizontal = DesignToken.padding.extraSmall),
                        imageVector = if (isExpanded) {
                            AppIcons.ChevronUp
                        } else {
                            AppIcons.ChevronDown
                        },
                        contentDescription = "",
                    )
                }
            }
        }
        AnimatedVisibility(isExpanded) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(
                    bottomStart = DesignToken.padding.medium,
                    bottomEnd = DesignToken.padding.medium,
                ),
                color = KptTheme.colorScheme.surfaceContainer,
            ) {
                Column(
                    modifier = Modifier.padding(
                        vertical = DesignToken.padding.small,
                    ),
                ) {
                    menuList.map { menuItem ->
                        ActionMenuRow(menuItem, onActionClicked)
                    }
                }
            }
        }
    }
}

/** Savings account card with its actions menu, expandable for the details. */
@Composable
fun AppActionsSavingsListingComponent(
    accountNo: String,
    savingsProduct: String,
    savingsProductName: String,
    lastActive: String,
    balance: String,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    menuList: List<Actions>,
    onActionClicked: (Actions) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        AppActionsListingComponentOutline(
            isExpanded = isExpanded,
        ) {
            Column(
                modifier = Modifier.padding(DesignToken.padding.large).onClick {
                    onExpandToggle()
                },
            ) {
                AppListingRowItemHeader(
                    text = accountNo,
                    keyStyle = AppTypography.titleSmallEmphasized,
                )

                Spacer(Modifier.height(DesignToken.padding.large))
                AppListingRowItem(
                    key = savingsProduct,
                    value = savingsProductName,
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

        AnimatedVisibility(isExpanded) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(
                    bottomStart = DesignToken.padding.medium,
                    bottomEnd = DesignToken.padding.medium,
                ),
                color = KptTheme.colorScheme.surfaceContainer,
            ) {
                Column(
                    modifier = Modifier.padding(
                        vertical = DesignToken.padding.small,
                    ),
                ) {
                    menuList.map { menuItem ->
                        ActionMenuRow(menuItem, onActionClicked)
                    }
                }
            }
        }
    }
}

/** Client fee card with its actions menu. */
@Composable
fun AppActionsClientFeeListingComponent(
    name: String,
    dueAsOf: String,
    due: String,
    paid: String,
    waived: String,
    isActive: Boolean,
    onClick: () -> Unit,
    outstanding: String,
    menuList: List<Actions>,
    onActionClicked: (Actions) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppActionsListingComponentOutline(modifier = modifier, isExpanded = isActive) {
        Column {
            Column(
                modifier = Modifier.padding(DesignToken.padding.large)
                    .clickable { onClick() },
            ) {
                AppListingRowItemHeader(
                    text = name,
                    keyStyle = AppTypography.titleSmallEmphasized,
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
            if (isActive) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(
                        bottomStart = DesignToken.padding.medium,
                        bottomEnd = DesignToken.padding.medium,
                    ),
                    color = KptTheme.colorScheme.surfaceContainer,
                ) {
                    Column(
                        modifier = Modifier.padding(
                            vertical = DesignToken.padding.small,
                        ),
                    ) {
                        menuList.map { menuItem ->
                            ActionMenuRow(menuItem, onActionClicked)
                        }
                    }
                }
            }
        }
    }
}

/**
 * An entry of a listing card's actions menu.
 *
 * @property icon the entry's icon
 * @property text the entry's label
 */
sealed class Actions(open val icon: ImageVector, open val text: StringResource) {
    /** Open the account. */
    data class ViewAccount(override val icon: ImageVector = AppIcons.PiggyBank) :
        Actions(icon, Res.string.core_ui_view_account)

    /** Approve the account. */
    data class ApproveAccount(override val icon: ImageVector = AppIcons.ApproveAccount) :
        Actions(icon, Res.string.core_ui_approve_account)

    /** Make a repayment. */
    data class MakeRepayment(override val icon: ImageVector = AppIcons.MakeRepayment) :
        Actions(icon, Res.string.core_ui_make_repayment)

    /** Open the document. */
    data class ViewDocument(override val icon: ImageVector = AppIcons.DocumentScanner) :
        Actions(icon, Res.string.core_ui_view_document)

    /** Upload the document again. */
    data class UploadAgain(override val icon: ImageVector = AppIcons.FileUpload) :
        Actions(icon, Res.string.core_ui_upload_again)

    /** Delete the document. */
    data class DeleteDocument(override val icon: ImageVector = AppIcons.DeleteDocument) :
        Actions(icon, Res.string.core_ui_delete_document)

    /** Edit the item. */
    data class Edit(override val icon: ImageVector = AppIcons.Edit) :
        Actions(icon, Res.string.core_ui_edit)

    /** Delete the item. */
    data class Delete(override val icon: ImageVector = AppIcons.DeleteDocument) :
        Actions(icon, Res.string.core_ui_delete)

    /** Open a row's detail screen — the charge list's first action. */
    data class ViewDetails(override val icon: ImageVector = AppIcons.Info) :
        Actions(icon, Res.string.core_ui_view_details)
}

/** Note card with its actions menu, expandable for the full text. */
@Composable
fun AppActionsNoteListingComponent(
    createdBy: String,
    date: String,
    notes: String,
    menuList: List<Actions>,
    onExpand: () -> Unit,
    isExpanded: Boolean,
    onActionClicked: (Actions) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current

    Column(modifier = modifier) {
        AppActionsListingComponentOutline(
            modifier = Modifier.clickable { onExpand() },
            isExpanded = isExpanded,
        ) {
            Column(
                modifier = Modifier.padding(DesignToken.padding.large),
            ) {
                AppListingRowItemHeader(
                    text = stringResource(Res.string.core_ui_note_createdBy) + " " + createdBy,
                    keyStyle = AppTypography.titleSmallEmphasized,
                )

                Spacer(Modifier.height(DesignToken.padding.large))

                AppListingColumnItem(
                    key = stringResource(Res.string.core_ui_note_date),
                    value = date,
                )

                Spacer(Modifier.height(DesignToken.padding.medium))

                AppListingColumnItem(
                    key = stringResource(Res.string.core_ui_note_note),
                    value = notes,
                )
            }
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = slideInVertically {
                with(density) { -40.dp.roundToPx() }
            } + expandVertically(
                expandFrom = Alignment.Top,
            ) + fadeIn(
                initialAlpha = 0.3f,
            ),
            exit = slideOutVertically() + shrinkVertically() + fadeOut(),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(
                    bottomStart = DesignToken.padding.medium,
                    bottomEnd = DesignToken.padding.medium,
                ),
                color = KptTheme.colorScheme.surfaceContainer,
            ) {
                Column(
                    modifier = Modifier.padding(
                        vertical = DesignToken.padding.small,
                    ),
                ) {
                    menuList.map { menuItem ->
                        ActionMenuRow(menuItem, onActionClicked)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun PreviewAppActionsNoteListingComponent() {
    AppTheme {
        AppActionsNoteListingComponent(
            createdBy = "John Doe",
            date = "02 September 2025",
            notes = "This is a note",
            menuList = listOf(
                Actions.Edit(),
                Actions.Delete(),
            ),
            onExpand = {},
            isExpanded = true,
            onActionClicked = { action ->
                when (action) {
                    is Actions.Edit -> println(Actions.ViewAccount::class.simpleName ?: "")
                    is Actions.Delete -> println(Actions.ApproveAccount::class.simpleName ?: "")
                    else -> println("Action not Handled")
                }
            },
        )
    }
}

@Preview
@Composable
private fun PreviewAppActionsIdentifierListingComponent() {
    AppTheme {
        AppActionsIdentifierListingComponent(
            type = "Passport",
            id = "P1234567",
            key = "Government ID",
            status = Status.Active,
            description = "Primary identification document",
            identifyDocuments = "Passport Scan",
            menuList = listOf(
                Actions.ViewDocument(),
                Actions.UploadAgain(),
                Actions.DeleteDocument(),
            ),
            onActionClicked = { action ->
                when (action) {
                    is Actions.ViewDocument -> println(Actions.ViewDocument::class.simpleName)
                    is Actions.UploadAgain -> println(Actions.UploadAgain::class.simpleName)
                    is Actions.DeleteDocument -> println(Actions.DeleteDocument::class.simpleName)
                    else -> println("Action not Handled")
                }
            },
            onClick = {},
            isExpanded = true,
        )
    }
}

@Preview
@Composable
private fun PreviewAppActionsClientFeeListingComponent() {
    AppTheme {
        AppActionsClientFeeListingComponent(
            name = "John Doe",
            dueAsOf = "2025-08-19",
            due = "$1000",
            paid = "$400",
            waived = "$50",
            outstanding = "$550",
            menuList = listOf(
                Actions.ViewAccount(),
                Actions.ApproveAccount(),
            ),
            isActive = true,
            onClick = {},
            onActionClicked = { action ->
                when (action) {
                    is Actions.ViewAccount -> println(Actions.ViewDocument::class.simpleName)
                    is Actions.ApproveAccount -> println(Actions.ApproveAccount::class.simpleName)
                    else -> println("Action not Handled")
                }
            },
        )
    }
}

@Preview
@Composable
private fun PreviewAppActionsSavingsListingComponent() {
    AppTheme {
        AppActionsSavingsListingComponent(
            accountNo = "SV9876",
            savingsProduct = "Savings Product",
            savingsProductName = "Wallet",
            lastActive = "2025-08-15",
            balance = "$1200",
            menuList = listOf(
                Actions.ViewAccount(),
                Actions.ApproveAccount(),
            ),
            onActionClicked = { action ->
                when (action) {
                    is Actions.ViewAccount -> println(Actions.ViewDocument::class.simpleName)
                    is Actions.ApproveAccount -> println(Actions.ApproveAccount::class.simpleName)
                    else -> println("Action not Handled")
                }
            },
            isExpanded = true,
            onExpandToggle = {},
        )
    }
}

@Preview
@Composable
private fun PreviewAppActionsCollateralDataListingComponent() {
    AppTheme {
        AppActionsCollateralDataListingComponent(
            name = "Gold Jewelry",
            quantity = "5",
            totalValue = "$2500",
            totalCollateralValue = "$2500",
            menuList = listOf(
                Actions.ViewAccount(),
                Actions.ApproveAccount(),
            ),
            onActionClicked = { action ->
                when (action) {
                    is Actions.ViewAccount -> println(Actions.ViewDocument::class.simpleName)
                    is Actions.ApproveAccount -> println(Actions.ApproveAccount::class.simpleName)
                    else -> println("Action not Handled")
                }
            },
        )
    }
}

@Preview
@Composable
private fun PreviewAppActionsLoanListingComponent() {
    AppTheme {
        AppActionsLoanListingComponent(
            accountNo = "LN12345",
            loanProduct = "Personal Loan",
            originalLoan = "$5000",
            amountPaid = "$2000",
            loanBalance = "$3000",
            type = "Active",
            status = "Active",
            menuList = listOf(
                Actions.ViewAccount(),
                Actions.ApproveAccount(),
            ),
            onActionClicked = { action ->
                when (action) {
                    is Actions.ViewAccount -> println(Actions.ViewAccount::class.simpleName)
                    is Actions.ApproveAccount -> println(Actions.ApproveAccount::class.simpleName)
                    else -> println("Action not Handled")
                }
            },
        )
    }
}

@Preview
@Composable
private fun AppActionsChargeListingComponentPreview() {
    AppTheme {
        AppActionsChargeListingComponent(
            chargeTitle = "Bank Fee",
            type = "Flat",
            date = "06-09-2025",
            collectedOn = "06-09-2025",
            amount = "1500 UGX",
            onActionClicked = { },
            isExpanded = true,
            onExpandToggle = {},
        )
    }
}

@Preview
@Composable
private fun PreviewAppActionsShareListingComponent() {
    val sampleMenu = listOf(
        Actions.Edit(),
        Actions.Delete(),
    )

    AppTheme {
        AppActionsShareListingComponent(
            accountNo = "AC123456",
            shareProductName = "Gold Savings Plan",
            pendingForApprovalShares = 5,
            approvedShares = 10,
            isExpanded = true,
            onClick = {},
            menuList = sampleMenu,
            onActionClicked = { },
        )
    }
}

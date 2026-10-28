/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-x-field-officer-app/blob/master/LICENSE.md
 */
package com.mifos.feature.savings.savingsAccountSummary

import androidclient.feature.savings.generated.resources.Res
import androidclient.feature.savings.generated.resources.feature_savings_account_balance
import androidclient.feature.savings.generated.resources.feature_savings_account_number
import androidclient.feature.savings.generated.resources.feature_savings_activate_savings
import androidclient.feature.savings.generated.resources.feature_savings_approve_savings
import androidclient.feature.savings.generated.resources.feature_savings_client_name
import androidclient.feature.savings.generated.resources.feature_savings_currency
import androidclient.feature.savings.generated.resources.feature_savings_date
import androidclient.feature.savings.generated.resources.feature_savings_documents
import androidclient.feature.savings.generated.resources.feature_savings_failed_to_fetch_savingsaccount
import androidclient.feature.savings.generated.resources.feature_savings_interest_earned
import androidclient.feature.savings.generated.resources.feature_savings_make_deposit
import androidclient.feature.savings.generated.resources.feature_savings_more_savings_account_info
import androidclient.feature.savings.generated.resources.feature_savings_no_transactions
import androidclient.feature.savings.generated.resources.feature_savings_product_name
import androidclient.feature.savings.generated.resources.feature_savings_running_balance
import androidclient.feature.savings.generated.resources.feature_savings_saving_account_id
import androidclient.feature.savings.generated.resources.feature_savings_savingsAccountSummary
import androidclient.feature.savings.generated.resources.feature_savings_savings_account_closed
import androidclient.feature.savings.generated.resources.feature_savings_total_deposits
import androidclient.feature.savings.generated.resources.feature_savings_total_withdrawals
import androidclient.feature.savings.generated.resources.feature_savings_transaction_id
import androidclient.feature.savings.generated.resources.feature_savings_transaction_type
import androidclient.feature.savings.generated.resources.feature_savings_transactions
import androidclient.feature.savings.generated.resources.feature_savings_view_standing_instructions
import androidclient.feature.savings.generated.resources.feature_savings_withdrawal
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.DarkGray
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mifos.core.common.utils.Constants
import com.mifos.core.common.utils.DateHelper
import com.mifos.core.designsystem.component.MifosCard
import com.mifos.core.designsystem.component.MifosMenuDropDownItem
import com.mifos.core.designsystem.component.MifosScaffold
import com.mifos.core.designsystem.component.MifosSweetError
import com.mifos.core.designsystem.icon.MifosIcons
import com.mifos.core.designsystem.theme.DesignToken
import com.mifos.core.ui.components.MifosEmptyUi
import com.mifos.core.ui.components.MifosProgressIndicator
import com.mifos.feature.savings.savingsAccountTransactionReceipt.SavingsAccountTransactionReceiptScreen
import com.mifos.room.entities.accounts.savings.SavingAccountCurrencyEntity
import com.mifos.room.entities.accounts.savings.SavingAccountDepositTypeEntity
import com.mifos.room.entities.accounts.savings.SavingsAccountStatusEntity
import com.mifos.room.entities.accounts.savings.SavingsAccountSummaryEntity
import com.mifos.room.entities.accounts.savings.SavingsAccountTransactionEntity
import com.mifos.room.entities.accounts.savings.SavingsAccountWithAssociationsEntity
import com.mifos.room.entities.accounts.savings.SavingsTransactionTypeEntity
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter
import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider
import org.koin.compose.viewmodel.koinViewModel
import template.core.base.designsystem.theme.KptTheme

/**
 * Created by Pronay Sarker on 10/07/2024 (6:21 PM)
 */

@Composable
internal fun SavingsAccountSummaryScreen(
    navigateBack: () -> Unit,
    loadMoreSavingsAccountInfo: (accountNumber: Int) -> Unit,
    loadDocuments: (accountNumber: Int) -> Unit,
    onDepositClick: (savings: SavingsAccountWithAssociationsEntity, type: SavingAccountDepositTypeEntity?) -> Unit,
    onWithdrawButtonClicked: (savings: SavingsAccountWithAssociationsEntity, type: SavingAccountDepositTypeEntity?) -> Unit,
    approveSavings: (type: SavingAccountDepositTypeEntity?, accountNumber: Int) -> Unit,
    activateSavings: (type: SavingAccountDepositTypeEntity?, accountNumber: Int) -> Unit,
    onViewStandingInstructions: (clientId: Long, clientName: String, accountId: Long, accountType: Int, currencyCode: String) -> Unit,
    viewmodel: SavingsAccountSummaryViewModel = koinViewModel(),
) {
    val uiState by viewmodel.savingsAccountSummaryUiState.collectAsStateWithLifecycle()
    val accountId = viewmodel.savingsNavigationData.id
    val savingsAccountType = viewmodel.savingsNavigationData.type

    LaunchedEffect(key1 = Unit) {
        viewmodel.loadSavingAccount(savingsAccountType.endpoint, accountId)
    }

    SavingsAccountSummaryScreen(
        uiState = uiState,
        navigateBack = navigateBack,
        onRetry = { viewmodel.loadSavingAccount(savingsAccountType.endpoint, accountId) },
        loadMoreSavingsAccountInfo = { loadMoreSavingsAccountInfo.invoke(accountId) },
        loadDocuments = { loadDocuments.invoke(accountId) },
        onDepositButtonClicked = {
            onDepositClick.invoke(it, savingsAccountType)
        },
        onWithdrawButtonClicked = {
            onWithdrawButtonClicked.invoke(
                it,
                savingsAccountType,
            )
        },
        approveSavings = {
            approveSavings.invoke(savingsAccountType, accountId)
        },
        activateSavings = {
            activateSavings.invoke(savingsAccountType, accountId)
        },
        onViewStandingInstructions = {
            val currentState = uiState
            if (currentState is SavingsAccountSummaryUiState.ShowSavingAccount) {
                onViewStandingInstructions.invoke(
                    currentState.savingsAccountWithAssociations.clientId?.toLong() ?: 0L,
                    currentState.savingsAccountWithAssociations.clientName ?: "",
                    accountId.toLong(),
                    Constants.SAVINGS_ACCOUNT_VAL,
                    currentState.savingsAccountWithAssociations.currency?.code ?: "",
                )
            }
        },
    )
}

@Composable
internal fun SavingsAccountSummaryScreen(
    uiState: SavingsAccountSummaryUiState,
    navigateBack: () -> Unit,
    onRetry: () -> Unit,
    loadMoreSavingsAccountInfo: () -> Unit,
    onViewStandingInstructions: () -> Unit,
    loadDocuments: () -> Unit,
    onDepositButtonClicked: (savings: SavingsAccountWithAssociationsEntity) -> Unit,
    onWithdrawButtonClicked: (savings: SavingsAccountWithAssociationsEntity) -> Unit,
    approveSavings: () -> Unit,
    modifier: Modifier = Modifier,
    activateSavings: () -> Unit,
) {
    val snackbarHostState = remember {
        SnackbarHostState()
    }
    var showDropdown by rememberSaveable {
        mutableStateOf(false)
    }

    MifosScaffold(
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        onBackPressed = navigateBack,
        title = stringResource(Res.string.feature_savings_savingsAccountSummary),
        actions = {
            IconButton(onClick = { showDropdown = !showDropdown }) {
                Icon(imageVector = MifosIcons.MoreVert, contentDescription = "")
            }

            if (showDropdown) {
                DropdownMenu(expanded = showDropdown, onDismissRequest = { showDropdown = false }) {
                    MifosMenuDropDownItem(
                        option = stringResource(Res.string.feature_savings_more_savings_account_info),
                        onClick = {
                            showDropdown = false
                            loadMoreSavingsAccountInfo.invoke()
                        },
                    )
                    MifosMenuDropDownItem(
                        option = stringResource(Res.string.feature_savings_documents),
                        onClick = {
                            showDropdown = false
                            loadDocuments.invoke()
                        },
                    )
                    MifosMenuDropDownItem(
                        option = stringResource(Res.string.feature_savings_view_standing_instructions),
                        onClick = {
                            showDropdown = false
                            onViewStandingInstructions.invoke()
                        },
                    )
                }
            }
        },
    ) {
        Box(
            modifier = Modifier
                .padding(it),
        ) {
            when (uiState) {
                is SavingsAccountSummaryUiState.ShowFetchingError -> {
                    MifosSweetError(message = stringResource(uiState.message)) {
                        onRetry()
                    }
                }

                SavingsAccountSummaryUiState.ShowProgressbar -> {
                    MifosProgressIndicator()
                }

                is SavingsAccountSummaryUiState.ShowSavingAccount -> {
                    SavingsAccountSummaryContent(
                        savingsAccountWithAssociations = uiState.savingsAccountWithAssociations,
                        onDepositButtonClicked = onDepositButtonClicked,
                        onWithdrawButtonClicked = onWithdrawButtonClicked,
                        approveSavings = approveSavings,
                        activateSavings = activateSavings,
                    )
                }
            }
        }
    }
}

@Composable
private fun SavingsAccountSummaryContent(
    savingsAccountWithAssociations: SavingsAccountWithAssociationsEntity,
    onDepositButtonClicked: (savingsAccountWithAssociations: SavingsAccountWithAssociationsEntity) -> Unit,
    onWithdrawButtonClicked: (savingsAccountWithAssociations: SavingsAccountWithAssociationsEntity) -> Unit,
    approveSavings: () -> Unit,
    modifier: Modifier = Modifier,
    activateSavings: () -> Unit,
) {
    val isSavingsButtonVisible by rememberSaveable {
        mutableStateOf(savingsButtonVisibilityStatus(savingsAccountWithAssociations.status))
    }
    val isWithdrawalAndDepositButtonVisible by rememberSaveable {
        mutableStateOf(depositAndWithdrawButtonVisibility(savingsAccountWithAssociations.status))
    }
    Column {
        Box(
            modifier = modifier.padding(horizontal = KptTheme.spacing.lg),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = KptTheme.spacing.sm),
                    text = savingsAccountWithAssociations.clientName
                        ?: stringResource(Res.string.feature_savings_client_name),
                    style = KptTheme.typography.bodyLarge,
                )

                HorizontalDivider(color = DarkGray)

                FarApartTextItem(
                    title = savingsAccountWithAssociations.savingsProductName
                        ?: stringResource(Res.string.feature_savings_product_name),
                    value = savingsAccountWithAssociations.accountNo?.toString() ?: "",
                )

                HorizontalDivider(modifier = Modifier.padding(top = DesignToken.padding.dp6), color = DarkGray)

                FarApartTextItem(
                    title = stringResource(Res.string.feature_savings_account_balance),
                    value = savingsAccountWithAssociations.summary?.accountBalance?.toString()
                        ?: "0.0",
                )

                FarApartTextItem(
                    title = stringResource(Res.string.feature_savings_total_deposits),
                    value = savingsAccountWithAssociations.summary?.totalDeposits?.toString()
                        ?: "0.0",
                )

                FarApartTextItem(
                    title = stringResource(Res.string.feature_savings_total_withdrawals),
                    value = savingsAccountWithAssociations.summary?.totalWithdrawals?.toString()
                        ?: "0.0",
                )

                FarApartTextItem(
                    title = stringResource(Res.string.feature_savings_interest_earned),
                    value = savingsAccountWithAssociations.summary?.totalInterestEarned?.toString()
                        ?: "0.0",
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = KptTheme.spacing.sm),
                    color = DarkGray,
                )

                if (savingsAccountWithAssociations.transactions.isEmpty()) {
                    MifosEmptyUi(text = stringResource(Res.string.feature_savings_no_transactions))
                } else {
                    Text(
                        style = KptTheme.typography.bodyLarge,
                        text = stringResource(Res.string.feature_savings_transactions),
                        color = KptTheme.colorScheme.onBackground,
                    )

                    LazyColumn {
                        items(savingsAccountWithAssociations.transactions) { transaction ->
                            TransactionItemRow(transaction = transaction)
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .background(
                        color = KptTheme.colorScheme.surface,
                    ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = KptTheme.spacing.sm),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    if (isWithdrawalAndDepositButtonVisible) {
                        Button(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = KptTheme.spacing.sm),
                            onClick = {
                                onWithdrawButtonClicked.invoke(
                                    savingsAccountWithAssociations,
                                )
                            },
                        ) {
                            Text(text = stringResource(Res.string.feature_savings_withdrawal))
                        }

                        Button(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = KptTheme.spacing.sm),
                            onClick = { onDepositButtonClicked.invoke(savingsAccountWithAssociations) },
                        ) {
                            Text(text = stringResource(Res.string.feature_savings_make_deposit))
                        }
                    }

                    if (isSavingsButtonVisible) {
                        Button(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = KptTheme.spacing.sm),
                            onClick = when {
                                savingsAccountWithAssociations.status?.submittedAndPendingApproval == true -> {
                                    { approveSavings.invoke() }
                                }

                                !savingsAccountWithAssociations.status?.active!! -> {
                                    { activateSavings.invoke() }
                                }

                                else -> {
                                    { }
                                }
                            },

                        ) {
                            Text(
                                text = stringResource(
                                    getSavingsButtonText(
                                        savingsAccountWithAssociations.status,
                                    ),
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionItemRow(
    transaction: SavingsAccountTransactionEntity,
    modifier: Modifier = Modifier,
) {
    var showTransactionDetails by rememberSaveable {
        mutableStateOf(false)
    }

    if (showTransactionDetails) {
        SummaryDialogBox(
            onDismissCall = { showTransactionDetails = false },
            transaction = transaction,
        )
    }
    Card(
        modifier = modifier
            .fillMaxWidth(),
        onClick = { showTransactionDetails = !showTransactionDetails },
        colors = CardDefaults.cardColors(
            containerColor = KptTheme.colorScheme.surface,
        ),
        shape = DesignToken.shapes.none,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = KptTheme.spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = DateHelper.getDateAsString(transaction.date as List<Int>),
                    style = KptTheme.typography.bodyLarge,
                    color = KptTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(4f),
                )

                Text(
                    text = transaction.transactionType?.value ?: "",
                    style = KptTheme.typography.bodyLarge,
                    color = KptTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(3f),
                )

                Text(
                    text = transaction.currency?.displaySymbol + " " + transaction.amount?.toString(),
                    style = KptTheme.typography.bodyLarge,
                    modifier = Modifier.weight(3f),
                    textAlign = TextAlign.End,
                    color = when {
                        transaction.transactionType?.deposit == true -> {
                            Color.Green
                        }

                        transaction.transactionType?.withdrawal == true -> {
                            Color.Red
                        }

                        else -> {
                            Color.Black
                        }
                    },
                )
            }
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun SummaryDialogBox(
    onDismissCall: () -> Unit,
    transaction: SavingsAccountTransactionEntity,
) {
    Dialog(
        onDismissRequest = { onDismissCall.invoke() },
        content = {
            MifosCard {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                        .padding(KptTheme.spacing.md),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(DesignToken.spacing.dp5),
                ) {
                    DialogBoxRowItem(
                        title = stringResource(Res.string.feature_savings_transaction_id),
                        value = transaction.id?.toString() ?: "",
                    )

                    DialogBoxRowItem(
                        title = stringResource(Res.string.feature_savings_date),
                        value = DateHelper.getDateAsString(transaction.date as List<Int>),
                    )

                    DialogBoxRowItem(
                        title = stringResource(Res.string.feature_savings_transaction_type),
                        value = transaction.transactionType?.value ?: "",
                    )

                    DialogBoxRowItem(
                        title = stringResource(Res.string.feature_savings_running_balance),
                        value = transaction.runningBalance.toString(),
                    )

                    DialogBoxRowItem(
                        title = stringResource(Res.string.feature_savings_saving_account_id),
                        value = transaction.accountId.toString(),
                    )

                    DialogBoxRowItem(
                        title = stringResource(Res.string.feature_savings_account_number),
                        value = transaction.accountNo ?: "",
                    )

                    DialogBoxRowItem(
                        title = stringResource(Res.string.feature_savings_currency),
                        value = transaction.currency?.name ?: "",
                    )

                    SavingsAccountTransactionReceiptScreen(
                        transactionId = transaction.id,
                    )
                }
            }
        },
    )
}

@Composable
private fun DialogBoxRowItem(
    title: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = DesignToken.spacing.dp2,
                color = KptTheme.colorScheme.primary,
                shape = DesignToken.shapes.none,
            )
            .padding(horizontal = KptTheme.spacing.sm, vertical = KptTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,

    ) {
        Text(
            modifier = Modifier.weight(5f),
            style = KptTheme.typography.bodyMedium,
            text = title,
        )

        Text(
            modifier = Modifier
                .weight(5f)
                .padding(end = KptTheme.spacing.sm),
            style = KptTheme.typography.bodyMedium,
            text = value,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun FarApartTextItem(title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = DesignToken.padding.dp6),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            style = KptTheme.typography.bodyLarge,
            text = title,
        )

        Text(
            style = KptTheme.typography.bodyLarge,
            text = value,
            color = DarkGray,
        )
    }
}

private fun getSavingsButtonText(status: SavingsAccountStatusEntity?): StringResource {
    return when {
        status?.submittedAndPendingApproval == true -> {
            Res.string.feature_savings_approve_savings
        }

        !status?.active!! -> Res.string.feature_savings_activate_savings

        status?.closed == true -> Res.string.feature_savings_savings_account_closed

        else -> {
            "Button not visible"
        }
    } as StringResource
}

private fun savingsButtonVisibilityStatus(status: SavingsAccountStatusEntity?): Boolean {
    return when {
        status?.submittedAndPendingApproval == true -> {
            true
        }

        !status?.active!! -> {
            true
        }

        status?.closed == true -> {
            true
        }

        else -> {
            false
        }
    }
}

private fun depositAndWithdrawButtonVisibility(status: SavingsAccountStatusEntity?): Boolean {
    return when {
        status?.submittedAndPendingApproval == true -> {
            false
        }

        !status?.active!! -> {
            false
        }

        status?.closed == true -> {
            false
        }

        else -> {
            true
        }
    }
}

class SavingsAccountSummaryScreenPreviewProvider :
    PreviewParameterProvider<SavingsAccountSummaryUiState> {
    val summary = SavingsAccountSummaryEntity(
        savingsId = 2232,
        currency = null,
        totalDeposits = 4332.333,
        accountBalance = 23232.333,
        totalWithdrawals = 3343.434,
        totalInterestEarned = 234.34,
    )

    val transaction = SavingsAccountTransactionEntity(
        transactionType = SavingsTransactionTypeEntity(
            value = "Transfer",
        ),
        date = listOf(2, 3, 2022),
        currency = SavingAccountCurrencyEntity(
            code = null,
        ),
    )
    override val values: Sequence<SavingsAccountSummaryUiState>
        get() = sequenceOf(
            SavingsAccountSummaryUiState.ShowProgressbar,
            SavingsAccountSummaryUiState.ShowFetchingError(Res.string.feature_savings_failed_to_fetch_savingsaccount),
            SavingsAccountSummaryUiState.ShowSavingAccount(
                SavingsAccountWithAssociationsEntity(
                    clientId = 343434343,
                    accountNo = "3830948",
                    clientName = "Pronay",
                    transactions = listOf(
                        transaction,
                        transaction,
                        transaction,
                        transaction,
                    ),
                    status = SavingsAccountStatusEntity(),
                    summary = summary,
                ),
            ),
        )
}

@Composable
@Preview
private fun PreviewSavingsAccountSummaryScreen(
    @PreviewParameter(SavingsAccountSummaryScreenPreviewProvider::class)
    savingsAccountSummaryUiState: SavingsAccountSummaryUiState,
) {
    SavingsAccountSummaryScreen(
        uiState = savingsAccountSummaryUiState,
        navigateBack = { },
        onRetry = { },
        loadMoreSavingsAccountInfo = { },
        loadDocuments = { },
        onDepositButtonClicked = { _ -> },
        onWithdrawButtonClicked = { _ -> },
        approveSavings = { },
        onViewStandingInstructions = {},
    ) {
    }
}

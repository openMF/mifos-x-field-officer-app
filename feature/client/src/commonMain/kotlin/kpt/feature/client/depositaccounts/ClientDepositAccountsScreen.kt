/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.depositaccounts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.base.ui.screen.RefreshableScreenContent
import kpt.core.common.utils.CurrencyFormatter
import kpt.core.common.utils.DateHelper
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.fieldofficer.component.FloatingActionButtonContent
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.AppTypography
import kpt.core.model.savings.SavingsAccount
import kpt.core.ui.components.Actions
import kpt.core.ui.components.AppActionsSavingsListingComponent
import kpt.core.ui.components.AppEmptyCard
import kpt.core.ui.components.AppSearchBar
import kpt.feature.client.generated.resources.Res
import kpt.feature.client.generated.resources.feature_client_add_record
import kpt.feature.client.generated.resources.feature_client_deposit_accounts_filtered_empty
import kpt.feature.client.generated.resources.feature_client_deposit_accounts_item
import kpt.feature.client.generated.resources.feature_client_deposit_accounts_not_available
import kpt.feature.client.generated.resources.feature_client_deposit_accounts_pending_approval
import kpt.feature.client.generated.resources.feature_client_fixed_deposit_accounts_empty
import kpt.feature.client.generated.resources.feature_client_product_fixed_deposit_account
import kpt.feature.client.generated.resources.feature_client_product_recurring_deposit_account
import kpt.feature.client.generated.resources.feature_client_product_savings_account
import kpt.feature.client.generated.resources.feature_client_recurring_deposit_accounts_empty
import kpt.feature.client.generated.resources.feature_client_savings_accounts_empty
import kpt.feature.client.generated.resources.feature_client_screen_fixed_deposit_accounts
import kpt.feature.client.generated.resources.feature_client_screen_recurring_deposit_accounts
import kpt.feature.client.generated.resources.feature_client_screen_savings_accounts
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * A client's deposit accounts of one kind. See [ClientDepositAccountsViewModel] for why the three
 * originals became one screen.
 *
 * Carried over from `6b66e8a43:feature/client/{savingsAccounts,fixedDepositAccount,
 * recurringDepositAccount}` screen files — the header with its count and icon buttons, the
 * collapsible search bar, and the expandable account cards with their per-status action menus.
 *
 * ## §6a adaptations
 *
 * The `isLoading` boolean plus the error `AlertDialog` become [ScreenState] through
 * [RefreshableScreenContent]. The breadcrumb bar at the top is not ported, per
 * `loan-summary-breadcrumb-not-ported` — the decision covers every screen in the original that
 * draws one.
 *
 * The FILTER icon button is not drawn: its dialog does not exist in any of the three originals, so
 * the control was inert in all of them (see the ViewModel KDoc). Affordances are absent, not inert.
 *
 * ## Which actions a card offers depends on the account's status
 *
 * A submitted-and-pending account offers View Account and Approve Account; everything else offers
 * View Account only — the originals' rule, kept. Both destinations are ported (the savings summary
 * and the status-change screen), so the whole menu is live.
 */
@Composable
internal fun ClientDepositAccountsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onViewAccount: ((accountId: Int, accountEndpoint: String) -> Unit)? = null,
    onApproveAccount: ((Int) -> Unit)? = null,
    onAddAccount: ((clientId: Int, kind: DepositAccountKind) -> Unit)? = null,
    viewModel: ClientDepositAccountsViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val totalCount by viewModel.totalCount.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            ClientDepositAccountsEvent.NavigateBack -> onNavigateBack()
            is ClientDepositAccountsEvent.ViewAccount ->
                onViewAccount?.invoke(event.accountId, event.accountEndpoint)
            is ClientDepositAccountsEvent.ApproveAccount -> onApproveAccount?.invoke(event.accountId)
            is ClientDepositAccountsEvent.AddAccount ->
                onAddAccount?.invoke(event.clientId, event.kind)
        }
    }

    AppScaffold(
        title = stringResource(state.kind.titleRes),
        // Dispatched, not called directly — the ViewModel owns the event.
        onBackPressed = { viewModel.trySendAction(ClientDepositAccountsAction.NavigateBack) },
        // The add affordance is a FAB, not the original's header icon.
        //
        // OPERATOR-DIRECTED UI CHANGE (2026-10-22), recorded as a §6a deviation. The original
        // puts "+" in the header beside the title — zero FABs anywhere in `feature/client` — and
        // on a real device an officer read that as "there is no way to add here". A FAB is the
        // platform's add affordance and is what the clients and groups lists already use, so this
        // also makes the app internally consistent.
        //
        // It also fixes an accessibility defect: the header icon passed
        // `contentDescription = null`, so the only way to add a record was unlabelled to a screen
        // reader.
        floatingActionButtonContent = onAddAccount?.let { add ->
            FloatingActionButtonContent(
                onClick = { viewModel.trySendAction(ClientDepositAccountsAction.AddAccount) },
                contentColor = KptTheme.colorScheme.onPrimaryContainer,
                content = {
                    Icon(
                        imageVector = AppIcons.Add,
                        contentDescription = stringResource(Res.string.feature_client_add_record),
                    )
                },
            )
        },
    ) { scaffoldPadding ->
        ClientDepositAccountsContent(
            state = state,
            accounts = accounts,
            totalCount = totalCount,
            onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
            canViewAccount = onViewAccount != null,
            canApproveAccount = onApproveAccount != null,
            canAddAccount = onAddAccount != null,
            modifier = modifier,
            scaffoldPadding = scaffoldPadding,
        )
    }
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun ClientDepositAccountsContent(
    state: ClientDepositAccountsState,
    accounts: ScreenState<List<SavingsAccount>>,
    onAction: (ClientDepositAccountsAction) -> Unit,
    modifier: Modifier = Modifier,
    totalCount: Int = 0,
    canViewAccount: Boolean = false,
    canApproveAccount: Boolean = false,
    canAddAccount: Boolean = false,
    /**
     * Insets from the host [AppScaffold]'s top bar.
     *
     * Defaulted so tests and previews can render this half alone; the production caller always
     * passes the scaffold's own padding.
     */
    scaffoldPadding: PaddingValues = PaddingValues(),
) {
    Column(
        modifier = modifier
            .padding(scaffoldPadding)
            .fillMaxSize()
            .padding(horizontal = KptTheme.spacing.md)
            .testTag(ClientDepositAccountsTestTags.SCREEN),
    ) {
        AccountsHeader(
            title = stringResource(state.kind.titleRes),
            totalItem = totalCount.toString(),
            // Drawn from the UNFILTERED total, so searching everything away never removes the
            // control that clears the search.
            hasAnyAccount = totalCount > 0,
            onAction = onAction,
        )

        if (state.isSearchBarActive) {
            AppSearchBar(
                query = state.searchText,
                onQueryChange = { onAction(ClientDepositAccountsAction.UpdateSearchValue(it)) },
                // Filtering is local — the list narrows as the officer types, so there is nothing
                // to submit. The originals re-fetched the whole account list per search tap.
                onSearchClick = {},
                onBackClick = { onAction(ClientDepositAccountsAction.ToggleSearch) },
                modifier = Modifier.testTag(ClientDepositAccountsTestTags.SEARCH_BAR),
            )
        }

        Spacer(Modifier.height(KptTheme.spacing.md))

        RefreshableScreenContent(
            state = accounts,
            onRefresh = { onAction(ClientDepositAccountsAction.Refresh) },
            modifier = Modifier.fillMaxSize().testTag(ClientDepositAccountsTestTags.CONTENT),
            empty = {
                // Two empty states where the originals had one. "No accounts" offers to open one;
                // "nothing matches your search" must not — opening an account would not reveal the
                // ones the query is hiding.
                AppEmptyCard(
                    msg = if (state.isFiltering) {
                        stringResource(Res.string.feature_client_deposit_accounts_filtered_empty)
                    } else {
                        stringResource(state.kind.emptyRes)
                    },
                    isButtonPresent = canAddAccount && !state.isFiltering,
                    onClick = { onAction(ClientDepositAccountsAction.AddAccount) },
                    modifier = Modifier.testTag(ClientDepositAccountsTestTags.EMPTY),
                )
            },
        ) { list, _ ->
            DepositAccountList(
                accounts = list,
                kind = state.kind,
                canViewAccount = canViewAccount,
                canApproveAccount = canApproveAccount,
                onAction = onAction,
            )
        }
    }
}

@Composable
private fun DepositAccountList(
    accounts: List<SavingsAccount>,
    kind: DepositAccountKind,
    canViewAccount: Boolean,
    canApproveAccount: Boolean,
    onAction: (ClientDepositAccountsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    // One card open at a time, as in the originals.
    var expandedId by rememberSaveable { mutableStateOf(-1) }
    val productLabel = stringResource(kind.productRes)
    val notAvailable = stringResource(Res.string.feature_client_deposit_accounts_not_available)
    val pendingApproval = stringResource(Res.string.feature_client_deposit_accounts_pending_approval)

    LazyColumn(modifier = modifier.testTag(ClientDepositAccountsTestTags.LIST)) {
        // Keyed by account id, not by index: the originals tracked the open card by LIST POSITION,
        // so a refresh that reordered or removed an account left a DIFFERENT account expanded.
        items(accounts, key = { it.id ?: it.accountNo.hashCode() }) { account ->
            val accountId = account.id ?: 0
            AppActionsSavingsListingComponent(
                accountNo = account.accountNo.orEmpty(),
                savingsProduct = productLabel,
                savingsProductName = account.productName.orEmpty(),
                balance = account.accountBalance
                    ?.let { CurrencyFormatter.format(it, account.currency?.code, account.currency?.decimalPlaces) }
                    ?: notAvailable,
                lastActive = account.lastActiveTransactionDate
                    ?.let { DateHelper.getDateAsString(it) }
                    ?: if (account.status?.submittedAndPendingApproval == true) {
                        pendingApproval
                    } else {
                        notAvailable
                    },
                menuList = accountMenu(account, canViewAccount, canApproveAccount),
                onActionClicked = { chosen -> onAccountAction(chosen, account, kind, onAction) },
                isExpanded = expandedId == accountId,
                onExpandToggle = { expandedId = if (expandedId == accountId) -1 else accountId },
            )
            Spacer(Modifier.height(KptTheme.spacing.sm))
        }
    }
}

private fun accountMenu(account: SavingsAccount, canViewAccount: Boolean, canApproveAccount: Boolean) = buildList {
    if (canViewAccount) add(Actions.ViewAccount())
    // Approve is offered only for an account actually awaiting approval — the
    // originals' rule — and only when the destination is wired.
    if (canApproveAccount && account.status?.submittedAndPendingApproval == true) {
        add(Actions.ApproveAccount())
    }
}

private fun onAccountAction(
    chosen: Actions,
    account: SavingsAccount,
    kind: DepositAccountKind,
    onAction: (ClientDepositAccountsAction) -> Unit,
) {
    val accountId = account.id ?: 0
    when (chosen) {
        is Actions.ViewAccount -> onAction(
            ClientDepositAccountsAction.ViewAccount(
                accountId = accountId,
                // The row's OWN endpoint, so the summary addresses the right
                // Fineract collection.
                accountEndpoint = account.depositType?.serverType?.endpoint
                    ?: kind.serverType.endpoint,
            ),
        )
        // The ACCOUNT id. The originals passed the CLIENT id here — see the
        // ViewModel KDoc for why that is a wrong-target write, not a rename.
        is Actions.ApproveAccount ->
            onAction(ClientDepositAccountsAction.ApproveAccount(accountId))
        else -> Unit
    }
}

@Composable
private fun AccountsHeader(
    title: String,
    totalItem: String,
    hasAnyAccount: Boolean,
    onAction: (ClientDepositAccountsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(text = title, style = AppTypography.titleMedium)
            Text(
                text = stringResource(Res.string.feature_client_deposit_accounts_item, totalItem),
                style = AppTypography.labelMedium,
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (hasAnyAccount) {
                IconButton(
                    onClick = { onAction(ClientDepositAccountsAction.ToggleSearch) },
                    modifier = Modifier.testTag(ClientDepositAccountsTestTags.SEARCH_TOGGLE),
                ) {
                    Icon(imageVector = AppIcons.Search, contentDescription = null)
                }
            }
            // The add affordance moved to the scaffold's FAB — see the screen KDoc.
        }
    }
}

private val DepositAccountKind.titleRes: StringResource
    get() = when (this) {
        DepositAccountKind.SAVINGS -> Res.string.feature_client_screen_savings_accounts
        DepositAccountKind.FIXED -> Res.string.feature_client_screen_fixed_deposit_accounts
        DepositAccountKind.RECURRING -> Res.string.feature_client_screen_recurring_deposit_accounts
    }

private val DepositAccountKind.productRes: StringResource
    get() = when (this) {
        DepositAccountKind.SAVINGS -> Res.string.feature_client_product_savings_account
        DepositAccountKind.FIXED -> Res.string.feature_client_product_fixed_deposit_account
        DepositAccountKind.RECURRING -> Res.string.feature_client_product_recurring_deposit_account
    }

private val DepositAccountKind.emptyRes: StringResource
    get() = when (this) {
        DepositAccountKind.SAVINGS -> Res.string.feature_client_savings_accounts_empty
        DepositAccountKind.FIXED -> Res.string.feature_client_fixed_deposit_accounts_empty
        DepositAccountKind.RECURRING -> Res.string.feature_client_recurring_deposit_accounts_empty
    }

internal object ClientDepositAccountsTestTags {
    const val SCREEN = "ClientDepositAccountsScreen"
    const val CONTENT = "ClientDepositAccountsContent"
    const val LIST = "ClientDepositAccountsList"
    const val EMPTY = "ClientDepositAccountsEmpty"
    const val SEARCH_BAR = "ClientDepositAccountsSearchBar"
    const val SEARCH_TOGGLE = "ClientDepositAccountsSearchToggle"
    const val ADD = "ClientDepositAccountsAdd"
}

/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.loanaccounts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.ui.effects.EventsEffect
import kpt.core.base.ui.screen.RefreshableScreenContent
import kpt.core.common.utils.CurrencyFormatter
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.fieldofficer.component.FloatingActionButtonContent
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.AppTypography
import kpt.core.designsystem.theme.DesignToken
import kpt.core.model.loan.LoanAccount
import kpt.core.ui.components.Actions
import kpt.core.ui.components.AppActionsLoanListingComponent
import kpt.core.ui.components.AppEmptyCard
import kpt.core.ui.components.AppSearchBar
import kpt.feature.client.generated.resources.Res
import kpt.feature.client.generated.resources.feature_client_account_status
import kpt.feature.client.generated.resources.feature_client_add_record
import kpt.feature.client.generated.resources.feature_client_filters
import kpt.feature.client.generated.resources.feature_client_loan_account
import kpt.feature.client.generated.resources.feature_client_loan_accounts_empty
import kpt.feature.client.generated.resources.feature_client_loan_accounts_filtered_empty
import kpt.feature.client.generated.resources.feature_client_loan_accounts_item
import kpt.feature.client.generated.resources.feature_client_loan_accounts_not_available
import kpt.feature.client.generated.resources.feature_client_screen_loan_accounts
import kpt.feature.client.generated.resources.feature_client_status_active
import kpt.feature.client.generated.resources.feature_client_status_closed
import kpt.feature.client.generated.resources.feature_client_status_overpaid
import kpt.feature.client.generated.resources.feature_client_status_pending
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Carried over from `6b66e8a43:feature/client/clientLoanAccounts/ClientLoanAccountsScreen.kt` — the
 * header with its count and three icon buttons, the collapsible search bar, the filter bottom sheet
 * with its four status checkboxes, and the expandable account cards with their per-status action
 * menus.
 *
 * ## §6a adaptations
 *
 * The `isLoading` boolean plus the error `AlertDialog` become [ScreenState] through [ScreenContent],
 * which is what removes the original's "No internet connection, Try Again" dialog over a blank
 * screen while a cached copy sat unread. The search and the filters are a derivation over the
 * stream rather than a second stored list (see the ViewModel).
 *
 * The BREADCRUMB bar at the top is not ported, for the reason in
 * `loan-summary-breadcrumb-not-ported` — the component exists in `core/ui` but takes a
 * `NavController`, and that decision covers every screen in the original that draws one.
 *
 * ## Which actions a card offers depends on the loan's status
 *
 * An ACTIVE loan offers View Account and Make Repayment; everything else offers View Account only —
 * the original's rule, kept. Both destinations are ported (the loan profile and the repayment
 * form), so this is the first screen in the port where a card's whole action menu is live.
 */
@Composable
internal fun ClientLoanAccountsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onViewAccount: ((Int) -> Unit)? = null,
    onMakeRepayment: ((Int) -> Unit)? = null,
    onAddAccount: ((Int) -> Unit)? = null,
    viewModel: ClientLoanAccountsViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val totalCount by viewModel.totalCount.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            ClientLoanAccountsEvent.NavigateBack -> onNavigateBack()
            is ClientLoanAccountsEvent.ViewAccount -> onViewAccount?.invoke(event.loanId)
            is ClientLoanAccountsEvent.MakeRepayment -> onMakeRepayment?.invoke(event.loanId)
            is ClientLoanAccountsEvent.AddAccount -> onAddAccount?.invoke(event.clientId)
        }
    }

    AppScaffold(
        title = stringResource(Res.string.feature_client_screen_loan_accounts),
        // Dispatched, not called directly: the ViewModel owns the event, which is the
        // path `ClientLoanAccountsAction.NavigateBack` already declared and nothing reached.
        onBackPressed = { viewModel.trySendAction(ClientLoanAccountsAction.NavigateBack) },
        // The add affordance is a FAB, not the original's header icon — operator-directed UI
        // change (2026-10-22), recorded as a §6a deviation. See ClientDepositAccountsScreen for
        // the full reasoning; in short, the original's header "+" read as "no way to add here" on
        // a real device, and it passed `contentDescription = null` so it was unlabelled too.
        floatingActionButtonContent = onAddAccount?.let {
            FloatingActionButtonContent(
                onClick = { viewModel.trySendAction(ClientLoanAccountsAction.AddAccount) },
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
        ClientLoanAccountsContent(
            state = state,
            accounts = accounts,
            totalCount = totalCount,
            onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
            canViewAccount = onViewAccount != null,
            canMakeRepayment = onMakeRepayment != null,
            canAddAccount = onAddAccount != null,
            modifier = modifier,
            scaffoldPadding = scaffoldPadding,
        )
    }
}

/** Stateless half — rendered in tests and previews without Koin. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ClientLoanAccountsContent(
    state: ClientLoanAccountsState,
    accounts: ScreenState<List<LoanAccount>>,
    onAction: (ClientLoanAccountsAction) -> Unit,
    modifier: Modifier = Modifier,
    totalCount: Int = 0,
    canViewAccount: Boolean = false,
    canMakeRepayment: Boolean = false,
    canAddAccount: Boolean = false,
    /**
     * Insets from the host [AppScaffold]'s top bar.
     *
     * Defaulted so tests and previews can render this half alone — the production caller always
     * passes the scaffold's own padding. These screens had NO top bar at all before S5d: the shell
     * draws one only for tab roots, so every client push destination rendered with no title and no
     * back affordance (which is why their `NavigateBack` action had no dispatcher).
     */
    scaffoldPadding: PaddingValues = PaddingValues(),
) {
    // The host scaffold's insets, folded into the modifier this half already receives so
    // the screen's own root layout places them.
    @Suppress("NAME_SHADOWING")
    val modifier = modifier.padding(scaffoldPadding)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = KptTheme.spacing.md)
            .testTag(ClientLoanAccountsTestTags.SCREEN),
    ) {
        AccountsHeader(
            totalItem = totalCount.toString(),
            isFilterActive = state.selectedStatus.isNotEmpty(),
            // The header's controls are drawn whenever the client HAS accounts — read from the
            // unfiltered total, not from the filtered list. The original hid them when the
            // FILTERED list was empty, so filtering everything out also removed the only way to
            // clear the filter.
            hasAnyAccount = totalCount > 0,
            onAction = onAction,
        )

        if (state.isSearchBarActive) {
            AppSearchBar(
                query = state.searchText,
                onQueryChange = { onAction(ClientLoanAccountsAction.UpdateSearchValue(it)) },
                // Filtering is local, so there is nothing to submit — the list narrows as the
                // officer types. The original re-fetched the whole account list per search.
                onSearchClick = {},
                onBackClick = { onAction(ClientLoanAccountsAction.ToggleSearch) },
                modifier = Modifier.testTag(ClientLoanAccountsTestTags.SEARCH_BAR),
            )
        }

        Spacer(Modifier.height(KptTheme.spacing.md))

        // `RefreshableScreenContent`: the same renderer inside a `PullToRefreshBox`, with the M3
        // indicator driven off the stream's own `freshnessSignal.isRefreshing` rather than a
        // boolean this screen would have to own. The original hand-rolled that boolean and set it
        // true -> false around a call that only LAUNCHED a coroutine, so its spinner could never
        // be observed true (`LoanChargeViewModel.refreshLoanChargeList`, `6b66e8a43`).
        //
        // Usable here only since the `empty` slot became forwardable — the wrapper used to pass
        // the inner `ScreenContent` nothing but `state`/`onRetry`/`content`, so a screen with a
        // branded empty state had to choose between it and the pull gesture
        // (openMF/kmp-project-template#310).
        RefreshableScreenContent(
            state = accounts,
            onRefresh = { onAction(ClientLoanAccountsAction.Refresh) },
            modifier = Modifier.fillMaxSize().testTag(ClientLoanAccountsTestTags.CONTENT),
            empty = {
                AppEmptyCard(
                    // Two different empty states, where the original had one. "No loan accounts"
                    // and "nothing matches your filter" need different actions: the first offers
                    // to create an account, the second must not — creating one would not reveal
                    // the accounts the filter is hiding.
                    msg = if (state.isFiltering) {
                        stringResource(Res.string.feature_client_loan_accounts_filtered_empty)
                    } else {
                        stringResource(Res.string.feature_client_loan_accounts_empty)
                    },
                    isButtonPresent = canAddAccount && !state.isFiltering,
                    onClick = { onAction(ClientLoanAccountsAction.AddAccount) },
                    modifier = Modifier.testTag(ClientLoanAccountsTestTags.EMPTY),
                )
            },
        ) { loans, _ ->
            LazyColumn(modifier = Modifier.testTag(ClientLoanAccountsTestTags.LIST)) {
                items(loans, key = { it.id ?: it.accountNo.orEmpty() }) { loan ->
                    LoanAccountCard(
                        loan = loan,
                        canViewAccount = canViewAccount,
                        canMakeRepayment = canMakeRepayment,
                        onAction = onAction,
                    )
                    Spacer(Modifier.height(KptTheme.spacing.sm))
                }
            }
        }
    }

    if (state.isFilterDialogOpen) {
        FilterSheet(
            selectedStatuses = state.selectedStatus,
            onAction = onAction,
        )
    }
}

private fun loanMenu(loan: LoanAccount, canViewAccount: Boolean, canMakeRepayment: Boolean) = buildList {
    if (loan.id != null && canViewAccount) add(Actions.ViewAccount())
    // Make Repayment only for an ACTIVE loan — the original's rule.
    if (loan.id != null && canMakeRepayment && loan.status?.active == true) {
        add(Actions.MakeRepayment())
    }
}

@Composable
private fun LoanAccountCard(
    loan: LoanAccount,
    canViewAccount: Boolean,
    canMakeRepayment: Boolean,
    onAction: (ClientLoanAccountsAction) -> Unit,
) {
    val notAvailable = stringResource(Res.string.feature_client_loan_accounts_not_available)
    // The app's one money format ("MX$900.00"), as the loan and savings summaries show it — this card
    // glued the symbol to a raw Double and read "$0.0" (device, 2026-10-25).
    val money: (Double) -> String = { CurrencyFormatter.format(it, loan.currency?.code, loan.currency?.decimalPlaces) }

    // `id` is what both destinations need. A card whose loan has no id cannot be opened, so it
    // offers no actions rather than sending `0` — the original passed `loan.id ?: 0`, and loan 0
    // is a request for a loan that does not exist.
    val loanId = loan.id
    val menu = loanMenu(loan, canViewAccount, canMakeRepayment)

    AppActionsLoanListingComponent(
        accountNo = loan.accountNo ?: notAvailable,
        loanProduct = loan.productName ?: notAvailable,
        originalLoan = loan.originalLoan?.let(money) ?: notAvailable,
        // A pending loan has no amounts yet, so the original showed "N/A" rather than a formatted
        // zero on both of these — kept, for the same reason the summary blanks its figures.
        amountPaid = loan.amountPaid.forPendingAware(loan, money, notAvailable),
        loanBalance = loan.loanBalance.forPendingAware(loan, money, notAvailable),
        type = loan.loanType?.value ?: notAvailable,
        status = loan.status?.value ?: notAvailable,
        menuList = menu,
        onActionClicked = { action ->
            loanId ?: return@AppActionsLoanListingComponent
            when (action) {
                is Actions.ViewAccount -> onAction(ClientLoanAccountsAction.ViewAccount(loanId))
                is Actions.MakeRepayment -> onAction(ClientLoanAccountsAction.MakeRepayment(loanId))
                else -> Unit
            }
        },
    )
}

/**
 * The original's rule: a PENDING loan shows "N/A" for paid and balance rather than a currency
 * symbol with zero behind it.
 *
 * It also fixes a detail of the original's version, which built the string as
 * `symbol + (if (pending) "N/A" else amount.toString())` — producing **"₹N/A"**, a currency symbol
 * glued to the words. The symbol belongs only to an actual amount.
 */
private fun Double?.forPendingAware(
    loan: LoanAccount,
    money: (Double) -> String,
    notAvailable: String,
): String = when {
    loan.status?.pendingApproval == true -> notAvailable
    else -> money(this ?: 0.0)
}

@Composable
private fun AccountsHeader(
    totalItem: String,
    isFilterActive: Boolean,
    hasAnyAccount: Boolean,
    onAction: (ClientLoanAccountsAction) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = stringResource(Res.string.feature_client_loan_account),
                style = AppTypography.titleMedium,
            )
            Text(
                // A placeholder, where the original concatenated `totalItem + " " +
                // stringResource(client_savings_item)` — and that string was the SAVINGS one,
                // reused on the loan screen.
                text = stringResource(Res.string.feature_client_loan_accounts_item, totalItem),
                style = AppTypography.labelMedium,
                modifier = Modifier.testTag(ClientLoanAccountsTestTags.COUNT),
            )
        }

        Spacer(Modifier.weight(1f))

        if (hasAnyAccount) {
            IconButton(
                onClick = { onAction(ClientLoanAccountsAction.ToggleSearch) },
                modifier = Modifier.testTag(ClientLoanAccountsTestTags.SEARCH_TOGGLE),
            ) {
                Icon(imageVector = AppIcons.Search, contentDescription = null)
            }
            Box(modifier = Modifier.wrapContentSize(), contentAlignment = Alignment.Center) {
                IconButton(
                    onClick = { onAction(ClientLoanAccountsAction.ToggleFilter) },
                    modifier = Modifier.testTag(ClientLoanAccountsTestTags.FILTER_TOGGLE),
                ) {
                    Icon(imageVector = AppIcons.FilterList, contentDescription = null)
                }
                // The "a filter is on" dot, in the error colour, as the original drew it.
                if (isFilterActive) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = DesignToken.padding.medium, end = KptTheme.spacing.md)
                            .size(DesignToken.sizes.iconMinyMiny)
                            .clip(CircleShape)
                            .background(KptTheme.colorScheme.error)
                            .testTag(ClientLoanAccountsTestTags.FILTER_ACTIVE_DOT),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSheet(
    selectedStatuses: Set<LoanStatusFilter>,
    onAction: (ClientLoanAccountsAction) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = { onAction(ClientLoanAccountsAction.ToggleFilter) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = null,
        containerColor = KptTheme.colorScheme.background,
        modifier = Modifier.testTag(ClientLoanAccountsTestTags.FILTER_SHEET),
    ) {
        Column(modifier = Modifier.padding(KptTheme.spacing.md)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(DesignToken.padding.medium),
            ) {
                Text(
                    text = stringResource(Res.string.feature_client_filters),
                    style = AppTypography.titleLargeEmphasized,
                    color = KptTheme.colorScheme.primary,
                )
                Row {
                    IconButton(
                        onClick = {
                            onAction(ClientLoanAccountsAction.ClearFilters)
                            onAction(ClientLoanAccountsAction.ToggleFilter)
                        },
                        modifier = Modifier.testTag(ClientLoanAccountsTestTags.FILTER_CLEAR),
                    ) {
                        Icon(imageVector = AppIcons.Redo, contentDescription = null)
                    }
                    IconButton(
                        onClick = { onAction(ClientLoanAccountsAction.ToggleFilter) },
                        modifier = Modifier.testTag(ClientLoanAccountsTestTags.FILTER_APPLY),
                    ) {
                        Icon(imageVector = AppIcons.Check, contentDescription = null)
                    }
                }
            }
        }

        HorizontalDivider(Modifier.fillMaxWidth(), thickness = DesignToken.strokes.thin)

        Column(modifier = Modifier.padding(DesignToken.padding.medium)) {
            Text(
                text = stringResource(Res.string.feature_client_account_status),
                style = AppTypography.titleMediumEmphasized,
                modifier = Modifier.padding(bottom = KptTheme.spacing.sm),
            )

            LoanStatusFilter.entries.forEach { status ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.width(DesignToken.padding.medium))
                    Checkbox(
                        checked = status in selectedStatuses,
                        onCheckedChange = {
                            onAction(ClientLoanAccountsAction.HandleFilterClick(status))
                        },
                        modifier = Modifier.testTag(ClientLoanAccountsTestTags.filterBox(status)),
                    )
                    Text(text = stringResource(status.labelRes()))
                }
            }
        }
    }
}

private fun LoanStatusFilter.labelRes() = when (this) {
    LoanStatusFilter.ACTIVE -> Res.string.feature_client_status_active
    LoanStatusFilter.PENDING -> Res.string.feature_client_status_pending
    LoanStatusFilter.CLOSED -> Res.string.feature_client_status_closed
    LoanStatusFilter.OVERPAID -> Res.string.feature_client_status_overpaid
}

/** Stable handles for UI tests and Maestro flows — see RULE-KMP-COMPOSE-UITEST-001. */
internal object ClientLoanAccountsTestTags {
    const val SCREEN = "clientLoanAccounts:screen"
    const val CONTENT = "clientLoanAccounts:content"
    const val LIST = "clientLoanAccounts:list"
    const val EMPTY = "clientLoanAccounts:empty"
    const val COUNT = "clientLoanAccounts:count"
    const val SEARCH_TOGGLE = "clientLoanAccounts:searchToggle"
    const val SEARCH_BAR = "clientLoanAccounts:searchBar"
    const val ADD = "clientLoanAccounts:add"
    const val FILTER_TOGGLE = "clientLoanAccounts:filterToggle"
    const val FILTER_ACTIVE_DOT = "clientLoanAccounts:filterActiveDot"
    const val FILTER_SHEET = "clientLoanAccounts:filterSheet"
    const val FILTER_CLEAR = "clientLoanAccounts:filterClear"
    const val FILTER_APPLY = "clientLoanAccounts:filterApply"
    fun filterBox(status: LoanStatusFilter): String = "clientLoanAccounts:filter:${status.name}"
}

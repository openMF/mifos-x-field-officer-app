/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.shareaccounts

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
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.designsystem.fieldofficer.component.FloatingActionButtonContent
import kpt.core.designsystem.icon.AppIcons
import kpt.core.designsystem.theme.AppTypography
import kpt.core.model.objects.account.share.ShareAccounts
import kpt.core.ui.components.Actions
import kpt.core.ui.components.AppActionsShareListingComponent
import kpt.core.ui.components.AppEmptyCard
import kpt.core.ui.components.AppSearchBar
import kpt.feature.client.generated.resources.Res
import kpt.feature.client.generated.resources.feature_client_add_record
import kpt.feature.client.generated.resources.feature_client_deposit_accounts_item
import kpt.feature.client.generated.resources.feature_client_shareaccounts_empty
import kpt.feature.client.generated.resources.feature_client_shareaccounts_filtered_empty
import kpt.feature.client.generated.resources.feature_client_shareaccounts_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * A client's share accounts. See [ClientShareAccountsViewModel] for the porting decisions.
 *
 * Carried over from `6b66e8a43:feature/client/shareAccounts/ShareAccountsScreen.kt` — the header
 * with its count and icon buttons, the collapsible search bar, and the expandable share cards with
 * their approved / pending-for-approval counts.
 *
 * ## §6a adaptations
 *
 * `isLoading` plus the error `AlertDialog` become [ScreenState] through
 * [RefreshableScreenContent]. The breadcrumb bar is not ported, per
 * `loan-summary-breadcrumb-not-ported`.
 *
 * The FILTER icon is not drawn: as on the deposit-accounts siblings, its action toggles a flag no
 * dialog reads, so the control was inert in the original. "View Account" IS drawn and does
 * nothing, because that is dead in the ORIGINAL's own graph rather than unported here — the
 * distinction is set out in the ViewModel's KDoc.
 */
@Composable
internal fun ClientShareAccountsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onAddAccount: ((Int) -> Unit)? = null,
    viewModel: ClientShareAccountsViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val totalCount by viewModel.totalCount.collectAsStateWithLifecycle()

    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            ClientShareAccountsEvent.NavigateBack -> onNavigateBack()
            // Dead in the original's graph (`navigateToViewAccount = {}`). Kept so the card's
            // menu matches; see the ViewModel KDoc.
            is ClientShareAccountsEvent.ViewAccount -> Unit
            is ClientShareAccountsEvent.AddAccount -> onAddAccount?.invoke(event.clientId)
        }
    }

    AppScaffold(
        title = stringResource(Res.string.feature_client_shareaccounts_title),
        onBackPressed = { viewModel.trySendAction(ClientShareAccountsAction.NavigateBack) },
        // The add affordance is a FAB, not the original's header icon — operator-directed UI
        // change (2026-10-22), recorded as a §6a deviation. See ClientDepositAccountsScreen for
        // the full reasoning; in short, the original's header "+" read as "no way to add here" on
        // a real device, and it passed `contentDescription = null` so it was unlabelled too.
        floatingActionButtonContent = onAddAccount?.let {
            FloatingActionButtonContent(
                onClick = { viewModel.trySendAction(ClientShareAccountsAction.AddAccount) },
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
        ClientShareAccountsContent(
            state = state,
            accounts = accounts,
            totalCount = totalCount,
            onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
            canAddAccount = onAddAccount != null,
            modifier = modifier,
            scaffoldPadding = scaffoldPadding,
        )
    }
}

/** Stateless half — rendered in tests and previews without Koin. */
@Composable
internal fun ClientShareAccountsContent(
    state: ClientShareAccountsState,
    accounts: ScreenState<List<ShareAccounts>>,
    onAction: (ClientShareAccountsAction) -> Unit,
    modifier: Modifier = Modifier,
    totalCount: Int = 0,
    canAddAccount: Boolean = false,
    scaffoldPadding: PaddingValues = PaddingValues(),
) {
    Column(
        modifier = modifier
            .padding(scaffoldPadding)
            .fillMaxSize()
            .padding(horizontal = KptTheme.spacing.md)
            .testTag(ClientShareAccountsTestTags.SCREEN),
    ) {
        Header(
            totalItem = totalCount.toString(),
            hasAnyAccount = totalCount > 0,
            onAction = onAction,
        )

        if (state.isSearchBarActive) {
            AppSearchBar(
                query = state.searchText,
                onQueryChange = { onAction(ClientShareAccountsAction.UpdateSearchValue(it)) },
                onSearchClick = {},
                onBackClick = { onAction(ClientShareAccountsAction.ToggleSearch) },
                modifier = Modifier.testTag(ClientShareAccountsTestTags.SEARCH_BAR),
            )
        }

        Spacer(Modifier.height(KptTheme.spacing.md))

        RefreshableScreenContent(
            state = accounts,
            onRefresh = { onAction(ClientShareAccountsAction.Refresh) },
            modifier = Modifier.fillMaxSize().testTag(ClientShareAccountsTestTags.CONTENT),
            empty = {
                AppEmptyCard(
                    msg = if (state.isFiltering) {
                        stringResource(Res.string.feature_client_shareaccounts_filtered_empty)
                    } else {
                        stringResource(Res.string.feature_client_shareaccounts_empty)
                    },
                    isButtonPresent = canAddAccount && !state.isFiltering,
                    onClick = { onAction(ClientShareAccountsAction.AddAccount) },
                    modifier = Modifier.testTag(ClientShareAccountsTestTags.EMPTY),
                )
            },
        ) { list, _ ->
            ShareAccountList(accounts = list, onAction = onAction)
        }
    }
}

@Composable
private fun ShareAccountList(
    accounts: List<ShareAccounts>,
    onAction: (ClientShareAccountsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    // One card open at a time, keyed by account id rather than list position — a refresh that
    // reorders the list must not leave a different account expanded.
    var expandedId by rememberSaveable { mutableStateOf(-1) }

    LazyColumn(modifier = modifier.testTag(ClientShareAccountsTestTags.LIST)) {
        items(accounts, key = { it.id ?: it.accountNo.hashCode() }) { account ->
            val accountId = account.id ?: 0
            AppActionsShareListingComponent(
                accountNo = account.accountNo.orEmpty(),
                shareProductName = account.productName ?: account.shortProductName.orEmpty(),
                approvedShares = account.totalApprovedShares,
                pendingForApprovalShares = account.totalPendingForApprovalShares,
                menuList = listOf(Actions.ViewAccount()),
                onActionClicked = { chosen ->
                    when (chosen) {
                        is Actions.ViewAccount ->
                            onAction(ClientShareAccountsAction.ViewAccount(accountId))
                        else -> Unit
                    }
                },
                isExpanded = expandedId == accountId,
                onClick = { expandedId = if (expandedId == accountId) -1 else accountId },
            )
            Spacer(Modifier.height(KptTheme.spacing.sm))
        }
    }
}

@Composable
private fun Header(
    totalItem: String,
    hasAnyAccount: Boolean,
    onAction: (ClientShareAccountsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = stringResource(Res.string.feature_client_shareaccounts_title),
                style = AppTypography.titleMedium,
            )
            Text(
                text = stringResource(Res.string.feature_client_deposit_accounts_item, totalItem),
                style = AppTypography.labelMedium,
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (hasAnyAccount) {
                IconButton(
                    onClick = { onAction(ClientShareAccountsAction.ToggleSearch) },
                    modifier = Modifier.testTag(ClientShareAccountsTestTags.SEARCH_TOGGLE),
                ) {
                    Icon(imageVector = AppIcons.Search, contentDescription = null)
                }
            }
        }
    }
}

internal object ClientShareAccountsTestTags {
    const val SCREEN = "ClientShareAccountsScreen"
    const val CONTENT = "ClientShareAccountsContent"
    const val LIST = "ClientShareAccountsList"
    const val EMPTY = "ClientShareAccountsEmpty"
    const val SEARCH_BAR = "ClientShareAccountsSearchBar"
    const val SEARCH_TOGGLE = "ClientShareAccountsSearchToggle"
    const val ADD = "ClientShareAccountsAdd"
}

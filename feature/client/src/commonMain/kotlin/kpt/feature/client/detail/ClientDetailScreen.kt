/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.database.client.entity.ClientAccounts
import kpt.core.database.client.entity.ClientEntity
import kpt.core.designsystem.icon.MifosIcons
import kpt.feature.client.generated.resources.Res
import androidx.compose.ui.draw.rotate
import kpt.feature.client.generated.resources.feature_client_charges_title
import kpt.feature.client.generated.resources.feature_client_detail_title
import kpt.feature.client.generated.resources.feature_client_loans
import kpt.feature.client.generated.resources.feature_client_savings
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ClientDetailScreen(
    onBackClick: () -> Unit,
    onChargesClick: ((Int) -> Unit)?,
    onLoanClick: ((Int) -> Unit)?,
    onSavingsClick: ((Int) -> Unit)?,
    onNotesClick: ((Int) -> Unit)?,
    onDocumentsClick: ((Int) -> Unit)?,
    modifier: Modifier = Modifier,
    viewModel: ClientDetailViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is ClientDetailEvent.OpenCharges -> onChargesClick?.invoke(event.clientId)
                is ClientDetailEvent.OpenLoan -> onLoanClick?.invoke(event.loanId)
                is ClientDetailEvent.OpenSavings -> onSavingsClick?.invoke(event.savingsId)
                is ClientDetailEvent.OpenNotes -> onNotesClick?.invoke(event.clientId)
                is ClientDetailEvent.OpenDocuments -> onDocumentsClick?.invoke(event.clientId)
            }
        }
    }

    ClientDetailContent(
        state = state,
        onAction = { viewModel.actionChannel.trySend(it) },
        onBackClick = onBackClick,
        chargesClickable = onChargesClick != null,
        loansClickable = onLoanClick != null,
        savingsClickable = onSavingsClick != null,
        modifier = modifier,
    )
}

/** Stateless half — testable without Koin or a signed-in session. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ClientDetailContent(
    state: ClientDetailState,
    onAction: (ClientDetailAction) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    chargesClickable: Boolean = true,
    loansClickable: Boolean = true,
    savingsClickable: Boolean = true,
) {
    Scaffold(
        modifier = modifier.fillMaxSize().testTag(ClientDetailTestTags.SCREEN),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.feature_client_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag(ClientDetailTestTags.BACK)) {
                        Icon(MifosIcons.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Identity renders from its OWN state. The original blocked the whole screen on a shared
            // `showLoading` written by two concurrent collectors, so a slow accounts call hid the
            // client's name even though it was already cached.
            ScreenContent(
                state = state.client,
                onRetry = { onAction(ClientDetailAction.RetryClient) },
                modifier = Modifier.fillMaxWidth().testTag(ClientDetailTestTags.IDENTITY),
            ) { client, _ -> ClientIdentity(client) }

            // Charges — the first of the original's nine missing detail actions to land. Rendered
            // only when a destination exists; `null` means the row is absent, never inert.
            if (chargesClickable) {
                ChargesAction(onClick = { onAction(ClientDetailAction.ChargesClicked) })
            }

            ScreenContent(
                state = state.accounts,
                onRetry = { onAction(ClientDetailAction.RetryAccounts) },
                modifier = Modifier.fillMaxWidth().testTag(ClientDetailTestTags.ACCOUNTS),
            ) { accounts, _ ->
                ClientAccountsSection(
                    accounts = accounts,
                    onLoanClick = if (loansClickable) { id -> onAction(ClientDetailAction.LoanClicked(id)) } else null,
                    onSavingsClick = if (savingsClickable) { id -> onAction(ClientDetailAction.SavingsClicked(id)) } else null,
                )
            }
        }
    }
}

@Composable
private fun ChargesAction(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(ClientDetailTestTags.CHARGES)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(stringResource(Res.string.feature_client_charges_title), style = MaterialTheme.typography.bodyLarge)
        Icon(MifosIcons.ArrowBack, contentDescription = null, modifier = Modifier.rotate(180f))
    }
}

@Composable
private fun ClientIdentity(client: ClientEntity) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(client.displayName.orEmpty(), style = MaterialTheme.typography.headlineSmall)
        client.accountNo?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            client.officeName?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            client.status?.value?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
private fun ClientAccountsSection(
    accounts: ClientAccounts,
    onLoanClick: ((Int) -> Unit)?,
    onSavingsClick: ((Int) -> Unit)?,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (accounts.loanAccounts.isNotEmpty()) {
            Text(stringResource(Res.string.feature_client_loans), style = MaterialTheme.typography.titleMedium)
            accounts.loanAccounts.forEach { loan ->
                AccountRow(
                    label = loan.productName.orEmpty(),
                    detail = loan.accountNo,
                    // An account with no id cannot be opened, so it is NOT made clickable — the row
                    // still renders. A tap that cannot resolve a destination is the dead clickable.
                    onClick = clickOrNull(loan.id, onLoanClick),
                )
                HorizontalDivider()
            }
        }
        if (accounts.savingsAccounts.isNotEmpty()) {
            Text(stringResource(Res.string.feature_client_savings), style = MaterialTheme.typography.titleMedium)
            accounts.savingsAccounts.forEach { savings ->
                AccountRow(
                    label = savings.productName.orEmpty(),
                    detail = savings.accountNo,
                    onClick = clickOrNull(savings.id, onSavingsClick),
                )
                HorizontalDivider()
            }
        }
    }
}

/** A click only when there is both a handler AND an id to hand it. */
private fun clickOrNull(id: Int?, handler: ((Int) -> Unit)?): (() -> Unit)? =
    if (id != null && handler != null) ({ handler(id) }) else null

@Composable
private fun AccountRow(label: String, detail: String?, onClick: (() -> Unit)?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 8.dp),
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        detail?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
    }
}

/** Stable handles for the client detail screen. */
object ClientDetailTestTags {
    const val SCREEN = "client-detail:screen"
    const val BACK = "client-detail:back"
    const val IDENTITY = "client-detail:identity"
    const val ACCOUNTS = "client-detail:accounts"
    const val CHARGES = "client-detail:charges"
}

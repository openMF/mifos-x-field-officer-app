/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.charges

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kpt.core.base.ui.screen.ScreenContent
import kpt.core.common.utils.DateHelper
import kpt.core.database.charge.entity.ChargesEntity
import kpt.core.designsystem.icon.MifosIcons
import kpt.core.designsystem.theme.LocalFinanceColors
import kpt.feature.client.generated.resources.Res
import kpt.feature.client.generated.resources.feature_client_charges_count
import kpt.feature.client.generated.resources.feature_client_charges_outstanding
import kpt.feature.client.generated.resources.feature_client_charges_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ClientChargesScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ClientChargesViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                ClientChargesEvent.NavigateBack -> onBackClick()
            }
        }
    }

    ClientChargesContent(
        state = state,
        onAction = { viewModel.actionChannel.trySend(it) },
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

/** Stateless half — rendered in tests without Koin or a signed-in session. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ClientChargesContent(
    state: ClientChargesState,
    onAction: (ClientChargesAction) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize().testTag(ClientChargesTestTags.SCREEN),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(Res.string.feature_client_charges_title))
                        // Count is DERIVED from the same state the list renders, so the header
                        // cannot disagree with it. The original pushed an itemCount up from the
                        // list through a setCount callback — and labelled it with the SAVINGS
                        // string, so a charges screen read "3 Savings Account".
                        Text(
                            text = stringResource(Res.string.feature_client_charges_count, state.count),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.testTag(ClientChargesTestTags.COUNT),
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag(ClientChargesTestTags.BACK),
                    ) {
                        Icon(MifosIcons.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        ScreenContent(
            state = state.charges,
            onRetry = { onAction(ClientChargesAction.Retry) },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag(ClientChargesTestTags.LIST),
        ) { charges, _ ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(charges) { charge ->
                    ChargeRow(charge)
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun ChargeRow(charge: ChargesEntity) {
    val finance = LocalFinanceColors.current
    val outstanding = charge.amountOutstanding ?: 0.0
    val symbol = charge.currency?.displaySymbol ?: charge.currency?.code.orEmpty()

    // Outstanding is the one fact this screen exists to convey, so it carries the SEMANTIC money
    // colour rather than the brand primary — painting a debt indigo because indigo is the brand
    // would bury it (DESIGN.md §"Money is not brand").
    val outstandingColor = if (outstanding > 0.0) finance.moneyNegative else finance.moneyPositive

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(charge.name.orEmpty(), style = MaterialTheme.typography.bodyLarge)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            val amountText = "$symbol ${charge.amount ?: 0.0}"
            Text(
                text = amountText,
                style = MaterialTheme.typography.bodyMedium,
                // A bare number read aloud is ambiguous — the currency travels with it.
                modifier = Modifier.semantics { contentDescription = amountText },
            )
            Text(
                text = stringResource(Res.string.feature_client_charges_outstanding, "$symbol $outstanding"),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = outstandingColor,
            )
        }

        // Fineract sends [yyyy, M, d], not a date type. Rendering it raw prints "[2026, 4, 1]";
        // DateHelper is the formatter the rest of the app already uses for these triples.
        charge.dueDate?.takeIf { it.isNotEmpty() }?.let { due ->
            Text(
                text = DateHelper.getDateAsString(due),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

/** Stable handles for the charges screen — asserted by tests, never shown to a user. */
object ClientChargesTestTags {
    const val SCREEN = "client-charges:screen"
    const val BACK = "client-charges:back"
    const val COUNT = "client-charges:count"
    const val LIST = "client-charges:list"
}

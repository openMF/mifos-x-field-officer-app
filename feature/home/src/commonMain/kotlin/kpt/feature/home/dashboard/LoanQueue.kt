/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.home.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.toRoute
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.Serializable
import kpt.core.base.designsystem.theme.KptTheme
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.combineContent
import kpt.core.base.store.screen.emptyIfContent
import kpt.core.base.ui.nav.composableWithPushTransitions
import kpt.core.base.ui.screen.RefreshableScreenContent
import kpt.core.common.utils.CurrencyFormatter
import kpt.core.data.dashboard.DashboardRepository
import kpt.core.data.session.SessionRepository
import kpt.core.designsystem.component.MoneyText
import kpt.core.designsystem.component.MoneyTone
import kpt.core.designsystem.fieldofficer.component.AppScaffold
import kpt.core.model.dashboard.DashboardLoan
import kpt.core.model.dashboard.DashboardScope
import kpt.core.model.dashboard.ownedBy
import kpt.core.model.objects.users.User
import kpt.feature.home.generated.resources.Res
import kpt.feature.home.generated.resources.feature_home_dashboard_loan_queue_empty
import kpt.feature.home.generated.resources.feature_home_dashboard_queue_approval
import kpt.feature.home.generated.resources.feature_home_dashboard_queue_disbursal
import kpt.feature.home.ui.TestTags
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** The officer's own loans in one Fineract status (100 submitted · 200 approved) — the work-queue tap-through. */
@Serializable
data class LoanQueueRoute(val status: Int)

fun NavController.navigateToLoanQueue(status: Int) = navigate(LoanQueueRoute(status))

/** Registered by the fork's nav graph, which owns the loan-profile route the rows open. */
fun NavGraphBuilder.loanQueueDestination(onBack: () -> Unit, onLoanClick: (Long) -> Unit) {
    composableWithPushTransitions<LoanQueueRoute> {
        LoanQueueScreen(onBack = onBack, onLoanClick = onLoanClick)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class LoanQueueViewModel(
    savedStateHandle: SavedStateHandle,
    sessionRepository: SessionRepository,
    dashboardRepository: DashboardRepository,
) : ViewModel() {
    val status: Int = savedStateHandle.toRoute<LoanQueueRoute>().status

    private val loans = dashboardRepository.loansStream(status, viewModelScope)

    /** Same "own" rule as the dashboard count, so the list always matches the number tapped. */
    val state: StateFlow<ScreenState<List<DashboardLoan>>> =
        sessionRepository.sessionStream(viewModelScope).state
            .filterIsInstance<ScreenState.Content<User>>()
            .flatMapLatest { session ->
                val user = session.data
                val scope = dashboardRepository.userStream(user.userId, viewModelScope).state.map { me ->
                    DashboardScope(user.officeId, (me as? ScreenState.Content)?.data?.staffId, user.username.orEmpty())
                }
                loans.state.combineContent(scope) { list, sc, _ -> list.ownedBy(sc) }
            }
            .emptyIfContent { it.isEmpty() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), ScreenState.Loading)

    fun retry() = loans.retry()

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

@Composable
internal fun LoanQueueScreen(
    onBack: () -> Unit,
    onLoanClick: (Long) -> Unit,
    viewModel: LoanQueueViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LoanQueueContent(viewModel.status, state, onBack, viewModel::retry, onLoanClick)
}

@Composable
internal fun LoanQueueContent(
    status: Int,
    state: ScreenState<List<DashboardLoan>>,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onLoanClick: (Long) -> Unit,
) {
    val title = if (status == DashboardRepository.STATUS_APPROVED) {
        Res.string.feature_home_dashboard_queue_disbursal
    } else {
        Res.string.feature_home_dashboard_queue_approval
    }
    AppScaffold(title = stringResource(title), onBackPressed = onBack) { padding ->
        RefreshableScreenContent(
            state = state,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize().padding(padding).testTag(TestTags.Home.LOAN_QUEUE),
            empty = {
                Text(
                    stringResource(Res.string.feature_home_dashboard_loan_queue_empty),
                    modifier = Modifier.fillMaxWidth().padding(KptTheme.spacing.lg),
                    color = KptTheme.colorScheme.onSurfaceVariant,
                )
            },
        ) { loans, _ ->
            LazyColumn(Modifier.fillMaxSize()) {
                items(loans, key = { it.id }) { loan ->
                    LoanRow(loan) { onLoanClick(loan.id) }
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun LoanRow(loan: DashboardLoan, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = KptTheme.spacing.md, vertical = KptTheme.spacing.sm)
            .testTag(TestTags.Home.loanQueueRow(loan.id)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KptTheme.spacing.sm),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.xs)) {
            Text(loan.clientName, style = KptTheme.typography.bodyLarge)
            Text(
                listOfNotNull(loan.accountNo, loan.productName).joinToString("  ·  "),
                style = KptTheme.typography.bodySmall,
                color = KptTheme.colorScheme.onSurfaceVariant,
            )
        }
        MoneyText(CurrencyFormatter.format(loan.principal, loan.currencyCode, 2), tone = MoneyTone.Neutral, style = KptTheme.typography.titleSmall)
    }
}

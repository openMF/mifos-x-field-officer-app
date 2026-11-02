/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(kpt.core.data.testing.ExperimentalDataTestingApi::class)

package kpt.feature.home.dashboard

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.screen.ScreenState
import kpt.core.data.dashboard.DashboardRepository
import kpt.core.data.testing.FakeCenterRepository
import kpt.core.data.testing.FakeCheckerInboxRepository
import kpt.core.data.testing.FakeCollectionSheetRepository
import kpt.core.data.testing.FakeDashboardRepository
import kpt.core.data.testing.FakeFormDraftRepository
import kpt.core.data.testing.FakeGroupRepository
import kpt.core.data.testing.FakeRecentRecordsRepository
import kpt.core.data.testing.FakeServerConfigRepository
import kpt.core.data.testing.FakeSessionRepository
import kpt.core.model.collectionsheet.IndividualCollectionSheet
import kpt.core.model.dashboard.DashboardLoan
import kpt.core.model.dashboard.DashboardUser
import kpt.core.model.dashboard.DueTodayLoan
import kpt.core.model.objects.users.User
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class HomeViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val fieldOfficer = listOf(
        "READ_COLLECTIONSHEET", "READ_LOAN", "READ_CLIENT", "READ_RESCHEDULELOAN", "APPROVE_LOAN_CHECKER",
        "READ_CALENDAR", "READ_CENTER", "READ_GROUP", "READ_SAVINGSACCOUNT",
        "CREATE_CLIENT", "CREATE_GROUP", "CREATE_CENTER", "CREATE_LOAN", "CREATE_SAVINGSACCOUNT",
    )

    private fun loan(id: Long, officer: Long?, by: String = "x") = DashboardLoan(
        id = id, accountNo = "$id", clientName = "c$id", loanOfficerId = officer, submittedByUsername = by,
        inArrears = false, principalOutstanding = 0.0, principalOverdue = 0.0, totalOutstanding = 0.0,
        totalOverdue = 0.0, currencyCode = "MXN", currencySymbol = "$",
    )

    private class Fixture(
        permissions: List<String>,
        staffId: Long? = 1,
        val dashboard: FakeDashboardRepository = FakeDashboardRepository(
            user = DashboardUser(id = 32, firstname = "Irving", staffId = staffId),
            loansByStatus = mapOf(100 to emptyList(), 200 to emptyList(), 300 to emptyList()),
        ),
        val sheet: FakeCollectionSheetRepository = FakeCollectionSheetRepository(ScreenState.Content(IndividualCollectionSheet())),
    ) {
        val vm = HomeViewModel(
            sessionRepository = FakeSessionRepository(User(username = "fieldofficer", userId = 32, officeId = 1, officeName = "CENTRAL", permissions = permissions)),
            dashboardRepository = dashboard,
            checkerInboxRepository = FakeCheckerInboxRepository(),
            collectionSheetRepository = sheet,
            centerRepository = FakeCenterRepository(),
            groupRepository = FakeGroupRepository(),
            formDraftRepository = FakeFormDraftRepository(),
            serverConfigRepository = FakeServerConfigRepository(),
            recentRecordsRepository = FakeRecentRecordsRepository(),
        )
    }

    private suspend fun Fixture.settled(): HomeUiState =
        vm.stateFlow.first { s -> s.cards.isNotEmpty() && s.cards.none { it.state == ScreenState.Loading } }

    @Test
    fun allPermittedCardsRenderAndHeaderUsesFirstName() = runTest(dispatcher) {
        val f = Fixture(fieldOfficer)
        val state = f.settled()
        assertEquals(HomeCardId.entries.toList(), state.cards.map { it.id })
        assertEquals("Irving", state.header.name)
        assertEquals("2026-10-26", state.header.businessDate)
        assertTrue(state.header.ownPortfolio)
    }

    @Test
    fun aCardWithoutItsPermissionIsAbsentAndNeverRequested() = runTest(dispatcher) {
        val f = Fixture(fieldOfficer - "READ_COLLECTIONSHEET" - "READ_LOAN")
        val state = f.settled()
        assertFalse(f.dashboard.requested.any { it.startsWith("loans:") })
    }

    @Test
    fun withoutTheCollectionSheetTodayIsComputedFromTheOfficersLoanSchedules() = runTest(dispatcher) {
        val dashboard = FakeDashboardRepository(
            user = DashboardUser(id = 32, staffId = 1),
            // loan 1 + 3 belong to officer 1 (3 is in arrears); loan 2 is someone else's and is never read
            loansByStatus = mapOf(100 to emptyList(), 200 to emptyList(), 300 to listOf(loan(1, 1).copy(clientId = 10), loan(2, 2).copy(clientId = 20), loan(3, 1).copy(clientId = 30, inArrears = true))),
            dueToday = listOf(
                DueTodayLoan(loanId = 1, clientId = 10, currencyCode = "MXN", currencySymbol = "$", due = 400.0, paid = 100.0),
                DueTodayLoan(loanId = 3, clientId = 30, currencyCode = "MXN", currencySymbol = "$", due = 250.0, paid = 0.0),
            ),
        )
        val f = Fixture(fieldOfficer - "READ_COLLECTIONSHEET", dashboard = dashboard)
        val state = f.settled()
        val card = assertIs<ScreenState.Content<HomeCard>>(state.cards.first { it.id == HomeCardId.COLLECTIONS }.state).data
        assertEquals(
            HomeCard.Collections(
                clientsDue = 2,
                loansDue = 2,
                dueByCurrency = listOf(Money(650.0, "MXN", "$")),
                collectedByCurrency = listOf(Money(100.0, "MXN", "$")),
                overdueClients = 1,
            ),
            card,
        )
        // Only the officer's own loans are read, for the business date — and never the sheet.
        assertTrue("dueToday:2026-10-26:1,3" in dashboard.requested)
        assertTrue(f.sheet.requested.isEmpty())
    }

    @Test
    fun loanQueuesCountOnlyTheOfficersOwnLoans() = runTest(dispatcher) {
        val dashboard = FakeDashboardRepository(
            user = DashboardUser(id = 32, staffId = 1),
            loansByStatus = mapOf(100 to listOf(loan(1, 1), loan(2, 2), loan(3, 1)), 200 to listOf(loan(4, 2)), 300 to emptyList()),
        )
        val state = Fixture(fieldOfficer, dashboard = dashboard).settled()
        val queue = assertIs<ScreenState.Content<HomeCard>>(state.cards.first { it.id == HomeCardId.WORK_QUEUE }.state).data
        assertIs<HomeCard.WorkQueue>(queue)
        assertEquals(2, queue.awaitingApproval)
        assertEquals(0, queue.awaitingDisbursal)
    }

    @Test
    fun withoutAStaffLinkOwnMeansSubmittedByTheUser() = runTest(dispatcher) {
        val dashboard = FakeDashboardRepository(
            user = DashboardUser(id = 32, staffId = null),
            loansByStatus = mapOf(100 to listOf(loan(1, null, by = "fieldofficer"), loan(2, null, by = "someone")), 200 to emptyList(), 300 to emptyList()),
        )
        val state = Fixture(fieldOfficer, staffId = null, dashboard = dashboard).settled()
        val queue = (state.cards.first { it.id == HomeCardId.WORK_QUEUE }.state as ScreenState.Content).data as HomeCard.WorkQueue
        assertEquals(1, queue.awaitingApproval)
        assertFalse(state.header.ownPortfolio)
    }

    @Test
    fun oneFailingCardDoesNotBlankTheOthers() = runTest(dispatcher) {
        val f = Fixture(fieldOfficer, sheet = FakeCollectionSheetRepository(ScreenState.Error(IllegalStateException("500"))))
        val state = f.settled()
        assertIs<ScreenState.Error>(state.cards.first { it.id == HomeCardId.COLLECTIONS }.state)
        assertIs<ScreenState.Content<HomeCard>>(state.cards.first { it.id == HomeCardId.WORK_QUEUE }.state)
        assertIs<ScreenState.Content<HomeCard>>(state.cards.first { it.id == HomeCardId.PORTFOLIO }.state)
    }

    @Test
    fun quickActionsAreTheSixStitchOptionsInOrder() = runTest(dispatcher) {
        // Stitch home: the same six tiles whatever the role — never client-owned account creates.
        val state = Fixture(fieldOfficer - "READ_COLLECTIONSHEET").settled()
        assertEquals(
            listOf(QuickAction.NEW_CLIENT, QuickAction.NEW_GROUP, QuickAction.NEW_CENTER, QuickAction.COLLECTION_SHEET, QuickAction.RUN_REPORTS, QuickAction.SEARCH, QuickAction.PATH_TRACKER),
            state.quickActions,
        )
    }

    @Test
    fun fineractDateIsDayMonthNameYear() {
        assertEquals("26 October 2026", fineractDate("2026-10-26"))
        assertEquals("05 January 2027", fineractDate("2027-01-05"))
    }

    @Test
    fun statusConstantsMatchFineract() {
        assertEquals(listOf(100, 200, 300), listOf(DashboardRepository.STATUS_SUBMITTED, DashboardRepository.STATUS_APPROVED, DashboardRepository.STATUS_ACTIVE))
    }
}

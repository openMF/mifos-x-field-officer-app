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

package kpt.feature.groups.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.screen.ScreenState
import kpt.core.data.testing.FakeRecentRecordsRepository
import kpt.core.model.client.Client
import kpt.core.model.group.Group
import kpt.core.model.group.GroupAccounts
import kpt.core.model.group.GroupWithAssociations
import kpt.core.model.loan.LoanAccount
import kpt.feature.groups.testing.FakeGroupRepository
import kpt.feature.groups.testing.group
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class GroupsDetailViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(
        repo: FakeGroupRepository,
        groupId: Int = 7,
    ) = GroupsDetailViewModel(repo, SavedStateHandle(mapOf("groupId" to groupId)), FakeRecentRecordsRepository())

    @Test
    fun theScreenShowsTheOneGroupItWasOpenedFor() = runTest(dispatcher) {
        val repo = FakeGroupRepository()
        val vm = viewModel(repo)
        repo.emit(group(1), group(7, name = "Target"), group(9))

        vm.group.test {
            assertIs<ScreenState.Loading>(awaitItem())
            val content = assertIs<ScreenState.Content<Group>>(awaitItem())
            assertEquals("Target", content.data.name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun aGroupOutsideTheCaseloadIsEmptyNotBlankFields() = runTest(dispatcher) {
        // The distinction that makes the screen honest: a Content holding null would render a
        // detail page of empty rows, which reads as "this group has no data" rather than "this
        // group is not yours".
        val repo = FakeGroupRepository()
        val vm = viewModel(repo)
        repo.emit(group(1), group(2))

        vm.group.test {
            assertIs<ScreenState.Loading>(awaitItem())
            assertIs<ScreenState.Empty>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun activateIsOfferedOnlyForAnInactiveGroup() = runTest(dispatcher) {
        val repo = FakeGroupRepository()
        val vm = viewModel(repo)

        vm.canActivate.test {
            assertFalse(awaitItem())
            repo.emit(group(7, active = true))
            expectNoEvents()

            repo.emit(group(7, active = false))
            assertTrue(awaitItem(), "an inactive group must offer Activate")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun aGroupWithAnUnknownStatusStillOffersActivation() = runTest(dispatcher) {
        // Fineract leaves `active` NULL for a pending group — the very group an officer opens this
        // screen to activate. Found on device: the screen printed "Inactive" and showed no button,
        // because the status text tested `!= true` and canActivate tested `== false`.
        val repo = FakeGroupRepository()
        val vm = viewModel(repo)
        vm.canActivate.test {
            assertFalse(awaitItem())
            repo.emit(group(7, active = null))
            assertTrue(awaitItem(), "a null status reads as Inactive and must be activatable")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun tappingActivateOpensTheSharedActivateScreenForThisGroup() = runTest(dispatcher) {
        // Activation moved OUT of this ViewModel. It used to write inline with an all-null
        // `ActivatePayload`, assuming the server defaulted the activation date; Fineract's command
        // takes `activationDate` + `dateFormat` + `locale`, and the original has always collected
        // the date on a dedicated screen (`feature/activate`, shared with client and centre). So
        // this screen navigates, and the in-flight guard / offline reporting live with the write.
        val repo = FakeGroupRepository()
        val vm = viewModel(repo)
        repo.emit(group(7, active = false))
        testScheduler.advanceUntilIdle()

        vm.eventFlow.test {
            vm.trySendAction(GroupsDetailAction.ActivateClicked)
            assertEquals(GroupsDetailEvent.Activate(groupId = 7), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun theActivateButtonSendsNoRequestFromThisScreen() = runTest(dispatcher) {
        val repo = FakeGroupRepository()
        val vm = viewModel(repo)
        repo.emit(group(7, active = false))
        testScheduler.advanceUntilIdle()

        // The tap must NOT post from here — doing so would activate with a date the officer was
        // never asked for, which is the defect this move fixed. The ViewModel holds no command
        // repository at all, so the only thing the tap can do is navigate.
        vm.eventFlow.test {
            vm.trySendAction(GroupsDetailAction.ActivateClicked)
            assertEquals(GroupsDetailEvent.Activate(groupId = 7), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun theGroupsClientsAndAccountsComeFromTheirOwnReads() = runTest(dispatcher) {
        val repo = FakeGroupRepository()
        val vm = viewModel(repo)
        repo.membersState.value = ScreenState.Content(
            GroupWithAssociations(id = 7, clientMembers = listOf(Client(id = 3, displayName = "Asha"))),
        )
        repo.accountsState.value = ScreenState.Content(
            GroupAccounts(loanAccounts = listOf(LoanAccount(id = 11, productName = "Group loan"))),
        )

        vm.members.test {
            val content = assertIs<ScreenState.Content<List<Client>>>(expectMostRecentItemOrLoading())
            assertEquals(listOf("Asha"), content.data.map { it.displayName })
            cancelAndIgnoreRemainingEvents()
        }
        vm.accounts.test {
            val content = assertIs<ScreenState.Content<GroupAccounts>>(expectMostRecentItemOrLoading())
            assertEquals(listOf(11), content.data.loanAccounts.map { it.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun eachRowAndTheNotesEntryOpenTheirDestination() = runTest(dispatcher) {
        val vm = viewModel(FakeGroupRepository())
        vm.eventFlow.test {
            vm.trySendAction(GroupsDetailAction.NotesClicked)
            assertEquals(GroupsDetailEvent.OpenNotes(7), awaitItem())
            vm.trySendAction(GroupsDetailAction.LoanClicked(11))
            assertEquals(GroupsDetailEvent.OpenLoan(11), awaitItem())
            vm.trySendAction(GroupsDetailAction.SavingsClicked(12, "recurring_deposit"))
            assertEquals(GroupsDetailEvent.OpenSavings(12, "recurring_deposit"), awaitItem())
            vm.trySendAction(GroupsDetailAction.ClientClicked(3))
            assertEquals(GroupsDetailEvent.OpenClient(3), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    /** Skips the initial Loading if a later state is already there. */
    private suspend fun <T> ReceiveTurbine<ScreenState<T>>.expectMostRecentItemOrLoading(): ScreenState<T> {
        var item = awaitItem()
        while (item is ScreenState.Loading) item = awaitItem()
        return item
    }
}

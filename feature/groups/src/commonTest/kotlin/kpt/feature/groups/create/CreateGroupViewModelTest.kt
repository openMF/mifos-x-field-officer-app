/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(kpt.core.base.store.screen.ExperimentalScreenDataStreamTestingApi::class)

package kpt.feature.groups.create

import app.cash.turbine.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.base.store.submit.SubmitOutboxStatus
import kpt.core.data.group.GroupCommandRepository
import kpt.core.data.office.OfficeRepository
import kpt.core.data.testing.ExperimentalDataTestingApi
import kpt.core.data.testing.FakeFormDraftRepository
import kpt.core.model.group.GroupPayload
import kpt.core.model.objects.clients.ActivatePayload
import kpt.core.model.objects.responses.SaveResponse
import kpt.core.model.office.Office
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** A created group opens its detail — the original's `onGroupCreated`; that needs the server's id. */
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalDataTestingApi::class)
class CreateGroupViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val offices = object : OfficeRepository {
        override fun officesStream(scope: CoroutineScope) =
            screenDataStreamForTesting(
                flowOf<ScreenState<List<Office>>>(ScreenState.Content(listOf(Office(id = 1, name = "CENTRAL")))),
            )
    }

    private fun commands(result: MutationResult<SaveResponse>) = object : GroupCommandRepository {
        override suspend fun createGroup(groupPayload: GroupPayload?) = result
        override suspend fun activateGroup(groupId: Int, activatePayload: ActivatePayload) = error("unused")
    }

    private suspend fun createdEvent(result: MutationResult<SaveResponse>): CreateGroupEvent {
        val vm = CreateGroupViewModel(offices, commands(result), FakeFormDraftRepository())
        vm.actionChannel.trySend(CreateGroupAction.UpdateName("FOTest Group"))
        vm.actionChannel.trySend(CreateGroupAction.UpdateOffice(OfficeOption(1, "CENTRAL")))
        var event: CreateGroupEvent? = null
        vm.eventFlow.test {
            vm.actionChannel.trySend(CreateGroupAction.Submit)
            event = awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
        return event!!
    }

    @Test
    fun anOnlineCreateCarriesTheServersGroupId() = runTest(dispatcher) {
        val event = createdEvent(MutationResult.Applied(SaveResponse(groupId = 30, resourceId = 30), synced = true))
        assertEquals(CreateGroupEvent.Created(synced = true, groupId = 30), event)
    }

    @Test
    fun aQueuedCreateHasNoIdToOpen() = runTest(dispatcher) {
        val event = createdEvent(MutationResult.Applied(SaveResponse(), synced = false))
        assertEquals(CreateGroupEvent.Created(synced = false, groupId = null), event)
    }

    // ---- drafts ----

    private fun TestScope.submitted(
        result: MutationResult<SaveResponse>,
        drafts: FakeFormDraftRepository,
    ): CreateGroupViewModel {
        val vm = CreateGroupViewModel(offices, commands(result), drafts)
        vm.actionChannel.trySend(CreateGroupAction.UpdateName("FOTest Group"))
        vm.actionChannel.trySend(CreateGroupAction.UpdateOffice(OfficeOption(1, "CENTRAL")))
        vm.actionChannel.trySend(CreateGroupAction.Submit)
        advanceUntilIdle()
        return vm
    }

    @Test
    fun anOfflineCreateIsKeptAsAPendingDraftWithoutAsking() = runTest(dispatcher) {
        val drafts = FakeFormDraftRepository()
        val vm = submitted(MutationResult.Blocked(BlockReason.OFFLINE), drafts)

        val kept = drafts.saved.value.single()
        assertEquals(SubmitOutboxStatus.PENDING, kept.status)
        assertEquals("FOTest Group", kept.draft.title)
        assertTrue(vm.stateFlow.value.draftSaved)
    }

    @Test
    fun aRefusedCreateIsKeptOnlyWhenAskedAndCarriesTheServersReason() = runTest(dispatcher) {
        val drafts = FakeFormDraftRepository()
        val vm = submitted(MutationResult.Failed(RuntimeException("rejected"), rolledBack = false), drafts)
        assertEquals(emptyList(), drafts.saved.value, "not kept until the officer asks")

        vm.actionChannel.trySend(CreateGroupAction.SaveDraft)
        advanceUntilIdle()

        val kept = drafts.saved.value.single()
        assertEquals(SubmitOutboxStatus.FAILED, kept.status)
        assertEquals("rejected", kept.errorMessage)
    }

    @Test
    fun resumingADraftRestoresTheFormAsLeft() = runTest(dispatcher) {
        val drafts = FakeFormDraftRepository()
        submitted(MutationResult.Blocked(BlockReason.OFFLINE), drafts)

        val reopened = CreateGroupViewModel(offices, commands(MutationResult.Applied(SaveResponse(), true)), drafts)
        advanceUntilIdle()
        assertEquals("FOTest Group", reopened.offeredDraft.value?.draft?.title)

        reopened.actionChannel.trySend(CreateGroupAction.ResumeDraft)
        advanceUntilIdle()

        assertEquals("FOTest Group", reopened.stateFlow.value.name)
        assertEquals(OfficeOption(1, "CENTRAL"), reopened.stateFlow.value.office)
        assertEquals(null, reopened.offeredDraft.value, "the banner goes once resumed")
    }

    @Test
    fun aSuccessfulCreateClearsTheDraft() = runTest(dispatcher) {
        val drafts = FakeFormDraftRepository()
        submitted(MutationResult.Blocked(BlockReason.OFFLINE), drafts)
        submitted(MutationResult.Applied(SaveResponse(groupId = 30), synced = true), drafts)
        assertEquals(emptyList(), drafts.saved.value)
    }
}

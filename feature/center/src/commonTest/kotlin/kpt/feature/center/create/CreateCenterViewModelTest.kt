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

package kpt.feature.center.create

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.base.store.submit.SubmitOutboxStatus
import kpt.core.data.center.CenterCommandRepository
import kpt.core.data.office.OfficeRepository
import kpt.core.data.testing.ExperimentalDataTestingApi
import kpt.core.data.testing.FakeFormDraftRepository
import kpt.core.data.testing.fineractRejection
import kpt.core.model.center.CenterPayload
import kpt.core.model.center.PostCentersCenterIdRequest
import kpt.core.model.collectionsheet.payload.CollectionSheetPayload
import kpt.core.model.objects.clients.ActivatePayload
import kpt.core.model.objects.responses.SaveResponse
import kpt.core.model.office.Office
import kpt.core.model.utils.ApiDateFormatter
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalDataTestingApi::class)
class CreateCenterViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val offices = object : OfficeRepository {
        override fun officesStream(scope: CoroutineScope) =
            screenDataStreamForTesting(
                flowOf<ScreenState<List<Office>>>(ScreenState.Content(listOf(Office(id = 1, name = "Head Office")))),
            )
    }

    private class FakeCommands(private val result: () -> MutationResult<SaveResponse>) : CenterCommandRepository {
        val created = mutableListOf<CenterPayload?>()
        override suspend fun createCenter(centerPayload: CenterPayload?): MutationResult<SaveResponse> {
            created += centerPayload
            return result()
        }
        override suspend fun activate2(
            centerId: Long,
            postCentersCenterIdRequest: PostCentersCenterIdRequest,
            command: String?,
        ) = error("unused")
        override suspend fun saveCollectionSheet(
            centerId: Int,
            collectionSheetPayload: CollectionSheetPayload?,
        ) = error("unused")
        override suspend fun saveCollectionSheetAsync(
            centerId: Int,
            collectionSheetPayload: CollectionSheetPayload?,
        ) = error("unused")
        override suspend fun activateCenter(centerId: Int, activatePayload: ActivatePayload?) = error("unused")
    }

    private val drafts = FakeFormDraftRepository()

    private fun vm(
        result: () -> MutationResult<SaveResponse> = { MutationResult.Applied(SaveResponse(), synced = true) },
    ) =
        FakeCommands(result).let { it to CreateCenterViewModel(offices, it, drafts) }

    private fun TestScope.submitNorthMarket(vm: CreateCenterViewModel) {
        vm.trySendAction(CreateCenterAction.NameChanged("North Market"))
        vm.trySendAction(CreateCenterAction.OfficeSelected(1, "Head Office"))
        vm.trySendAction(CreateCenterAction.Submit)
        runCurrent()
    }

    @Test
    fun anOfflineCreateIsKeptAsAPendingDraftAndSaysSo() = runTest(dispatcher) {
        val (_, vm) = vm { MutationResult.Blocked(BlockReason.OFFLINE) }
        submitNorthMarket(vm)

        assertEquals(SubmitOutboxStatus.PENDING, drafts.saved.value.single().status)
        assertTrue(vm.stateFlow.value.draftSaved)
    }

    @Test
    fun aRefusedCreateIsKeptOnSaveAsDraftWithTheServersReason() = runTest(dispatcher) {
        val (_, vm) = vm { MutationResult.Failed(fineractRejection("Center name already exists"), rolledBack = false) }
        submitNorthMarket(vm)
        assertTrue(drafts.saved.value.isEmpty(), "not kept until asked")

        vm.trySendAction(CreateCenterAction.SaveDraft)
        runCurrent()

        val kept = drafts.saved.value.single()
        assertEquals(SubmitOutboxStatus.FAILED, kept.status)
        assertEquals("Center name already exists", kept.errorMessage)
    }

    @Test
    fun aResumedDraftRestoresNameAndOffice() = runTest(dispatcher) {
        submitNorthMarket(vm { MutationResult.Blocked(BlockReason.OFFLINE) }.second)
        val (_, reopened) = vm()
        runCurrent()

        reopened.trySendAction(CreateCenterAction.ResumeDraft)
        runCurrent()

        assertEquals("North Market", reopened.stateFlow.value.name)
        assertEquals("Head Office", reopened.stateFlow.value.officeName)
    }

    @Test
    fun theOriginalsNameRulesHold() {
        assertEquals(CenterNameError.EMPTY, validateCenterName("  "))
        assertEquals(CenterNameError.TOO_SHORT, validateCenterName("Abc"))
        assertEquals(CenterNameError.INVALID_CHARACTERS, validateCenterName("Center 7"))
        assertNull(validateCenterName("North Market"))
    }

    @Test
    fun anInvalidFormShowsWhyAndSendsNothing() = runTest(dispatcher) {
        val (commands, vm) = vm()
        vm.trySendAction(CreateCenterAction.NameChanged("Ab"))
        vm.trySendAction(CreateCenterAction.Submit)
        runCurrent()
        // The original's Submit did nothing and said nothing (its Toasts were commented out).
        assertEquals(CenterNameError.TOO_SHORT, vm.stateFlow.value.nameError)
        assertTrue(vm.stateFlow.value.officeMissing)
        assertTrue(commands.created.isEmpty())
    }

    @Test
    fun aValidFormSendsThePayloadWithItsDateFormat() = runTest(dispatcher) {
        val (commands, vm) = vm()
        vm.trySendAction(CreateCenterAction.NameChanged(" North Market "))
        vm.trySendAction(CreateCenterAction.OfficeSelected(1, "Head Office"))
        vm.trySendAction(CreateCenterAction.ActiveToggled(true))
        vm.trySendAction(CreateCenterAction.DatePicked(DAY))
        vm.trySendAction(CreateCenterAction.Submit)
        runCurrent()

        val sent = commands.created.single()!!
        assertEquals("North Market", sent.name)
        assertEquals(1, sent.officeId)
        assertEquals(true, sent.active)
        assertEquals(ApiDateFormatter.formatForApi(DAY), sent.activationDate)
        assertEquals(ApiDateFormatter.DATE_FORMAT, sent.dateFormat)
        assertEquals(CreateCenterState.Dialog.Created, vm.stateFlow.value.dialog)
    }

    @Test
    fun anInactiveCenterSendsNoActivationDate() = runTest(dispatcher) {
        val (commands, vm) = vm()
        vm.trySendAction(CreateCenterAction.NameChanged("North Market"))
        vm.trySendAction(CreateCenterAction.OfficeSelected(1, "Head Office"))
        vm.trySendAction(CreateCenterAction.Submit)
        runCurrent()
        assertNull(commands.created.single()!!.activationDate)
    }

    @Test
    fun failuresSayWhy() = runTest(dispatcher) {
        val exists = "Center with name North Market already exists"
        val (_, rejected) = vm { MutationResult.Failed(fineractRejection(exists), rolledBack = false) }
        rejected.trySendAction(CreateCenterAction.NameChanged("North Market"))
        rejected.trySendAction(CreateCenterAction.OfficeSelected(1, "Head Office"))
        rejected.trySendAction(CreateCenterAction.Submit)
        runCurrent()
        assertEquals(CreateCenterState.Dialog.Failed(exists), rejected.stateFlow.value.dialog)

        val (_, offline) = vm { MutationResult.Blocked(BlockReason.OFFLINE) }
        offline.trySendAction(CreateCenterAction.NameChanged("North Market"))
        offline.trySendAction(CreateCenterAction.OfficeSelected(1, "Head Office"))
        offline.trySendAction(CreateCenterAction.Submit)
        runCurrent()
        assertEquals(CreateCenterState.Dialog.Offline, offline.stateFlow.value.dialog)
    }

    private companion object {
        /** 2026-10-23T12:00Z. */
        const val DAY = 1_792_756_800_000L
    }
}

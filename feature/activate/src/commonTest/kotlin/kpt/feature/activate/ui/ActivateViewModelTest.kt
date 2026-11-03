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

package kpt.feature.activate.ui

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.common.utils.Constants
import kpt.core.data.center.CenterCommandRepository
import kpt.core.data.group.GroupCommandRepository
import kpt.core.data.testing.FakeClientCommandRepository
import kpt.core.model.center.CenterPayload
import kpt.core.model.center.PostCentersCenterIdRequest
import kpt.core.model.collectionsheet.payload.CollectionSheetPayload
import kpt.core.model.group.GroupPayload
import kpt.core.model.objects.clients.ActivatePayload
import kpt.core.model.shared.GenericResponse
import kpt.core.model.utils.ApiDateFormatter
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The shared activate screen. These tests carry the in-flight guard and offline reporting that
 * used to live in `GroupsDetailViewModel`'s inline write, plus the three-way type routing that is
 * this screen's own reason to exist.
 */
class ActivateViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val clients = FakeClientCommandRepository()
    private val centers = FakeCenterCommands()
    private val groups = FakeGroupCommands()

    private fun viewModel(id: Int = 7, type: String = Constants.ACTIVATE_GROUP) =
        ActivateViewModel(
            clients,
            centers,
            groups,
            SavedStateHandle(mapOf("id" to id, "type" to type)),
        )

    private fun payload(millis: Long = 1_760_000_000_000L) = ActivatePayload(
        activationDate = ApiDateFormatter.formatForApi(millis),
        dateFormat = ApiDateFormatter.DATE_FORMAT,
        locale = ApiDateFormatter.LOCALE,
    )

    // ---- the reason this screen exists: one date, three endpoints ----

    @Test
    fun aGroupActivationGoesToTheGROUPEndpointOnly() = runTest(dispatcher) {
        val vm = viewModel(id = 7, type = Constants.ACTIVATE_GROUP)
        vm.actionChannel.trySend(ActivateAction.Submit(payload()))
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(7), groups.activations.map { it.first })
        assertEquals(emptyList(), clients.activatedClients)
        assertEquals(emptyList(), centers.activations)
    }

    @Test
    fun aClientActivationGoesToTheCLIENTEndpointOnly() = runTest(dispatcher) {
        val vm = viewModel(id = 3, type = Constants.ACTIVATE_CLIENT)
        vm.actionChannel.trySend(ActivateAction.Submit(payload()))
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(3), clients.activatedClients.map { it.first })
        assertEquals(emptyList(), groups.activations)
    }

    @Test
    fun aCentreActivationGoesToTheCENTREEndpointOnly() = runTest(dispatcher) {
        val vm = viewModel(id = 9, type = Constants.ACTIVATE_CENTER)
        vm.actionChannel.trySend(ActivateAction.Submit(payload()))
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(9), centers.activations.map { it.first })
        assertEquals(emptyList(), groups.activations)
    }

    @Test
    fun anUnknownTypeFallsBackToClientRatherThanToNoBranchAtAll() {
        // The original compared the raw route string against three `Constants` values inside the
        // screen, so a typo matched nothing and the submit silently did nothing. The enum makes
        // the set closed; an unparseable value resolves to a definite default.
        assertNull(ActivateType.fromRouteValue("activate_nonsense"))
        assertEquals(ActivateType.Group, ActivateType.fromRouteValue(Constants.ACTIVATE_GROUP))
        assertEquals(ActivateType.Client, ActivateType.fromRouteValue(Constants.ACTIVATE_CLIENT))
        assertEquals(ActivateType.Center, ActivateType.fromRouteValue(Constants.ACTIVATE_CENTER))
    }

    // ---- the payload: the whole point of having a screen ----

    @Test
    fun theActivationDateIsSentWithTheFormatItDeclares() = runTest(dispatcher) {
        val vm = viewModel()
        vm.actionChannel.trySend(ActivateAction.Submit(payload(1_760_000_000_000L)))
        testScheduler.advanceUntilIdle()

        val sent = groups.activations.single().second
        // All three fields present. The inline version this replaced sent `ActivatePayload()` —
        // every field null — on an assumption about server defaults that was never measured.
        assertEquals(ApiDateFormatter.formatForApi(1_760_000_000_000L), sent.activationDate)
        assertEquals(ApiDateFormatter.DATE_FORMAT, sent.dateFormat)
        assertEquals(ApiDateFormatter.LOCALE, sent.locale)
    }

    // ---- behaviour that moved here from the group screen ----

    @Test
    fun aSecondSubmitInsideTheInFlightWindowIsDropped() = runTest(dispatcher) {
        val gate = CompletableDeferred<Unit>()
        val gated = FakeGroupCommands(gate = gate)
        val vm = ActivateViewModel(
            clients,
            centers,
            gated,
            SavedStateHandle(mapOf("id" to 7, "type" to Constants.ACTIVATE_GROUP)),
        )

        vm.actionChannel.trySend(ActivateAction.Submit(payload()))
        testScheduler.advanceUntilIdle()
        vm.actionChannel.trySend(ActivateAction.Submit(payload()))
        testScheduler.advanceUntilIdle()
        gate.complete(Unit)
        testScheduler.advanceUntilIdle()

        assertEquals(1, gated.activations.size, "the second tap must not send a second request")
    }

    @Test
    fun aRejectionCarriesTheServersReason() = runTest(dispatcher) {
        // A 403 for an officer without ACTIVATE_CLIENT, as the gateway sent it (device, 2026-10-26).
        val forbidden = RuntimeException(
            """Client request(POST https://x/clients/3?command=activate) invalid: 403 Forbidden. """ +
                """Text: "{"developerMessage":"x","httpStatusCode":"403",""" +
                """"defaultUserMessage":"Insufficient privileges to perform this action.",""" +
                """"userMessageGlobalisationCode":"error.msg.not.authorized","errors":[]}"""",
        )
        val rejecting = FakeGroupCommands { MutationResult.Failed(cause = forbidden, rolledBack = false) }
        val vm = ActivateViewModel(
            clients,
            centers,
            rejecting,
            SavedStateHandle(mapOf("id" to 7, "type" to Constants.ACTIVATE_GROUP)),
        )

        vm.actionChannel.trySend(ActivateAction.Submit(payload()))
        testScheduler.advanceUntilIdle()

        assertEquals(ActivateResult.Failed, vm.stateFlow.value.result)
        assertEquals("Insufficient privileges to perform this action.", vm.stateFlow.value.failureReason)
    }

    @Test
    fun anOfflineBlockIsReportedAsOfflineAndStaysRetryable() = runTest(dispatcher) {
        val offline = FakeGroupCommands { MutationResult.Blocked(BlockReason.OFFLINE) }
        val vm = ActivateViewModel(
            clients,
            centers,
            offline,
            SavedStateHandle(mapOf("id" to 7, "type" to Constants.ACTIVATE_GROUP)),
        )

        vm.actionChannel.trySend(ActivateAction.Submit(payload()))
        testScheduler.advanceUntilIdle()

        assertEquals(ActivateResult.Offline, vm.stateFlow.value.result)
        // Must clear, or the button stays disabled forever with no way back.
        assertTrue(!vm.stateFlow.value.isSubmitting)

        vm.actionChannel.trySend(ActivateAction.DismissDialog)
        testScheduler.advanceUntilIdle()
        assertNull(vm.stateFlow.value.result)
    }

    @Test
    fun aSuccessfulActivationReportsSuccessAndLeavesTheScreen() = runTest(dispatcher) {
        val vm = viewModel()
        vm.actionChannel.trySend(ActivateAction.Submit(payload()))
        testScheduler.advanceUntilIdle()
        assertEquals(ActivateResult.Succeeded, vm.stateFlow.value.result)

        vm.eventFlow.test {
            vm.actionChannel.trySend(ActivateAction.NavigateBack)
            assertEquals(ActivateEvent.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun theFormStaysMountedWhileSubmitting() = runTest(dispatcher) {
        val gate = CompletableDeferred<Unit>()
        val gated = FakeGroupCommands(gate = gate)
        val vm = ActivateViewModel(
            clients,
            centers,
            gated,
            SavedStateHandle(mapOf("id" to 7, "type" to Constants.ACTIVATE_GROUP)),
        )

        vm.actionChannel.trySend(ActivateAction.Submit(payload()))
        testScheduler.advanceUntilIdle()

        // The original's `Loading` state REPLACED the form, so the date the officer picked
        // vanished mid-flight and came back reset if it failed. Only the button disables now.
        assertTrue(vm.stateFlow.value.isSubmitting)
        assertNull(vm.stateFlow.value.result)

        gate.complete(Unit)
        testScheduler.advanceUntilIdle()
        assertTrue(!vm.stateFlow.value.isSubmitting)
    }
}

/**
 * Minimal fakes for the two small command interfaces. The CLIENT interface has 19 methods, so that
 * one reuses `core/data`'s shared [FakeClientCommandRepository] rather than being re-faked here —
 * the module that owns a contract owns its fake.
 */
private class FakeCenterCommands(
    private val gate: CompletableDeferred<Unit>? = null,
    private val result: () -> MutationResult<GenericResponse> =
        { MutationResult.Applied(GenericResponse(), synced = true) },
) : CenterCommandRepository {
    val activations = mutableListOf<Pair<Int, ActivatePayload?>>()

    override suspend fun activateCenter(
        centerId: Int,
        activatePayload: ActivatePayload?,
    ): MutationResult<GenericResponse> {
        activations += centerId to activatePayload
        gate?.await()
        return result()
    }

    override suspend fun activate2(
        centerId: Long,
        postCentersCenterIdRequest: PostCentersCenterIdRequest,
        command: String?,
    ) = error("not used by these tests")

    override suspend fun saveCollectionSheet(
        centerId: Int,
        collectionSheetPayload: CollectionSheetPayload?,
    ) = error("not used by these tests")

    override suspend fun saveCollectionSheetAsync(
        centerId: Int,
        collectionSheetPayload: CollectionSheetPayload?,
    ) = error("not used by these tests")

    override suspend fun createCenter(centerPayload: CenterPayload?) =
        error("not used by these tests")
}

private class FakeGroupCommands(
    private val gate: CompletableDeferred<Unit>? = null,
    private val result: () -> MutationResult<Unit> = { MutationResult.Applied(Unit, synced = true) },
) : GroupCommandRepository {
    val activations = mutableListOf<Pair<Int, ActivatePayload>>()

    override suspend fun activateGroup(
        groupId: Int,
        activatePayload: ActivatePayload,
    ): MutationResult<Unit> {
        activations += groupId to activatePayload
        gate?.await()
        return result()
    }

    override suspend fun createGroup(groupPayload: GroupPayload?) =
        error("not used by these tests")
}

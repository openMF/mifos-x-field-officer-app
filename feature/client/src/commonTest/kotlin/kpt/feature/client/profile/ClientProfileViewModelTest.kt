/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(
    kpt.core.base.store.screen.ExperimentalScreenDataStreamTestingApi::class,
    kpt.core.data.testing.ExperimentalDataTestingApi::class,
)

package kpt.feature.client.profile

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.screen.ScreenState
import kpt.core.data.testing.FakeClientRepository
import kpt.core.data.testing.FakeRecentRecordsRepository
import kpt.core.model.client.Client
import kpt.feature.client.imageCodecAvailable
import kpt.feature.client.profile.components.ClientProfileActionItem
import kpt.feature.client.profile.components.clientsActionItems
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Locks the client PROFILE screen — the one the client list opens.
 *
 * An earlier pass of this port replaced it with a rebuilt "detail" screen (Scaffold, TopAppBar, raw
 * identity text, invented Charges/Notes rows, an accounts section). These tests pin the original's
 * shape instead: one client read, one photo read, seven declared actions of which six render.
 */
class ClientProfileViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val clientState = MutableStateFlow<ScreenState<Client>>(ScreenState.Loading)

    private fun repository(image: String? = null) =
        FakeClientRepository(client = clientState, image = image)

    private fun viewModel(
        repo: FakeClientRepository = repository(),
        clientId: Int = 42,
    ) = ClientProfileViewModel(repo, SavedStateHandle(mapOf("clientId" to clientId)), FakeRecentRecordsRepository())

    private fun client(id: Int = 42) = Client(
        id = id,
        displayName = "Asha Devi",
        accountNo = "000000042",
        officeName = "Branch A",
        groupName = "Finance",
        groupId = 7,
    )

    @Test
    fun theRouteArgumentReachesBothReads() = runTest(dispatcher) {
        val repo = repository()
        viewModel(repo, clientId = 7)
        testScheduler.advanceUntilIdle()

        assertEquals(listOf<Any>(7), repo.requestedKeys["client"]?.toList())
        // The photo is a SECOND read of the same client — a `0` here is the SavedStateHandle
        // default bug, and it would quietly show somebody else's avatar.
        assertEquals(listOf(7), repo.imageRequests.toList())
    }

    @Test
    fun theClientIsReadAsAStream() = runTest(dispatcher) {
        val vm = viewModel()
        clientState.value = ScreenState.Content(client())

        vm.client.test {
            assertIs<ScreenState.Loading>(awaitItem())
            val content = assertIs<ScreenState.Content<Client>>(awaitItem())
            assertEquals("Asha Devi", content.data.displayName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun aFailedClientReadStaysAReadStateRatherThanBecomingADialog() = runTest(dispatcher) {
        val vm = viewModel()
        clientState.value = ScreenState.Error(RuntimeException("boom"))
        testScheduler.advanceUntilIdle()

        // The recents collector in init{} subscribes to [client], so the read state is already
        // current by the time the screen collects — no Loading in front of it.
        vm.client.test {
            assertIs<ScreenState.Error>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun aMissingPhotoIsNotAnErrorState() = runTest(dispatcher) {
        // Fineract 404s when no photo was ever uploaded, which is the common case. The repository
        // returns null and the avatar placeholder IS the empty state — it must never turn the
        // profile into an error screen.
        val vm = viewModel(repository(image = null))
        testScheduler.advanceUntilIdle()
        assertNull(vm.stateFlow.value.profileImage)
    }

    @Test
    fun aPhotoArrivesWithTheDataUrlPrefixStillOnIt() = runTest(dispatcher) {
        if (!imageCodecAvailable()) return@runTest
        // Fineract sends `data:image/png;base64,<payload>`. The prefix must be stripped EXACTLY
        // ONCE: the repository leaves it on (`bodyAsText()` verbatim) and `imageToByteArray` does
        // the `substringAfter(",")`. Stripping it in both places would cut into the payload of any
        // photo whose base64 contains a comma.
        //
        // A real 1x1 PNG, because the decode also runs `ImageUtil.compressImage` — arbitrary bytes
        // decode to base64 fine and then come back EMPTY from the image codec, which is how a
        // first version of this test asserted the wrong thing and failed.
        val vm = viewModel(repository(image = "data:image/png;base64,$ONE_PIXEL_PNG"))
        testScheduler.advanceUntilIdle()

        val bytes = vm.stateFlow.value.profileImage
        assertTrue(bytes != null && bytes.isNotEmpty(), "expected decoded image bytes, got $bytes")
    }

    @Test
    fun aPhotoWithNoDataUrlPrefixDecodesToo() = runTest(dispatcher) {
        if (!imageCodecAvailable()) return@runTest
        // A deployment that returns bare base64 must still work — `substringAfter(",")` returns
        // the whole string when there is no comma.
        val vm = viewModel(repository(image = ONE_PIXEL_PNG))
        testScheduler.advanceUntilIdle()
        assertTrue(vm.stateFlow.value.profileImage?.isNotEmpty() == true)
    }

    @Test
    fun retryReReadsBothTheClientAndThePhoto() = runTest(dispatcher) {
        val repo = repository()
        val vm = viewModel(repo)
        testScheduler.advanceUntilIdle()
        assertEquals(1, repo.imageRequests.size)

        repo.refreshesFor("client").test {
            vm.actionChannel.trySend(ClientProfileAction.OnRetry)
            testScheduler.advanceUntilIdle()
            expectMostRecentItem()
            cancelAndIgnoreRemainingEvents()
        }
        // The original's retry re-ran BOTH loads. A photo that failed on a dead connection would
        // otherwise stay missing until the screen was left and re-entered.
        assertEquals(2, repo.imageRequests.size)
    }

    @Test
    fun actionClicksCarryTheClientIdRatherThanLettingTheScreenReadItMidLoad() = runTest(dispatcher) {
        val vm = viewModel(clientId = 42)
        vm.eventFlow.test {
            vm.actionChannel.trySend(ClientProfileAction.OnActionClick(ClientProfileActionItem.Notes))
            val event = assertIs<ClientProfileEvent.OnActionClick>(awaitItem())
            // The original read `state.client?.id ?: -1` at the CALL SITE, so every one of its
            // seven branches could navigate to client -1 while the load was still in flight.
            assertEquals(42, event.clientId)
            assertEquals(ClientProfileActionItem.Notes, event.action)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun theProfileCardTapCarriesTheClientIdToo() = runTest(dispatcher) {
        val vm = viewModel(clientId = 42)
        vm.eventFlow.test {
            vm.actionChannel.trySend(ClientProfileAction.NavigateToClientDetailsScreen)
            val event = assertIs<ClientProfileEvent.NavigateToClientDetailsScreen>(awaitItem())
            assertEquals(42, event.clientId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun theGroupChipCarriesTheGroupIdNotTheClientId() = runTest(dispatcher) {
        val vm = viewModel(clientId = 42)
        vm.eventFlow.test {
            vm.actionChannel.trySend(ClientProfileAction.OnGroupClick(groupId = 7))
            assertEquals(ClientProfileEvent.NavigateToGroupDetails(7), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun theActionListIsTheOriginalsSixInTheOriginalsOrder() {
        // Order is load-bearing: it is the order the rows render in. FamilyMembers is DECLARED by
        // the sealed class and deliberately absent from the list — the original left it out too,
        // and its event branch is an empty `{}`, so including it would draw a dead row.
        assertEquals(
            listOf(
                ClientProfileActionItem.General,
                ClientProfileActionItem.Address,
                ClientProfileActionItem.Identifiers,
                ClientProfileActionItem.Documents,
                ClientProfileActionItem.Notes,
                ClientProfileActionItem.PinpointLocation,
            ),
            clientsActionItems.toList(),
        )
        assertTrue(ClientProfileActionItem.FamilyMembers !in clientsActionItems)
    }

    @Test
    fun theStateComparesThePhotoByContentNotByIdentity() {
        // A ByteArray in a data class compares by IDENTITY, so two decodes of the same photo would
        // read as different state and recompose the whole screen on every refresh.
        val a = ClientProfileState(profileImage = byteArrayOf(1, 2, 3))
        val b = ClientProfileState(profileImage = byteArrayOf(1, 2, 3))
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    private companion object {
        /**
         * A valid 1x1 red PNG, base64.
         *
         * Real image bytes, because the decode runs through `ImageUtil.compressImage` — an actual
         * image codec. Arbitrary bytes base64-decode fine and then come back EMPTY (desktop) or
         * null, which is how two earlier versions of this fixture made the test assert the wrong
         * thing. Verified by round-tripping it through `ImageIO.read` + `write`.
         */
        const val ONE_PIXEL_PNG =
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAIAAACQd1PeAAAADElEQVR4nGP4z8AAAAMBAQDJ/pLvAAAAAElFTkSuQmCC"
    }
}

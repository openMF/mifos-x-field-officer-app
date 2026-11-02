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

package kpt.feature.datatable.rows

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kpt.core.base.store.mutation.BlockReason
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.data.datatable.DataTableCommandRepository
import kpt.core.data.datatable.DataTableRepository
import kpt.core.model.objects.users.UserLocation
import kpt.core.model.shared.GenericResponse
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The JSON→rows mapping is the substance here: it is what the original did inside its composition
 * body, and it is where that screen lost the tenant's data.
 */
class DataTableRowsTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private fun json(raw: String): JsonArray = Json.parseToJsonElement(raw).jsonArray

    // ---- the mapping ----

    @Test
    fun everyTenantColumnReachesTheRowNotJustTwo() {
        // THE REGRESSION. The original kept only `client_id` and `id` and discarded the rest, so a
        // datatable — whose entire purpose is the tenant's own columns — showed none of them.
        val rows = json(
            """[{"id":3,"client_id":42,"Blood Group":"O+","Height":172.5}]""",
        ).toRows()

        val row = rows.single()
        assertEquals(3, row.id)
        assertEquals(
            listOf("id", "client_id", "Blood Group", "Height"),
            row.cells.map { it.first },
        )
        assertEquals("O+", row.cells.first { it.first == "Blood Group" }.second)
    }

    @Test
    fun aStringValueIsNotReQuoted() {
        // The original used `toString()` on the JsonElement, which rendered a text column as
        // `"O+"` — with the quotes — in the UI.
        val row = json("""[{"Blood Group":"O+"}]""").toRows().single()
        assertEquals("O+", row.cells.single().second)
    }

    @Test
    fun theDeleteTargetIsTheROWSIdNotTheEntitysId() {
        // The original accepted `client_id` as the delete target when `id` was absent
        // (`dataItem.clientId?.toInt() ?: dataItem.id?.toInt() ?: 0`) — deleting by the ENTITY's id
        // would address the wrong row, or row 0.
        val row = json("""[{"client_id":42,"Notes":"x"}]""").toRows().single()
        assertNull(row.id, "a row with no id of its own must not offer a delete target")
    }

    @Test
    fun aNonObjectEntryIsSkippedRatherThanCrashing() {
        // The shape is tenant-defined; the original did `jsonElement.jsonObject` unguarded, which
        // throws on anything that is not an object.
        val rows = json("""[{"id":1},"unexpected",5]""").toRows()
        assertEquals(1, rows.size)
    }

    @Test
    fun anEmptyArrayMapsToNoRows() {
        assertEquals(emptyList(), json("[]").toRows())
    }

    // ---- the ViewModel ----

    private val rowsState = MutableStateFlow<ScreenState<JsonArray>>(ScreenState.Loading)
    private val refreshes = MutableSharedFlow<Unit>(extraBufferCapacity = 8)
    private val requested = mutableListOf<Pair<String, Int>>()

    private val repository = object : DataTableRepository {
        override fun dataTablesStream(appTable: String, scope: CoroutineScope) =
            error("not used by the rows screen")

        override fun dataTableDataStream(
            tableName: String,
            entityId: Int,
            scope: CoroutineScope,
        ): ScreenDataStream<JsonArray> {
            requested += tableName to entityId
            return screenDataStreamForTesting(rowsState, refreshTrigger = refreshes)
        }
    }

    private class FakeCommands(
        private val gate: CompletableDeferred<Unit>? = null,
        private val result: () -> MutationResult<GenericResponse> =
            { MutationResult.Applied(GenericResponse(), synced = true) },
    ) : DataTableCommandRepository {
        val deleted = mutableListOf<Triple<String, Int, Int>>()

        override suspend fun deleteEntryOfDataTableManyToMany(
            dataTableName: String,
            entityId: Int,
            dataTableRowId: Int,
        ): MutationResult<GenericResponse> {
            deleted += Triple(dataTableName, entityId, dataTableRowId)
            gate?.await()
            return result()
        }

        override suspend fun deleteDatatableEntry(
            datatable: String,
            apptableId: Long,
            datatableId: Long,
        ) = error("not used here")

        override suspend fun createEntryInDataTable(
            dataTableName: String,
            entityId: Int,
            requestPayload: Map<String, String>,
        ) = error("not used here")

        override suspend fun addUserPathTracking(userId: Int, userLocation: UserLocation?) =
            error("not used here")
    }

    private fun viewModel(commands: FakeCommands = FakeCommands()) = DataTableRowsViewModel(
        repository,
        commands,
        SavedStateHandle(mapOf("tableName" to "client_extra", "entityId" to 42)),
    )

    @Test
    fun theReadIsScopedToTheTableAndEntityFromTheRoute() = runTest(dispatcher) {
        viewModel()
        testScheduler.advanceUntilIdle()
        // The original decoded these from a JSON blob in the SavedStateHandle, inside the
        // constructor — so a malformed argument threw before the screen could render.
        assertEquals(listOf("client_extra" to 42), requested.toList())
    }

    @Test
    fun theRowsAreDerivedFromTheStream() = runTest(dispatcher) {
        val vm = viewModel()
        rowsState.value = ScreenState.Content(json("""[{"id":1,"Notes":"a"},{"id":2,"Notes":"b"}]"""))

        vm.rows.test {
            assertIs<ScreenState.Loading>(awaitItem())
            val content = assertIs<ScreenState.Content<List<DataTableRow>>>(awaitItem())
            // Derived, so it cannot grow on recomposition the way the original's
            // `dataList = dataList + item` in the composition body did.
            assertEquals(listOf(1, 2), content.data.map { it.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deletingTheLastRowIsEmptyNotTheDeletedRow() = runTest(dispatcher) {
        // The stream reports "no rows" as an empty Content (no `isEmpty` predicate); the ViewModel's
        // `emptyIfContent` turns it into Empty. With the predicate the deleted row stayed on screen.
        val vm = viewModel()
        vm.rows.test {
            assertIs<ScreenState.Loading>(awaitItem())
            rowsState.value = ScreenState.Content(json("""[{"id":3,"Notes":"a"}]"""))
            assertIs<ScreenState.Content<List<DataTableRow>>>(awaitItem())
            rowsState.value = ScreenState.Content(json("[]"))
            assertEquals(ScreenState.Empty, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun aSuccessfulDeleteNeedsNoReRead() = runTest(dispatcher) {
        // The command records the delete through the rows' store (DataTableRepositoryTest), so the
        // stream drops the row by itself; a forced re-read here would be a second fetch for nothing.
        val commands = FakeCommands()
        val vm = viewModel(commands)
        testScheduler.advanceUntilIdle()

        refreshes.test {
            vm.actionChannel.trySend(DataTableRowsAction.Delete(3))
            testScheduler.advanceUntilIdle()

            assertEquals(listOf(Triple("client_extra", 42, 3)), commands.deleted.toList())
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(RowNotice.Deleted, vm.stateFlow.value.notice)
    }

    @Test
    fun aSecondDeleteInsideTheInFlightWindowIsDropped() = runTest(dispatcher) {
        val gate = CompletableDeferred<Unit>()
        val commands = FakeCommands(gate = gate)
        val vm = viewModel(commands)

        vm.actionChannel.trySend(DataTableRowsAction.Delete(3))
        testScheduler.advanceUntilIdle()
        vm.actionChannel.trySend(DataTableRowsAction.Delete(3))
        testScheduler.advanceUntilIdle()
        gate.complete(Unit)
        testScheduler.advanceUntilIdle()

        assertEquals(1, commands.deleted.size)
    }

    @Test
    fun anOfflineDeleteSaysSoAndClearsTheInFlightFlag() = runTest(dispatcher) {
        val commands = FakeCommands(result = { MutationResult.Blocked(BlockReason.OFFLINE) })
        val vm = viewModel(commands)

        vm.actionChannel.trySend(DataTableRowsAction.Delete(3))
        testScheduler.advanceUntilIdle()

        assertEquals(RowNotice.Offline, vm.stateFlow.value.notice)
        assertTrue(!vm.stateFlow.value.isDeleting)
    }

    @Test
    fun aNoticeClearsSoTheSameMessageCanFireTwice() = runTest(dispatcher) {
        val vm = viewModel()
        vm.actionChannel.trySend(DataTableRowsAction.Delete(3))
        testScheduler.advanceUntilIdle()
        assertEquals(RowNotice.Deleted, vm.stateFlow.value.notice)

        vm.actionChannel.trySend(DataTableRowsAction.DismissNotice)
        testScheduler.advanceUntilIdle()
        assertNull(vm.stateFlow.value.notice)
    }
}

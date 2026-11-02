/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.center

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.center.impl.CenterCommandRepositoryImpl
import kpt.core.data.center.impl.CenterRepositoryImpl
import kpt.core.data.collectionsheet.keyedReadStore
import kpt.core.data.collectionsheet.listReadStore
import kpt.core.data.collectionsheet.localRowStore
import kpt.core.data.infra.testMutationGateway
import kpt.core.model.center.Center
import kpt.core.model.center.CenterAccounts
import kpt.core.model.center.CenterPayload
import kpt.core.model.center.CenterWithAssociations
import kpt.core.model.center.PostCentersCenterIdRequest
import kpt.core.model.center.PostCentersCenterIdResponse
import kpt.core.model.client.ClientStatus
import kpt.core.model.collectionsheet.payload.CollectionSheetPayload
import kpt.core.model.objects.clients.ActivatePayload
import kpt.core.model.objects.clients.Page
import kpt.core.model.objects.databaseobjects.CollectionSheet
import kpt.core.model.objects.groups.CenterInfo
import kpt.core.model.objects.responses.SaveResponse
import kpt.core.model.shared.GenericResponse
import kpt.core.model.shared.Payload
import kpt.core.network.mifos.center.api.CenterApi
import kpt.core.store.center.CenterCollectionSheetKey
import kpt.core.store.report.GetCenterSummaryInfoKey
import org.mobilenativefoundation.store.store5.impl.extensions.get
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The center repositories over REAL Store5 stores: the list read store and the local write store
 * share one `Center` table, so a write through one is visible through the other with nothing
 * refreshed in between.
 */
class CenterRepositoryTest {

    // ---- the server ----

    private var online = true
    private var recordReadFails = false
    private var nextServerId = 10
    private var sheetGeneration = 1
    private val records = mutableMapOf<Int, CenterWithAssociations>()

    private val api = object : CenterApi {
        override suspend fun createCenter(centerPayload: CenterPayload?): SaveResponse {
            val id = nextServerId++
            records[id] = CenterWithAssociations(
                id = id,
                accountNo = "000$id",
                name = centerPayload?.name,
                officeId = centerPayload?.officeId,
                officeName = "Head Office",
                status = ClientStatus(value = "Pending"),
                active = false,
            )
            return SaveResponse(resourceId = id)
        }

        override suspend fun activateCenter(centerId: Int, activatePayload: ActivatePayload?): GenericResponse {
            records[centerId] = records.getValue(centerId).copy(status = ClientStatus(value = "Active"), active = true)
            return GenericResponse()
        }

        override suspend fun activate2(
            centerId: Long,
            postCentersCenterIdRequest: PostCentersCenterIdRequest,
            command: String?,
        ): PostCentersCenterIdResponse {
            records[centerId.toInt()] = records.getValue(centerId.toInt()).copy(
                status = ClientStatus(value = "Closed"),
                active = false,
            )
            return PostCentersCenterIdResponse(resourceId = centerId)
        }

        override suspend fun getCenterWithGroupMembersAndCollectionMeetingCalendar(
            centerId: Int,
        ): CenterWithAssociations {
            check(online && !recordReadFails) { "offline" }
            return records.getValue(centerId)
        }

        override suspend fun getCenters(b: Boolean, offset: Int, limit: Int): Page<Center> {
            check(online) { "offline" }
            return Page(pageItems = records.values.map { Center(id = it.id, name = it.name) })
        }

        override suspend fun getCollectionSheet(centerId: Long, payload: Payload?): CollectionSheet {
            check(online) { "offline" }
            return CollectionSheet(dueDate = intArrayOf(sheetGeneration), groups = emptyList())
        }

        override suspend fun saveCollectionSheet(
            centerId: Int,
            collectionSheetPayload: CollectionSheetPayload?,
        ): SaveResponse {
            sheetGeneration++
            return SaveResponse(resourceId = centerId)
        }

        override suspend fun saveCollectionSheetAsync(centerId: Int, collectionSheetPayload: CollectionSheetPayload?) =
            saveCollectionSheet(centerId, collectionSheetPayload)

        override suspend fun retrieveAll23(
            officeId: Long?,
            staffId: Long?,
            externalId: String?,
            name: String?,
            underHierarchy: String?,
            paged: Boolean?,
            offset: Int?,
            limit: Int?,
            orderBy: String?,
            sortOrder: String?,
            meetingDate: String?,
            dateFormat: String?,
            locale: String?,
        ) = error("not used")

        override suspend fun getCenterAccounts(centerId: Int): CenterAccounts = error("not used")
        override suspend fun getAllCentersInOffice(
            officeId: Int,
            additionalParams: Map<String, String>,
        ) = error("not used")
        override suspend fun getAllGroupsForCenter(centerId: Int) = error("not used")
        override suspend fun getCenterList(
            dateFormat: String?,
            locale: String?,
            meetingDate: String?,
            officeId: Int,
            staffId: Int,
        ) =
            error("not used")
    }

    // ---- core/database: the `Center` table and the read cache ----

    private val centerTable = MutableStateFlow<Map<Int, Center>>(emptyMap())
    private val recordCache = MutableStateFlow<Map<Int, CenterWithAssociations>>(emptyMap())
    private val sheetCache = MutableStateFlow<Map<CenterCollectionSheetKey, CollectionSheet>>(emptyMap())

    // ---- core/store, as provided in CenterStore.kt / CenterKeyedReadStores.kt ----

    private val listStore = listReadStore(centerTable, { it.id!! }) { api.getCenters(true, 0, 100).pageItems }
    private val itemStore = localRowStore(centerTable)
    private val recordStore = keyedReadStore(recordCache) { id: Int ->
        api.getCenterWithGroupMembersAndCollectionMeetingCalendar(id)
    }
    private val sheetStore = keyedReadStore(sheetCache) { key: CenterCollectionSheetKey ->
        api.getCollectionSheet(key.centerId, Payload())
    }

    private val summaryStore = keyedReadStore(
        MutableStateFlow<Map<GetCenterSummaryInfoKey, List<CenterInfo>>>(emptyMap()),
    ) { _: GetCenterSummaryInfoKey ->
        emptyList<CenterInfo>()
    }

    private val repository = CenterRepositoryImpl(listStore, sheetStore, recordStore, summaryStore)
    private fun commands(isOnline: Boolean = true) =
        CenterCommandRepositoryImpl(api, testMutationGateway(isOnline), recordStore, itemStore, sheetStore)

    private val newCenter = CenterPayload(name = "Riverside", officeId = 1, active = false)
    private fun meeting() = Payload().apply {
        calendarId = 4
        transactionDate = "01 October 2026"
    }

    // ---- writes land in the table the list reads ----

    @Test
    fun aCreatedCenterIsTheServersRecordInTheListTable() = runTest {
        val result = commands().createCenter(newCenter)

        assertEquals(10, assertIs<MutationResult.Applied<SaveResponse>>(result).value.resourceId)
        // Account number and office name are the server's — the request carried neither.
        val row = centerTable.value.getValue(10)
        assertEquals("00010", row.accountNo)
        assertEquals("Head Office", row.officeName)
        online = false
        assertEquals(listOf(10), listStore.get(Unit).map { it.id })
    }

    @Test
    fun aCreateWhoseRecordReadFailsStillLandsFromWhatWasSent() = runTest {
        recordReadFails = true

        val result = commands().createCenter(newCenter)

        // The server accepted it: reporting Failed would invite a duplicate center.
        assertIs<MutationResult.Applied<SaveResponse>>(result)
        assertEquals("Riverside", centerTable.value.getValue(10).name)
    }

    @Test
    fun activatingIngestsTheServersStatus() = runTest {
        commands().createCenter(newCenter)

        commands().activateCenter(10, ActivatePayload(activationDate = "01 October 2026"))

        val row = centerTable.value.getValue(10)
        assertEquals(true, row.active)
        assertEquals("Active", row.status?.value)
    }

    @Test
    fun closingIngestsTheServersStatus() = runTest {
        commands().createCenter(newCenter)

        commands().activate2(10, PostCentersCenterIdRequest(closureDate = "01 October 2026"), command = "close")

        assertEquals("Closed", centerTable.value.getValue(10).status?.value)
    }

    @Test
    fun anOfflineCreateIsBlockedAndWritesNothing() = runTest {
        assertIs<MutationResult.Blocked>(commands(isOnline = false).createCenter(newCenter))
        assertTrue(centerTable.value.isEmpty())
        assertTrue(records.isEmpty())
    }

    // ---- the generated sheet is a read through its store ----

    @Test
    fun theSheetIsReadThroughItsStoreAndAnswersOffline() = runTest {
        assertEquals(1, repository.collectionSheet(1, meeting())?.dueDate?.single())
        online = false
        assertEquals(1, repository.collectionSheet(1, meeting())?.dueDate?.single())
    }

    @Test
    fun offlineWithNoSheetCachedIsNull() = runTest {
        online = false
        assertNull(repository.collectionSheet(1, meeting()))
    }

    @Test
    fun aSavedSheetReplacesThePreSaveOneInTheCache() = runTest {
        repository.collectionSheet(1, meeting())

        val save = CollectionSheetPayload().apply {
            calendarId = 4
            transactionDate = "01 October 2026"
        }
        assertIs<MutationResult.Applied<SaveResponse>>(commands().saveCollectionSheet(1, save))

        online = false
        // Offline the officer sees the post-save sheet, not the one they already collected against.
        assertEquals(2, repository.collectionSheet(1, meeting())?.dueDate?.single())
    }

    @Test
    fun anOfflineSaveIsBlockedAndLeavesTheSheet() = runTest {
        repository.collectionSheet(1, meeting())

        val save = CollectionSheetPayload().apply {
            calendarId = 4
            transactionDate = "01 October 2026"
        }
        assertIs<MutationResult.Blocked>(commands(isOnline = false).saveCollectionSheet(1, save))
        assertEquals(1, sheetGeneration)
        assertEquals(1, sheetCache.value.values.single().dueDate.single())
    }
}

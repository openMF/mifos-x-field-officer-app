/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.group

import io.ktor.client.call.HttpClientCall
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.util.date.GMTDate
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.InternalAPI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.collectionsheet.keyedReadStore
import kpt.core.data.collectionsheet.listReadStore
import kpt.core.data.collectionsheet.localRowStore
import kpt.core.data.group.impl.GroupCommandRepositoryImpl
import kpt.core.data.infra.testMutationGateway
import kpt.core.model.client.ClientStatus
import kpt.core.model.group.Group
import kpt.core.model.group.GroupAccounts
import kpt.core.model.group.GroupPayload
import kpt.core.model.group.GroupWithAssociations
import kpt.core.model.objects.clients.ActivatePayload
import kpt.core.model.objects.clients.Page
import kpt.core.model.objects.responses.SaveResponse
import kpt.core.network.mifos.group.api.GroupApi
import org.mobilenativefoundation.store.store5.impl.extensions.get
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * The group commands over REAL Store5 stores: the list read store and the local write store share
 * one `GroupTable`, so a write through one is visible through the other with nothing refreshed.
 */
class GroupRepositoryTest {

    // ---- the server ----

    private var online = true
    private var recordReadFails = false
    private var nextServerId = 20
    private val records = mutableMapOf<Int, Group>()

    private val api = object : GroupApi {
        override suspend fun createGroup(groupPayload: GroupPayload?): SaveResponse {
            val id = nextServerId++
            records[id] = Group(
                id = id,
                accountNo = "G$id",
                name = groupPayload?.name,
                officeId = groupPayload?.officeId,
                officeName = "Head Office",
                status = ClientStatus(value = "Pending"),
                active = false,
            )
            return SaveResponse(resourceId = id)
        }

        override suspend fun activateGroup(groupId: Int, activatePayload: ActivatePayload): HttpResponse {
            records[groupId] = records.getValue(groupId).copy(status = ClientStatus(value = "Active"), active = true)
            return NoBodyResponse
        }

        override suspend fun getGroup(groupId: Int): Group {
            check(online && !recordReadFails) { "offline" }
            return records.getValue(groupId)
        }

        override suspend fun getGroups(b: Boolean, offset: Int, limit: Int): Page<Group> {
            check(online) { "offline" }
            return Page(pageItems = records.values.toList())
        }

        override suspend fun retrieveAll24(
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
            orphansOnly: Boolean?,
        ) = error("not used")

        override suspend fun getGroupWithAssociations(groupId: Int): GroupWithAssociations = error("not used")
        override suspend fun getAllGroupsInOffice(officeId: Int, params: Map<String, String>) = error("not used")
        override suspend fun getGroupAccounts(groupId: Int): GroupAccounts = error("not used")
    }

    // ---- core/database + core/store, as provided in GroupStore.kt / GroupKeyedReadStores.kt ----

    private val groupTable = MutableStateFlow<Map<Int, Group>>(emptyMap())
    private val recordCache = MutableStateFlow<Map<Int, Group>>(emptyMap())

    private val listStore = listReadStore(groupTable, { it.id!! }) { api.getGroups(true, 0, 100).pageItems }
    private val itemStore = localRowStore(groupTable)
    private val recordStore = keyedReadStore(recordCache) { id: Int -> api.getGroup(id) }

    private fun commands(isOnline: Boolean = true) =
        GroupCommandRepositoryImpl(api, testMutationGateway(isOnline), recordStore, itemStore)

    private val newGroup = GroupPayload(name = "Weavers", officeId = 1, externalId = "EXT-1")

    @Test
    fun aCreatedGroupIsTheServersRecordInTheListTable() = runTest {
        val result = commands().createGroup(newGroup)

        assertEquals(20, assertIs<MutationResult.Applied<SaveResponse>>(result).value.resourceId)
        assertEquals("G20", groupTable.value.getValue(20).accountNo)
        online = false
        assertEquals(listOf(20), listStore.get(Unit).map { it.id })
    }

    @Test
    fun aCreateWhoseRecordReadFailsStillLandsFromWhatWasSent() = runTest {
        recordReadFails = true

        assertIs<MutationResult.Applied<SaveResponse>>(commands().createGroup(newGroup))
        val row = groupTable.value.getValue(20)
        assertEquals("Weavers", row.name)
        assertEquals("EXT-1", row.externalId)
    }

    @Test
    fun activatingIngestsTheServersStatus() = runTest {
        commands().createGroup(newGroup)

        assertIs<MutationResult.Applied<Unit>>(
            commands().activateGroup(
                20,
                ActivatePayload(activationDate = "01 October 2026"),
            ),
        )

        val row = groupTable.value.getValue(20)
        assertEquals(true, row.active)
        assertEquals("Active", row.status?.value)
    }

    @Test
    fun anActivationWhoseRecordReadFailsIsStillApplied() = runTest {
        commands().createGroup(newGroup)
        recordReadFails = true

        // The server activated it; a Failed here would have the officer activate twice.
        assertIs<MutationResult.Applied<Unit>>(commands().activateGroup(20, ActivatePayload()))
    }

    @Test
    fun anOfflineActivationIsBlockedAndWritesNothing() = runTest {
        assertIs<MutationResult.Blocked>(commands(isOnline = false).activateGroup(20, ActivatePayload()))
        assertTrue(groupTable.value.isEmpty())
    }

    /** `activateGroup` answers a raw response the command never reads. */
    @OptIn(InternalAPI::class)
    private object NoBodyResponse : HttpResponse() {
        override val call: HttpClientCall get() = error("unread")
        override val status: HttpStatusCode = HttpStatusCode.OK
        override val version: HttpProtocolVersion = HttpProtocolVersion.HTTP_1_1
        override val requestTime: GMTDate = GMTDate()
        override val responseTime: GMTDate = GMTDate()
        override val rawContent: ByteReadChannel = ByteReadChannel.Empty
        override val headers: Headers = Headers.Empty
        override val coroutineContext: CoroutineContext = EmptyCoroutineContext
    }
}

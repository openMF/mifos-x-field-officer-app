/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.client

import io.ktor.client.call.HttpClientCall
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.util.date.GMTDate
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.InternalAPI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kpt.core.base.store.infra.StoreFactory
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.client.impl.ClientCommandRepositoryImpl
import kpt.core.data.client.impl.ClientRepositoryImpl
import kpt.core.data.infra.testMutationGateway
import kpt.core.database.cache.dao.ApiResponseCacheDao
import kpt.core.database.cache.entity.ApiResponseCacheEntity
import kpt.core.model.client.Client
import kpt.core.model.client.ClientPayload
import kpt.core.model.client.ClientStatus
import kpt.core.model.client.PostClientAddressRequest
import kpt.core.model.client.PostClientAddressResponse
import kpt.core.model.client.PostClientsClientIdRequest
import kpt.core.model.objects.clients.ActivatePayload
import kpt.core.model.objects.clients.AssignStaffRequest
import kpt.core.model.objects.clients.ClientAddressEntity
import kpt.core.model.objects.clients.ClientAddressRequest
import kpt.core.model.objects.clients.ClientAddressResponse
import kpt.core.model.objects.clients.ClientCloseRequest
import kpt.core.model.objects.clients.CollateralPayload
import kpt.core.model.objects.clients.ProposeTransferRequest
import kpt.core.model.objects.clients.UpdateSavingsAccountRequest
import kpt.core.model.objects.noncoreobjects.DocumentType
import kpt.core.model.objects.noncoreobjects.DocumentUpload
import kpt.core.model.objects.noncoreobjects.Identifier
import kpt.core.model.objects.noncoreobjects.IdentifierPayload
import kpt.core.model.shared.CollateralItemResult
import kpt.core.model.shared.GenericResponse
import kpt.core.model.shared.PinpointLocationActionResponse
import kpt.core.network.mifos.client.api.ClientApi
import kpt.core.network.mifos.client.api.ClientIdentifierApi
import kpt.core.store.cache.cachedRead
import kpt.core.store.client.provideAddressConfigurationStore
import kpt.core.store.client.provideAddressTemplateStore
import kpt.core.store.client.provideClientCloseTemplateStore
import kpt.core.store.client.provideClientIdentifierItemStore
import kpt.core.store.client.provideClientLocalStore
import kpt.core.store.client.provideClientPinpointItemStore
import kpt.core.store.client.provideClientsTemplateStore
import kpt.core.store.client.provideCollateralItemsStore
import kpt.core.store.client.provideGetClientAccountsStore
import kpt.core.store.client.provideGetClientAddressesStore
import kpt.core.store.client.provideGetClientCollateralItemsStore
import kpt.core.store.client.provideGetClientIdentifierTemplateStore
import kpt.core.store.client.provideGetClientListIdentifiersStore
import kpt.core.store.client.provideGetClientPinpointLocationsStore
import kpt.core.store.client.provideGetClientStore
import kpt.core.store.client.provideGetClientTemplateStore
import kpt.core.store.config.AppCacheKeys
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.StoreBuilder
import org.mobilenativefoundation.store.store5.impl.extensions.fresh
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The client repositories over the REAL `core/store` providers: every read store and its local
 * write store are built by the same `provide*Store` functions the app uses, over one in-memory
 * `api_response_cache`, so a write through a local store is visible through the read store's row
 * with nothing refreshed in between. Only the roster's `Client` table is modelled in the test (its
 * DAO is the app's widest), wired the way `ClientStore.kt` wires it.
 */
class ClientRepositoryTest {

    private val json = Json { ignoreUnknownKeys = true }

    // ---- the server ----

    private var online = true
    private var client = Client(
        id = 1,
        displayName = "Asha",
        staffId = 7,
        staffName = "Ravi",
        status = ClientStatus(id = 300),
    )
    private var pinpoints = listOf(ClientAddressResponse(id = 11, clientId = 1, placeAddress = "Market"))
    private var identifiers = listOf(Identifier(id = 21, clientId = 1, documentKey = "P-1"))
    private var image: String? = "data:image/png;base64,AAAA"
    private var clientFetches = 0

    // ---- core/database ----

    private val cacheRows = MutableStateFlow<Map<String, ApiResponseCacheEntity>>(emptyMap())
    private val cache = object : ApiResponseCacheDao {
        override fun observe(cacheKey: String) = cacheRows.map { it[cacheKey] }
        override fun observeByPrefix(
            prefix: String,
        ) = cacheRows.map { rows -> rows.filterKeys { it.startsWith(prefix) }.values.toList() }
        override suspend fun upsert(row: ApiResponseCacheEntity) {
            cacheRows.value += row.cacheKey to row
        }
        override suspend fun delete(cacheKey: String) {
            cacheRows.value -= cacheKey
        }
        override suspend fun deleteByPrefix(prefix: String) {
            cacheRows.value = cacheRows.value.filterKeys { !it.startsWith(prefix) }
        }
        override suspend fun deleteAll() {
            cacheRows.value = emptyMap()
        }
    }

    /** The roster's `Client` table. */
    private val roster = MutableStateFlow<Map<Int, Client>>(emptyMap())
    private val clientRowStore = StoreFactory.createOfflineMutableStore(
        sourceOfTruth = SourceOfTruth.of(
            reader = { id: Int -> roster.map { it[id] } },
            writer = { id: Int, row: Client -> roster.value += id to row },
            delete = { id: Int -> roster.value -= id },
        ),
    )

    private inline fun <reified T> row(key: String): T? =
        cacheRows.value[key]?.let { json.decodeFromString<T>(it.payload) }

    private fun detail() = row<Client>(AppCacheKeys.ClientGetClient.forKey("1"))
    private fun pinpointRow() =
        row<List<ClientAddressResponse>>(AppCacheKeys.ClientGetClientPinpointLocations.forKey("1"))
    private fun identifierRow() = row<List<Identifier>>(AppCacheKeys.ClientGetClientListIdentifiers.forKey("1"))

    // ---- core/network ----

    @OptIn(InternalAPI::class)
    private val accepted: HttpResponse = object : HttpResponse() {
        override val call: HttpClientCall get() = error("the body is not read")
        override val status = HttpStatusCode.OK
        override val version = HttpProtocolVersion.HTTP_1_1
        override val requestTime = GMTDate()
        override val responseTime = GMTDate()
        override val rawContent = ByteReadChannel.Empty
        override val headers: Headers = Headers.Empty
        override val coroutineContext = EmptyCoroutineContext
        override fun toString() = "accepted"
    }

    private val api = object : ClientApi {
        private fun serverRead() = check(online) { "offline" }

        override suspend fun getClient(clientId: Int): Client {
            serverRead()
            clientFetches++
            return client
        }
        override suspend fun getClientPinpointLocations(clientId: Int) = run {
            serverRead()
            pinpoints
        }
        override suspend fun getClientAddresses(clientId: Int) = run {
            serverRead()
            listOf(ClientAddressEntity(clientID = 1, addressId = 5, addressType = "Home"))
        }
        override suspend fun getClientCollateralItems(clientId: Int) = run {
            serverRead()
            listOf(CollateralItemResult("Gold", 2, 10.0, 50.0, 3.0))
        }

        override suspend fun closeClient(clientId: Int, payload: ClientCloseRequest): HttpResponse {
            client = client.copy(status = ClientStatus(id = 600, value = "Closed"))
            return accepted
        }

        override suspend fun unassignStaff(clientId: Int, payload: AssignStaffRequest) = accepted
        override suspend fun assignStaff(clientId: Int, payload: AssignStaffRequest): HttpResponse {
            client = client.copy(staffId = payload.staffId, staffName = "Meera")
            return accepted
        }

        override suspend fun updateSavingsAccount(clientId: Int, payload: UpdateSavingsAccountRequest) = accepted
        override suspend fun addClientPinpointLocation(
            clientId: Int,
            clientAddressRequest: ClientAddressRequest?,
        ): PinpointLocationActionResponse {
            pinpoints = pinpoints + ClientAddressResponse(
                id = 12,
                clientId = 1,
                placeAddress = clientAddressRequest?.placeAddress,
            )
            return PinpointLocationActionResponse(resourceId = 1)
        }

        override suspend fun deleteClientPinpointLocation(
            apptableId: Int,
            datatableId: Int,
        ) = PinpointLocationActionResponse()
        override suspend fun updateClientPinpointLocation(
            apptableId: Int,
            datatableId: Int,
            address: ClientAddressRequest?,
        ) =
            PinpointLocationActionResponse()

        override suspend fun activateClient(clientId: Int, clientActivate: ActivatePayload?) = GenericResponse()
        override suspend fun deleteClientImage(clientId: Int) {
            image = null
        }
        override suspend fun uploadClientImage(clientId: Int, body: MultiPartFormDataContent) {
            image = "data:image/png;base64,BBBB"
        }
        override suspend fun createClientAddress(
            clientId: Int,
            addressTypeId: Int,
            addressPayload: PostClientAddressRequest,
        ) =
            PostClientAddressResponse(resourceId = 5)

        override suspend fun createCollateral(clientId: Int, payload: CollateralPayload) = accepted
        override suspend fun proposeTransfer(clientId: Int, payload: ProposeTransferRequest) = accepted

        override suspend fun activate1(
            clientId: Long,
            postClientsClientIdRequest: PostClientsClientIdRequest,
            command: String?,
        ) =
            error("unused")
        override suspend fun retrieveAll21(
            officeId: Long?,
            externalId: String?,
            displayName: String?,
            firstName: String?,
            lastName: String?,
            status: String?,
            underHierarchy: String?,
            offset: Int?,
            limit: Int?,
            orderBy: String?,
            sortOrder: String?,
            orphansOnly: Boolean?,
        ) = error("unused")
        override suspend fun retrieveAssociatedAccounts(clientId: Long) = error("unused")
        override suspend fun getAllClients(b: Boolean, offset: Int, limit: Int) = error("unused")
        override suspend fun getClientImage(clientId: Int) = error("the image store is built over `image` in this test")
        override suspend fun createClient(clientPayload: ClientPayload?) = error("unused")
        override suspend fun updateClient(clientId: Int, clientPayload: ClientPayload?) = error("unused")
        override suspend fun getClientTemplate() = error("unused")
        override suspend fun getClientAccounts(clientId: Int) = error("unused")
        override suspend fun getAddressConfiguration() = error("unused")
        override suspend fun getAddressTemplate() = error("unused")
        override suspend fun getClientTemplate(clientId: Int) = error("unused")
        override suspend fun getClientCloseTemplate() = error("unused")
        override suspend fun getCollateralItems() = error("unused")
    }

    private val identifierApi = object : ClientIdentifierApi {
        override suspend fun getClientListIdentifiers(clientId: Long) = run {
            check(online) { "offline" }
            identifiers
        }
        override suspend fun getClientIdentifiers(clientId: Long, identifierId: Long) = error("unused")
        override suspend fun getClientIdentifierTemplate(clientId: Long) = error("unused")
        override suspend fun deleteClientIdentifier(clientId: Long, identifierId: Long) = GenericResponse()
        override suspend fun createClientIdentifier(
            clientId: Long,
            identifierPayload: IdentifierPayload,
        ): HttpResponse {
            identifiers = identifiers + Identifier(
                id = 22,
                clientId = 1,
                documentKey = identifierPayload.documentKey,
                documentType = DocumentType(id = 3, name = "Passport"),
            )
            return accepted
        }

        override suspend fun updateClientIdentifier(
            clientId: Long,
            identifierId: Long,
            identifierPayload: IdentifierPayload,
        ) = GenericResponse()
    }

    // ---- core/store, through the app's own providers ----

    private val clientStore = provideGetClientStore(api, cache)
    private val pinpointStore = provideGetClientPinpointLocationsStore(api, cache)
    private val identifiersStore = provideGetClientListIdentifiersStore(identifierApi, cache)

    /** `provideClientImageStore` with its fetch over [image] (a 404 is `""` there). */
    private val imageStore = cachedRead<Int, String>(
        dao = cache,
        keyOf = { AppCacheKeys.ClientImage.forKey(it.toString()) },
        fetch = {
            check(online) { "offline" }
            image.orEmpty()
        },
    )

    private fun commands(isOnline: Boolean = true, clientApi: ClientApi = api) = ClientCommandRepositoryImpl(
        clientApi = clientApi,
        clientIdentifierApi = identifierApi,
        gateway = testMutationGateway(isOnline),
        clientStore = provideGetClientStore(clientApi, cache),
        clientLocalStore = provideClientLocalStore(cache),
        clientRowStore = clientRowStore,
        imageStore = imageStore,
        pinpointStore = pinpointStore,
        pinpointItemStore = provideClientPinpointItemStore(cache),
        addressesStore = provideGetClientAddressesStore(api, cache),
        collateralStore = provideGetClientCollateralItemsStore(api, cache),
        identifiersStore = identifiersStore,
        identifierItemStore = provideClientIdentifierItemStore(cache),
    )

    /** The roster read store, as `provideClientStore` wires it over the `Client` table. */
    private val rosterStore = StoreBuilder.from(
        fetcher = Fetcher.of { _: Unit -> error("unused") },
        sourceOfTruth = SourceOfTruth.of(
            reader = { _: Unit -> roster.map { rows -> rows.values.toList().takeIf { it.isNotEmpty() } } },
            writer = { _: Unit, rows: List<Client> -> roster.value += rows.associateBy { it.id } },
        ),
    ).build()

    private val repository = ClientRepositoryImpl(
        store = rosterStore,
        clientStore = clientStore,
        accountsStore = provideGetClientAccountsStore(api, cache),
        identifierTemplateStore = provideGetClientIdentifierTemplateStore(identifierApi, cache),
        identifiersStore = identifiersStore,
        pinpointStore = pinpointStore,
        clientCollateralStore = provideGetClientCollateralItemsStore(api, cache),
        collateralCatalogStore = provideCollateralItemsStore(api, cache),
        closeTemplateStore = provideClientCloseTemplateStore(api, cache),
        clientTemplateStore = provideGetClientTemplateStore(api, cache),
        addressesStore = provideGetClientAddressesStore(api, cache),
        addressTemplateStore = provideAddressTemplateStore(api, cache),
        clientsTemplateStore = provideClientsTemplateStore(api, cache),
        addressConfigurationStore = provideAddressConfigurationStore(api, cache),
        imageStore = imageStore,
    )

    // ---- server-computed state is ingested ----

    @Test
    fun aClosedClientReadsClosedInTheDetailAndTheRoster() = runTest {
        val result = commands().closeClient(1, ClientCloseRequest("23 October 2026", 4, "dd MMMM yyyy", "en"))

        assertIs<MutationResult.Applied<Unit>>(result)
        assertEquals(600, detail()?.status?.id)
        assertEquals(600, roster.value[1]?.status?.id)
    }

    @Test
    fun anAssignedStaffNameIsTheServers() = runTest {
        commands().assignStaff(1, AssignStaffRequest(staffId = 9))

        assertEquals("Meera", detail()?.staffName)
        assertEquals("Meera", roster.value[1]?.staffName)
    }

    @Test
    fun aNewPinpointAndANewIdentifierAreIngestedWithTheirServerIds() = runTest {
        commands().addClientPinpointLocation(1, ClientAddressRequest(placeAddress = "Clinic"))
        commands().createClientIdentifier(1, IdentifierPayload(documentTypeId = 3, documentKey = "P-2"))

        assertEquals(listOf(11, 12), pinpointRow()?.map { it.id })
        assertEquals("Passport", identifierRow()?.single { it.id == 22 }?.documentType?.name)
    }

    @Test
    fun aRecordThatCannotBeReadBackDoesNotFailAnAcceptedWrite() = runTest {
        val flaky = object : ClientApi by api {
            override suspend fun getClient(clientId: Int): Client = error("read-back failed")
        }
        val result = commands(clientApi = flaky).activateClient(1, ActivatePayload())

        assertIs<MutationResult.Applied<GenericResponse>>(result)
    }

    // ---- a change known from the request is applied through a local store ----

    @Test
    fun unassigningStaffClearsItWithoutAnotherClientFetch() = runTest {
        clientStore.fresh(1)
        val fetchesBefore = clientFetches

        commands().unassignStaff(1, AssignStaffRequest(staffId = 7))

        assertNull(detail()?.staffName)
        assertEquals(0, detail()?.staffId)
        assertNull(roster.value[1]?.staffName)
        assertEquals(fetchesBefore, clientFetches)
    }

    @Test
    fun theDefaultSavingsAccountLandsOnTheClient() = runTest {
        clientStore.fresh(1)

        commands().updateSavingsAccount(1, UpdateSavingsAccountRequest(savingsAccountId = 44))

        assertEquals(44L, detail()?.savingsAccountId)
        assertEquals(44L, roster.value[1]?.savingsAccountId)
    }

    @Test
    fun anEditedPinpointReplacesItsRow() = runTest {
        pinpointStore.fresh(1)

        commands().updateClientPinpointLocation(1, 11, ClientAddressRequest(placeAddress = "Bus stand", latitude = 1.5))

        assertEquals("Bus stand", pinpointRow()?.single()?.placeAddress)
        assertEquals(1.5, pinpointRow()?.single()?.latitude)
    }

    @Test
    fun deletesRemoveTheRowsTheReadsObserve() = runTest {
        pinpointStore.fresh(1)
        identifiersStore.fresh(1L)

        commands().deleteClientPinpointLocation(1, 11)
        commands().deleteClientIdentifier(1, 21)

        assertEquals(emptyList(), pinpointRow())
        assertEquals(emptyList(), identifierRow())
    }

    @Test
    fun aDeleteOfAListNeverFetchedDoesNotInventOne() = runTest {
        commands().deleteClientPinpointLocation(1, 11)
        commands().updateClientPinpointLocation(1, 11, ClientAddressRequest(placeAddress = "x"))

        // A one-item row would read as the client's WHOLE pinpoint list.
        assertNull(pinpointRow())
    }

    // ---- the photo ----

    @Test
    fun aPhotoSeenOnceRendersOffline() = runTest {
        assertEquals("data:image/png;base64,AAAA", repository.clientImage(1))
        online = false
        assertEquals("data:image/png;base64,AAAA", repository.clientImage(1))
    }

    @Test
    fun anUploadedPhotoIsTheServersAndADeletedOneIsGone() = runTest {
        commands().uploadClientImage(
            1,
            DocumentUpload(name = "photo", fileName = "photo.png", mimeType = "image/png", bytes = ByteArray(0)),
        )
        assertEquals("data:image/png;base64,BBBB", repository.clientImage(1))

        commands().deleteClientImage(1)
        assertNull(cacheRows.value[AppCacheKeys.ClientImage.forKey("1")])
        assertNull(repository.clientImage(1))
    }

    // ---- offline ----

    @Test
    fun anOfflineWriteIsBlockedAndWritesNothing() = runTest {
        online = false
        val offline = commands(isOnline = false)

        assertIs<MutationResult.Blocked>(offline.closeClient(1, ClientCloseRequest("d", 1, "f", "en")))
        assertIs<MutationResult.Blocked>(offline.unassignStaff(1, AssignStaffRequest(7)))
        assertIs<MutationResult.Blocked>(offline.deleteClientPinpointLocation(1, 11))
        assertTrue(cacheRows.value.isEmpty())
        assertTrue(roster.value.isEmpty())
    }
}

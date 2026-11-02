/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.collectionsheet

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.collectionsheet.impl.CollectionSheetCommandRepositoryImpl
import kpt.core.data.collectionsheet.impl.CollectionSheetRepositoryImpl
import kpt.core.data.infra.testMutationGateway
import kpt.core.model.center.CenterWithAssociations
import kpt.core.model.collectionsheet.CenterDetail
import kpt.core.model.collectionsheet.CollectionSheetPayload
import kpt.core.model.collectionsheet.CollectionSheetResponse
import kpt.core.model.collectionsheet.IndividualCollectionSheet
import kpt.core.model.collectionsheet.IndividualCollectionSheetPayload
import kpt.core.model.collectionsheet.ProductiveCollectionSheetPayload
import kpt.core.model.collectionsheet.RequestCollectionSheetPayload
import kpt.core.model.objects.collectionsheets.CollectionSheetRequestPayload
import kpt.core.model.shared.GenericResponse
import kpt.core.network.mifos.collectionsheet.api.CollectionSheetApi
import kpt.core.store.collectionsheet.CollectionSheetKey
import kpt.core.store.collectionsheet.GeneratedSheetKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kpt.core.model.collectionsheet.CenterDetail as CenterDetailModel
import kpt.core.model.collectionsheet.CollectionSheetResponse as CollectionSheetResponseModel
import kpt.core.model.collectionsheet.IndividualCollectionSheet as IndividualCollectionSheetModel

/**
 * The collection-sheet repositories over REAL Store5 stores. The read repository and the command
 * repository share each sheet store, so a save's outcome is what the next read answers — offline
 * included — with nothing refreshed outside the write door.
 *
 * Each sheet the fake server generates carries the number of saves so far, which is how a test tells
 * a post-save sheet from the pre-save one.
 */
class CollectionSheetRepositoryTest {

    // ---- the server ----

    private var online = true
    private var regenerateFails = false
    private var saves = 0

    private fun generate(): Int {
        check(online && !regenerateFails) { "offline" }
        return saves
    }

    private val api = object : CollectionSheetApi {
        override suspend fun getIndividualCollectionSheet(payload: RequestCollectionSheetPayload?) =
            IndividualCollectionSheet(dueDate = "sheet-${generate()}")

        override suspend fun saveIndividualCollectionSheet(
            payload: IndividualCollectionSheetPayload?,
        ): GenericResponse {
            saves++
            return GenericResponse()
        }

        override suspend fun fetchProductiveSheet(centerId: Int, payload: CollectionSheetRequestPayload?) =
            CollectionSheetResponse(dueDate = intArrayOf(generate()))

        override suspend fun submitProductiveSheet(
            centerId: Int,
            payload: ProductiveCollectionSheetPayload?,
        ): GenericResponse {
            saves++
            return GenericResponse()
        }

        override suspend fun fetchCollectionSheet(groupId: Int, payload: CollectionSheetRequestPayload?) =
            CollectionSheetResponse(dueDate = intArrayOf(generate()))

        override suspend fun submitCollectionSheet(groupId: Int, payload: CollectionSheetPayload?): GenericResponse {
            saves++
            return GenericResponse()
        }

        override suspend fun fetchCenterDetails(
            format: String?,
            locale: String?,
            meetingDate: String?,
            officeId: Int,
            staffId: Int,
        ): List<CenterDetail> = error("not used")

        override suspend fun fetchGroupsAssociatedWithCenter(centerId: Int): CenterWithAssociations = error("not used")
    }

    // ---- core/database (the read cache) + core/store, as in CollectionSheetKeyedReadStores.kt ----

    private val individualCache =
        MutableStateFlow<Map<RequestCollectionSheetPayload, IndividualCollectionSheetModel>>(emptyMap())
    private val productiveCache = MutableStateFlow<Map<GeneratedSheetKey, CollectionSheetResponseModel>>(emptyMap())
    private val groupCache = MutableStateFlow<Map<GeneratedSheetKey, CollectionSheetResponseModel>>(emptyMap())

    private val individualStore = keyedReadStore(individualCache) { key -> api.getIndividualCollectionSheet(key) }
    private val productiveStore = keyedReadStore(productiveCache) { key ->
        api.fetchProductiveSheet(
            key.ownerId,
            key.request,
        )
    }
    private val groupStore = keyedReadStore(groupCache) { key -> api.fetchCollectionSheet(key.ownerId, key.request) }
    private val rosterStore = keyedReadStore(
        MutableStateFlow<Map<CollectionSheetKey, List<CenterDetailModel>>>(emptyMap()),
    ) {
        error("not used")
    }

    private val repository = CollectionSheetRepositoryImpl(rosterStore, individualStore, productiveStore, groupStore)
    private fun commands(isOnline: Boolean = true) =
        CollectionSheetCommandRepositoryImpl(
            api,
            testMutationGateway(isOnline),
            individualStore,
            productiveStore,
            groupStore,
        )

    private val individual = RequestCollectionSheetPayload(
        officeId = 1,
        staffId = 2,
        transactionDate = "01 October 2026",
    )
    private val meeting = CollectionSheetRequestPayload(calendarId = 4, transactionDate = "01 October 2026")

    // ---- reads ----

    @Test
    fun aGeneratedSheetIsReadThroughItsStoreAndAnswersOffline() = runTest {
        assertEquals(0, repository.productiveSheet(1, meeting)?.dueDate?.single())
        online = false
        assertEquals(0, repository.productiveSheet(1, meeting)?.dueDate?.single())
    }

    @Test
    fun offlineWithNoSheetCachedIsNull() = runTest {
        online = false
        assertNull(repository.individualCollectionSheet(individual))
    }

    @Test
    fun aDifferentMeetingIsADifferentSheet() = runTest {
        repository.groupCollectionSheet(7, meeting)
        online = false
        assertNull(repository.groupCollectionSheet(7, meeting.copy(transactionDate = "08 October 2026")))
    }

    // ---- writes land in the rows the reads answer from ----

    @Test
    fun aSavedProductiveSheetReplacesThePreSaveOne() = runTest {
        repository.productiveSheet(1, meeting)

        val save = ProductiveCollectionSheetPayload(calendarId = 4, transactionDate = "01 October 2026")
        assertIs<MutationResult.Applied<GenericResponse>>(commands().submitProductiveSheet(1, save))

        online = false
        assertEquals(1, repository.productiveSheet(1, meeting)?.dueDate?.single())
    }

    @Test
    fun aSavedGroupSheetReplacesThePreSaveOne() = runTest {
        repository.groupCollectionSheet(7, meeting)

        commands().submitCollectionSheet(7, CollectionSheetPayload(calendarId = 4, transactionDate = "01 October 2026"))

        online = false
        assertEquals(1, repository.groupCollectionSheet(7, meeting)?.dueDate?.single())
    }

    @Test
    fun aSavedIndividualSheetReplacesThePreSaveOne() = runTest {
        repository.individualCollectionSheet(individual)

        commands().saveIndividualCollectionSheet(
            individual,
            IndividualCollectionSheetPayload(transactionDate = "01 October 2026"),
        )

        online = false
        assertEquals("sheet-1", repository.individualCollectionSheet(individual)?.dueDate)
    }

    @Test
    fun aSaveWhoseReReadFailsDropsThePreSaveSheetAndIsStillApplied() = runTest {
        repository.productiveSheet(1, meeting)
        regenerateFails = true

        val save = ProductiveCollectionSheetPayload(calendarId = 4, transactionDate = "01 October 2026")
        // The server took the collection: Failed would invite collecting it twice.
        assertIs<MutationResult.Applied<GenericResponse>>(commands().submitProductiveSheet(1, save))
        // And the sheet collected against must not be served again as current.
        assertTrue(productiveCache.value.isEmpty())
    }

    @Test
    fun anOfflineSaveIsBlockedAndWritesNothing() = runTest {
        repository.productiveSheet(1, meeting)

        val save = ProductiveCollectionSheetPayload(calendarId = 4, transactionDate = "01 October 2026")
        assertIs<MutationResult.Blocked>(commands(isOnline = false).submitProductiveSheet(1, save))
        assertEquals(0, saves)
        assertEquals(0, productiveCache.value.values.single().dueDate?.single())
    }
}

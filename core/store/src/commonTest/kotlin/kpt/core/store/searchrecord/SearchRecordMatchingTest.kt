/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.searchrecord

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kpt.core.common.utils.Constants
import kpt.core.database.cache.entity.ApiResponseCacheEntity
import kpt.core.model.objects.clients.ClientAddressEntity
import kpt.core.model.objects.noncoreobjects.DocumentType
import kpt.core.model.objects.noncoreobjects.Identifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The matching rules ported from the original SQL (`6b66e8a43:core/database/.../ClientDao.kt`
 * searchAddressesByQuery / searchIdentifiersByQuery), over cache rows shaped exactly as the client
 * stores write them: one JSON list per client, keyed `<prefix><clientId>`.
 */
class SearchRecordMatchingTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun addressRow(clientId: Int, vararg a: ClientAddressEntity) = ApiResponseCacheEntity(
        cacheKey = "clientGetClientAddresses:$clientId",
        payload = json.encodeToString(ListSerializer(ClientAddressEntity.serializer()), a.toList()),
        fetchedAtMs = 0,
    )

    private fun identifierRow(clientId: Int, vararg i: Identifier) = ApiResponseCacheEntity(
        cacheKey = "clientGetClientListIdentifiers:$clientId",
        payload = json.encodeToString(ListSerializer(Identifier.serializer()), i.toList()),
        fetchedAtMs = 0,
    )

    private val home = ClientAddressEntity(
        clientID = 1,
        addressId = 5,
        addressType = "Home",
        addressLine1 = "12 Lake Road",
        city = "Dhaka",
        countryName = "Bangladesh",
    )
    private val office = ClientAddressEntity(
        clientID = 2,
        addressId = 6,
        addressType = "Office",
        addressLine1 = "4 Mill Lane",
        city = "Sylhet",
        postalCode = "3100",
    )

    @Test
    fun anAddressMatchesOnAnyOfItsFieldsCaseInsensitively() {
        val rows = listOf(addressRow(1, home), addressRow(2, office))
        assertEquals(listOf(5), searchAddresses(rows, "dhaka").map { it.id })
        assertEquals(listOf(6), searchAddresses(rows, "3100").map { it.id }) // postal code
        assertEquals(listOf(6), searchAddresses(rows, "MILL").map { it.id })
        assertTrue(searchAddresses(rows, "nowhere").isEmpty())
    }

    @Test
    fun anAddressResultCarriesWhatTheScreenAndNavigationNeed() {
        val r = searchAddresses(listOf(addressRow(1, home)), "lake").single()
        assertEquals("Home", r.name)
        assertEquals("12 Lake Road, Dhaka, Bangladesh", r.description) // blank state skipped
        assertEquals("1", r.metadata[Constants.CLIENT_ID]) // where a tap goes
        assertEquals("12 Lake Road", r.metadata[Constants.ADDRESS_LINE_1])
    }

    @Test
    fun identifiersMatchSortByDocumentKeyAndTakeTheOwnerFromTheRowKey() {
        val passport = Identifier(
            id = 9,
            documentKey = "P-200",
            documentType = DocumentType(name = "Passport"),
            status = "Active",
        )
        val idCard = Identifier(
            id = 3,
            documentKey = "A-100",
            documentType = DocumentType(name = "National ID"),
            description = "passport scan",
        )
        val rows = listOf(identifierRow(7, passport, idCard))

        val found = searchIdentifiers(rows, "passport")
        // Both match ("Passport" type, "passport scan" description); ordered by document key.
        assertEquals(listOf("A-100", "P-200"), found.map { it.metadata[Constants.DOCUMENT_KEY] })
        // The list endpoint leaves Identifier.clientId null — the row key names the owner.
        assertEquals(setOf("7"), found.map { it.metadata[Constants.CLIENT_ID] }.toSet())
        assertEquals(listOf(9), searchIdentifiers(rows, "9").map { it.id }) // the id, as text
    }

    @Test
    fun aRowThatNoLongerDecodesIsSkippedNotFatal() {
        val broken = ApiResponseCacheEntity(
            cacheKey = "clientGetClientAddresses:3",
            payload = "{not json",
            fetchedAtMs = 0,
        )
        assertEquals(listOf(5), searchAddresses(listOf(broken, addressRow(1, home)), "dhaka").map { it.id })
    }
}

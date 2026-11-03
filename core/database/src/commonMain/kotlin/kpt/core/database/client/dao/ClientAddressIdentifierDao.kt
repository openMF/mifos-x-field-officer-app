/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.client.dao

import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import kotlinx.coroutines.flow.Flow
import kpt.core.database.client.entity.ClientAddressEntity
import kpt.core.database.client.entity.ClientIdentifierEntity

/** Client addresses and identifiers. */
interface ClientAddressIdentifierDao {
    /** Saves `address`, replacing any row with the same key. */
    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = ClientAddressEntity::class)
    suspend fun insertAddress(address: ClientAddressEntity)

    /** Saves `addresses`, replacing rows with the same key. */
    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = ClientAddressEntity::class)
    suspend fun insertAddresses(addresses: List<ClientAddressEntity>)

    /** Observes the `ClientAddress` rows whose `clientID` is `clientId`. */
    @Query("SELECT * FROM ClientAddress WHERE clientID = :clientId")
    fun getAddressesByClientId(clientId: Int): Flow<List<ClientAddressEntity>>

    /** Observes the `ClientAddress` row whose `addressId` is `addressId`. */
    @Query("SELECT * FROM ClientAddress WHERE addressId = :addressId LIMIT 1")
    fun getAddressById(addressId: Int): Flow<ClientAddressEntity?>

    /** Deletes the `ClientAddress` rows whose `clientID` is `clientId`. */
    @Query("DELETE FROM ClientAddress WHERE clientID = :clientId")
    suspend fun deleteAddressesByClientId(clientId: Int)

    /** Observes the addresses with any text field matching `query`, a LIKE pattern. */
    @Query(
        """
    SELECT * FROM ClientAddress 
    WHERE addressLine1 LIKE :query ESCAPE '\'
       OR addressLine2 LIKE :query ESCAPE '\'
       OR addressLine3 LIKE :query ESCAPE '\'
       OR city LIKE :query ESCAPE '\'
       OR stateName LIKE :query ESCAPE '\'
       OR countryName LIKE :query ESCAPE '\'
       OR postalCode LIKE :query ESCAPE '\'
    ORDER BY city
    LIMIT 50
    """,
    )
    fun searchAddressesByQuery(query: String): Flow<List<ClientAddressEntity>>

    /** Saves `identifiers`, replacing rows with the same key. */
    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = ClientIdentifierEntity::class)
    suspend fun insertIdentifiers(identifiers: List<ClientIdentifierEntity>)

    /** Observes the `ClientIdentifier` rows whose `clientId` is `clientId`. */
    @Query("SELECT * FROM ClientIdentifier WHERE clientId = :clientId")
    fun getIdentifiersByClientId(clientId: Int): Flow<List<ClientIdentifierEntity>>

    /** Deletes the `ClientIdentifier` rows whose `clientId` is `clientId`. */
    @Query("DELETE FROM ClientIdentifier WHERE clientId = :clientId")
    suspend fun deleteIdentifiersByClientId(clientId: Int)

    /** Observes the identifiers with any text field matching `query`, a LIKE pattern. */
    @Query(
        """
    SELECT * FROM ClientIdentifier 
    WHERE CAST(id AS TEXT) LIKE :query ESCAPE '\'
       OR documentKey LIKE :query ESCAPE '\'
       OR description LIKE :query ESCAPE '\'
       OR documentTypeName LIKE :query ESCAPE '\'
    ORDER BY documentKey
    LIMIT 50
    """,
    )
    fun searchIdentifiersByQuery(query: String): Flow<List<ClientIdentifierEntity>>

    /**
     * Replace every identifier belonging to the touched clients.
     *
     * Was `ClientDaoHelper.insertIdentifiers`, which hand-rolled a backup/restore around the
     * delete+insert to fake atomicity — reading every row back, then re-inserting it from memory if
     * the write threw. `@Transaction` is that guarantee for free, and a real rollback rather than a
     * best-effort one (the hand-rolled restore could itself throw, which it only logged).
     */
    @Transaction
    suspend fun replaceIdentifiersForClients(identifiers: List<ClientIdentifierEntity>) {
        val valid = identifiers.filter { it.clientId != null }
        if (valid.isEmpty()) return
        valid.mapNotNull { it.clientId }.distinct().forEach { deleteIdentifiersByClientId(it) }
        insertIdentifiers(valid)
    }
}

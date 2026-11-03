/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.charge.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import kotlinx.coroutines.flow.Flow
import kpt.core.base.database.annotation.DbDao
import kpt.core.database.charge.entity.ChargesEntity

/**
 * Created by Pronay Sarker on 14/02/2025 (3:32 PM)
 *
 * Every query is scoped by the OWNER (`resourceType` + `resourceId`), which is also the leading half
 * of the table's primary key — see [ChargesEntity]. The per-owner `clientId` / `loanId` queries this
 * replaced could not cover savings at all, and keyed on `id` they let one owner's row overwrite
 * another's.
 */
@DbDao
@Dao
interface ChargeDao {

    /** Observes the `Charges` rows whose `resourceType` is `resourceType` and `resourceId` is `resourceId`. */
    @Query("SELECT * FROM Charges WHERE resourceType = :resourceType AND resourceId = :resourceId")
    fun observeCharges(resourceType: String, resourceId: Int): Flow<List<ChargesEntity>>

    /** Observes one charge of a resource. */
    @Query(
        "SELECT * FROM Charges WHERE resourceType = :resourceType AND resourceId = :resourceId " +
            "AND id = :chargeId LIMIT 1",
    )
    fun observeCharge(resourceType: String, resourceId: Int, chargeId: Int): Flow<ChargesEntity?>

    /** Saves `charges`, replacing rows with the same key. */
    @Insert(entity = ChargesEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCharges(charges: List<ChargesEntity>)

    /** Deletes the `Charges` rows whose `resourceType` is `resourceType` and `resourceId` is `resourceId`. */
    @Query("DELETE FROM Charges WHERE resourceType = :resourceType AND resourceId = :resourceId")
    suspend fun deleteCharges(resourceType: String, resourceId: Int)

    /** Deletes one charge of a resource. */
    @Query(
        "DELETE FROM Charges WHERE resourceType = :resourceType AND resourceId = :resourceId " +
            "AND id = :chargeId",
    )
    suspend fun deleteCharge(resourceType: String, resourceId: Int, chargeId: Int)

    /** What the logout purge reaches through the charge store's `deleteAll`. */
    @Query("DELETE FROM Charges")
    suspend fun deleteAllCharges()

    /**
     * A fetch is the server's WHOLE list for the owner, so a row it no longer contains was deleted
     * and must leave the table — an upsert-only write kept a waived or deleted charge on screen.
     * Stamps the owner, because the list endpoints do not echo it (loans send no `loanId`).
     */
    @Transaction
    suspend fun replaceCharges(resourceType: String, resourceId: Int, charges: List<ChargesEntity>) {
        deleteCharges(resourceType, resourceId)
        insertAllCharges(charges.map { it.copy(resourceType = resourceType, resourceId = resourceId) })
    }

    /** One charge, stamped with its owner. */
    suspend fun upsertCharge(resourceType: String, resourceId: Int, charge: ChargesEntity) =
        insertAllCharges(listOf(charge.copy(resourceType = resourceType, resourceId = resourceId)))
}

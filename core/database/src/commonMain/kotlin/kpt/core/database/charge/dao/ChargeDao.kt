/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.charge.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow
import kpt.core.base.database.annotation.DbDao
import kpt.core.database.charge.entity.ChargesEntity
import kpt.core.database.client.entity.ClientDateEntity

/**
 * Created by Pronay Sarker on 14/02/2025 (3:32 PM)
 */
@DbDao
@Dao
interface ChargeDao {

    @Query("SELECT * FROM Charges where clientId = :clientId")
    fun getClientCharges(clientId: Int): Flow<List<ChargesEntity>>

    @Insert(entity = ChargesEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCharges(charges: List<ChargesEntity>)

    /**
     * Persist a client's charges, stamping the owning client and splitting `dueDate` into columns.
     *
     * Was `ChargeDaoHelper.saveClientCharges`. Note this one maps semantically — `dueDate` is
     * Fineract's [year, month, day], so day/month/year land in the matching columns — unlike the
     * client/group/center `activationDate` path, which is positional. Both round-trip; they simply
     * disagree about what the columns mean.
     */
    suspend fun saveClientCharges(charges: List<ChargesEntity>, resourceId: Int) {
        insertAllCharges(
            charges.map { charge ->
                val parts = charge.dueDate.orEmpty()
                val due = if (parts.size == 3) {
                    ClientDateEntity(
                        clientId = 0,
                        chargeId = charge.id.toLong(),
                        day = parts[2] ?: 0,
                        month = parts[1] ?: 0,
                        year = parts[0] ?: 0,
                    )
                } else {
                    null
                }
                charge.copy(clientId = resourceId, chargeDueDate = due)
            },
        )
    }
}

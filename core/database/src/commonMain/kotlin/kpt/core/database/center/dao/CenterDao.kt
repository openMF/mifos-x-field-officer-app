/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.center.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow
import kpt.core.base.database.annotation.DbDao
import kpt.core.database.center.entity.CenterAccounts
import kpt.core.database.center.entity.CenterDateEntity
import kpt.core.database.center.entity.CenterEntity
import kpt.core.database.center.entity.CenterPayloadEntity
import kpt.core.database.group.entity.GroupEntity
import kpt.core.database.loan.entity.LoanAccountEntity
import kpt.core.database.savings.entity.SavingsAccountEntity

@DbDao
@Dao
interface CenterDao {

    @Insert(entity = CenterEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCenter(center: CenterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = LoanAccountEntity::class)
    suspend fun saveLoanAccount(loanAccount: LoanAccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = SavingsAccountEntity::class)
    suspend fun saveSavingsAccount(savingsAccount: SavingsAccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = LoanAccountEntity::class)
    suspend fun saveMemberLoanAccount(loanAccount: LoanAccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = CenterPayloadEntity::class)
    suspend fun saveCenterPayload(centerPayload: CenterPayloadEntity)

    @Update(entity = CenterPayloadEntity::class, onConflict = OnConflictStrategy.NONE)
    suspend fun updateCenterPayload(centerPayload: CenterPayloadEntity)

    @Query("DELETE FROM CenterPayload WHERE id = :id")
    suspend fun deleteCenterPayloadById(id: Int)

    @Query("SELECT * FROM Center")
    fun readAllCenters(): Flow<List<CenterEntity>>

    @Query("SELECT * FROM CenterPayload")
    fun readAllCenterPayload(): Flow<List<CenterPayloadEntity>>

    @Query("SELECT * FROM GroupTable WHERE centerId = :centerId")
    fun getCenterAssociateGroups(centerId: Int): Flow<List<GroupEntity>>

    /**
     * Persist a center, deriving its split date columns from `activationDate`.
     *
     * Was `CenterDaoHelper.saveCenter`. The list is written positionally into (day, month, year);
     * see the file note — the names do not match Fineract's ordering but the round-trip does.
     */
    suspend fun saveCenterWithDate(center: CenterEntity) {
        val id = center.id
        val date = if (center.activationDate.size >= 3 && id != null) {
            CenterDateEntity(
                centerId = id.toLong(),
                chargeId = 0,
                day = center.activationDate[0] ?: 0,
                month = center.activationDate[1] ?: 0,
                year = center.activationDate[2] ?: 0,
            )
        } else {
            null
        }
        saveCenter(if (date != null) center.copy(centerDate = date) else center)
    }

    /**
     * Persist a center's accounts across three tables, stamping the owning center onto each.
     *
     * Was `CenterDaoHelper.saveCenterAccounts`, which wrote all three without a transaction — a
     * failure part-way left the center holding some of its accounts and not others.
     */
    @Transaction
    suspend fun saveCenterAccounts(centerAccounts: CenterAccounts, centerId: Int) {
        val owner = centerId.toLong()
        centerAccounts.loanAccounts.forEach { saveLoanAccount(it.copy(centerId = owner)) }
        centerAccounts.savingsAccounts.forEach { saveSavingsAccount(it.copy(centerId = owner)) }
        centerAccounts.memberLoanAccounts.forEach { saveMemberLoanAccount(it.copy(centerId = owner)) }
    }

    /** One queued offline center, for the write store's source of truth. */
    @Query("SELECT * FROM CenterPayload WHERE id = :id LIMIT 1")
    fun observeCenterPayload(id: Int): Flow<CenterPayloadEntity?>
}

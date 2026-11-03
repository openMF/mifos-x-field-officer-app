/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.group.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kpt.core.base.database.annotation.DbDao
import kpt.core.database.group.entity.GroupAccounts
import kpt.core.database.group.entity.GroupDateEntity
import kpt.core.database.group.entity.GroupEntity
import kpt.core.database.loan.entity.LoanAccountEntity
import kpt.core.database.savings.entity.SavingsAccountEntity

/**
 * Created by Pronay Sarker on 15/02/2025 (1:07 PM)
 */
@DbDao
@Dao
interface GroupsDao :
    GroupPayloadDao {
    /** Saves `group`, replacing any row with the same key. */
    @Insert(entity = GroupEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity)

    /** Saves `loanAccount`, replacing any row with the same key. */
    @Insert(entity = LoanAccountEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoanAccount(loanAccount: LoanAccountEntity)

    /** Saves `savingsAccount`, replacing any row with the same key. */
    @Insert(entity = SavingsAccountEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsAccount(savingsAccount: SavingsAccountEntity)

    /** Observes the `GroupTable` rows. */
    @Query("SELECT * FROM GroupTable")
    fun getAllGroups(): Flow<List<GroupEntity>>

    /** Reads one page of groups. */
    @Query("SELECT * FROM GroupTable LIMIT :limit OFFSET :offset")
    suspend fun getAllGroups(offset: Int, limit: Int): List<GroupEntity>

    /** Observes the `GroupTable` row whose `id` is `groupId`. */
    @Query("SELECT * FROM GroupTable WHERE id = :groupId")
    fun getGroupById(groupId: Int): Flow<GroupEntity>

    /** Observes the `LoanAccountEntity` rows whose `groupId` is `groupId`. */
    @Query("SELECT * FROM LoanAccountEntity WHERE groupId = :groupId")
    fun getLoanAccountsByGroupId(groupId: Int): Flow<List<LoanAccountEntity>>

    /** Observes the `SavingsAccount` rows whose `groupId` is `groupId`. */
    @Query("SELECT * FROM SavingsAccount WHERE groupId = :groupId")
    fun getSavingsAccountsByGroupId(groupId: Int): Flow<List<SavingsAccountEntity>>

    /** Persist a group, deriving its split date columns. Was `GroupsDaoHelper.saveGroup`. */
    suspend fun saveGroupWithDate(group: GroupEntity) {
        val id = group.id
        val date = if (group.activationDate.size >= 3 && id != null) {
            GroupDateEntity(
                groupId = id.toLong(),
                chargeId = 0,
                day = group.activationDate[0] ?: 0,
                month = group.activationDate[1] ?: 0,
                year = group.activationDate[2] ?: 0,
            )
        } else {
            null
        }
        insertGroup(if (date != null) group.copy(groupDate = date) else group)
    }

    /**
     * Read a group back with `activationDate` rebuilt from its columns — the inverse of
     * [saveGroupWithDate], so a stored group round-trips. Was `GroupsDaoHelper.getGroup`.
     */
    fun observeGroupWithDate(groupId: Int): Flow<GroupEntity> =
        getGroupById(groupId).map { group ->
            group.copy(
                activationDate = listOf(
                    group.groupDate?.day ?: 0,
                    group.groupDate?.month ?: 0,
                    group.groupDate?.year ?: 0,
                ),
            )
        }

    /** Was `GroupsDaoHelper.saveGroupAccounts` — now atomic across both tables. */
    @Transaction
    suspend fun saveGroupAccounts(groupAccounts: GroupAccounts, groupId: Int) {
        val owner = groupId.toLong()
        groupAccounts.loanAccounts.forEach { insertLoanAccount(it.copy(groupId = owner)) }
        groupAccounts.savingsAccounts.forEach { insertSavingsAccount(it.copy(groupId = owner)) }
    }

    /**
     * One group row, nullable — [getGroupById] declares a non-null row and so cannot say "absent",
     * which a write store's reader has to.
     */
    @Query("SELECT * FROM GroupTable WHERE id = :id LIMIT 1")
    fun observeGroup(id: Int): Flow<GroupEntity?>

    /** Deletes the `GroupTable` rows whose `id` is `id`. */
    @Query("DELETE FROM GroupTable WHERE id = :id")
    suspend fun deleteGroupById(id: Int)

    /** What the `groups` store's `deleteAll` runs at logout — the rows must not outlive the session. */
    @Query("DELETE FROM GroupTable")
    suspend fun deleteAllGroups()
}

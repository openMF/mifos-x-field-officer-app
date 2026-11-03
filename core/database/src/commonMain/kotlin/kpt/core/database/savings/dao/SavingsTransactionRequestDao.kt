/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.savings.dao

import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow
import kpt.core.database.savings.entity.SavingsAccountTransactionRequestEntity

/** Offline savings-transaction request queue. */
interface SavingsTransactionRequestDao {
    /** Saves `transactionRequest`, replacing any row with the same key. */
    @Insert(entity = SavingsAccountTransactionRequestEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsAccountTransactionRequest(transactionRequest: SavingsAccountTransactionRequestEntity)

    /** Updates the stored row of `transactionRequest`. */
    @Update(entity = SavingsAccountTransactionRequestEntity::class, onConflict = OnConflictStrategy.NONE)
    suspend fun updateSavingsAccountTransactionRequest(transactionRequest: SavingsAccountTransactionRequestEntity)

    /** Deletes the `SavingsAccountTransactionRequest` rows whose `savingAccountId` is `savingsAccountId`. */
    @Query("DELETE FROM SavingsAccountTransactionRequest where savingAccountId = :savingsAccountId")
    suspend fun deleteSavingsAccountTransactionRequest(savingsAccountId: Int)

    /** The logout purge for the offline savings-transaction queue. */
    @Query("DELETE FROM SavingsAccountTransactionRequest")
    suspend fun deleteAllSavingsAccountTransactionRequests()

    /** Observes the `SavingsAccountTransactionRequest` rows. */
    @Query("SELECT * FROM SavingsAccountTransactionRequest")
    fun getAllSavingsAccountTransactionRequest(): Flow<List<SavingsAccountTransactionRequestEntity>>

    /** Observes the `SavingsAccountTransactionRequest` row whose `savingAccountId` is `savingsAccountId`. */
    @Query("SELECT * FROM SavingsAccountTransactionRequest where savingAccountId = :savingsAccountId")
    fun getSavingsAccountTransactionRequest(savingsAccountId: Int): Flow<SavingsAccountTransactionRequestEntity?>

    /**
     * Queue an offline savings transaction, stamping the account, its type and the transaction kind.
     * Was `SavingsDaoHelper.saveSavingsAccountTransaction`.
     */
    suspend fun saveTransactionRequestFor(
        savingsAccountType: String?,
        savingsAccountId: Int,
        transactionType: String?,
        request: SavingsAccountTransactionRequestEntity,
    ) {
        insertSavingsAccountTransactionRequest(
            request.copy(
                savingAccountId = savingsAccountId,
                savingsAccountType = savingsAccountType,
                transactionType = transactionType,
            ),
        )
    }
}

/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.savings.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kpt.core.base.database.annotation.DbDao
import kpt.core.database.payment.entity.PaymentTypeOptionEntity
import kpt.core.database.savings.entity.SavingsAccountTransactionEntity
import kpt.core.database.savings.entity.SavingsAccountTransactionRequestEntity
import kpt.core.database.savings.entity.SavingsAccountTransactionTemplateEntity
import kpt.core.database.savings.entity.SavingsAccountWithAssociationsEntity
import kpt.core.database.savings.entity.SavingsTransactionDateEntity

@DbDao
@Dao
interface SavingsDao {

    @Insert(entity = SavingsAccountTransactionEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTransactions(transaction: List<SavingsAccountTransactionEntity>)

    @Insert(entity = SavingsAccountWithAssociationsEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsAccountWithAssociations(savingsAccountWithAssociations: SavingsAccountWithAssociationsEntity)

    @Insert(entity = SavingsAccountTransactionRequestEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsAccountTransactionRequest(transactionRequest: SavingsAccountTransactionRequestEntity)

    @Insert(entity = PaymentTypeOptionEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPaymentTypeOption(paymentTypeOption: List<PaymentTypeOptionEntity>)

    @Insert(entity = SavingsAccountTransactionTemplateEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsAccountTransactionTemplate(transactionTemplate: SavingsAccountTransactionTemplateEntity)

    @Update(entity = SavingsAccountTransactionRequestEntity::class, onConflict = OnConflictStrategy.NONE)
    suspend fun updateSavingsAccountTransactionRequest(transactionRequest: SavingsAccountTransactionRequestEntity)

    @Query("SELECT * FROM TransactionTable WHERE savingsAccountId = :savingsAccountId")
    suspend fun getAllTransactions(savingsAccountId: Int): List<SavingsAccountTransactionEntity>

    @Query("DELETE FROM SavingsAccountTransactionRequest where savingAccountId = :savingsAccountId")
    suspend fun deleteSavingsAccountTransactionRequest(savingsAccountId: Int)

    @Query("SELECT * FROM SavingsAccountTransactionRequest")
    fun getAllSavingsAccountTransactionRequest(): Flow<List<SavingsAccountTransactionRequestEntity>>

    @Query("SELECT * FROM SavingsAccountTransactionRequest where savingAccountId = :savingsAccountId")
    fun getSavingsAccountTransactionRequest(savingsAccountId: Int): Flow<SavingsAccountTransactionRequestEntity?>

    @Query("SELECT * FROM SavingsAccountTransactionTemplate where accountId = :savingsAccountId")
    fun getSavingsAccountTransactionTemplate(savingsAccountId: Int): Flow<SavingsAccountTransactionTemplateEntity?>

    @Query("SELECT * FROM SavingsAccountWithAssociations where id = :savingsAccountId")
    fun getSavingsAccountWithAssociations(savingsAccountId: Int): Flow<SavingsAccountWithAssociationsEntity?>

    @Query("SELECT * FROM PaymentTypeOption")
    fun getAllPaymentTypeOption(): Flow<List<PaymentTypeOptionEntity>>

    /**
     * Persist a savings account with its transactions, stamping the account onto each transaction
     * and the summary, and splitting each transaction date into columns.
     * Was `SavingsDaoHelper.saveSavingsAccount` — now atomic across both tables.
     */
    @Transaction
    suspend fun saveSavingsAccountWithTransactions(
        account: SavingsAccountWithAssociationsEntity,
    ): SavingsAccountWithAssociationsEntity {
        val transactions = account.transactions.map { transaction ->
            val id = transaction.id ?: return@map transaction
            transaction.copy(
                savingsAccountId = account.id,
                savingsTransactionDate = SavingsTransactionDateEntity(
                    transactionId = id,
                    year = transaction.date.getOrNull(0),
                    month = transaction.date.getOrNull(1),
                    day = transaction.date.getOrNull(2),
                ),
            )
        }
        insertAllTransactions(transactions)
        val shaped = account.copy(
            transactions = transactions,
            summary = account.summary?.copy(savingsId = account.id),
        )
        insertSavingsAccountWithAssociations(shaped)
        return shaped
    }

    /**
     * Read a savings account back with its transactions and their date lists rebuilt — the inverse
     * of [saveSavingsAccountWithTransactions]. Was `SavingsDaoHelper.readSavingsAccount`.
     */
    fun observeSavingsAccountWithTransactions(
        savingsAccountId: Int,
    ): Flow<SavingsAccountWithAssociationsEntity?> =
        getSavingsAccountWithAssociations(savingsAccountId).map { account ->
            account?.copy(
                transactions = getAllTransactions(savingsAccountId).map { transaction ->
                    transaction.copy(
                        date = listOf(
                            transaction.savingsTransactionDate?.year,
                            transaction.savingsTransactionDate?.month,
                            transaction.savingsTransactionDate?.day,
                        ),
                    )
                },
            )
        }

    /**
     * Persist a transaction template with its payment-type options.
     * Was `SavingsDaoHelper.saveSavingsAccountTransactionTemplate` — now atomic.
     */
    @Transaction
    suspend fun saveTransactionTemplateWithOptions(
        template: SavingsAccountTransactionTemplateEntity,
    ) {
        insertAllPaymentTypeOption(template.paymentTypeOptions)
        insertSavingsAccountTransactionTemplate(template)
    }

    /**
     * Read a transaction template back with its payment-type options attached.
     * Was `SavingsDaoHelper.readSavingsAccountTransactionTemplate`.
     */
    fun observeTransactionTemplateWithOptions(
        savingsAccountId: Int,
    ): Flow<SavingsAccountTransactionTemplateEntity?> =
        getSavingsAccountTransactionTemplate(savingsAccountId).map { template ->
            template?.copy(paymentTypeOptions = getAllPaymentTypeOption().first())
        }

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

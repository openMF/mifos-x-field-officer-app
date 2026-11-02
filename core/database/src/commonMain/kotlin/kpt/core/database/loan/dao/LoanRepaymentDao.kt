/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.loan.dao

import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kpt.core.database.loan.entity.LoanRepaymentRequestEntity
import kpt.core.database.loan.entity.LoanRepaymentTemplateEntity
import kpt.core.database.payment.entity.PaymentTypeOptionEntity
import kpt.core.database.utils.getCurrentTimeInMillis

/** Repayment queue, repayment template and payment-type options. */
interface LoanRepaymentDao {
    @Insert(entity = LoanRepaymentRequestEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoanRepaymentTransaction(request: LoanRepaymentRequestEntity)

    @Insert(entity = PaymentTypeOptionEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentTypeOption(paymentTypeOption: PaymentTypeOptionEntity)

    @Query("SELECT * FROM PaymentTypeOption")
    fun getPaymentTypeOptions(): Flow<List<PaymentTypeOptionEntity>>

    @Query("SELECT * FROM LoanRepaymentRequestEntity WHERE loanId = :loanId LIMIT 1")
    suspend fun getLoanRepaymentRequest(loanId: Int): LoanRepaymentRequestEntity?

    @Update(entity = LoanRepaymentRequestEntity::class, onConflict = OnConflictStrategy.NONE)
    suspend fun updateLoanRepaymentRequest(loanRepaymentRequest: LoanRepaymentRequestEntity)

    @Query("SELECT * FROM LoanRepaymentRequestEntity ORDER BY timeStamp ASC")
    fun readAllLoanRepaymentTransaction(): Flow<List<LoanRepaymentRequestEntity>>

    @Insert(entity = LoanRepaymentTemplateEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoanRepaymentTemplate(template: LoanRepaymentTemplateEntity)

    @Query("SELECT * FROM LoanRepaymentTemplate WHERE loanId = :loanId LIMIT 1")
    suspend fun getLoanRepaymentTemplate(loanId: Int): LoanRepaymentTemplateEntity?

    @Query("DELETE FROM LoanRepaymentTemplate WHERE loanId = :loanId")
    suspend fun deleteLoanRepaymentByLoanId(loanId: Int)

    @Query("DELETE FROM LoanRepaymentTemplate")
    suspend fun deleteAllLoanRepaymentTemplates()

    /**
     * Queue an offline repayment, stamping the loan and the submission time (seconds, which is what
     * the sync payload expects). Was `LoanDaoHelper.saveLoanRepaymentTransaction`.
     */
    suspend fun saveLoanRepaymentTransaction(loanId: Int, request: LoanRepaymentRequestEntity) {
        insertLoanRepaymentTransaction(
            request.copy(loanId = loanId, timeStamp = getCurrentTimeInMillis() / 1000),
        )
    }

    /**
     * Persist a repayment template with its payment-type options.
     * Was `LoanDaoHelper.saveLoanRepaymentTemplate` — now atomic, so the template cannot land
     * without the options a repayment form needs to render.
     */
    @Transaction
    suspend fun saveLoanRepaymentTemplateFor(
        loanId: Int,
        template: LoanRepaymentTemplateEntity,
    ): LoanRepaymentTemplateEntity {
        val stamped = template.copy(loanId = loanId)
        stamped.paymentTypeOptions?.forEach { insertPaymentTypeOption(it) }
        insertLoanRepaymentTemplate(stamped)
        return stamped
    }

    /**
     * Read a repayment template back with its payment-type options attached — they live in a
     * separate table. Was `LoanDaoHelper.getLoanRepayTemplate`.
     */
    fun observeLoanRepaymentTemplate(loanId: Int): Flow<LoanRepaymentTemplateEntity?> =
        getPaymentTypeOptions().map { options ->
            getLoanRepaymentTemplate(loanId)?.copy(paymentTypeOptions = options.toMutableList())
        }

    /**
     * One queued offline repayment, keyed by loan — the table's own primary key is `timeStamp`, but
     * every read, write and delete path addresses it by `loanId`, which is what the queue is keyed by.
     */
    @Query("SELECT * FROM LoanRepaymentRequestEntity WHERE loanId = :loanId LIMIT 1")
    fun observeLoanRepaymentRequest(loanId: Int): Flow<LoanRepaymentRequestEntity?>

    /**
     * Drop a QUEUED repayment. The queue store used `deleteLoanRepaymentByLoanId`, which deletes
     * from `LoanRepaymentTemplate` — the cached form template — so clearing a synced or discarded
     * repayment left the request queued (to be sent again) and wiped the template instead.
     */
    @Query("DELETE FROM LoanRepaymentRequestEntity WHERE loanId = :loanId")
    suspend fun deleteLoanRepaymentRequestByLoanId(loanId: Int)

    /** The logout purge for the offline repayment queue. */
    @Query("DELETE FROM LoanRepaymentRequestEntity")
    suspend fun deleteAllLoanRepaymentRequests()
}

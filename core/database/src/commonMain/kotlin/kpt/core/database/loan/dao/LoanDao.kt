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

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kpt.core.base.database.annotation.DbDao
import kpt.core.database.loan.entity.ActualDisbursementDateEntity
import kpt.core.database.loan.entity.LoanWithAssociationsEntity

@DbDao
@Dao
interface LoanDao :
    LoanRepaymentDao,
    LoanTemplateCacheDao {
    @Insert(entity = LoanWithAssociationsEntity::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLoanWithAssociations(loanWithAssociations: LoanWithAssociationsEntity)

    @Query("SELECT * FROM LoanWithAssociations WHERE id = :loanId LIMIT 1")
    fun getLoanById(loanId: Int): Flow<LoanWithAssociationsEntity?>

    /** Per-key clear for the `loans` store (`Store.clear(loanId)`). */
    @Query("DELETE FROM LoanWithAssociations WHERE id = :loanId")
    suspend fun deleteLoanById(loanId: Int)

    /** What the logout purge reaches through the `loans` store's `deleteAll`. */
    @Query("DELETE FROM LoanWithAssociations")
    suspend fun deleteAllLoans()

    /**
     * Persist a loan, stamping its id onto the summary and timeline rows and splitting the
     * timeline's `actualDisbursementDate` into columns. Was `LoanDaoHelper.saveLoanById`.
     */
    @Transaction
    suspend fun saveLoanWithAssociationsShaped(loan: LoanWithAssociationsEntity) {
        saveLoanWithAssociations(
            loan.copy(
                summary = loan.summary.copy(loanId = loan.id),
                timeline = loan.timeline.copy(
                    loanId = loan.id,
                    actualDisburseDate = loan.timeline.actualDisbursementDate?.let {
                        ActualDisbursementDateEntity(
                            loanId = loan.id,
                            year = it.getOrNull(0),
                            month = it.getOrNull(1),
                            date = it.getOrNull(2),
                        )
                    },
                ),
            ),
        )
    }

    /**
     * Read a loan back with `actualDisbursementDate` rebuilt — the inverse of
     * [saveLoanWithAssociationsShaped]. Was `LoanDaoHelper.getLoanById`.
     */
    fun observeLoanWithAssociationsShaped(loanId: Int): Flow<LoanWithAssociationsEntity?> =
        getLoanById(loanId).map { loan ->
            loan?.copy(
                timeline = loan.timeline.copy(
                    actualDisbursementDate = listOf(
                        loan.timeline.actualDisburseDate?.year,
                        loan.timeline.actualDisburseDate?.month,
                        loan.timeline.actualDisburseDate?.date,
                    ),
                ),
            )
        }
}

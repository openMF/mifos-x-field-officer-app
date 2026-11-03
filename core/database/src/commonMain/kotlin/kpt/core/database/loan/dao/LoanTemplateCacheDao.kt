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

import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow
import kpt.core.database.loan.entity.LoanDisburseTemplateCacheEntity
import kpt.core.database.loan.entity.LoanTemplateCacheEntity

/** Application + disbursement template caches (payload-blob rows). */
interface LoanTemplateCacheDao {
    //
    // Both are payload-blob rows: an officer out of signal still needs the option lists to fill in
    // an application or a disbursement, and those lists only ever travel whole.

    /** Observes the cached loan template for a client and product. */
    @Query(
        "SELECT * FROM loan_application_templates " +
            "WHERE clientId = :clientId AND productId = :productId LIMIT 1",
    )
    fun observeLoanTemplate(clientId: Int, productId: Int): Flow<LoanTemplateCacheEntity?>

    /** Inserts `template`, or replaces the row with the same key. */
    @Upsert
    suspend fun upsertLoanTemplate(template: LoanTemplateCacheEntity)

    /** Observes the `loan_disburse_templates` row whose `loanId` is `loanId`. */
    @Query("SELECT * FROM loan_disburse_templates WHERE loanId = :loanId LIMIT 1")
    fun observeLoanDisburseTemplate(loanId: Int): Flow<LoanDisburseTemplateCacheEntity?>

    /** Inserts `template`, or replaces the row with the same key. */
    @Upsert
    suspend fun upsertLoanDisburseTemplate(template: LoanDisburseTemplateCacheEntity)

    /**
     * Deletes the `loan_application_templates` rows whose `clientId` is `clientId` and `productId` is `productId`.
     */
    @Query("DELETE FROM loan_application_templates WHERE clientId = :clientId AND productId = :productId")
    suspend fun deleteLoanTemplate(clientId: Int, productId: Int)

    /** Deletes every `loan_application_templates` row. */
    @Query("DELETE FROM loan_application_templates")
    suspend fun deleteAllLoanTemplates()

    /** Deletes the `loan_disburse_templates` rows whose `loanId` is `loanId`. */
    @Query("DELETE FROM loan_disburse_templates WHERE loanId = :loanId")
    suspend fun deleteLoanDisburseTemplate(loanId: Int)

    /** Deletes every `loan_disburse_templates` row. */
    @Query("DELETE FROM loan_disburse_templates")
    suspend fun deleteAllLoanDisburseTemplates()
}

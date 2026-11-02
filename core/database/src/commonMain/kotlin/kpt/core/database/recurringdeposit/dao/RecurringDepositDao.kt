/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.recurringdeposit.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow
import kpt.core.base.database.annotation.DbDao
import kpt.core.database.recurringdeposit.entity.RecurringDepositTemplateEntity

@DbDao
@Dao
interface RecurringDepositDao {

    /** Observe the cached opening template for one client. */
    @Query("SELECT * FROM recurring_deposit_templates WHERE clientId = :clientId LIMIT 1")
    fun observeTemplate(clientId: Int): Flow<RecurringDepositTemplateEntity?>

    @Query("SELECT * FROM recurring_deposit_templates WHERE clientId = :clientId LIMIT 1")
    suspend fun getTemplate(clientId: Int): RecurringDepositTemplateEntity?

    @Upsert
    suspend fun upsert(template: RecurringDepositTemplateEntity)

    @Query("DELETE FROM recurring_deposit_templates WHERE clientId = :clientId")
    suspend fun deleteForClient(clientId: Int)

    @Query("DELETE FROM recurring_deposit_templates")
    suspend fun deleteAll()
}

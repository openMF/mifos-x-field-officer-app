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

/** Room queries for recurring deposit rows. */
@DbDao
@Dao
interface RecurringDepositDao {

    /** Observe the cached opening template for one client. */
    @Query("SELECT * FROM recurring_deposit_templates WHERE clientId = :clientId LIMIT 1")
    fun observeTemplate(clientId: Int): Flow<RecurringDepositTemplateEntity?>

    /** Reads the `recurring_deposit_templates` row whose `clientId` is `clientId`. */
    @Query("SELECT * FROM recurring_deposit_templates WHERE clientId = :clientId LIMIT 1")
    suspend fun getTemplate(clientId: Int): RecurringDepositTemplateEntity?

    /** Inserts `template`, or replaces the row with the same key. */
    @Upsert
    suspend fun upsert(template: RecurringDepositTemplateEntity)

    /** Deletes the `recurring_deposit_templates` rows whose `clientId` is `clientId`. */
    @Query("DELETE FROM recurring_deposit_templates WHERE clientId = :clientId")
    suspend fun deleteForClient(clientId: Int)

    /** Deletes every `recurring_deposit_templates` row. */
    @Query("DELETE FROM recurring_deposit_templates")
    suspend fun deleteAll()
}

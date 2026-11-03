/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.client.dao

import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow
import kpt.core.database.client.entity.ClientsTemplateEntity
import kpt.core.database.client.entity.InterestTypeEntity
import kpt.core.database.client.entity.OfficeOptionsEntity
import kpt.core.database.client.entity.OptionsEntity
import kpt.core.database.client.entity.SavingProductOptionsEntity
import kpt.core.database.client.entity.StaffOptionsEntity

/** Client template + option-table reads/writes. */
interface ClientTemplateOptionsDao {
    /** Saves `clientsTemplate`, replacing any row with the same key. */
    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = ClientsTemplateEntity::class)
    suspend fun insertClientsTemplate(clientsTemplate: ClientsTemplateEntity)

    /** Saves `officeOptions`, replacing rows with the same key. */
    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = OfficeOptionsEntity::class)
    suspend fun insertOfficeOptions(officeOptions: List<OfficeOptionsEntity>)

    /** Saves `staffOptions`, replacing rows with the same key. */
    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = StaffOptionsEntity::class)
    suspend fun insertStaffOptions(staffOptions: List<StaffOptionsEntity>)

    /** Saves `savingProductOptions`, replacing rows with the same key. */
    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = SavingProductOptionsEntity::class)
    suspend fun insertSavingProductOptions(savingProductOptions: List<SavingProductOptionsEntity>)

    /** Saves `options`, replacing any row with the same key. */
    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = OptionsEntity::class)
    suspend fun insertOption(options: OptionsEntity)

    /** Saves `interestTypes`, replacing rows with the same key. */
    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = InterestTypeEntity::class)
    suspend fun insertInterestTypes(interestTypes: List<InterestTypeEntity>)

    /** Reads the `ClientsTemplate` row. */
    @Query("SELECT * FROM ClientsTemplate LIMIT 1")
    suspend fun getClientsTemplate(): ClientsTemplateEntity

    /** Observes the `ClientTemplateOfficeOptions` rows. */
    @Query("SELECT * FROM ClientTemplateOfficeOptions")
    fun getOfficeOptions(): Flow<List<OfficeOptionsEntity>>

    /** Observes the `ClientTemplateStaffOptions` rows. */
    @Query("SELECT * FROM ClientTemplateStaffOptions")
    fun getStaffOptions(): Flow<List<StaffOptionsEntity>>

    /** Observes the `ClientTemplateSavingProductsOptions` rows. */
    @Query("SELECT * FROM ClientTemplateSavingProductsOptions")
    fun getSavingProductOptions(): Flow<List<SavingProductOptionsEntity>>

    /** Observes the `ClientTemplateOptions` rows whose `optionType` is `optionType`. */
    @Query("SELECT * FROM ClientTemplateOptions WHERE optionType = :optionType")
    fun getOptions(optionType: String): Flow<List<OptionsEntity>>

    /** Observes the `ClientTemplateInterest` rows. */
    @Query("SELECT * FROM ClientTemplateInterest")
    fun getAllInterestType(): Flow<List<InterestTypeEntity>>
}

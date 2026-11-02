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
    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = ClientsTemplateEntity::class)
    suspend fun insertClientsTemplate(clientsTemplate: ClientsTemplateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = OfficeOptionsEntity::class)
    suspend fun insertOfficeOptions(officeOptions: List<OfficeOptionsEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = StaffOptionsEntity::class)
    suspend fun insertStaffOptions(staffOptions: List<StaffOptionsEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = SavingProductOptionsEntity::class)
    suspend fun insertSavingProductOptions(savingProductOptions: List<SavingProductOptionsEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = OptionsEntity::class)
    suspend fun insertOption(options: OptionsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = InterestTypeEntity::class)
    suspend fun insertInterestTypes(interestTypes: List<InterestTypeEntity>)

    @Query("SELECT * FROM ClientsTemplate LIMIT 1")
    suspend fun getClientsTemplate(): ClientsTemplateEntity

    @Query("SELECT * FROM ClientTemplateOfficeOptions")
    fun getOfficeOptions(): Flow<List<OfficeOptionsEntity>>

    @Query("SELECT * FROM ClientTemplateStaffOptions")
    fun getStaffOptions(): Flow<List<StaffOptionsEntity>>

    @Query("SELECT * FROM ClientTemplateSavingProductsOptions")
    fun getSavingProductOptions(): Flow<List<SavingProductOptionsEntity>>

    @Query("SELECT * FROM ClientTemplateOptions WHERE optionType = :optionType")
    fun getOptions(optionType: String): Flow<List<OptionsEntity>>

    @Query("SELECT * FROM ClientTemplateInterest")
    fun getAllInterestType(): Flow<List<InterestTypeEntity>>
}

/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.client.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable
import kpt.core.base.database.annotation.DbEntity
import kpt.core.database.datatable.entity.DataTableEntity
/** Room row for the new-client template. */
@Serializable
@DbEntity
@Entity(
    indices = [],
    inheritSuperIndices = false,
    primaryKeys = [],
    foreignKeys = [],
    ignoredColumns = [],
    tableName = "ClientsTemplate",
)
data class ClientsTemplateEntity(
    /** The date the record was activated. */
    val activationDate: List<Int> = emptyList(),

    /** The server id of the office. */
    @PrimaryKey(autoGenerate = true)
    val officeId: Int = 0,

    /** The offices that may be chosen. */
    val officeOptions: List<OfficeOptionsEntity>? = emptyList(),

    /** The staff members that may be assigned. */
    val staffOptions: List<StaffOptionsEntity>? = emptyList(),

    /** The savings products that may be chosen. */
    val savingProductOptions: List<SavingProductOptionsEntity>? = emptyList(),

    /** The genders that may be chosen. */
    val genderOptions: List<OptionsEntity>? = emptyList(),

    /** The client types that may be chosen. */
    val clientTypeOptions: List<OptionsEntity>? = emptyList(),

    /** The client classifications that may be chosen. */
    val clientClassificationOptions: List<OptionsEntity>? = emptyList(),

    /** The legal forms that may be chosen. */
    val clientLegalFormOptions: List<InterestTypeEntity>? = emptyList(),

    /** The data tables registered for this entity. */
    val dataTables: List<DataTableEntity>? = emptyList(),
)

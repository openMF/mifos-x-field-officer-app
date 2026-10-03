/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.client.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable
import kpt.core.base.database.annotation.DbEntity

@DbEntity
@Entity(
    tableName = "ClientAddress",
    indices = [],
    inheritSuperIndices = false,
    primaryKeys = [],
    foreignKeys = [],
    ignoredColumns = [],
)
@Serializable
data class ClientAddressEntity(
    @PrimaryKey(autoGenerate = true)
    val addressId: Int = 0,
    @ColumnInfo(
        name = "clientID",
        index = true,
        typeAffinity = ColumnInfo.UNDEFINED,
        collate = ColumnInfo.UNSPECIFIED,
        defaultValue = ColumnInfo.VALUE_UNSPECIFIED,
    )
    val clientId: Int = -1,
    val addressType: String = "",
    val addressTypeId: Int = -1,
    val isActive: Boolean = false,
    val addressLine1: String = "",
    val addressLine2: String = "",
    val addressLine3: String = "",
    val city: String = "",
    val stateProvinceId: Int = -1,
    val countryName: String = "",
    val stateName: String = "",
    val countryId: Int = -1,
    val postalCode: String = "",
)

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

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable
import kpt.core.base.database.annotation.DbEntity

/** Room row for a client address. */
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
    /** The server id of the address. */
    @PrimaryKey(autoGenerate = true)
    val addressId: Int = 0,
    /** The server id of the client. */
    @ColumnInfo(
        name = "clientID",
        index = true,
        typeAffinity = ColumnInfo.UNDEFINED,
        collate = ColumnInfo.UNSPECIFIED,
        defaultValue = ColumnInfo.VALUE_UNSPECIFIED,
    )
    val clientId: Int = -1,
    /** The address type. */
    val addressType: String = "",
    /** The server id of the address type. */
    val addressTypeId: Int = -1,
    /** True when the record is active. */
    val isActive: Boolean = false,
    /** The first address line. */
    val addressLine1: String = "",
    /** The second address line. */
    val addressLine2: String = "",
    /** The third address line. */
    val addressLine3: String = "",
    /** The city. */
    val city: String = "",
    /** The server id of the state or province. */
    val stateProvinceId: Int = -1,
    /** The name of the country. */
    val countryName: String = "",
    /** The name of the state or province. */
    val stateName: String = "",
    /** The server id of the country. */
    val countryId: Int = -1,
    /** The postal code. */
    val postalCode: String = "",
)

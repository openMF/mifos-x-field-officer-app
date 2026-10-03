/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.clients

import kotlinx.serialization.Serializable
@Serializable
data class ClientAddressEntity(
    val clientID: Int = -1,
    val addressType: String = "",
    val addressId: Int = -1,
    val addressTypeId: Int = -1,
    val isActive: Boolean = false,
    val street: String = "",
    val addressLine1: String = "",
    val addressLine2: String = "",
    val addressLine3: String = "",
    val townVillage: String = "",
    val city: String = "",
    val countyDistrict: String = "",
    val stateProvinceId: Int = -1,
    val countryName: String = "",
    val stateName: String = "",
    val countryId: Int = -1,
    val postalCode: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val createdBy: String = "",
    val createdOn: String = "",
    val updatedBy: String = "",
    val updatedOn: String = "",
)

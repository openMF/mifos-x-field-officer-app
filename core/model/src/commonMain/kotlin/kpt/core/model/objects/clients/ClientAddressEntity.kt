/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.clients

import kotlinx.serialization.Serializable
/** Room row for a client address. */
@Serializable
data class ClientAddressEntity(
    /** The server id of the client. */
    val clientID: Int = -1,
    /** The address type. */
    val addressType: String = "",
    /** The server id of the address. */
    val addressId: Int = -1,
    /** The server id of the address type. */
    val addressTypeId: Int = -1,
    /** True when the record is active. */
    val isActive: Boolean = false,
    /** The street. */
    val street: String = "",
    /** The first address line. */
    val addressLine1: String = "",
    /** The second address line. */
    val addressLine2: String = "",
    /** The third address line. */
    val addressLine3: String = "",
    /** The town or village. */
    val townVillage: String = "",
    /** The city. */
    val city: String = "",
    /** The county or district. */
    val countyDistrict: String = "",
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
    /** The latitude. */
    val latitude: Double = 0.0,
    /** The longitude. */
    val longitude: Double = 0.0,
    /** Who created the address. */
    val createdBy: String = "",
    /** When the record was created. */
    val createdOn: String = "",
    /** Who last updated the address. */
    val updatedBy: String = "",
    /** When the record was last updated. */
    val updatedOn: String = "",
)

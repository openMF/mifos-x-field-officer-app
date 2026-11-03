/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.clients

import kotlinx.serialization.Serializable

/**
 * Created by Rajan Maurya on 15/12/16.
 */
@Serializable
data class Address(
    /** The server id of the address type. */
    val addressTypeId: Int? = null,

    /** True when the record is active. */
    val isActive: Boolean = false,

    /** The first address line. */
    val addressLine1: String? = null,

    /** The second address line. */
    val addressLine2: String? = null,

    /** The third address line. */
    val addressLine3: String? = null,

    /** The city. */
    val city: String? = null,

    /** The server id of the state or province. */
    val stateProvinceId: Int? = null,

    /** The server id of the country. */
    val countryId: Int? = null,

    /** The postal code. */
    val postalCode: String? = null,
)

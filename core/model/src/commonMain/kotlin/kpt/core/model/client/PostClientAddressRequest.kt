/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.client

import kotlinx.serialization.Serializable
/** Request body for a post client address. */
@Serializable
data class PostClientAddressRequest(
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

    /** The server id of the country. */
    val countryId: Int = -1,

    /** The postal code. */
    val postalCode: String = "",
)

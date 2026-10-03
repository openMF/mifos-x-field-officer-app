/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.client.dto

import kotlinx.serialization.Serializable
@Serializable
data class PostClientAddressRequest(
    val addressLine1: String = "",

    val addressLine2: String = "",

    val addressLine3: String = "",

    val city: String = "",

    val stateProvinceId: Int = -1,

    val countryId: Int = -1,

    val postalCode: String = "",
)

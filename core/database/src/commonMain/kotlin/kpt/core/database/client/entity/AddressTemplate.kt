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

import kotlinx.serialization.Serializable

/** Server template for an address: the options its form offers. */
@Serializable
data class AddressTemplate(
    /** The address types that may be chosen. */
    val addressTypeIdOptions: List<OptionsEntity> = emptyList(),
    /** The countries that may be chosen. */
    val countryIdOptions: List<OptionsEntity> = emptyList(),
    /** The states or provinces that may be chosen. */
    val stateProvinceIdOptions: List<OptionsEntity> = emptyList(),
)

/** An address configuration. */
@Serializable
data class AddressConfiguration(
    /** True when address capture is enabled on the server. */
    val enabled: Boolean = false,
)

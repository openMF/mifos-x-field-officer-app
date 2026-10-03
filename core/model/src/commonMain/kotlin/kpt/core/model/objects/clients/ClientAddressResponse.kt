/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.clients

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClientAddressResponse(
    var id: Int? = null,

    @SerialName("client_id")
    var clientId: Int? = null,

    var latitude: Double? = null,

    var longitude: Double? = null,

    @SerialName("description")
    var placeAddress: String? = null,

    var placeId: String? = null,
)

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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Server response to a client address request. */
@Serializable
data class ClientAddressResponse(
    /** The server id of this record. */
    var id: Int? = null,

    /** The server id of the client. */
    @SerialName("client_id")
    var clientId: Int? = null,

    /** The latitude. */
    var latitude: Double? = null,

    /** The longitude. */
    var longitude: Double? = null,

    /** The address of the picked place. */
    @SerialName("description")
    var placeAddress: String? = null,

    /** The map provider's id of the picked place. */
    var placeId: String? = null,
)

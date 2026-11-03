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

/** Request body for a client address. */
@Serializable
data class ClientAddressRequest(
    /** The map provider's id of the picked place. */
    var placeId: String? = null,

    /** The address of the picked place. */
    @SerialName("description")
    var placeAddress: String? = null,

    /** The latitude. */
    var latitude: Double? = null,

    /** The longitude. */
    var longitude: Double? = null,

    // Defaults
    /** The pattern Fineract parses this request's dates with (e.g. `dd MMMM yyyy`). */
    var dateFormat: String? = null,

    /** The locale Fineract parses the dates and numbers in this request with (e.g. `en`). */
    var locale: String? = null,
)

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

/** Request body for a propose transfer. */
@Serializable
data class ProposeTransferRequest(
    /** The server id of the office the client moves to. */
    val destinationOfficeId: Int,
    /** The date of the transfer. */
    val transferDate: String,
    /** A free-text note. */
    val note: String,
    /** The pattern Fineract parses this request's dates with (e.g. `dd MMMM yyyy`). */
    val dateFormat: String,
    /** The locale Fineract parses the dates and numbers in this request with (e.g. `en`). */
    val locale: String,
)

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
 * Created by Tarun on 13-08-17.
 */
@Serializable
data class ChargeCreationResponse(
    /** The server id of the client. */
    var clientId: Int = 0,

    /** The server id of the office. */
    var officeId: Int = 0,

    /** The server id of the resource the request created or changed. */
    var resourceId: Int = 0,

    /** The server id of the loan. */
    var loanId: Int = 0,
)

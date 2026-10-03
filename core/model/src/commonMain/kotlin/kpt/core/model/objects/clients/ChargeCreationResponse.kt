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

import kotlinx.serialization.Serializable

/**
 * Created by Tarun on 13-08-17.
 */
@Serializable
data class ChargeCreationResponse(
    var clientId: Int = 0,

    var officeId: Int = 0,

    var resourceId: Int = 0,

    var loanId: Int = 0,
)

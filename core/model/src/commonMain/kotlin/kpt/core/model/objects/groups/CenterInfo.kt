/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.groups

import kotlinx.serialization.Serializable

/**
 * Created by Rajan Maurya on 05/02/17.
 */
@Serializable
data class CenterInfo(
    var activeClients: Int? = null,

    var activeLoans: Int? = null,

    var activeClientLoans: Int? = null,

    var activeGroupLoans: Int? = null,

    var activeBorrowers: Int? = null,

    var activeClientBorrowers: Int? = null,

    var activeGroupBorrowers: Int? = null,

    var overdueLoans: Int? = null,

    var overdueClientLoans: Int? = null,

    var overdueGroupLoans: Int? = null,
)

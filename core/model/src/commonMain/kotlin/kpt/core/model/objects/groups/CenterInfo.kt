/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.groups

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by Rajan Maurya on 05/02/17.
 *
 * The `GroupSummaryCounts` report answers in LOWER-case keys (`activeclients`, `overduegrouploans`) —
 * measured on the gateway 2026-10-25. Without the [SerialName]s every count decoded as null (unknown
 * keys are ignored), so the centre summary showed zeros whatever the real figures were.
 */
@Serializable
data class CenterInfo(
    @SerialName("activeclients")
    var activeClients: Int? = null,

    @SerialName("activeloans")
    var activeLoans: Int? = null,

    @SerialName("activeclientloans")
    var activeClientLoans: Int? = null,

    @SerialName("activegrouploans")
    var activeGroupLoans: Int? = null,

    @SerialName("activeborrowers")
    var activeBorrowers: Int? = null,

    @SerialName("activeclientborrowers")
    var activeClientBorrowers: Int? = null,

    @SerialName("activegroupborrowers")
    var activeGroupBorrowers: Int? = null,

    @SerialName("overdueloans")
    var overdueLoans: Int? = null,

    @SerialName("overdueclientloans")
    var overdueClientLoans: Int? = null,

    @SerialName("overduegrouploans")
    var overdueGroupLoans: Int? = null,
)

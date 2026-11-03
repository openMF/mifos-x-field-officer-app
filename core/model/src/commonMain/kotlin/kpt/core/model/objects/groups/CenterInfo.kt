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
    /** The number of active clients. */
    @SerialName("activeclients")
    var activeClients: Int? = null,

    /** The number of active loans. */
    @SerialName("activeloans")
    var activeLoans: Int? = null,

    /** The number of active client loans. */
    @SerialName("activeclientloans")
    var activeClientLoans: Int? = null,

    /** The number of active group loans. */
    @SerialName("activegrouploans")
    var activeGroupLoans: Int? = null,

    /** The number of active borrowers. */
    @SerialName("activeborrowers")
    var activeBorrowers: Int? = null,

    /** The number of clients with an active loan. */
    @SerialName("activeclientborrowers")
    var activeClientBorrowers: Int? = null,

    /** The number of groups with an active loan. */
    @SerialName("activegroupborrowers")
    var activeGroupBorrowers: Int? = null,

    /** The number of overdue loans. */
    @SerialName("overdueloans")
    var overdueLoans: Int? = null,

    /** The number of overdue client loans. */
    @SerialName("overdueclientloans")
    var overdueClientLoans: Int? = null,

    /** The number of overdue group loans. */
    @SerialName("overduegrouploans")
    var overdueGroupLoans: Int? = null,
)

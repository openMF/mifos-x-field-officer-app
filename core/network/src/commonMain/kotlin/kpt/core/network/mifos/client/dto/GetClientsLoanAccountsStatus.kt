/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.client.dto

import kotlinx.serialization.Serializable

/**
 *
 *
 * @param active
 * @param closed
 * @param closedObligationsMet
 * @param closedRescheduled
 * @param closedWrittenOff
 * @param code
 * @param value
 * @param id
 * @param overpaid
 * @param pendingApproval
 * @param waitingForDisbursal
 */

@Serializable
data class GetClientsLoanAccountsStatus(

    val active: Boolean? = null,

    val closed: Boolean? = null,

    val closedObligationsMet: Boolean? = null,

    val closedRescheduled: Boolean? = null,

    val closedWrittenOff: Boolean? = null,

    val code: String? = null,

    val value: String? = null,

    val id: Long? = null,

    val overpaid: Boolean? = null,

    val pendingApproval: Boolean? = null,

    val waitingForDisbursal: Boolean? = null,

)

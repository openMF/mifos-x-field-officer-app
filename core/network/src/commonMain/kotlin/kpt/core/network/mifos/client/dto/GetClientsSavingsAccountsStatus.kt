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
 * @param approved
 * @param closed
 * @param code
 * @param id
 * @param matured
 * @param prematureClosed
 * @param rejected
 * @param submittedAndPendingApproval
 * @param transferInProgress
 * @param transferOnHold
 * @param `value`
 * @param withdrawnByApplicant
 */

@Serializable
data class GetClientsSavingsAccountsStatus(

    val active: Boolean? = null,

    val approved: Boolean? = null,

    val closed: Boolean? = null,

    val code: String? = null,

    val id: Long? = null,

    val matured: Boolean? = null,

    val prematureClosed: Boolean? = null,

    val rejected: Boolean? = null,

    val submittedAndPendingApproval: Boolean? = null,

    val transferInProgress: Boolean? = null,

    val transferOnHold: Boolean? = null,

    val value: String? = null,

    val withdrawnByApplicant: Boolean? = null,

)

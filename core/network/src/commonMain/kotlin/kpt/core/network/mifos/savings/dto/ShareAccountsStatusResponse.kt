/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.savings.dto

import kotlinx.serialization.Serializable

/** Server response to a share accounts status request. */
@Serializable
data class ShareAccountsStatusResponse(
    /** The server id of this record. */
    val id: Int? = null,

    /**
     * The Fineract code that identifies this value (an enum code such as `loanStatusType.active`, or an ISO
     * currency code).
     */
    val code: String? = null,

    /** The human-readable label Fineract sends for this value. */
    val value: String? = null,

    /** True while the account waits for approval. */
    val submittedAndPendingApproval: Boolean? = null,

    /** True when the record has been approved. */
    val approved: Boolean? = null,

    /** True when the record has been rejected. */
    val rejected: Boolean? = null,

    /** True when the record is active. */
    val active: Boolean? = null,

    /** True when the account is closed. */
    val closed: Boolean? = null,
)

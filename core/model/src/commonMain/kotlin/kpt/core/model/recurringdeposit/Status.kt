/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.recurringdeposit

import kotlinx.serialization.Serializable

/** Lifecycle status flags of an account. */
@Serializable
data class Status(
    /** True when the record is active. */
    val active: Boolean? = null,
    /** True when the record has been approved. */
    val approved: Boolean? = null,
    /** True when the account is closed. */
    val closed: Boolean? = null,
    /**
     * The Fineract code that identifies this value (an enum code such as `loanStatusType.active`, or an ISO
     * currency code).
     */
    val code: String? = null,
    /** The server id of this record. */
    val id: Int? = null,
    /** True when the deposit has matured. */
    val matured: Boolean? = null,
    /** True when the deposit was closed before maturity. */
    val prematureClosed: Boolean? = null,
    /** True when the record has been rejected. */
    val rejected: Boolean? = null,
    /** True while the account waits for approval. */
    val submittedAndPendingApproval: Boolean? = null,
    /** True while a transfer is in progress. */
    val transferInProgress: Boolean? = null,
    /** True when a transfer is on hold. */
    val transferOnHold: Boolean? = null,
    /** The human-readable label Fineract sends for this value. */
    val value: String? = null,
    /** True when the applicant withdrew the application. */
    val withdrawnByApplicant: Boolean? = null,
)

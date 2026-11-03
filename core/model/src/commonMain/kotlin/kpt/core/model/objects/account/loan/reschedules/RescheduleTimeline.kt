/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.account.loan.reschedules

import kotlinx.serialization.Serializable

/** A reschedule timeline. */
@Serializable
data class RescheduleTimeline(
    /** The date the record was submitted. */
    val submittedOnDate: List<Int>? = null,
    /** The username of whoever submitted the record. */
    val submittedByUsername: String? = null,
    /** The date the record was approved. */
    val approvedOnDate: List<Int>? = null,
    /** The username of whoever approved the record. */
    val approvedByUsername: String? = null,
)

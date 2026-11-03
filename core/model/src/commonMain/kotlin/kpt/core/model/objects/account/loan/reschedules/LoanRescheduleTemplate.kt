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

/** Server template for a loan reschedule: the options its form offers. */
@Serializable
data class LoanRescheduleTemplate(
    /** The reschedule reasons that may be chosen. */
    val rescheduleReasons: List<RescheduleReasonOption> = emptyList(),
)

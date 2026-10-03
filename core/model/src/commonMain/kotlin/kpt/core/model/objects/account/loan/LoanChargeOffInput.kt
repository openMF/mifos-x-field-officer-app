/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.account.loan

import kpt.core.model.utils.DateConstants

data class LoanChargeOffInput(
    val chargeOffReasonId: Int,
    val transactionDate: String,
    val externalId: String? = null,
    val note: String? = null,
    val locale: String = DateConstants.LOCALE,
    val dateFormat: String = "dd-MM-yyyy",
)

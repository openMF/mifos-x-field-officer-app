/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.collectionsheets

import kotlinx.serialization.Serializable
import kpt.core.model.objects.template.loan.Currency
@Serializable
data class LoanCollectionSheet(
    val accountId: String? = null,
    val accountStatusId: Int = 0,
    val currency: Currency? = null,
    val interestDue: Double? = null,
    val interestPaid: Double? = null,
    val loanId: Int = 0,
    val principalDue: Double? = null,
    val productId: Double? = null,
    val totalDue: Double = 0.0,
    val chargesDue: Double = 0.0,
    val productShortName: String? = null,
)

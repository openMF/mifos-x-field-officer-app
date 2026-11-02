/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.loan

import kotlinx.serialization.Serializable
import kpt.core.model.objects.account.loan.Currency

/**
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `LoanAccountEntity`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class LoanAccount(
    val id: Int? = null,
    val clientId: Long = 0,
    val groupId: Long = 0,
    val centerId: Long = 0,
    val accountNo: String? = null,
    val externalId: String? = null,
    val productId: Int? = null,
    val productName: String? = null,
    val currency: Currency? = null,
    val status: LoanStatus? = null,
    val loanType: LoanAccountType? = null,
    val loanCycle: Int? = null,
    val inArrears: Boolean? = null,
    val originalLoan: Double? = null,
    val loanBalance: Double? = null,
    val amountPaid: Double? = null,
)

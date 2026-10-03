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
 * @param accountNo
 * @param externalId
 * @param id
 * @param loanCycle
 * @param loanType
 * @param productId
 * @param productName
 * @param status
 */

@Serializable
data class GetClientsLoanAccounts(

    val accountNo: String? = null,

    val externalId: String? = null,

    val id: Long? = null,

    val loanCycle: Int? = null,

    val loanType: GetClientsLoanAccountsType? = null,

    val productId: Long? = null,

    val productName: String? = null,

    val status: GetClientsLoanAccountsStatus? = null,

    val originalLoan: Double? = null,

    val loanBalance: Double? = null,

    val amountPaid: Double? = null,

)

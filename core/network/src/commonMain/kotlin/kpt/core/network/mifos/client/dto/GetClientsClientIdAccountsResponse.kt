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
import kpt.core.network.mifos.savings.dto.ShareAccountResponse

/**
 * GetClientsClientIdAccountsResponse
 *
 * @param loanAccounts
 * @param savingsAccounts
 */

@Serializable
data class GetClientsClientIdAccountsResponse(

    val loanAccounts: Set<GetClientsLoanAccounts>? = null,

    val savingsAccounts: Set<GetClientsSavingsAccounts>? = null,

    val shareAccounts: Set<ShareAccountResponse>? = null,
)

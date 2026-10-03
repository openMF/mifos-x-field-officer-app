/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.savings.dto

import kotlinx.serialization.Serializable
import kpt.core.model.objects.organisations.Currency
import kpt.core.model.shared.Timeline

@Serializable
data class ShareAccountResponse(
    val id: Int? = null,
    val accountNo: String? = null,
    val totalApprovedShares: Int? = null,
    val totalPendingForApprovalShares: Int? = null,
    val shortProductName: String? = null,
    val clientId: Int? = null,
    val clientName: String? = null,
    val productId: Int? = null,
    val productName: String? = null,
    val status: ShareAccountsStatusResponse? = null,
    val timeline: Timeline? = null,
    val currency: Currency? = null,
)

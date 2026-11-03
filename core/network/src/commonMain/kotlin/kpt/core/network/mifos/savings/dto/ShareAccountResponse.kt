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
import kpt.core.model.objects.organisations.Currency
import kpt.core.model.shared.Timeline

/** Server response to a share account request. */
@Serializable
data class ShareAccountResponse(
    /** The server id of this record. */
    val id: Int? = null,
    /** The account number Fineract assigned. */
    val accountNo: String? = null,
    /** The number of shares approved. */
    val totalApprovedShares: Int? = null,
    /** The number of shares waiting for approval. */
    val totalPendingForApprovalShares: Int? = null,
    /** The product's short name. */
    val shortProductName: String? = null,
    /** The server id of the client. */
    val clientId: Int? = null,
    /** The client's display name. */
    val clientName: String? = null,
    /** The server id of the product. */
    val productId: Int? = null,
    /** The name of the product. */
    val productName: String? = null,
    /** The record's Fineract status. */
    val status: ShareAccountsStatusResponse? = null,
    /** The dates and users of the record's lifecycle events (submitted, approved, activated, …). */
    val timeline: Timeline? = null,
    /** The currency the amounts are in. */
    val currency: Currency? = null,
)

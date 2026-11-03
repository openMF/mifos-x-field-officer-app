/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.account.share

import kotlinx.serialization.Serializable

/** A share account as listed on a client. */
@Serializable
data class ShareAccounts(
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
    val status: ShareAccountsStatus? = null,
)

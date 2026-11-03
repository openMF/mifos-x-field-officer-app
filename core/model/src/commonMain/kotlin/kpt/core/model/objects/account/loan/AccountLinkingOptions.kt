/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.account.loan

import kotlinx.serialization.Serializable

/**
 * Created by Tarun on 12/16/2016.
 */
@Serializable
data class AccountLinkingOptions(
    /** The account number Fineract assigned. */
    var accountNo: String? = null,

    /** The server id of the client. */
    var clientId: Int? = null,

    /** The client's display name. */
    var clientName: String? = null,

    /** The currency the amounts are in. */
    var currency: Currency? = null,

    /** The server id of the field officer. */
    var fieldOfficerId: Int? = null,

    /** The server id of this record. */
    var id: Int? = null,

    /** The server id of the product. */
    var productId: Int? = null,

    /** The name of the product. */
    var productName: String? = null,
)

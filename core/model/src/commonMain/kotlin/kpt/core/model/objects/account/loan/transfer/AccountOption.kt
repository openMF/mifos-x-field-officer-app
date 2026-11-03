/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.account.loan.transfer

import kotlinx.serialization.Serializable
import kpt.core.model.objects.template.loan.Currency

/**
 * Data class representing an account option for transfers
 */
@Serializable
data class AccountOption(
    /** The server id of this record. */
    val id: Int? = null,
    /** The account number Fineract assigned. */
    val accountNo: String? = null,
    /** The server id of the client. */
    val clientId: Int? = null,
    /** The client's display name. */
    val clientName: String? = null,
    /** The server id of the product. */
    val productId: Int? = null,
    /** The name of the product. */
    val productName: String? = null,
    /** The server id of the field officer. */
    val fieldOfficerId: Int? = null,
    /** The currency the amounts are in. */
    val currency: Currency? = null,
    /** The account's currency code. */
    val currencyCodeFromCurrency: String? = null,
)

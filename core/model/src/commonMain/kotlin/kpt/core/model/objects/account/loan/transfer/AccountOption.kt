/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.account.loan.transfer

import kotlinx.serialization.Serializable
import kpt.core.model.objects.template.loan.Currency

/**
 * Data class representing an account option for transfers
 */
@Serializable
data class AccountOption(
    val id: Int? = null,
    val accountNo: String? = null,
    val clientId: Int? = null,
    val clientName: String? = null,
    val productId: Int? = null,
    val productName: String? = null,
    val fieldOfficerId: Int? = null,
    val currency: Currency? = null,
    val currencyCodeFromCurrency: String? = null,
)

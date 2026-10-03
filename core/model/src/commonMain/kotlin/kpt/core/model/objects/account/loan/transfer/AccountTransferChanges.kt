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
@Serializable
data class AccountTransferChanges(
    val fromOfficeId: Int? = null,
    val fromClientId: Int? = null,
    val fromAccountId: Int? = null,
    val fromAccountType: Int? = null,
    val toOfficeId: Int? = null,
    val toClientId: Int? = null,
    val toAccountId: Int? = null,
    val toAccountType: Int? = null,
    val transferDate: String? = null,
    val transferAmount: Double? = null,
    val transferDescription: String? = null,
    val currencyCode: String? = null,
    val locale: String? = null,
    val dateFormat: String? = null,
)

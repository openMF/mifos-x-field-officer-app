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
 * Data class representing the template for account transfers
 */
@Serializable
data class AccountTransferTemplate(
    val fromOfficeOptions: List<OfficeOption> = emptyList(),
    val fromClientOptions: List<ClientOption> = emptyList(),
    val fromAccountTypeOptions: List<AccountTypeOption> = emptyList(),
    val fromAccountOptions: List<AccountOption> = emptyList(),
    val toOfficeOptions: List<OfficeOption> = emptyList(),
    val toClientOptions: List<ClientOption> = emptyList(),
    val toAccountTypeOptions: List<AccountTypeOption> = emptyList(),
    val toAccountOptions: List<AccountOption> = emptyList(),
    val currencyOptions: List<Currency> = emptyList(),
)

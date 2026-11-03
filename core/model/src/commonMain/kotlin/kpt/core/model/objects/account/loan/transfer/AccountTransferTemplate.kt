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
 * Data class representing the template for account transfers
 */
@Serializable
data class AccountTransferTemplate(
    /** The offices money may leave from. */
    val fromOfficeOptions: List<OfficeOption> = emptyList(),
    /** The clients money may leave from. */
    val fromClientOptions: List<ClientOption> = emptyList(),
    /** The account types money may leave from. */
    val fromAccountTypeOptions: List<AccountTypeOption> = emptyList(),
    /** The accounts money may leave from. */
    val fromAccountOptions: List<AccountOption> = emptyList(),
    /** The offices money may go to. */
    val toOfficeOptions: List<OfficeOption> = emptyList(),
    /** The clients money may go to. */
    val toClientOptions: List<ClientOption> = emptyList(),
    /** The account types money may go to. */
    val toAccountTypeOptions: List<AccountTypeOption> = emptyList(),
    /** The accounts money may go to. */
    val toAccountOptions: List<AccountOption> = emptyList(),
    /** The currencies that may be chosen. */
    val currencyOptions: List<Currency> = emptyList(),
)

/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.savings.entity

import kotlinx.serialization.Serializable

/**
 * Created by Pronay Sarker on 15/08/2024 (11:12 PM)
 */
@Serializable
data class SavingsTransactionData(
    /** The savings account with its transactions and charges. */
    val savingsAccountWithAssociations: SavingsAccountWithAssociationsEntity = SavingsAccountWithAssociationsEntity(),
    /** The deposit product type (savings, fixed deposit or recurring deposit). */
    val depositType: SavingAccountDepositTypeEntity?,
    /** The transaction type. */
    val transactionType: String,
)

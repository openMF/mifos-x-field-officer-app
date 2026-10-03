/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.savings.entity

import kotlinx.serialization.Serializable

/**
 * Created by Pronay Sarker on 15/08/2024 (11:12 PM)
 */
@Serializable
data class SavingsTransactionData(
    val savingsAccountWithAssociations: SavingsAccountWithAssociationsEntity = SavingsAccountWithAssociationsEntity(),
    val depositType: SavingAccountDepositTypeEntity?,
    val transactionType: String,
)

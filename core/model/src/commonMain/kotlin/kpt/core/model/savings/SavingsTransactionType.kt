/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.savings

import kotlinx.serialization.Serializable

/**
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `SavingsTransactionTypeEntity`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class SavingsTransactionType(
    val id: Int? = null,
    val code: String? = null,
    val value: String? = null,
    val deposit: Boolean? = null,
    val withdrawal: Boolean? = null,
    val interestPosting: Boolean? = null,
    val feeDeduction: Boolean? = null,
    val initiateTransfer: Boolean? = null,
    val approveTransfer: Boolean? = null,
    val withdrawTransfer: Boolean? = null,
    val rejectTransfer: Boolean? = null,
    val overdraftInterest: Boolean? = null,
    val writtenoff: Boolean? = null,
    val overdraftFee: Boolean? = null,
)

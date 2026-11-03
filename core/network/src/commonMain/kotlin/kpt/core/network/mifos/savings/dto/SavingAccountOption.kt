/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.savings.dto

import kotlinx.serialization.Serializable

/** One saving account choice offered by a template. */
@Serializable
data class SavingAccountOption(
    /** The server id of this record. */
    val id: Long,
    /** The account number Fineract assigned. */
    val accountNo: String,
)

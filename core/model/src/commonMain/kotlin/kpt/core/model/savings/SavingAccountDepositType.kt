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
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `SavingAccountDepositTypeEntity`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class SavingAccountDepositType(
    val id: Int? = null,
    val code: String? = null,
    val value: String? = null,
) {

    val isRecurring: Boolean
        get() = ServerTypes.RECURRING.id == id

    val endpoint: String
        get() = ServerTypes.fromId(id).endpoint

    val serverType: ServerTypes
        get() = ServerTypes.fromId(id)

    enum class ServerTypes(val id: Int, val code: String, val endpoint: String) {
        SAVINGS(100, "depositAccountType.savingsDeposit", "savingsaccounts"),
        FIXED(200, "depositAccountType.fixedDeposit", "fixeddepositaccounts"),
        RECURRING(300, "depositAccountType.recurringDeposit", "recurringdepositaccounts"),
        ;

        companion object {
            fun fromId(id: Int?): ServerTypes {
                for (type in entries) {
                    if (type.id == id) {
                        return type
                    }
                }
                return SAVINGS
            }
        }
    }
}

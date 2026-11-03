/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.collectionsheets

import kotlinx.serialization.Serializable

/**
 * Created by Tarun on 31-07-17.
 */
@Serializable
data class SavingsProduct(
    /** True when the account may be overdrawn. */
    var isAllowOverdraft: Boolean = false,

    /** The kind of deposit account. */
    var depositAccountType: String? = null,

    /** True when the minimum balance is enforced. */
    var isEnforceMinRequiredBalance: Boolean = false,

    /** The server id of this record. */
    var id: Int = 0,

    /** The display name. */
    var name: String? = null,

    /** True when tax is withheld on interest. */
    var isWithHoldTax: Boolean = false,

    /** True when the withdrawal fee also applies to transfers. */
    var isWithdrawalFeeForTransfers: Boolean = false,
)

/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.template.loan

import kotlinx.serialization.Serializable

/**
 * Created by Rajan Maurya on 16/07/16.
 */
@Serializable
data class ProductOptions(
    /** The server id of this record. */
    var id: Int? = null,

    /** The display name. */
    var name: String? = null,

    /** True when the loan counts towards the borrower cycle. */
    var includeInBorrowerCycle: Boolean? = null,

    /** True when terms depend on the borrower cycle. */
    var useBorrowerCycle: Boolean? = null,

    /** True when the product is linked to floating interest rates. */
    var isLinkedToFloatingInterestRates: Boolean? = null,

    /** True when the floating-rate calculation may be used. */
    var isFloatingInterestRateCalculationAllowed: Boolean? = null,

    /** True when installment amounts may vary. */
    var allowVariableInstallments: Boolean? = null,

    /** True when interest is recalculated as repayments arrive. */
    var isInterestRecalculationEnabled: Boolean? = null,

    /** True when the installment amount can be set on the account. */
    var canDefineInstallmentAmount: Boolean? = null,

    /** True when guarantee funds are held on the guarantor's account. */
    var holdGuaranteeFunds: Boolean? = null,

    /** True when the loan leaves non-performing status only once its arrears are cleared. */
    var accountMovesOutOfNPAOnlyOnArrearsCompletion: Boolean? = null,
)

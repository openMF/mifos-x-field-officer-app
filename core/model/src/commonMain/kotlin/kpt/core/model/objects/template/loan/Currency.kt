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
data class Currency(
    /**
     * The Fineract code that identifies this value (an enum code such as `loanStatusType.active`, or an ISO
     * currency code).
     */
    var code: String? = null,

    /** The display name. */
    var name: String? = null,

    /** The number of decimal places amounts in this currency carry. */
    var decimalPlaces: Double? = null,

    /** The unit amounts in this currency are rounded to. */
    var inMultiplesOf: Int? = null,

    /** The currency symbol to display (e.g. `$`). */
    var displaySymbol: String? = null,

    /** The currency's name code used in translations (e.g. `currency.USD`). */
    var nameCode: String? = null,

    /** The currency's display label (name and symbol). */
    var displayLabel: String? = null,
)

/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.charge

import kotlinx.serialization.Serializable

/**
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `ClientChargeCurrencyEntity`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class ClientChargeCurrency(
    /** The server id of this record. */
    val id: Int = 0,
    /**
     * The Fineract code that identifies this value (an enum code such as `loanStatusType.active`, or an ISO
     * currency code).
     */
    val code: String? = null,
    /** The display name. */
    val name: String? = null,
    /** The number of decimal places amounts in this currency carry. */
    val decimalPlaces: Int? = null,
    /** The unit amounts in this currency are rounded to. */
    val inMultiplesOf: Int? = null,
    /** The currency symbol to display (e.g. `$`). */
    val displaySymbol: String? = null,
    /** The currency's name code used in translations (e.g. `currency.USD`). */
    val nameCode: String? = null,
    /** The currency's display label (name and symbol). */
    val displayLabel: String? = null,
)

/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.databaseobjects

import kotlinx.serialization.Serializable
@Serializable
data class Currency(
    var code: String? = null,

    var name: String? = null,

    var decimalPlaces: Int = 0,

    var inMultiplesOf: Int = 0,

    var displaySymbol: String? = null,

    var nameCode: String? = null,

    var displayLabel: String? = null,

    var loan: Loan? = null,
)

/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.template.recurring.period

import kotlinx.serialization.Serializable
@Serializable
data class ProductOption(
    val id: Int? = null,
    val name: String? = null,
    val withHoldTax: Boolean? = null,
)

/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.template.recurring.period

import kotlinx.serialization.Serializable
/** One product choice offered by a template. */
@Serializable
data class ProductOption(
    /** The server id of this record. */
    val id: Int? = null,
    /** The display name. */
    val name: String? = null,
    /** True when tax is withheld on interest. */
    val withHoldTax: Boolean? = null,
)

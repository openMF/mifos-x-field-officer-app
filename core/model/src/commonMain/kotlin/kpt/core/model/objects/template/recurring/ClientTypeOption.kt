/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.template.recurring

import kotlinx.serialization.Serializable
/** One client type choice offered by a template. */
@Serializable
data class ClientTypeOption(
    /** True when the record is active. */
    val active: Boolean? = null,
    /** A free-text description. */
    val description: String? = null,
    /** The server id of this record. */
    val id: Int? = null,
    /** True when this option must be filled in. */
    val mandatory: Boolean? = null,
    /** The display name. */
    val name: String? = null,
    /** The sort position of this option. */
    val position: Int? = null,
)

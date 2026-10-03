/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.searchrecord

import kotlinx.serialization.Serializable

@Serializable
data class GenericSearchRecord(
    val id: Int = -1,
    val name: String = "",
    val description: String = "",
    val type: String = "",
    val metadata: Map<String, String> = emptyMap(),
)

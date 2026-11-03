/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.searchrecord

import kotlinx.serialization.Serializable

/** One search hit: a client, group, centre or account. */
@Serializable
data class GenericSearchRecord(
    /** The server id of this record. */
    val id: Int = -1,
    /** The display name. */
    val name: String = "",
    /** A free-text description. */
    val description: String = "",
    /** The record's type. */
    val type: String = "",
    /** Extra fields of the found record, by name. */
    val metadata: Map<String, String> = emptyMap(),
)

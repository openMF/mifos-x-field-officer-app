/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.datatable

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kpt.core.common.utils.MapDeserializer

/**
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `DataTablePayload`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class DataTablePayload(
    val id: Int? = null,
    val clientCreationTime: Long? = null,
    val dataTableString: String? = null,
    val registeredTableName: String? = null,
    @Serializable(with = MapDeserializer::class) @Contextual
    val data: Map<String, @Contextual Any>,
)

/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.survey

import kotlinx.serialization.Serializable

/**
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `ResponseDatasEntity`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class ResponseDatas(
    /** The server id of the response. */
    val responseId: Int = 0,
    /** The server id of this record. */
    val id: Int = 0,
    /** The text shown for this item. */
    val text: String? = null,
    /** The order this item is shown in. */
    val sequenceNo: Int = 0,
    /** The score this answer carries. */
    val value: Int = 0,
)

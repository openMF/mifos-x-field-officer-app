/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.office

import kotlinx.serialization.Serializable

/**
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `OfficeEntity`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class Office(
    /** The server id of this record. */
    val id: Int,
    /** The id an external system uses for this record, if one was set. */
    val externalId: String? = null,
    /** The display name. */
    val name: String? = null,
    /** The office name indented to show its place in the hierarchy. */
    val nameDecorated: String? = null,
    /** The office's opening date. */
    val officeOpeningDate: OfficeOpeningDate? = null,
    /** The date the office opened. */
    val openingDate: List<Int?> = emptyList(),
)

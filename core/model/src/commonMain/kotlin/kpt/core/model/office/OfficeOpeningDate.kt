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
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `OfficeOpeningDateEntity`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class OfficeOpeningDate(
    /** The server id of the office. */
    val officeId: Int,
    /** The year. */
    val year: Int? = null,
    /** The month (1–12). */
    val month: Int? = null,
    /** The day of the month. */
    val day: Int? = null,
)

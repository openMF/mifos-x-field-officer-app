/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.center.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable
import kpt.core.base.database.annotation.DbEntity

/** Room row for a center payload. */
@Serializable
@DbEntity
@Entity(
    indices = [],
    inheritSuperIndices = false,
    primaryKeys = [],
    foreignKeys = [],
    ignoredColumns = [],
    tableName = "CenterPayload",
)
data class CenterPayloadEntity(
    /** The server id of this record. */
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    /** The error from the last failed sync attempt; null when it has not failed. */
    val errorMessage: String? = null,

    /** The pattern Fineract parses this request's dates with (e.g. `dd MMMM yyyy`). */
    val dateFormat: String? = null,

    /** The locale Fineract parses the dates and numbers in this request with (e.g. `en`). */
    val locale: String? = null,

    /** The display name. */
    val name: String? = null,

    /** The server id of the office. */
    val officeId: Int? = null,

    /** True when the record is active. */
    val active: Boolean = false,

    /** The date the record was activated. */
    val activationDate: String? = null,
)

/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.document.entity

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import kpt.core.base.database.annotation.DbEntity

/**
 * Document METADATA attached to a Fineract resource (client, loan, group…).
 *
 * The listing is cached so a field officer can see WHAT is attached while offline; the binary
 * attachment itself is not stored — `downloadDocument` streams on demand, and holding client
 * documents on the device is a data-protection decision, not a caching one.
 *
 * Indexed on the parent pair because every read is scoped to one resource.
 */
@DbEntity
@Entity(
    tableName = "documents",
    indices = [Index(value = ["parentEntityType", "parentEntityId"])],
)
data class DocumentEntity(
    @PrimaryKey
    val id: Int,
    val parentEntityType: String? = null,
    val parentEntityId: Int = 0,
    val name: String? = null,
    val fileName: String? = null,
    val size: Long = 0,
    val type: String? = null,
    val description: String? = null,
)

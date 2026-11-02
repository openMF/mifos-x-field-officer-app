/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.note

/**
 * A note on a client — what every layer above `core/store` sees.
 *
 * The Room row is `core/database`'s `NoteEntity`; `core/store` maps between the two, so a feature
 * never depends on the database. Field names match the row, so the two cannot drift silently.
 */
data class Note(
    val id: Long? = null,
    val clientId: Long? = null,
    val noteContent: String? = null,
    val createdById: Long? = null,
    val createdByUsername: String? = null,
    val createdOn: String? = null,
    val updatedById: Long? = null,
    val updatedByUsername: String? = null,
    val updatedOn: String? = null,
)

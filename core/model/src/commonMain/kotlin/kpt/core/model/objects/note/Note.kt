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
    /** The server id of this record. */
    val id: Long? = null,
    /** The server id of the client. */
    val clientId: Long? = null,
    /** The note's text. */
    val noteContent: String? = null,
    /** The server id of the user who created the record. */
    val createdById: Long? = null,
    /** The username of whoever created the record. */
    val createdByUsername: String? = null,
    /** When the record was created. */
    val createdOn: String? = null,
    /** The server id of the user who last updated the record. */
    val updatedById: Long? = null,
    /** The username of whoever last updated the record. */
    val updatedByUsername: String? = null,
    /** When the record was last updated. */
    val updatedOn: String? = null,
)

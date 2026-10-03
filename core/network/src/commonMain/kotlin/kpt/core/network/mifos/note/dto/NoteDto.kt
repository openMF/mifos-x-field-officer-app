/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.note.dto

import kotlinx.serialization.Serializable

/**
 * Data Transfer Object (DTO) representing a Note returned by the API.
 *
 * This model is used in the network layer to deserialize note-related responses.
 * It may represent a single note or be part of a list of notes.
 *
 * @property id
 * @property clientId
 * @property note
 * @property noteType
 * @property createdById
 * @property createdByUsername
 * @property createdOn
 * @property updatedById
 * @property updatedByUsername
 * @property updatedOn
 */
@Serializable
data class NoteDto(
    val clientId: Long? = null,
    val createdById: Long? = null,
    val createdByUsername: String? = null,
    val createdOn: String? = null,
    val id: Long? = null,
    val note: String? = null,
    val updatedById: Long? = null,
    val updatedByUsername: String? = null,
    val updatedOn: String? = null,
    val noteType: NoteTypeDto? = null,
)

/**
 * DTO representing the type/category of a Note.
 *
 * Typically nested inside [NoteDto] and provides metadata about the note type.
 *
 * @property id
 * @property code
 * @property value
 */
@Serializable
data class NoteTypeDto(
    val id: Long? = null,
    val code: String? = null,
    val value: String? = null,
)

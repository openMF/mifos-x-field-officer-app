/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.note

import kpt.core.database.note.entity.NoteEntity
import kpt.core.model.objects.note.Note

// The row ↔ model boundary. Entities never leave core/store: the stores read rows and emit models.

fun NoteEntity.toModel() = Note(
    id = id,
    clientId = clientId,
    noteContent = noteContent,
    createdById = createdById,
    createdByUsername = createdByUsername,
    createdOn = createdOn,
    updatedById = updatedById,
    updatedByUsername = updatedByUsername,
    updatedOn = updatedOn,
)

fun Note.toEntity() = NoteEntity(
    id = id,
    clientId = clientId,
    noteContent = noteContent,
    createdById = createdById,
    createdByUsername = createdByUsername,
    createdOn = createdOn,
    updatedById = updatedById,
    updatedByUsername = updatedByUsername,
    updatedOn = updatedOn,
)

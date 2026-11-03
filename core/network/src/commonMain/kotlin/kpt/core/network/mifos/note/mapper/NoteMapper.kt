/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.note.mapper

import kpt.core.model.objects.note.Note
import kpt.core.network.mifos.note.dto.NoteDto

/**
 * Wire → model. `NoteDto.note` is the body text; `Note` names the same column
 * `noteContent`, which is the one field whose name differs across the boundary.
 * `noteType` is dropped: it is Fineract classification metadata the app never reads.
 */
fun NoteDto.toModel(): Note = Note(
    id = id,
    clientId = clientId,
    noteContent = note,
    createdById = createdById,
    createdByUsername = createdByUsername,
    createdOn = createdOn,
    updatedById = updatedById,
    updatedByUsername = updatedByUsername,
    updatedOn = updatedOn,
)

/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.document.mapper

import kpt.core.database.document.entity.DocumentEntity
import kpt.core.model.objects.noncoreobjects.Document

fun DocumentEntity.toDomain(): Document = Document(
    id = id,
    parentEntityType = parentEntityType,
    parentEntityId = parentEntityId,
    name = name,
    fileName = fileName,
    size = size,
    type = type,
    description = description,
)

/**
 * `parentEntityType` / `parentEntityId` come from the REQUEST, not the response — Fineract's
 * document payload does not echo the owner back, and they are the index this table is read by.
 */
fun Document.toEntity(parentEntityType: String, parentEntityId: Int): DocumentEntity = DocumentEntity(
    id = id,
    parentEntityType = parentEntityType,
    parentEntityId = parentEntityId,
    name = name,
    fileName = fileName,
    size = size,
    type = type,
    description = description,
)

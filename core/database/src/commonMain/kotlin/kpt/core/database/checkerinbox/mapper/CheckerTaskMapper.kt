/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.checkerinbox.mapper

import kpt.core.database.checkerinbox.entity.CheckerTaskEntity
import kpt.core.model.objects.checkerinboxtask.CheckerTask

/**
 * The entity columns are nullable because Fineract omits them on partially-processed tasks; the
 * domain type is not, so the read direction substitutes empty rather than widening the domain.
 */
fun CheckerTaskEntity.toDomain(): CheckerTask = CheckerTask(
    id = id,
    madeOnDate = madeOnDate,
    processingResult = processingResult.orEmpty(),
    maker = maker.orEmpty(),
    actionName = actionName.orEmpty(),
    entityName = entityName.orEmpty(),
    resourceId = resourceId.orEmpty(),
)

fun CheckerTask.toEntity(): CheckerTaskEntity = CheckerTaskEntity(
    id = id,
    madeOnDate = madeOnDate,
    processingResult = processingResult,
    maker = maker,
    actionName = actionName,
    entityName = entityName,
    resourceId = resourceId,
)

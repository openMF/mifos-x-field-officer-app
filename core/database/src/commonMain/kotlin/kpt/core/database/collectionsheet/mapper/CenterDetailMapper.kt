/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.collectionsheet.mapper

import kpt.core.database.collectionsheet.entity.CenterDetail
import kpt.core.database.collectionsheet.entity.CenterDetailEntity

fun CenterDetailEntity.toDomain(): CenterDetail = CenterDetail(
    staffId = staffId,
    staffName = staffName,
    meetingFallCenters = meetingFallCenters,
)

fun CenterDetail.toEntity(): CenterDetailEntity = CenterDetailEntity(
    staffId = staffId,
    staffName = staffName,
    meetingFallCenters = meetingFallCenters,
)

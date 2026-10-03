/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.runreport

import kotlinx.serialization.Serializable

/**
 * Created by Tarun on 03-08-17.
 */
@Serializable
data class FullParameterListResponse(
    var columnHeaders: List<ColumnHeader>,
    var data: List<DataRow>,
)

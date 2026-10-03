/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.datatable.dto

import kotlinx.serialization.Serializable
import kpt.core.model.shared.ResultsetColumnHeaderData

/**
 * GetDataTablesResponse
 *
 * @param applicationTableName
 * @param columnHeaderData
 * @param registeredTableName
 */

@Serializable
data class GetDataTablesResponse(

    val applicationTableName: String? = null,

    val columnHeaderData: List<ResultsetColumnHeaderData>? = null,

    val registeredTableName: String? = null,

)

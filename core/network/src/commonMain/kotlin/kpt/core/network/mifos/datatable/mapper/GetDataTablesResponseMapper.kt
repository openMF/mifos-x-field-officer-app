/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.datatable.mapper

import kpt.core.database.datatable.entity.ColumnHeader
import kpt.core.database.datatable.entity.DataTableEntity
import kpt.core.model.shared.ResultsetColumnHeaderData
import kpt.core.network.data.AbstractMapper
import kpt.core.network.mifos.datatable.dto.GetDataTablesResponse

/**
 * Created by Aditya Gupta on 31/08/23.
 */

object GetDataTablesResponseMapper : AbstractMapper<GetDataTablesResponse, DataTableEntity>() {

    override fun mapFromEntity(entity: GetDataTablesResponse): DataTableEntity {
        return DataTableEntity(
            applicationTableName = entity.applicationTableName,
            registeredTableName = entity.registeredTableName,
            columnHeaderData = entity.columnHeaderData!!.map {
                ColumnHeader(
                    columnCode = it.columnCode,
                    columnType = it.columnType?.name,
                    columnDisplayType = it.columnDisplayType?.name,
                    columnLength = it.columnLength?.toInt(),
                    columnNullable = it.isColumnNullable,
                    columnPrimaryKey = it.isColumnPrimaryKey,
                )
            },
        )
    }

    override fun mapToEntity(domainModel: DataTableEntity): GetDataTablesResponse {
        return GetDataTablesResponse(
            applicationTableName = domainModel.applicationTableName,
            registeredTableName = domainModel.registeredTableName,
            columnHeaderData = domainModel.columnHeaderData.map {
                ResultsetColumnHeaderData(
                    columnCode = it.columnCode,
                    columnType = ResultsetColumnHeaderData.ColumnType.valueOf(it.columnType!!),
                    columnDisplayType = ResultsetColumnHeaderData.ColumnDisplayType.valueOf(it.columnDisplayType!!),
                    columnLength = it.columnLength?.toLong(),
                    isColumnNullable = it.columnNullable,
                    isColumnPrimaryKey = it.columnPrimaryKey,
                )
            },
        )
    }
}

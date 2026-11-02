/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.datatable

import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.JsonArray
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.model.datatable.DataTable

interface DataTableRepository {
    /** Registered datatables for one application table (m_client, m_loan, ...). */
    fun dataTablesStream(appTable: String, scope: CoroutineScope): ScreenDataStream<List<DataTable>>

    /**
     * One datatable's ROWS for one entity.
     *
     * A raw `JsonArray`: a datatable's columns are configured per DEPLOYMENT, so there is no DTO to
     * deserialize into and the screen renders whatever keys the tenant defined. That is also why
     * the original's screen walked the JSON by hand.
     */
    fun dataTableDataStream(
        tableName: String,
        entityId: Int,
        scope: CoroutineScope,
    ): ScreenDataStream<JsonArray>
}

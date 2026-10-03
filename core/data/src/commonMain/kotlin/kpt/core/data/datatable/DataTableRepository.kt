/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.datatable

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.database.datatable.entity.DataTableEntity

interface DataTableRepository {
    /** Registered datatables for one application table (m_client, m_loan, ...). */
    fun dataTablesStream(appTable: String, scope: CoroutineScope): ScreenDataStream<List<DataTableEntity>>
}

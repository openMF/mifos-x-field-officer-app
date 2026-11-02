/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.testing

import kotlinx.coroutines.flow.MutableStateFlow
import kpt.core.data.recent.RecentRecordsRepository
import kpt.core.model.recent.RecentRecord

/** In-memory recents: [recorded] is everything written, most recent first. */
@ExperimentalDataTestingApi
class FakeRecentRecordsRepository(initial: List<RecentRecord> = emptyList()) : RecentRecordsRepository {
    override val recent = MutableStateFlow(initial)
    val recorded = mutableListOf<RecentRecord>()

    override suspend fun record(record: RecentRecord) {
        recorded += record
        recent.value = listOf(record) + recent.value.filterNot { it.kind == record.kind && it.id == record.id }
    }
}

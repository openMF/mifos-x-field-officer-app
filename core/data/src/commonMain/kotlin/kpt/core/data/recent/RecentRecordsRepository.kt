/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.recent

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kpt.core.database.recent.dao.RecentRecordDao
import kpt.core.database.recent.entity.RecentRecordEntity
import kpt.core.datastore.prefs.ProjectPreferencesRepository
import kpt.core.model.recent.RecentKind
import kpt.core.model.recent.RecentRecord
import kotlin.time.Clock

/** The signed-in officer's recently-opened records — written by the profile screens, read by Home. */
interface RecentRecordsRepository {
    val recent: Flow<List<RecentRecord>>
    suspend fun record(record: RecentRecord)
}

/** Room-backed and keyed by the signed-in user, so a shared device never mixes officers' histories. */
@OptIn(ExperimentalCoroutinesApi::class)
class RecentRecordsRepositoryImpl(
    private val dao: RecentRecordDao,
    private val preferences: ProjectPreferencesRepository,
) : RecentRecordsRepository {

    override val recent: Flow<List<RecentRecord>> =
        preferences.fineractUser.map { it.userId }.distinctUntilChanged()
            .flatMapLatest { userId -> dao.observe(userId, KEEP) }
            .map { rows -> rows.mapNotNull { it.toModel() } }

    override suspend fun record(record: RecentRecord) {
        val userId = preferences.fineractUser.value.userId
        dao.record(record.toEntity(userId, Clock.System.now().toEpochMilliseconds()), KEEP)
    }

    private companion object {
        const val KEEP = 5
    }
}

private fun RecentRecord.toEntity(userId: Long, at: Long) =
    RecentRecordEntity(
        userId = userId,
        kind = kind.name,
        id = id,
        title = title,
        accountNo = accountNo,
        detail = detail,
        openedAtMs = at,
    )

private fun RecentRecordEntity.toModel(): RecentRecord? =
    RecentKind.entries.firstOrNull { it.name == kind }?.let {
        RecentRecord(
            it,
            id,
            title,
            accountNo,
            detail,
            openedAtMs,
        )
    }

/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.store.mutation.conflict.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kpt.core.base.database.infra.dao.ConflictDao
import kpt.core.base.database.infra.entity.ConflictEntity
import kpt.core.base.store.mutation.conflict.ConflictEntry
import kpt.core.base.store.mutation.conflict.ConflictInbox
import kpt.core.base.store.mutation.conflict.ConflictResolution

/**
 * Room-backed [ConflictInbox] — persists write conflicts in the `framework_write_conflicts` table so
 * they survive process death and are surfaced in Settings. `resolve` marks the entry resolved (the
 * server record is already ingested; a `RETRY_LOCAL` re-submit is driven by the caller from the form).
 *
 * @param now monotonic millis provider (injected for deterministic tests).
 */
class RoomConflictInbox(
    private val dao: ConflictDao,
    private val now: () -> Long,
) : ConflictInbox {

    override suspend fun record(
        entity: String,
        key: String,
        localPayloadJson: String,
        serverPayloadJson: String,
        formRoute: String?,
    ): String {
        val id = dao.insert(
            ConflictEntity(
                entity = entity,
                entityKey = key,
                localPayloadJson = localPayloadJson,
                serverPayloadJson = serverPayloadJson,
                formRoute = formRoute,
                recordedAtMs = now(),
            ),
        )
        return id.toString()
    }

    override fun observePending(): Flow<List<ConflictEntry>> =
        dao.observePending().map { rows -> rows.map { it.toEntry() } }

    override suspend fun resolve(conflictId: String, resolution: ConflictResolution) {
        // Both ACCEPT_SERVER and RETRY_LOCAL clear the entry from the inbox once the user has acted;
        // the server value is already applied, and a retry re-submit is a separate mutation.
        val id = conflictId.toLongOrNull() ?: return
        dao.markResolved(id)
    }

    private fun ConflictEntity.toEntry() = ConflictEntry(
        id = id.toString(),
        entity = entity,
        key = entityKey,
        localPayloadJson = localPayloadJson,
        serverPayloadJson = serverPayloadJson,
        formRoute = formRoute,
        recordedAtMs = recordedAtMs,
    )
}

/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.database.infra.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * A recorded write conflict awaiting user resolution — the durable backing for the framework
 * `ConflictInbox` surfaced in Settings. Purely additive framework table (`framework_write_conflicts`).
 *
 * @property resolved 0 = pending (shown in the inbox), 1 = resolved (kept for audit, hidden from the list).
 */
@Entity(tableName = "framework_write_conflicts")
data class ConflictEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entity: String,
    val entityKey: String,
    val localPayloadJson: String,
    val serverPayloadJson: String,
    val formRoute: String?,
    val recordedAtMs: Long,
    val resolved: Int = 0,
)

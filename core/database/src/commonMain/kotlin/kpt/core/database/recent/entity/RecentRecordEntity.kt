/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.recent.entity

import androidx.room3.Entity
import kpt.core.base.database.annotation.DbEntity

/**
 * A record the signed-in officer opened, for Home's "Recently opened". Keyed by user as well as
 * record, so on a shared field device one officer's history is never shown to the next.
 */
@DbEntity
@Entity(tableName = "recent_records", primaryKeys = ["userId", "kind", "id"])
data class RecentRecordEntity(
    val userId: Long,
    /** `RecentKind` name — CLIENT, GROUP, CENTER or LOAN. */
    val kind: String,
    val id: Long,
    val title: String,
    val accountNo: String?,
    val detail: String?,
    val openedAtMs: Long,
)

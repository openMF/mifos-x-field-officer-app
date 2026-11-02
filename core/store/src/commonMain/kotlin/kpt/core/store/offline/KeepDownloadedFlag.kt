/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.offline

import kpt.core.database.center.entity.CenterEntity
import kpt.core.database.group.entity.GroupEntity

/*
 * `sync` is LOCAL state — "downloaded for offline use" — that the server knows nothing about. The
 * center and group LIST stores write whole server pages over their tables, so without this every list
 * refresh reset each downloaded row to `sync = false` and the list's "downloaded" marker vanished.
 * The list writers pass incoming rows through these first.
 */

@kotlin.jvm.JvmName("centersKeepingDownloadedFlag")
internal fun List<CenterEntity>.keepingDownloadedFlag(existing: List<CenterEntity>): List<CenterEntity> {
    val downloaded = existing.filter { it.sync }.mapNotNullTo(HashSet()) { it.id }
    return map { if (it.id in downloaded) it.copy(sync = true) else it }
}

@kotlin.jvm.JvmName("groupsKeepingDownloadedFlag")
internal fun List<GroupEntity>.keepingDownloadedFlag(existing: List<GroupEntity>): List<GroupEntity> {
    val downloaded = existing.filter { it.sync }.mapNotNullTo(HashSet()) { it.id }
    return map { if (it.id in downloaded) it.copy(sync = true) else it }
}

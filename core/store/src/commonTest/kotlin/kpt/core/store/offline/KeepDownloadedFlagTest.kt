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
import kotlin.test.Test
import kotlin.test.assertEquals

class KeepDownloadedFlagTest {

    @Test
    fun aListRefreshKeepsWhatWasDownloaded() {
        val existing = listOf(CenterEntity(id = 1, sync = true), CenterEntity(id = 2))
        // The server's page knows nothing of `sync` — every row arrives false.
        val incoming = listOf(
            CenterEntity(id = 1, name = "North"),
            CenterEntity(id = 2, name = "South"),
            CenterEntity(id = 3),
        )
        assertEquals(listOf(true, false, false), incoming.keepingDownloadedFlag(existing).map { it.sync })
        assertEquals("North", incoming.keepingDownloadedFlag(existing).first().name) // server data still wins
    }

    @Test
    fun groupsKeepItToo() {
        val out = listOf(GroupEntity(id = 7)).keepingDownloadedFlag(listOf(GroupEntity(id = 7, sync = true)))
        assertEquals(true, out.single().sync)
    }
}

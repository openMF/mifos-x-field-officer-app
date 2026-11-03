/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos

import kotlinx.serialization.json.Json
import kpt.core.model.objects.groups.CenterInfo
import kotlin.test.Test
import kotlin.test.assertEquals

/** `runreports/GroupSummaryCounts` as the gateway answers it (lower-case keys, 2026-10-25). */
class CenterInfoDecodeTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun theReportsLowerCaseKeysDecodeIntoEveryCount() {
        val body = """[{"activeclients": 12, "activeloans": 9, "activeclientloans": 7, "activegrouploans": 2,
            "activeborrowers": 8, "activeclientborrowers": 6, "activegroupborrowers": 2,
            "overdueloans": 3, "overdueclientloans": 1, "overduegrouploans": 2}]"""
        val info = json.decodeFromString<List<CenterInfo>>(body).single()
        assertEquals(
            listOf(12, 9, 7, 2, 8, 6, 2, 3, 1, 2),
            listOf(
                info.activeClients, info.activeLoans, info.activeClientLoans, info.activeGroupLoans,
                info.activeBorrowers, info.activeClientBorrowers, info.activeGroupBorrowers,
                info.overdueLoans, info.overdueClientLoans, info.overdueGroupLoans,
            ),
        )
    }
}

/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.center

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kpt.core.database.AppDatabase
import kpt.core.database.center.entity.CenterEntity
import kpt.core.database.collectionsheet.entity.CenterDetailEntity
import kpt.core.database.collectionsheet.entity.MeetingFallCalendar
import kpt.core.database.di.testPlatformModule
import kpt.core.database.group.entity.GroupEntity
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.mp.KoinPlatform
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * The queries the center / group / collection-sheet stores added: a nullable single-row reader for
 * each local write store, and the deletes `Store.clear()` reaches at logout. See `DocumentDaoTest`.
 */
class CenterGroupDaoTest {

    private lateinit var database: AppDatabase

    @BeforeTest
    fun setup() {
        startKoin { modules(testPlatformModule) }
        database = KoinPlatform.getKoin().get()
    }

    @AfterTest
    fun teardown() {
        runCatching { database.close() }
        stopKoin()
    }

    @Test
    fun aCenterRowRoundTripsAndDeletesById() = runTest {
        val dao = database.centerDao
        dao.saveCenter(CenterEntity(id = 1, name = "North"))
        dao.saveCenter(CenterEntity(id = 2, name = "South"))

        assertEquals("North", dao.observeCenter(1).first()?.name)
        dao.deleteCenterById(1)
        assertNull(dao.observeCenter(1).first())
        assertEquals(listOf(2), dao.readAllCenters().first().map { it.id })

        dao.deleteAllCenters()
        assertEquals(emptyList(), dao.readAllCenters().first())
    }

    @Test
    fun anAbsentGroupReadsAsNullNotACrash() = runTest {
        val dao = database.groupsDao
        // `getGroupById` declares a non-null row and so cannot answer "absent"; the write store's
        // reader has to.
        assertNull(dao.observeGroup(7).first())

        dao.insertGroup(GroupEntity(id = 7, name = "Weavers"))
        assertEquals("Weavers", dao.observeGroup(7).first()?.name)

        dao.deleteGroupById(7)
        assertNull(dao.observeGroup(7).first())

        dao.insertGroup(GroupEntity(id = 8))
        dao.deleteAllGroups()
        assertEquals(emptyList(), dao.getAllGroups().first())
    }

    @Test
    fun aRosterRoundTripsItsMeetings() = runTest {
        val dao = database.collectionSheetDao
        // Every insert used to throw here — `MeetingFallCalendar` had no serializer, so the converter
        // could not encode even an empty list and the roster store never persisted a row.
        dao.upsertAll(
            listOf(
                CenterDetailEntity(
                    staffId = 1,
                    meetingFallCenters = listOf(MeetingFallCalendar(id = 3, name = "North")),
                ),
            ),
        )

        assertEquals(listOf("North"), dao.getForStaff(1)?.meetingFallCenters?.map { it.name })
    }

    @Test
    fun aRosterClearsPerStaffMember() = runTest {
        val dao = database.collectionSheetDao
        dao.upsertAll(listOf(CenterDetailEntity(staffId = 1), CenterDetailEntity(staffId = 2)))

        dao.deleteForStaff(1)

        assertNull(dao.getForStaff(1))
        assertEquals(2, dao.getForStaff(2)?.staffId)
    }
}

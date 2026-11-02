/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.savings

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kpt.core.database.AppDatabase
import kpt.core.database.di.testPlatformModule
import kpt.core.database.savings.entity.SavingsAccountWithAssociationsEntity
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.mp.KoinPlatform
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** The deletes behind `savingsAccounts`' per-key clear and logout purge — see `FetchedAtDaoTest`. */
class SavingsDaoTest {

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
    fun deleteClearsOneAccountAndLeavesTheOthers() = runTest {
        val dao = database.savingsDao
        dao.insertSavingsAccountWithAssociations(SavingsAccountWithAssociationsEntity(id = 1, accountNo = "a"))
        dao.insertSavingsAccountWithAssociations(SavingsAccountWithAssociationsEntity(id = 2, accountNo = "b"))

        dao.deleteSavingsAccountWithAssociations(1)

        assertNull(dao.getSavingsAccountWithAssociations(1).first())
        assertEquals("b", dao.getSavingsAccountWithAssociations(2).first()?.accountNo)
    }

    @Test
    fun deleteAllEmptiesTheTable() = runTest {
        val dao = database.savingsDao
        dao.insertSavingsAccountWithAssociations(SavingsAccountWithAssociationsEntity(id = 1))
        dao.insertSavingsAccountWithAssociations(SavingsAccountWithAssociationsEntity(id = 2))

        dao.deleteAllSavingsAccountsWithAssociations()

        assertNull(dao.getSavingsAccountWithAssociations(1).first())
        assertNull(dao.getSavingsAccountWithAssociations(2).first())
    }
}

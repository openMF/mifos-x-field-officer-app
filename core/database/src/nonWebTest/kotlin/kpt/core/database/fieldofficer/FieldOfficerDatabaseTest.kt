/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.fieldofficer

import kpt.core.database.AppDatabase
import kpt.core.database.di.TestDatabaseModule
import kpt.core.database.di.testPlatformModule
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.mp.KoinPlatform
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * The field officer's own schema: its DAOs are reachable from the database and from DI, and the
 * schema version this app ships. Fork-owned, beside the template's schema-independent checks in
 * `config/AppDatabaseTest` and `di/DatabaseModuleTest`, which a template sync replaces.
 */
class FieldOfficerDatabaseTest {

    private var database: AppDatabase? = null

    @AfterTest
    fun teardown() {
        // stopKoin() must run even when a test failed before building, or the first fault leaks a
        // started Koin and every later test dies with KoinApplicationAlreadyStartedException.
        runCatching { database?.close() }
        stopKoin()
    }

    @Test
    fun databaseExposesTheFieldOfficerDaos() {
        startKoin { modules(testPlatformModule) }
        database = KoinPlatform.getKoin().get()

        assertNotNull(database!!.clientDao)
        assertNotNull(database!!.loanDao)
    }

    @Test
    fun clientDaoComesFromTheDatabaseModule() {
        startKoin { modules(TestDatabaseModule) }
        database = KoinPlatform.getKoin().get()

        assertNotNull(database!!.clientDao)
    }

    @Test
    fun databaseVersionIsCurrent() {
        // 14: the 13→14 change dropped six inverted FOREIGN KEY constraints that made every insert
        // fail with SQLite 787, so the offline cache could never be written (see
        // `app-profile/migration-ledger.yaml`). Update this constant with
        // `ForkDatabaseConfig.VERSION` so the guardrail stays meaningful.
        // 15: added `document_contents` (document file bytes) — an additive AutoMigration.
        // 16: `Charges` re-keyed to (resourceType, resourceId, id) so client / loan / savings
        // charges sharing an id coexist (a plain AutoMigration recreate).
        // 17: added `server_instances` (the server picker's instance list) — an additive AutoMigration.
        // 18: `Note.resourceType` (default 'clients') — notes for any owner. Additive AutoMigration.
        // 19: added `recent_records` (Home's recently opened). Additive AutoMigration.
        // 20: added `offline_records` + `sync_jobs` (Offline sync). Additive AutoMigration.
        assertEquals(20, AppDatabase.VERSION)
    }
}

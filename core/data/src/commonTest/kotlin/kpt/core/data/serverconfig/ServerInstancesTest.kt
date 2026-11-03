/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.serverconfig

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kpt.core.database.serverconfig.dao.ServerInstanceDao
import kpt.core.database.serverconfig.entity.ServerInstanceEntity
import kpt.core.model.utils.DemoCredentials
import kpt.core.network.lwmswhoxvvoagzkqxiyd.appserverconfig.api.ServerInstanceApi
import kpt.core.network.lwmswhoxvvoagzkqxiyd.appserverconfig.dto.RemoteDemoAccountDto
import kpt.core.network.lwmswhoxvvoagzkqxiyd.appserverconfig.dto.RemoteServerInstanceDto
import kpt.core.network.mifos.server.AppServerConfig
import kpt.core.network.mifos.server.BundledInstanceConfigLoader
import kpt.core.store.serverconfig.provideServerInstancesStore
import org.mobilenativefoundation.store.store5.impl.extensions.fresh
import org.mobilenativefoundation.store.store5.impl.extensions.get
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The `serverInstances` store, bottom to top: Supabase `app_server_config` (this app's rows) ->
 * `server_instances` table -> the picker's list.
 */
class ServerInstancesTest {

    private class FakeServerInstanceApi(
        private val rows: List<RemoteServerInstanceDto>,
        override val isConfigured: Boolean = true,
        private val failure: Throwable? = null,
    ) : ServerInstanceApi {
        val asked = mutableListOf<String>()
        override suspend fun fetchInstances(app: String): List<RemoteServerInstanceDto> {
            asked += app
            failure?.let { throw it }
            return rows
        }
    }

    private class MemoryDao : ServerInstanceDao {
        val rows = MutableStateFlow<List<ServerInstanceEntity>>(emptyList())
        override fun observeAll(): Flow<List<ServerInstanceEntity>> = rows
        override suspend fun insertAll(instances: List<ServerInstanceEntity>) {
            rows.value = rows.value + instances
        }
        override suspend fun deleteAll() {
            rows.value = emptyList()
        }
    }

    // As the server returns them: already filtered to this app by `supported_apps`, in sort_order.
    private val remoteRows = listOf(
        RemoteServerInstanceDto(
            label = "Remote",
            protocol = "https://",
            endpoint = "remote.example.org",
            path = "/fineract-provider/api/v1/",
            platformTenantId = "t",
            supportedApps = listOf("field-officer"),
            demoAccounts = listOf(RemoteDemoAccountDto("Officer", "u", "p")),
        ),
        RemoteServerInstanceDto(
            label = "Second",
            protocol = "http://",
            endpoint = "localhost",
            path = "/fineract-provider/api/v1/",
            port = "8080",
            platformTenantId = "default",
            supportedApps = listOf("field-officer"),
        ),
    )

    @Test
    fun aFetchStoresThisAppsRowsInOrderWithTheirDemoSignIns() = runTest {
        val api = FakeServerInstanceApi(remoteRows)
        val dao = MemoryDao()
        val list = provideServerInstancesStore(api, dao).fresh(Unit)
        assertEquals(listOf("Remote", "Second"), list.map { it.name })
        assertEquals(listOf(DemoCredentials("Officer", "u", "p")), list.first().demoAccounts)
        assertEquals(listOf(AppServerConfig.APP_ID), api.asked, "the server is asked for THIS app's rows")
        assertEquals(
            listOf("Remote", "Second"),
            dao.rows.value.sortedBy { it.position }.map { it.name },
            "kept in the database",
        )
    }

    @Test
    fun aFailedFetchServesWhatTheDatabaseAlreadyHas() = runTest {
        val dao = MemoryDao()
        provideServerInstancesStore(FakeServerInstanceApi(remoteRows), dao).fresh(Unit)
        val offline = provideServerInstancesStore(
            FakeServerInstanceApi(emptyList(), failure = RuntimeException("offline")),
            dao,
        )
        assertEquals(listOf("Remote", "Second"), offline.get(Unit).map { it.name })
    }

    @Test
    fun noKeyOrNoRowsForThisAppUsesTheListShippedInTheApp() = runTest {
        val bundled = BundledInstanceConfigLoader.load()
        assertEquals(
            bundled,
            provideServerInstancesStore(
                FakeServerInstanceApi(remoteRows, isConfigured = false),
                MemoryDao(),
            ).fresh(Unit),
        )
        assertEquals(bundled, provideServerInstancesStore(FakeServerInstanceApi(emptyList()), MemoryDao()).fresh(Unit))
    }
}

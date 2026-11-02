/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(kpt.core.base.store.screen.ExperimentalScreenDataStreamTestingApi::class)

package kpt.core.data.testing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.data.serverconfig.ServerConfigRepository
import kpt.core.model.utils.DemoCredentials
import kpt.core.model.utils.ServerConfig
import kpt.core.model.utils.ServerInstance

/**
 * Test double for [ServerConfigRepository]: [saved] is the persisted preference, so a test reads
 * what a submit actually wrote. [failSaves] models the write failing.
 */
@ExperimentalDataTestingApi
class FakeServerConfigRepository(
    override val defaultConfig: ServerConfig = TEST_DEFAULT,
    initial: ServerConfig = defaultConfig,
    instanceList: List<ServerInstance> = TEST_INSTANCES,
) : ServerConfigRepository {

    val saved = MutableStateFlow(initial)

    /** The instance list's state — set to Loading / Error to exercise the screen's other states. */
    val instancesState = MutableStateFlow<ScreenState<List<ServerInstance>>>(ScreenState.Content(instanceList))

    override fun instancesStream(scope: CoroutineScope): ScreenDataStream<List<ServerInstance>> =
        screenDataStreamForTesting(instancesState)

    var failSaves: Boolean = false

    override fun serverConfigStream(scope: CoroutineScope): ScreenDataStream<ServerConfig> =
        screenDataStreamForTesting(saved.map<ServerConfig, ScreenState<ServerConfig>> { ScreenState.Content(it) })

    override suspend fun updateServerConfig(config: ServerConfig): MutationResult<Unit> {
        if (failSaves) return MutationResult.Failed(cause = IllegalStateException("save failed"), rolledBack = false)
        saved.value = config
        return MutationResult.Applied(value = Unit, synced = true)
    }

    companion object {
        /** A shipped-default stand-in: tests must not depend on which server app-profile names. */
        val TEST_DEFAULT = ServerConfig(
            protocol = "https://",
            endPoint = "fineract.example.org",
            apiPath = "/fineract-provider/api/v1/",
            port = "443",
            tenant = "default",
        )

        /** A demo instance at [TEST_DEFAULT] plus a non-demo one, so both shapes are exercised. */
        val TEST_INSTANCES = listOf(
            ServerInstance(
                "test",
                "Test server",
                TEST_DEFAULT,
                listOf(
                    DemoCredentials("Officer", "demo", "demo-pass"),
                    DemoCredentials("Supervisor", "boss", "boss-pass"),
                ),
            ),
            ServerInstance("localhost", "Localhost", ServerConfig.LOCALHOST),
        )
    }
}

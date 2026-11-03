/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.testing

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkChangeEvent
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkInfo
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkStatus
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kpt.core.base.data.infra.NetworkMonitor

/** A [NetworkMonitor] the test switches: set [online] to drive a screen into / out of its offline state. */
@ExperimentalDataTestingApi
class FakeNetworkMonitor(online: Boolean = true) : NetworkMonitor {
    /** Whether the fake network is online; set it to simulate a change. */
    val online = MutableStateFlow(online)

    override val isOnline: StateFlow<Boolean> = this.online

    override val networkStatus: StateFlow<NetworkStatus> = this.online
        .map {
            if (it) {
                NetworkStatus.Available(
                    NetworkInfo(
                        type = NetworkType.WiFi,
                        isMetered = false,
                    ),
                )
            } else {
                NetworkStatus.Unavailable
            }
        }
        .stateIn(CoroutineScope(Dispatchers.Unconfined), SharingStarted.Eagerly, NetworkStatus.Unavailable)

    override val networkChanges: SharedFlow<NetworkChangeEvent> = MutableSharedFlow<NetworkChangeEvent>().asSharedFlow()

    override fun close() = Unit
}

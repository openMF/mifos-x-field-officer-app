/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.datastore.offline

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kpt.core.model.offline.RefreshMode
import kpt.core.model.offline.RefreshSettings

/**
 * How Offline sync keeps the officer's chosen records current (Manual, or Automatic with an
 * interval and Wi-Fi / charging constraints).
 */
// datastore-scope: per-device — the choice is about this phone's data plan and battery, not the
// officer's account, so it survives sign-out like the review-prompt counters.
interface OfflineSyncSettingsStore {
    val settings: StateFlow<RefreshSettings>
    fun update(settings: RefreshSettings)
}

internal const val OFFLINE_MODE_KEY = "fork.offline_sync.mode"
internal const val OFFLINE_INTERVAL_KEY = "fork.offline_sync.interval_hours"
internal const val OFFLINE_WIFI_KEY = "fork.offline_sync.wifi_only"
internal const val OFFLINE_CHARGING_KEY = "fork.offline_sync.charging_only"

/** [Settings]-backed, on the same plain store as the other device preferences. */
class SettingsOfflineSyncSettingsStore(private val plainSettings: Settings) : OfflineSyncSettingsStore {

    private val state = MutableStateFlow(read())
    override val settings: StateFlow<RefreshSettings> = state.asStateFlow()

    override fun update(settings: RefreshSettings) {
        plainSettings.putString(OFFLINE_MODE_KEY, settings.mode.name)
        plainSettings.putInt(OFFLINE_INTERVAL_KEY, settings.intervalHours)
        plainSettings.putBoolean(OFFLINE_WIFI_KEY, settings.wifiOnly)
        plainSettings.putBoolean(OFFLINE_CHARGING_KEY, settings.chargingOnly)
        state.value = settings
    }

    private fun read(): RefreshSettings {
        val default = RefreshSettings()
        val interval = plainSettings.getInt(OFFLINE_INTERVAL_KEY, default.intervalHours)
        val storedMode = plainSettings.getStringOrNull(OFFLINE_MODE_KEY)
        return RefreshSettings(
            mode = RefreshMode.entries.firstOrNull { it.name == storedMode } ?: default.mode,
            intervalHours = interval.takeIf { it in RefreshSettings.INTERVALS } ?: default.intervalHours,
            wifiOnly = plainSettings.getBoolean(OFFLINE_WIFI_KEY, default.wifiOnly),
            chargingOnly = plainSettings.getBoolean(OFFLINE_CHARGING_KEY, default.chargingOnly),
        )
    }
}

/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.settings

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable
import kpt.core.base.store.infra.DraftRecord
import kpt.core.base.ui.nav.composableWithPushTransitions

/** The settings screen. */
@Serializable
data object SettingsRoute

/** The notification settings screen. */
@Serializable
data object NotificationRoute

/** The sync and drafts screen. */
@Serializable
data object SyncAndDraftsRoute

/** Opens the settings screen. */
fun NavController.navigateToSettings(navOptions: NavOptions? = null) = navigate(SettingsRoute, navOptions)

/** Opens the notification settings. */
fun NavController.navigateToNotification(navOptions: NavOptions? = null) = navigate(NotificationRoute, navOptions)

/** Opens the sync and drafts screen. */
fun NavController.navigateToSyncAndDrafts(navOptions: NavOptions? = null) = navigate(SyncAndDraftsRoute, navOptions)

/**
 * The settings backbone destination. [settingsBody] is the fork-owned inner content (default supplied by
 * `cmp-navigation`'s `ForkBackboneRegistry.settingsBody`, which wires the back / sync-and-drafts / dev-menu
 * callbacks into the demo body); this template graph forwards the opaque body. (WS01 base-feature seam,
 * epic AC7 — mirrors the `home` shell/seam split.)
 */
fun NavGraphBuilder.settingsDestination(settingsBody: @Composable () -> Unit = {}) {
    composableWithPushTransitions<SettingsRoute> {
        settingsBody()
    }
}

/** Registers the notification settings screen in the nav graph. */
fun NavGraphBuilder.notificationDestination(onBackClick: () -> Unit) {
    composableWithPushTransitions<NotificationRoute> {
        NotificationScreen(
            onBackClick = onBackClick,
        )
    }
}

/**
 * [draftExtras] lets the app name its drafts and reopen their forms (`DraftRowExtras`): called once
 * per screen, it returns the per-row lookup. Default = the template's raw form-key rows.
 */
fun NavGraphBuilder.syncAndDraftsDestination(
    onBackClick: () -> Unit,
    draftExtras: @Composable () -> (DraftRecord) -> DraftRowExtras? = { { null } },
) {
    composableWithPushTransitions<SyncAndDraftsRoute> {
        SyncAndDraftsScreen(
            onBackClick = onBackClick,
            draftExtras = draftExtras,
        )
    }
}

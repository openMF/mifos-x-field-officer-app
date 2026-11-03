/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.pathtracking.ui

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.objects.users.UserLatLng
import org.jetbrains.compose.ui.tooling.preview.Preview
import kpt.core.base.designsystem.theme.KptTheme as KptBaseTheme

/** A morning round through central Mexico City, and a short stop with no stored address. */
private val sample = listOf(
    TrackedPath(
        points = listOf(
            UserLatLng(19.4326, -99.1332),
            UserLatLng(19.4340, -99.1405),
            UserLatLng(19.4282, -99.1446),
            UserLatLng(19.4231, -99.1580),
        ),
        startAddress = "Zócalo, Centro",
        endAddress = "Av. Chapultepec, Juárez",
        date = "05 Mar 2026",
        startTime = "09:10",
        stopTime = "10:42",
    ),
    TrackedPath(listOf(UserLatLng(19.4011, -99.1700)), null, null, "04 Mar 2026", "15:00", "15:20"),
)

@Composable
private fun Render(state: ScreenState<List<TrackedPath>>) = KptTheme(darkTheme = false) {
    Surface(color = KptBaseTheme.colorScheme.background) {
        PathTrackingScreenContent(paths = state, onNavigateBack = {}, onRefresh = {}, onOpenRoute = {})
    }
}

@Preview @Composable
internal fun PathTrackingListPreview() = Render(ScreenState.Content(sample))

@Preview @Composable
internal fun PathTrackingEmptyPreview() = Render(ScreenState.Content(emptyList()))

@Preview @Composable
internal fun PathTrackingErrorPreview() = Render(ScreenState.Error(IllegalStateException("preview")))

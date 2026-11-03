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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.mapContent
import kpt.core.data.pathtracking.PathTrackingRepository
import kpt.core.data.session.SessionRepository
import kpt.core.model.objects.users.UserLatLng
import kpt.core.model.objects.users.UserLocation

/**
 * Ported from `6b66e8a43:feature/path-tracking/…/PathTrackingViewModel.kt`.
 *
 * Two deviations, both recorded in MIGRATION.md. The original passed the user's OFFICE id to a
 * read keyed by USER id, so it asked for the wrong row; this reads the signed-in user's own paths.
 * And its Start/Stop action only flipped a preference — nothing in the original recorded a path —
 * so it is not ported: a control that records nothing would be a dead control.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class PathTrackingViewModel(
    session: SessionRepository,
    private val repository: PathTrackingRepository,
) : ViewModel() {

    private var stream: ScreenDataStream<List<UserLocation>>? = null

    val paths: StateFlow<ScreenState<List<TrackedPath>>> = session.sessionStream(viewModelScope).state
        .flatMapLatest { s ->
            if (s is ScreenState.Content) {
                repository.userPathsStream(s.data.userId.toInt(), viewModelScope).also { stream = it }
                    .state.mapContent { rows, _ -> rows.map(UserLocation::toTrackedPath) }
            } else {
                // Re-typed unchanged — the transform never runs on a state without content.
                flowOf(s).mapContent { _, _ -> emptyList() }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_TIMEOUT_MS), ScreenState.Loading)

    fun onRefresh() {
        stream?.refresh(forceFresh = true)
    }

    fun onRetry() {
        stream?.retry()
    }

    private companion object {
        const val STATE_TIMEOUT_MS = 5_000L
    }
}

/** One recorded path, its stored `latLng` JSON decoded once rather than on every recomposition. */
internal data class TrackedPath(
    val points: List<UserLatLng>,
    val startAddress: String?,
    val endAddress: String?,
    val date: String?,
    val startTime: String?,
    val stopTime: String?,
)

private val latLngJson = Json { ignoreUnknownKeys = true }

/** The original's `getLatLngList`; a malformed or empty value is an empty path, never a crash. */
internal fun UserLocation.toTrackedPath() = TrackedPath(
    points = latLng?.takeIf { it.isNotBlank() }
        ?.let { runCatching { latLngJson.decodeFromString<List<UserLatLng>>(it) }.getOrNull() }
        .orEmpty(),
    startAddress = startAddress,
    endAddress = endAddress,
    date = date,
    startTime = startTime,
    stopTime = stopTime,
)

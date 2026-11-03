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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.data.pathtracking.PathTrackingRepository
import kpt.core.model.objects.users.UserLocation

/** Scripted paths; [requested] records the user id each stream was opened for. */
@ExperimentalDataTestingApi
class FakePathTrackingRepository(paths: List<UserLocation> = emptyList()) : PathTrackingRepository {

    /** The tracked paths the stream emits. */
    val paths = MutableStateFlow<ScreenState<List<UserLocation>>>(ScreenState.Content(paths))

    /** The users whose paths were read, in order. */
    val requested = mutableListOf<Int>()

    override fun userPathsStream(
        userId: Int,
        scope: CoroutineScope,
    ) = screenDataStreamForTesting(paths).also { requested += userId }
}

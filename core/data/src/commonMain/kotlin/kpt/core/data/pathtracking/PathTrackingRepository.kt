/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.pathtracking

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.model.objects.users.UserLocation

/** Ported from `6b66e8a43:core/data/…/PathTrackingRepository.kt` — one read, now a stream. */
interface PathTrackingRepository {

    /** Every path recorded for [userId], as Fineract's datatable returns them. */
    fun userPathsStream(userId: Int, scope: CoroutineScope): ScreenDataStream<List<UserLocation>>
}

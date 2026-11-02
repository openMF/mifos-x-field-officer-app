/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.pathtracking.impl

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.data.annotation.FromStore
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.data.pathtracking.PathTrackingRepository
import kpt.core.model.objects.users.UserLocation
import kpt.core.store.config.AppCacheKeys
import kpt.core.store.config.AppStoreIds
import kpt.core.store.config.AppStoreRegistry
import org.mobilenativefoundation.store.store5.Store

@RepositoryBinding(binds = PathTrackingRepository::class)
internal class PathTrackingRepositoryImpl(
    @FromStore(AppStoreIds.UserPathTracking) private val store: Store<Int, List<UserLocation>>,
) : PathTrackingRepository {

    override fun userPathsStream(userId: Int, scope: CoroutineScope): ScreenDataStream<List<UserLocation>> =
        store.asScreenStream(
            key = userId,
            cacheKey = AppCacheKeys.UserPathTracking.forKey("$userId"),
            scope = scope,
            fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
            ttl = AppStoreRegistry.Ttl.USER_PATH_TRACKING,
        )
}

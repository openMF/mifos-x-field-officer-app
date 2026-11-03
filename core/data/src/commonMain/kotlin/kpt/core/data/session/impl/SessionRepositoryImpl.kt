/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.session.impl

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.data.annotation.FromStore
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.data.session.SessionRepository
import kpt.core.model.objects.users.User
import kpt.core.store.config.AppCacheKeys
import kpt.core.store.config.AppStoreIds
import org.mobilenativefoundation.store.store5.Store

@RepositoryBinding(binds = SessionRepository::class)
internal class SessionRepositoryImpl(
    @FromStore(AppStoreIds.Session) private val store: Store<Unit, User>,
) : SessionRepository {

    override fun sessionStream(scope: CoroutineScope): ScreenDataStream<User> =
        store.asScreenStream(
            key = Unit,
            cacheKey = AppCacheKeys.Session.KEY,
            scope = scope,
            fetchPolicy = FetchPolicy.CACHE_ONLY,
        )
}

/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.session

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.model.objects.users.User

/** The stored Fineract session, read through core/data — never by a feature reaching into core/datastore. */
interface SessionRepository {

    /** Local-only (`CACHE_ONLY`). An empty [User] means no stored session. */
    fun sessionStream(scope: CoroutineScope): ScreenDataStream<User>
}

/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(kpt.core.base.store.screen.ExperimentalScreenDataStreamTestingApi::class)

package kpt.core.data.testing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.data.session.SessionRepository
import kpt.core.model.objects.users.User

/** Test double for [SessionRepository]: [session] is the stored user; an empty [User] is "signed out". */
@ExperimentalDataTestingApi
class FakeSessionRepository(initial: User = User()) : SessionRepository {

    /** The stored session the stream emits. */
    val session = MutableStateFlow(initial)

    override fun sessionStream(scope: CoroutineScope): ScreenDataStream<User> =
        screenDataStreamForTesting(session.map<User, ScreenState<User>> { ScreenState.Content(it) })
}

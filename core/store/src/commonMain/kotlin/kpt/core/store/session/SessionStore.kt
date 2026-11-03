/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:Suppress("MatchingDeclarationName")

package kpt.core.store.session

import kotlinx.coroutines.flow.Flow
import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.model.objects.users.User
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store

/** The read PORT for the stored Fineract session, bound in core/data (as `ServerConfigSource` is). */
fun interface SessionSource {
    /** The stored session user, re-emitted on every change. */
    fun observe(): Flow<User>
}

/**
 * OFFLINE_LOCAL_ONLY store over the stored session — what sign-in reads to decide whether a
 * biometric unlock has a session to unlock INTO.
 *
 * `logout = false`: sign-out clears the session through `AuthCommandRepository.signOut`, which
 * makes [SessionSource.observe] re-emit the empty user; a purge here would have nothing to remove.
 * Writes go through `AuthCommandRepository`, so the writer is never invoked.
 */
// logout-exempt: sign-out clears the session via AuthCommandRepository; nothing left to purge
@StoreProvider(id = "session", logout = false)
@CacheKey(name = "KEY", key = "session")
fun provideSessionStore(source: SessionSource): Store<Unit, User> =
    StoreFactory.createOfflineStore(
        sourceOfTruth = SourceOfTruth.of(
            reader = { _: Unit -> source.observe() },
            writer = { _: Unit, _: User -> Unit },
        ),
    )

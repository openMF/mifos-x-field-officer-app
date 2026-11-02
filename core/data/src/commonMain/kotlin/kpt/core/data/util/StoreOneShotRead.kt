/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.util

import kotlinx.coroutines.CancellationException
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.impl.extensions.fresh
import org.mobilenativefoundation.store.store5.impl.extensions.get

/**
 * One-shot read for a value a FORM needs once — a template, a generated sheet: the server's
 * answer when reachable, the cached one when not, null when there is neither.
 *
 * `fresh` writes the response through the store's source of truth, so the offline `get` answers
 * with the last value fetched. The alternative, `stream(cached(key, refresh = false)).first()`,
 * is wrong for any store backed by a persistent cache: once a value is cached it is served on
 * every later call, across restarts, and the server is never asked again — a form keeps offering
 * charges, products or rules the server has since changed (Store5 gate S5-5).
 */
internal suspend fun <K : Any, V : Any> Store<K, V>.freshOrCached(key: K): V? =
    orNull { fresh(key) } ?: orNull { get(key) }

private inline fun <T> orNull(block: () -> T): T? = try {
    block()
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (_: Exception) {
    null
}

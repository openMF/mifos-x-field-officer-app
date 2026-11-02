/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.charge.impl

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kpt.core.base.data.annotation.FromStore
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.data.charge.ChargeRepository
import kpt.core.model.charge.Charges
import kpt.core.model.objects.charge.ChargeResource
import kpt.core.model.objects.template.client.ChargeTemplate
import kpt.core.store.charge.ChargeKey
import kpt.core.store.charge.GetChargeTemplateKey
import kpt.core.store.config.AppCacheKeys
import kpt.core.store.config.AppStoreIds
import kpt.core.store.config.AppStoreRegistry
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.impl.extensions.fresh
import org.mobilenativefoundation.store.store5.impl.extensions.get

/** Every read goes through a store — network → `core/database` → `core/store` → here. */
@RepositoryBinding(binds = ChargeRepository::class)
internal class ChargeRepositoryImpl(
    @FromStore(AppStoreIds.Charges) private val store: Store<ChargeKey, List<Charges>>,
    @FromStore(AppStoreIds.ChargeGetChargeTemplate)
    private val templateStore: Store<GetChargeTemplateKey, ChargeTemplate>,
) : ChargeRepository {

    // NETWORK_WITH_CACHE, not the CACHE_FIRST_SWR default: SWR's read path is
    // `cached(key, refresh = false)` and leaves fetching to a freshness-band gate that cannot fire
    // until something HAS been fetched, so on a clean install nothing ever loads. Room is still
    // served first, so the offline-first behaviour is unchanged.
    override fun chargesStream(
        resource: ChargeResource,
        resourceId: Int,
        scope: CoroutineScope,
    ): ScreenDataStream<List<Charges>> =
        store.asScreenStream(
            key = ChargeKey(resource.path, resourceId),
            cacheKey = AppCacheKeys.Charges.forResource(resource.path, resourceId),
            scope = scope,
            // NO `isEmpty = { it.isEmpty() }`. That predicate means "no data has arrived yet", and
            // the stream answers it by holding the LAST content as "refresh in progress" — so
            // deleting an owner's last charge left it on screen although the row was gone (device,
            // 2026-10-23). An empty list is real content; the ViewModel's `emptyIfContent` renders it.
            fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
            ttl = AppStoreRegistry.Ttl.CHARGES,
        )

    override suspend fun chargeTemplate(resource: ChargeResource, resourceId: Int): ChargeTemplate? =
        // Cache-first (`get`): a template changes only when the tenant reconfigures its charges,
        // and the read store's ttl bounds how stale it can be.
        orNull { templateStore.get(GetChargeTemplateKey(resource.path, resourceId)) }

    /**
     * The server's list first — `fresh` writes it through the table before returning it — then the
     * table offline. Not cache-first: the reader reports an empty table as an empty LIST (it must,
     * see `NullReaderStreamTest`), so a `cached` read of a never-fetched owner answers "no charges"
     * before the fetch ever runs.
     */
    override suspend fun charge(resource: ChargeResource, resourceId: Int, chargeId: Int): Charges? {
        val key = ChargeKey(resource.path, resourceId)
        val rows = orNull { store.fresh(key) } ?: orNull { store.get(key) }
        return rows?.firstOrNull { it.id == chargeId }
    }

    private inline fun <T> orNull(block: () -> T): T? = try {
        block()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (_: Exception) {
        null
    }
}

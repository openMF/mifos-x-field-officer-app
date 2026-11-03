/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.center.impl

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kpt.core.base.data.annotation.FromStore
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.data.center.CenterRepository
import kpt.core.model.center.Center
import kpt.core.model.center.CenterWithAssociations
import kpt.core.model.objects.databaseobjects.CollectionSheet
import kpt.core.model.objects.groups.CenterInfo
import kpt.core.model.shared.Payload
import kpt.core.store.center.CenterCollectionSheetKey
import kpt.core.store.config.AppCacheKeys
import kpt.core.store.config.AppStoreIds
import kpt.core.store.config.AppStoreRegistry
import kpt.core.store.report.GetCenterSummaryInfoKey
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.impl.extensions.fresh
import org.mobilenativefoundation.store.store5.impl.extensions.get

@RepositoryBinding(binds = CenterRepository::class)
internal class CenterRepositoryImpl(
    @FromStore(AppStoreIds.Centers) private val store: Store<Unit, List<Center>>,
    @FromStore(AppStoreIds.CenterCollectionSheet)
    private val sheetStore: Store<CenterCollectionSheetKey, CollectionSheet>,
    @FromStore(AppStoreIds.CenterGetCenterWithGroupMembersAndCollectionMeetingCalendar)
    private val withMeetingStore: Store<Int, CenterWithAssociations>,
    @FromStore(AppStoreIds.ReportGetCenterSummaryInfo)
    private val summaryStore: Store<GetCenterSummaryInfoKey, List<CenterInfo>>,
) : CenterRepository {

    // NETWORK_WITH_CACHE, not the CACHE_FIRST_SWR default: SWR's read path is
    // `cached(key, refresh = false)` and leaves fetching to a freshness-band gate that cannot fire
    // until something HAS been fetched, so on a clean install nothing ever loads. Room is still
    // served first, so the offline-first behaviour is unchanged. The ttl stays the declared
    // freshness bound — it drives the "updated N ago" indicator, never whether cache is served.
    override fun centersStream(scope: CoroutineScope): ScreenDataStream<List<Center>> =
        store.asScreenStream(
            key = Unit,
            cacheKey = AppCacheKeys.Centers.LIST,
            scope = scope,
            // No `isEmpty`: an empty list is real content (see ChargeRepositoryImpl.chargesStream).
            fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
            ttl = AppStoreRegistry.Ttl.CENTERS,
        )

    /**
     * Through [sheetStore], never `CenterApi` directly: `fresh` writes the generated sheet through
     * the read cache, and offline `get` answers from it. A sheet the officer SAVED is re-read into
     * that row by the save itself, so the cached answer is never the pre-save one.
     */
    override suspend fun collectionSheet(centerId: Long, payload: Payload): CollectionSheet? {
        val key = CenterCollectionSheetKey.of(centerId, payload)
        return orNull { sheetStore.fresh(key) } ?: orNull { sheetStore.get(key) }
    }

    private inline fun <T> orNull(block: () -> T): T? = try {
        block()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (_: Exception) {
        null
    }

    override fun centerWithMeetingStream(
        centerId: Int,
        scope: CoroutineScope,
    ): ScreenDataStream<CenterWithAssociations> =
        withMeetingStore.asScreenStream(
            key = centerId,
            cacheKey = AppCacheKeys.CenterGetCenterWithGroupMembersAndCollectionMeetingCalendar
                .forKey(centerId.toString()),
            scope = scope,
            fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
            ttl = AppStoreRegistry.Ttl.CENTER_GET_CENTER_WITH_GROUP_MEMBERS_AND_COLLECTION_MEETING_CALENDAR,
        )

    override fun centerSummaryStream(centerId: Int, scope: CoroutineScope): ScreenDataStream<List<CenterInfo>> {
        // `genericResultSet = false` — the original's call, and the shape `CenterInfo` decodes.
        val key = GetCenterSummaryInfoKey(centerId = centerId, genericResultSet = false)
        return summaryStore.asScreenStream(
            key = key,
            cacheKey = AppCacheKeys.ReportGetCenterSummaryInfo.forKey("${key.centerId}:${key.genericResultSet}"),
            scope = scope,
            fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
            ttl = AppStoreRegistry.Ttl.REPORT_GET_CENTER_SUMMARY_INFO,
        )
    }
}

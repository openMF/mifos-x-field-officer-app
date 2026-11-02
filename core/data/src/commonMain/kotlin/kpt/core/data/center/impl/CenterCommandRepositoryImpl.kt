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
import kpt.core.base.data.annotation.FromStore
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.mutation.CommandSpec
import kpt.core.base.store.mutation.MutationGateway
import kpt.core.base.store.mutation.MutationPolicy
import kpt.core.data.center.CenterCommandRepository
import kpt.core.model.center.Center
import kpt.core.model.center.CenterPayload
import kpt.core.model.center.CenterWithAssociations
import kpt.core.model.center.PostCentersCenterIdRequest
import kpt.core.model.collectionsheet.payload.CollectionSheetPayload
import kpt.core.model.objects.clients.ActivatePayload
import kpt.core.model.objects.databaseobjects.CollectionSheet
import kpt.core.network.mifos.center.api.CenterApi
import kpt.core.store.center.CenterCollectionSheetKey
import kpt.core.store.config.AppStoreIds
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreWriteRequest
import org.mobilenativefoundation.store.store5.impl.extensions.fresh

/**
 * Center writes, through the single write door ([MutationGateway.command]).
 *
 * Network → store → database: [CommandSpec.endpoint] is the Fineract call, and
 * [CommandSpec.localApply] records the server's acceptance THROUGH a store. 6b66e8a43 ended at the
 * endpoint with no local write, so a center created or activated a moment ago read as missing or
 * still pending until the next list refresh.
 *
 * What a center write changes is computed by the server — the account number, the status, the
 * activation/closure dates — and neither the request nor Fineract's `{resourceId}` reply carries it.
 * So `localApply` ingests the server's record: it reads the center through its keyed store
 * (network → read cache) and writes it through [itemStore] into the `Center` table the list reads.
 * That read happens only once the server has accepted the write, and failing it never turns the
 * accepted write into a `Failed` the officer would retry (see [record]).
 *
 * Every write stays `OnlineRequired` (no offline queue here — that is `OfflineQueueRepository`).
 */
@RepositoryBinding(binds = CenterCommandRepository::class)
internal class CenterCommandRepositoryImpl(
    private val centerApi: CenterApi,
    private val gateway: MutationGateway,
    @FromStore(AppStoreIds.CenterGetCenterWithGroupMembersAndCollectionMeetingCalendar)
    private val centerStore: Store<Int, CenterWithAssociations>,
    @FromStore(AppStoreIds.CenterItem) private val itemStore: MutableStore<Int, Center>,
    @FromStore(AppStoreIds.CenterCollectionSheet)
    private val sheetStore: Store<CenterCollectionSheetKey, CollectionSheet>,
) : CenterCommandRepository {

    /** A center state command (`close`, ...). The resulting status is the server's. */
    override suspend fun activate2(
        centerId: Long,
        postCentersCenterIdRequest: PostCentersCenterIdRequest,
        command: String?,
    ) =
        gateway.command(
            CommandSpec(
                payload = postCentersCenterIdRequest,
                endpoint = {
                    centerApi.activate2(
                        centerId = centerId,
                        postCentersCenterIdRequest = it,
                        command = command,
                    )
                },
                localApply = { response -> if (response != null) record(centerId.toInt()) },
            ),
            policy = MutationPolicy.OnlineRequired,
        )

    /**
     * Saving a sheet posts repayments the server applies to live balances, so the generated sheet
     * cached for that meeting is no longer true. `localApply` re-reads it through [sheetStore] —
     * the same row a later read answers from.
     */
    override suspend fun saveCollectionSheet(centerId: Int, collectionSheetPayload: CollectionSheetPayload?) =
        gateway.command(
            CommandSpec(
                payload = centerId,
                endpoint = {
                    centerApi.saveCollectionSheet(
                        centerId = it,
                        collectionSheetPayload = collectionSheetPayload,
                    )
                },
                localApply = { response ->
                    if (response != null) {
                        recordSheet(collectionSheetPayload?.let { CenterCollectionSheetKey.of(centerId.toLong(), it) })
                    }
                },
            ),
            policy = MutationPolicy.OnlineRequired,
        )

    override suspend fun saveCollectionSheetAsync(centerId: Int, collectionSheetPayload: CollectionSheetPayload?) =
        gateway.command(
            CommandSpec(
                payload = centerId,
                endpoint = {
                    centerApi.saveCollectionSheetAsync(
                        centerId = it,
                        collectionSheetPayload = collectionSheetPayload,
                    )
                },
                localApply = { response ->
                    if (response != null) {
                        recordSheet(collectionSheetPayload?.let { CenterCollectionSheetKey.of(centerId.toLong(), it) })
                    }
                },
            ),
            policy = MutationPolicy.OnlineRequired,
        )

    /**
     * Fineract answers with the new center's id only. Should the record read fail, the row is
     * written from what was sent — the center exists on the server and must not vanish locally.
     */
    override suspend fun createCenter(centerPayload: CenterPayload?) =
        gateway.command(
            CommandSpec(
                payload = centerPayload ?: CenterPayload(),
                endpoint = { centerApi.createCenter(centerPayload = centerPayload) },
                localApply = { response ->
                    response?.resourceId?.let { id ->
                        record(id) {
                            Center(
                                id = id,
                                name = centerPayload?.name,
                                officeId = centerPayload?.officeId,
                                active = centerPayload?.active,
                            )
                        }
                    }
                },
            ),
            policy = MutationPolicy.OnlineRequired,
        )

    override suspend fun activateCenter(centerId: Int, activatePayload: ActivatePayload?) =
        gateway.command(
            CommandSpec(
                payload = activatePayload ?: ActivatePayload(),
                endpoint = { centerApi.activateCenter(centerId = centerId, activatePayload = activatePayload) },
                localApply = { response -> if (response != null) record(centerId) },
            ),
            policy = MutationPolicy.OnlineRequired,
        )

    /**
     * Ingest the server's record of [centerId] into the `Center` table. If reading it fails, write
     * [fallback] instead, or leave the row for the next list refresh: the server has ALREADY
     * accepted the write, and a throw here would report it `Failed` and invite a duplicate.
     */
    private suspend fun record(centerId: Int, fallback: (() -> Center)? = null) {
        val center = orNull { centerStore.fresh(centerId).toCenter() } ?: fallback?.invoke() ?: return
        itemStore.write(StoreWriteRequest.of<Int, Center, Any>(key = centerId, value = center))
    }

    /** Re-read the saved sheet; if that fails, drop the pre-save one rather than keep serving it. */
    @OptIn(ExperimentalStoreApi::class)
    private suspend fun recordSheet(key: CenterCollectionSheetKey?) {
        if (key != null && orNull { sheetStore.fresh(key) } == null) sheetStore.clear(key)
    }
}

/** The detail read carries every column the `Center` row holds except the (unused) timeline. */
internal fun CenterWithAssociations.toCenter() = Center(
    id = id,
    accountNo = accountNo,
    name = name,
    officeId = officeId,
    officeName = officeName,
    staffId = staffId,
    staffName = staffName,
    hierarchy = hierarchy,
    status = status,
    active = active,
    activationDate = activationDate,
    externalId = externalId,
)

private inline fun <T> orNull(block: () -> T): T? = try {
    block()
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (_: Exception) {
    null
}

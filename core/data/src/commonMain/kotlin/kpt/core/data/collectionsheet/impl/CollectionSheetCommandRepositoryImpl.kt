/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.collectionsheet.impl

import kotlinx.coroutines.CancellationException
import kpt.core.base.data.annotation.FromStore
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.mutation.CommandSpec
import kpt.core.base.store.mutation.MutationGateway
import kpt.core.base.store.mutation.MutationPolicy
import kpt.core.data.collectionsheet.CollectionSheetCommandRepository
import kpt.core.model.collectionsheet.CollectionSheetPayload
import kpt.core.model.collectionsheet.CollectionSheetResponse
import kpt.core.model.collectionsheet.IndividualCollectionSheet
import kpt.core.model.collectionsheet.IndividualCollectionSheetPayload
import kpt.core.model.collectionsheet.ProductiveCollectionSheetPayload
import kpt.core.model.collectionsheet.RequestCollectionSheetPayload
import kpt.core.model.objects.collectionsheets.CollectionSheetRequestPayload
import kpt.core.model.utils.DateConstants
import kpt.core.network.mifos.collectionsheet.api.CollectionSheetApi
import kpt.core.store.collectionsheet.GeneratedSheetKey
import kpt.core.store.config.AppStoreIds
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.impl.extensions.fresh

/**
 * Collection-sheet saves, through the single write door ([MutationGateway.command]).
 *
 * A save posts repayments, disbursements and deposits that Fineract applies to live balances, so the
 * sheet generated for that meeting is no longer true once it lands — and the new sheet is the
 * server's to compute. [CommandSpec.localApply] therefore ingests it: it re-reads the saved sheet
 * through its store (network → read cache), the same row [kpt.core.data.collectionsheet.CollectionSheetRepository]
 * answers from offline. 6b66e8a43 ended at the endpoint, which here would leave the pre-save sheet
 * cached for an officer to collect against twice.
 *
 * All three stay `OnlineRequired`: financial writes reach Fineract or they are not real.
 */
@RepositoryBinding(binds = CollectionSheetCommandRepository::class)
internal class CollectionSheetCommandRepositoryImpl(
    private val collectionSheetApi: CollectionSheetApi,
    private val gateway: MutationGateway,
    @FromStore(AppStoreIds.CollectionsheetIndividual)
    private val individualStore: Store<RequestCollectionSheetPayload, IndividualCollectionSheet>,
    @FromStore(AppStoreIds.CollectionsheetProductive)
    private val productiveStore: Store<GeneratedSheetKey, CollectionSheetResponse>,
    @FromStore(AppStoreIds.CollectionsheetGroup)
    private val groupStore: Store<GeneratedSheetKey, CollectionSheetResponse>,
) : CollectionSheetCommandRepository {

    override suspend fun saveIndividualCollectionSheet(
        request: RequestCollectionSheetPayload,
        payload: IndividualCollectionSheetPayload?,
    ) = gateway.command(
        CommandSpec(
            payload = request.copy(),
            endpoint = { collectionSheetApi.saveIndividualCollectionSheet(payload = payload) },
            localApply = { response -> if (response != null) record(individualStore, request.copy()) },
        ),
        policy = MutationPolicy.OnlineRequired,
    )

    override suspend fun submitProductiveSheet(centerId: Int, payload: ProductiveCollectionSheetPayload?) =
        gateway.command(
            CommandSpec(
                payload = centerId,
                endpoint = { collectionSheetApi.submitProductiveSheet(centerId = it, payload = payload) },
                localApply = { response ->
                    if (response != null && payload != null) {
                        record(
                            productiveStore,
                            GeneratedSheetKey(
                                centerId,
                                sheetRequest(
                                    payload.calendarId,
                                    payload.dateFormat,
                                    payload.locale,
                                    payload.transactionDate,
                                ),
                            ),
                        )
                    }
                },
            ),
            policy = MutationPolicy.OnlineRequired,
        )

    override suspend fun submitCollectionSheet(groupId: Int, payload: CollectionSheetPayload?) =
        gateway.command(
            CommandSpec(
                payload = groupId,
                endpoint = { collectionSheetApi.submitCollectionSheet(groupId = it, payload = payload) },
                localApply = { response ->
                    if (response != null && payload != null) {
                        record(
                            groupStore,
                            GeneratedSheetKey(
                                groupId,
                                sheetRequest(
                                    payload.calendarId,
                                    payload.dateFormat,
                                    payload.locale,
                                    payload.transactionDate,
                                ),
                            ),
                        )
                    }
                },
            ),
            policy = MutationPolicy.OnlineRequired,
        )

    /**
     * Re-read the saved sheet into its row. If that read fails, DROP the row instead: the server has
     * already accepted the save, so throwing would report it `Failed` and invite a second
     * collection — and keeping the pre-save sheet would serve it offline as current.
     */
    @OptIn(ExperimentalStoreApi::class)
    private suspend fun <K : Any, V : Any> record(store: Store<K, V>, key: K) {
        val reread = try {
            store.fresh(key)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            null
        }
        if (reread == null) store.clear(key)
    }
}

/**
 * The generate request a save answers. A save body carries the same meeting fields the generate
 * request did — calendar, date, format, locale — so the two address the same sheet. A null format
 * or locale on the save body is the request's own default, never a different sheet.
 */
private fun sheetRequest(calendarId: Int?, dateFormat: String?, locale: String?, transactionDate: String?) =
    CollectionSheetRequestPayload(
        calendarId = calendarId,
        dateFormat = dateFormat ?: DateConstants.DATE_FORMAT,
        locale = locale ?: DateConstants.LOCALE,
        transactionDate = transactionDate,
    )

/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.collectionsheet

import kotlinx.coroutines.flow.map
import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.collectionsheet.dao.CollectionSheetDao
import kpt.core.database.collectionsheet.entity.CenterDetail
import kpt.core.database.collectionsheet.mapper.toDomain
import kpt.core.database.collectionsheet.mapper.toEntity
import kpt.core.network.mifos.collectionsheet.api.CollectionSheetApi
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store

/**
 * The full query is the key: a centre sheet is only meaningful for one (office, staff, meeting
 * date) triple, so date or staff changing must re-fetch rather than serve the previous sheet.
 */
data class CollectionSheetKey(
    val officeId: Int,
    val staffId: Int,
    val meetingDate: String,
    val dateFormat: String = "dd MMMM yyyy",
    val locale: String = "en",
)

@StoreProvider(id = "collectionSheetCenters", ttl = "5m")
@CacheKey(
    fn = "forQuery",
    key = "collectionSheetCenters:{officeId}:{staffId}:{meetingDate}",
    params = ["officeId:Int", "staffId:Int", "meetingDate:String"],
)
fun provideCollectionSheetStore(
    service: CollectionSheetApi,
    dao: CollectionSheetDao,
): Store<CollectionSheetKey, List<CenterDetail>> = StoreFactory.createStore(
    fetcher = Fetcher.of { key: CollectionSheetKey ->
        service.fetchCenterDetails(
            format = key.dateFormat,
            locale = key.locale,
            meetingDate = key.meetingDate,
            officeId = key.officeId,
            staffId = key.staffId,
        )
    },
    sourceOfTruth = SourceOfTruth.of(
        // Read back only the keyed staff's row: the table is keyed by staffId, so observeAll()
        // would leak other officers' cached sheets into this key's stream.
        reader = { key: CollectionSheetKey ->
            dao.observeForStaff(key.staffId).map { row -> listOfNotNull(row?.toDomain()) }
        },
        writer = { _: CollectionSheetKey, details: List<CenterDetail> ->
            dao.upsertAll(details.map { it.toEntity() })
        },
    ),
)

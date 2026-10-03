/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.document

import kotlinx.coroutines.flow.map
import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.document.dao.DocumentDao
import kpt.core.database.document.mapper.toDomain
import kpt.core.database.document.mapper.toEntity
import kpt.core.model.objects.noncoreobjects.Document
import kpt.core.network.mifos.document.api.DocumentApi
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store

/** Documents hang off any entity (client, loan, group, ...), so the owner pair is the key. */
data class DocumentKey(val entityType: String, val entityId: Int)

@StoreProvider(id = "documents", ttl = "15m")
@CacheKey(
    fn = "forEntity",
    key = "documents:{entityType}:{entityId}",
    params = ["entityType:String", "entityId:Int"],
)
fun provideDocumentStore(
    service: DocumentApi,
    dao: DocumentDao,
): Store<DocumentKey, List<Document>> = StoreFactory.createStore(
    fetcher = Fetcher.of { key: DocumentKey -> service.getDocuments(key.entityType, key.entityId) },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: DocumentKey ->
            dao.observeForEntity(key.entityType, key.entityId).map { rows -> rows.map { it.toDomain() } }
        },
        writer = { key: DocumentKey, docs: List<Document> ->
            dao.upsertAll(docs.map { it.toEntity(key.entityType, key.entityId) })
        },
    ),
)

/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.document

import io.ktor.client.statement.readRawBytes
import kotlinx.coroutines.flow.map
import kpt.core.base.store.annotation.CacheKey
import kpt.core.base.store.annotation.StoreProvider
import kpt.core.base.store.infra.StoreFactory
import kpt.core.database.document.dao.DocumentContentDao
import kpt.core.database.document.dao.DocumentDao
import kpt.core.database.document.entity.DocumentContentEntity
import kpt.core.database.document.mapper.toDomain
import kpt.core.database.document.mapper.toEntity
import kpt.core.model.objects.noncoreobjects.Document
import kpt.core.network.mifos.document.api.DocumentApi
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store

/**
 * Documents hang off any entity (client, loan, group, ...), so the owner pair is the key.
 *
 * @property entityId the id of the entity the record belongs to
 * @property entityType the entity kind, as Fineract names it in the path (`clients`, `loans`, `groups`)
 */
data class DocumentKey(val entityType: String, val entityId: Int)

/**
 * One document's FILE — the owner pair plus the document's id.
 *
 * @property documentId the document's server id
 * @property entityId the id of the entity the record belongs to
 * @property entityType the entity kind, as Fineract names it in the path (`clients`, `loans`, `groups`)
 */
data class DocumentContentKey(val entityType: String, val entityId: Int, val documentId: Int)

/**
 * One document ROW — the owner pair plus the document's id.
 *
 * @property documentId the document's server id
 * @property entityId the id of the entity the record belongs to
 * @property entityType the entity kind, as Fineract names it in the path (`clients`, `loans`, `groups`)
 */
data class DocumentItemKey(val entityType: String, val entityId: Int, val documentId: Int)

/** Store reading the document. */
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
        // An empty table reads as an empty LIST, never as null. A reader that maps "no rows" to null
        // stops the stream from ever showing "empty": measured 2026-10-23 (NullReaderStreamTest),
        // deleting the last row made Store5 emit Loading and refetch instead of an empty list. The
        // never-fetched-vs-empty question is answered where it matters — the offline lookup in
        // `DocumentRepositoryImpl.fetchDocuments` — not here.
        reader = { key: DocumentKey ->
            dao.observeForEntity(key.entityType, key.entityId).map { rows -> rows.map { it.toDomain() } }
        },
        // REPLACE, not upsert: a fetch is the server's whole list for the entity, so a row it no
        // longer contains was deleted and must leave the table. The upsert-only writer kept deleted
        // documents on screen indefinitely.
        writer = { key: DocumentKey, docs: List<Document> ->
            dao.replaceForEntity(key.entityType, key.entityId, docs.map { it.toEntity(key.entityType, key.entityId) })
        },
        delete = { key: DocumentKey -> dao.deleteForEntity(key.entityType, key.entityId) },
        // What the logout purge (`StoreCacheManager.clearAll` → `Store.clear()`) calls. Without it
        // the purge cleared the in-memory cache and left every row in the database.
        deleteAll = { dao.deleteAll() },
    ),
)

/**
 * A document's file bytes: `DocumentApi.downloadDocument` → `document_contents` → this store →
 * `DocumentRepository.downloadDocument`. Cached so a document viewed once can be reopened offline.
 *
 * No TTL refresh: a file never changes under the same id except through `updateDocument`, and
 * `DocumentCommandRepository` clears this key itself when that happens.
 */
@StoreProvider(id = "documentContent", ttl = "30d")
@CacheKey(
    fn = "forDocument",
    key = "documentContent:{entityType}:{entityId}:{documentId}",
    params = ["entityType:String", "entityId:Int", "documentId:Int"],
)
fun provideDocumentContentStore(
    service: DocumentApi,
    dao: DocumentContentDao,
): Store<DocumentContentKey, ByteArray> = StoreFactory.createStore(
    fetcher = Fetcher.of { key: DocumentContentKey ->
        service.downloadDocument(key.entityType, key.entityId, key.documentId).readRawBytes()
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: DocumentContentKey ->
            dao.observe(key.entityType, key.entityId, key.documentId).map { it?.bytes }
        },
        writer = { key: DocumentContentKey, bytes: ByteArray ->
            dao.upsert(DocumentContentEntity(key.entityType, key.entityId, key.documentId, bytes))
        },
        delete = { key: DocumentContentKey -> dao.delete(key.entityType, key.entityId, key.documentId) },
        // Client identity scans must not outlive the session that downloaded them.
        deleteAll = { dao.deleteAll() },
    ),
)

/*
 * The WRITE half. A document write is an `OnlineRequired` command (a file upload is not queued
 * offline), and once the server accepts it `DocumentCommandRepository` records the outcome locally
 * by writing THROUGH these stores — never a DAO call in `core/data` (S5-1). Each shares its table
 * with a read store above, so the read streams re-emit with no refresh: the write lands in the same
 * rows the reads observe.
 *
 * Local-only (`createOfflineMutableStore`): the network leg already ran in the command, so the store
 * must not post again. `logout = false` because Store5 keeps `MutableStore` outside the `Store`
 * hierarchy the purge registers; the rows are still purged by the paired read store's `deleteAll`
 * (same pairing as `queue/OfflineQueueStores.kt`).
 */
/** Store reading the document item. */
// logout-exempt: MutableStore is outside the purge; the paired read store's deleteAll clears its rows
@StoreProvider(id = "documentItem", logout = false)
@CacheKey(
    fn = "item",
    key = "documentItem:{entityType}:{entityId}:{documentId}",
    params = ["entityType:String", "entityId:Int", "documentId:Int"],
)
fun provideDocumentItemStore(
    dao: DocumentDao,
): MutableStore<DocumentItemKey, Document> = StoreFactory.createOfflineMutableStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: DocumentItemKey -> dao.observeById(key.documentId).map { it?.toDomain() } },
        writer = { key: DocumentItemKey, doc: Document ->
            dao.upsertAll(listOf(doc.toEntity(key.entityType, key.entityId)))
        },
        delete = { key: DocumentItemKey -> dao.deleteById(key.documentId) },
    ),
)

/** Store reading the document content. */
// logout-exempt: MutableStore is outside the purge; the paired read store's deleteAll clears its rows
@StoreProvider(id = "documentContentLocal", logout = false)
@CacheKey(
    fn = "item",
    key = "documentContentLocal:{entityType}:{entityId}:{documentId}",
    params = ["entityType:String", "entityId:Int", "documentId:Int"],
)
fun provideDocumentContentLocalStore(
    dao: DocumentContentDao,
): MutableStore<DocumentContentKey, ByteArray> = StoreFactory.createOfflineMutableStore(
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: DocumentContentKey ->
            dao.observe(key.entityType, key.entityId, key.documentId).map { it?.bytes }
        },
        writer = { key: DocumentContentKey, bytes: ByteArray ->
            dao.upsert(DocumentContentEntity(key.entityType, key.entityId, key.documentId, bytes))
        },
        delete = { key: DocumentContentKey -> dao.delete(key.entityType, key.entityId, key.documentId) },
    ),
)

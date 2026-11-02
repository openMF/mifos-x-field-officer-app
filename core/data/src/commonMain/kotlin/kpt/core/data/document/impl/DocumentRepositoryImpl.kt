/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.document.impl

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kpt.core.base.data.annotation.FromStore
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.screen.FetchPolicy
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.asScreenStream
import kpt.core.data.document.DocumentRepository
import kpt.core.model.objects.noncoreobjects.Document
import kpt.core.store.config.AppCacheKeys
import kpt.core.store.config.AppStoreIds
import kpt.core.store.config.AppStoreRegistry
import kpt.core.store.document.DocumentContentKey
import kpt.core.store.document.DocumentKey
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.impl.extensions.fresh
import org.mobilenativefoundation.store.store5.impl.extensions.get

/**
 * Every read here goes through a store — network → `core/database` → `core/store` → here. This
 * class used to hold `DocumentApi` and call it directly for the download, so the file was never
 * persisted and the layering the rest of the app follows was skipped for documents.
 */
@RepositoryBinding(binds = DocumentRepository::class)
internal class DocumentRepositoryImpl(
    @FromStore(AppStoreIds.Documents) private val store: Store<DocumentKey, List<Document>>,
    @FromStore(AppStoreIds.DocumentContent) private val contentStore: Store<DocumentContentKey, ByteArray>,
) : DocumentRepository {

    override fun documentsStream(
        entityType: String,
        entityId: Int,
        scope: CoroutineScope,
    ): ScreenDataStream<List<Document>> =
        store.asScreenStream(
            key = DocumentKey(entityType, entityId),
            cacheKey = AppCacheKeys.Documents.forEntity(entityType, entityId),
            scope = scope,
            // No `isEmpty`: an empty list is real content (see ChargeRepositoryImpl.chargesStream).
            fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
            ttl = AppStoreRegistry.Ttl.DOCUMENTS,
        )

    /**
     * Cache-first: a file already in `document_contents` opens offline; otherwise it is fetched and
     * persisted. `null` on any failure — a client with no signature yet is the common case.
     */
    override suspend fun downloadDocument(entityType: String, entityId: Int, documentId: Int): ByteArray? =
        orNull { contentStore.get(DocumentContentKey(entityType, entityId, documentId)) }

    /**
     * `fresh` writes the server's list through the source of truth before returning it. Offline it
     * falls back to that source of truth (`get` answers from the database when the key is cached):
     * a scan viewed before must still open in the field, and failing the lookup there stranded a
     * file that was sitting in `document_contents` (found on device 2026-10-23, airplane mode).
     */
    override suspend fun fetchDocuments(entityType: String, entityId: Int): List<Document>? {
        val key = DocumentKey(entityType, entityId)
        // Offline, an EMPTY cached list is "unknown", not "none": the table cannot tell a
        // never-fetched entity from an empty one, and answering "no document attached" for a scan
        // that was simply never loaded is the wrong message. Only a non-empty cache is trusted.
        return orNull { store.fresh(key) } ?: orNull { store.get(key) }?.takeIf { it.isNotEmpty() }
    }

    private inline fun <T> orNull(block: () -> T): T? = try {
        block()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (_: Exception) {
        null
    }
}

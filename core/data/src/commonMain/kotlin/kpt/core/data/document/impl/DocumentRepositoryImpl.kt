/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.document.impl

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
import kpt.core.store.document.DocumentKey
import org.mobilenativefoundation.store.store5.Store

@RepositoryBinding(binds = DocumentRepository::class)
internal class DocumentRepositoryImpl(
    @FromStore(AppStoreIds.Documents) private val store: Store<DocumentKey, List<Document>>,
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
            isEmpty = { it.isEmpty() },
            fetchPolicy = FetchPolicy.NETWORK_WITH_CACHE,
            ttl = AppStoreRegistry.Ttl.DOCUMENTS,
        )
}

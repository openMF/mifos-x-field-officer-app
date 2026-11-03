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

import kpt.core.base.data.annotation.FromStore
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.mutation.CommandSpec
import kpt.core.base.store.mutation.MutationGateway
import kpt.core.base.store.mutation.MutationPolicy
import kpt.core.data.document.DocumentCommandRepository
import kpt.core.model.objects.noncoreobjects.Document
import kpt.core.model.objects.noncoreobjects.DocumentUpload
import kpt.core.network.mifos.document.api.DocumentApi
import kpt.core.network.mifos.document.toMultipart
import kpt.core.store.config.AppStoreIds
import kpt.core.store.document.DocumentContentKey
import kpt.core.store.document.DocumentItemKey
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.StoreWriteRequest

/**
 * Document writes, through the single write door ([MutationGateway.command]).
 *
 * Network → store → database: [CommandSpec.endpoint] is the Fineract call (`core/network`), and
 * [CommandSpec.localApply] records the server's acceptance by writing THROUGH the document stores
 * (`core/store`, which persist to `core/database`). The read stores share those tables, so the
 * documents list and the preview see the change the moment this returns.
 *
 * It used to end at the endpoint with `payload = Unit` and no local side: the `documents` table kept
 * its pre-write rows, so a scan uploaded seconds earlier read as missing (device finding,
 * 2026-10-23).
 *
 * `OnlineRequired` — a file upload is not queued offline — so `localApply` runs only with the
 * server's result (the gateway skips the optimistic pre-apply for this policy).
 */
@RepositoryBinding(binds = DocumentCommandRepository::class)
internal class DocumentCommandRepositoryImpl(
    private val documentApi: DocumentApi,
    private val gateway: MutationGateway,
    @FromStore(AppStoreIds.DocumentItem) private val itemStore: MutableStore<DocumentItemKey, Document>,
    @FromStore(AppStoreIds.DocumentContentLocal) private val contentStore: MutableStore<DocumentContentKey, ByteArray>,
) : DocumentCommandRepository {

    override suspend fun createDocument(entityType: String, entityId: Int, upload: DocumentUpload) =
        gateway.command(
            CommandSpec(
                payload = upload,
                endpoint = { documentApi.createDocument(entityType, entityId, it.toMultipart()) },
                // Fineract answers with the new document's id only; the row is the upload we sent
                // under that id. Storing the bytes too means it opens offline straight away.
                localApply = { response ->
                    response?.resourceId?.let { id -> record(entityType, entityId, id, upload) }
                },
            ),
            policy = MutationPolicy.OnlineRequired,
        )

    override suspend fun updateDocument(entityType: String, entityId: Int, documentId: Int, upload: DocumentUpload) =
        gateway.command(
            CommandSpec(
                payload = upload,
                endpoint = { documentApi.updateDocument(entityType, entityId, documentId, it.toMultipart()) },
                localApply = { response -> if (response != null) record(entityType, entityId, documentId, upload) },
            ),
            policy = MutationPolicy.OnlineRequired,
        )

    @OptIn(ExperimentalStoreApi::class)
    override suspend fun removeDocument(entityType: String, entityId: Int, documentId: Int) =
        gateway.command(
            CommandSpec(
                payload = documentId,
                endpoint = { documentApi.removeDocument(entityType, entityId, it) },
                localApply = { response ->
                    if (response != null) {
                        itemStore.clear(DocumentItemKey(entityType, entityId, documentId))
                        contentStore.clear(DocumentContentKey(entityType, entityId, documentId))
                    }
                },
            ),
            policy = MutationPolicy.OnlineRequired,
        )

    /** Write the accepted document — row and file — through the stores. */
    private suspend fun record(entityType: String, entityId: Int, documentId: Int, upload: DocumentUpload) {
        itemStore.write(
            StoreWriteRequest.of<DocumentItemKey, Document, Any>(
                key = DocumentItemKey(entityType, entityId, documentId),
                value = Document(
                    id = documentId,
                    parentEntityType = entityType,
                    parentEntityId = entityId,
                    name = upload.name,
                    fileName = upload.fileName,
                    size = upload.bytes.size.toLong(),
                    type = upload.mimeType,
                    description = upload.description,
                ),
            ),
        )
        contentStore.write(
            StoreWriteRequest.of<DocumentContentKey, ByteArray, Any>(
                key = DocumentContentKey(entityType, entityId, documentId),
                value = upload.bytes,
            ),
        )
    }
}

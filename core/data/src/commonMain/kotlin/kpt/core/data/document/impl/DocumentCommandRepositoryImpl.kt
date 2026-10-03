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

import io.ktor.client.request.forms.MultiPartFormDataContent
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.mutation.CommandSpec
import kpt.core.base.store.mutation.MutationGateway
import kpt.core.base.store.mutation.MutationPolicy
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.document.DocumentCommandRepository
import kpt.core.network.mifos.document.api.DocumentApi

@RepositoryBinding(binds = DocumentCommandRepository::class)
internal class DocumentCommandRepositoryImpl(
    private val documentApi: DocumentApi,
    private val gateway: MutationGateway,
) : DocumentCommandRepository {

    override suspend fun createDocument(entityType: String, entityId: Int, request: MultiPartFormDataContent) =
        online { documentApi.createDocument(entityType = entityType, entityId = entityId, request = request) }

    override suspend fun removeDocument(entityType: String, entityId: Int, documentId: Int) =
        online { documentApi.removeDocument(entityType = entityType, entityId = entityId, documentId = documentId) }

    override suspend fun updateDocument(entityType: String, entityId: Int, documentId: Int, request: MultiPartFormDataContent) =
        online { documentApi.updateDocument(entityType = entityType, entityId = entityId, documentId = documentId, request = request) }

    /**
     * Commands run [MutationPolicy.OnlineRequired]: they await the server and write nothing
     * offline, yielding `Blocked(OFFLINE)` rather than a local success the officer would act on.
     *
     * The call is closed over rather than threaded through [CommandSpec.payload]: the payload hook
     * exists for `localApply` / `rollback` / `conflictOf`, none of which apply to a write that never
     * lands locally. It also keeps the 28 endpoints whose body is nullable usable, which
     * `CommandSpec<P : Any>` would otherwise reject.
     */
    private suspend fun <R : Any> online(endpoint: suspend () -> R): MutationResult<R> =
        gateway.command(
            CommandSpec(payload = Unit, endpoint = { endpoint() }),
            policy = MutationPolicy.OnlineRequired,
        )
}

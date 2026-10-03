/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.note.impl

import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.mutation.CommandSpec
import kpt.core.base.store.mutation.MutationGateway
import kpt.core.base.store.mutation.MutationPolicy
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.note.NoteCommandRepository
import kpt.core.network.mifos.note.api.NoteApi
import kpt.core.network.mifos.note.dto.NoteRequestDto

@RepositoryBinding(binds = NoteCommandRepository::class)
internal class NoteCommandRepositoryImpl(
    private val noteApi: NoteApi,
    private val gateway: MutationGateway,
) : NoteCommandRepository {

    override suspend fun addNewNote(resourceType: String, resourceId: Long, noteRequestDto: NoteRequestDto) =
        online { noteApi.addNewNote(resourceType = resourceType, resourceId = resourceId, noteRequestDto = noteRequestDto) }

    override suspend fun deleteNote(resourceType: String, resourceId: Long, noteId: Long) =
        online { noteApi.deleteNote(resourceType = resourceType, resourceId = resourceId, noteId = noteId) }

    override suspend fun updateNote(resourceType: String, resourceId: Long, noteId: Long, noteRequestDto: NoteRequestDto) =
        online { noteApi.updateNote(resourceType = resourceType, resourceId = resourceId, noteId = noteId, noteRequestDto = noteRequestDto) }

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

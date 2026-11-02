/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.note.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Header
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Path
import kpt.core.base.network.annotation.ApiBinding
import kpt.core.model.objects.note.CreateNoteResponseDto
import kpt.core.model.objects.note.DeleteNoteResponseDto
import kpt.core.model.objects.note.NoteRequestDto
import kpt.core.model.objects.note.UpdateNoteResponseDto
import kpt.core.network.mifos.note.dto.NoteDto

@ApiBinding("mifos")
interface NoteApi {

    /**
     * Add a Resource Note
     * Adds a new note to a supported resource.  Example Requests:  clients/1/notes   groups/1/notes
     * Responses:
     *  - 200: OK
     *
     * @param resourceType resourceType, eg : Client, Loan, Group, Savings Account
     * @param resourceId resourceId, eg : ClientId, LoanId, GroupId, Savings AccountId
     * @param noteRequestDto
     * @return [CreateNoteResponseDto]
     */
    @POST("{resourceType}/{resourceId}/notes")
    suspend fun addNewNote(
        @Path("resourceType") resourceType: String,
        @Path("resourceId") resourceId: Long,
        @Body noteRequestDto: NoteRequestDto,
        /**
         * De-duplication token for retried submissions (Fineract command idempotency).
         *
         * Creates are the only note operation that can duplicate: an update and a delete name the
         * note they act on, so replaying either is naturally idempotent. A create does not, so a
         * replay of a POST the server already accepted — but whose response the client never saw —
         * writes a second note. Observed on device 2026-10-20.
         *
         * **NOT honoured by `apis.mifos.community` — measured 2026-10-21.** Two creates were sent
         * with the key deliberately pinned to one constant; BOTH notes were created. (A first
         * reading of that experiment said the opposite, because the notes list it was read from
         * was stale — the same missing re-read that made delete look broken. The list was
         * re-opened from a cold start and both rows were there.)
         *
         * The header is therefore sent but currently buys NOTHING on this deployment: it is
         * correct, harmless, and inert. It is kept because it costs nothing, is the right shape,
         * and starts working the moment it reaches a Fineract with command-idempotency enabled.
         *
         * Until then the replay-after-unseen-success duplicate is prevented CLIENT-side — see
         * `NoteCommandRepository.submitDraft`. Re-run the pinned-key experiment against any new
         * deployment before relying on the server half.
         */
        @Header("Idempotency-Key") idempotencyKey: String,
    ): CreateNoteResponseDto

    /**
     * Delete a Resource Note
     * Deletes a Resource Note
     * Responses:
     *  - 200: OK
     *
     * @param resourceType resourceType, eg : Client, Loan, Group, Savings Account
     * @param resourceId resourceId, eg : ClientId, LoanId, GroupId, Savings AccountId
     * @param noteId noteId
     * @return [DeleteNoteResponseDto]
     */
    @DELETE("{resourceType}/{resourceId}/notes/{noteId}")
    suspend fun deleteNote(
        @Path("resourceType") resourceType: String,
        @Path("resourceId") resourceId: Long,
        @Path("noteId") noteId: Long,
    ): DeleteNoteResponseDto

    /**
     * Retrieve a single Note
     * Responses:
     *  - 200: OK
     *
     * @param resourceType resourceType, eg : Client, Loan, Group, Savings Account
     * @param resourceId resourceId, eg : ClientId, LoanId, GroupId, Savings AccountId
     * @param noteId noteId
     * @return [NoteDto]
     */
    @GET("{resourceType}/{resourceId}/notes/{noteId}")
    suspend fun retrieveNote(
        @Path("resourceType") resourceType: String,
        @Path("resourceId") resourceId: Long,
        @Path("noteId") noteId: Long,
    ): NoteDto

    /**
     * Retrieve List of notes
     * Note: Notes are returned in descending createOn order.
     * Responses:
     *  - 200: OK
     *
     * @param resourceType resourceType, eg : Client, Loan, Group, Savings Account
     * @param resourceId resourceId, eg : ClientId, LoanId, GroupId, Savings AccountId
     * @return [List<NoteDto>]
     */
    @GET("{resourceType}/{resourceId}/notes")
    suspend fun retrieveListNotes(
        @Path("resourceType") resourceType: String,
        @Path("resourceId") resourceId: Long,
    ): List<NoteDto>

    /**
     * Update a Note
     * Responses:
     *  - 200: OK
     *
     * @param resourceType resourceType, eg : Client, Loan, Group, Savings Account
     * @param resourceId resourceId, eg : ClientId, LoanId, GroupId, Savings AccountId
     * @param noteId noteId
     * @param noteRequestDto
     * @return UpdateNoteResponseDto
     */
    @PUT("{resourceType}/{resourceId}/notes/{noteId}")
    suspend fun updateNote(
        @Path("resourceType") resourceType: String,
        @Path("resourceId") resourceId: Long,
        @Path("noteId") noteId: Long,
        @Body noteRequestDto: NoteRequestDto,
    ): UpdateNoteResponseDto
}

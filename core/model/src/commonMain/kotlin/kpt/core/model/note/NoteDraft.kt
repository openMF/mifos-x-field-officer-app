/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.note

import kotlinx.serialization.Serializable

/**
 * A note the officer has written, as a DURABLE payload.
 *
 * ## Why this type exists
 *
 * `NoteCommandRepository` takes four loose parameters (`resourceType`, `resourceId`, `noteId`,
 * `NoteRequestDto`). An outbox persists ONE serializable payload per draft, so the parameters have
 * to travel together — and they have to survive process death, which is the whole point: a field
 * officer types a note in a village with no signal, closes the app, and the note is still there and
 * still sends when a signal returns.
 *
 * Before this, an offline note showed "you are offline", kept the text on screen, and lost it the
 * moment the user navigated away. `OnlineRequired` was always the right policy — it stops the app
 * claiming a note was saved when it was not — but it never implied discarding the typing.
 *
 * [noteId] distinguishes the two operations a draft can replay: null → create, non-null → update.
 * Both are durable; an edit made offline is as worth keeping as a new note.
 */
@Serializable
data class NoteDraft(
    /** The entity the note hangs off — a client id today; centres and groups when those land. */
    val resourceId: Long,
    /** The note's text, exactly as typed. */
    val text: String,
    /** Null for a new note; the id of the note being edited otherwise. */
    val noteId: Long? = null,
    /**
     * Fineract's entity-type path segment (`clients`, `centers`, `groups`).
     *
     * Stored ON the draft rather than resolved at replay time: a draft written today must still
     * send correctly after an app update that changes which screen is open, and re-deriving the
     * type from UI context at replay would be guessing.
     */
    val resourceType: String,
    /**
     * Client-minted, stable for the life of this draft — the de-duplication token.
     *
     * ## Why it lives ON the payload
     *
     * The template's `EditBillReminderViewModel` makes a replay idempotent by minting the row's
     * own id client-side (`billId ?: randomId()`), so replaying upserts the SAME row. Fineract
     * assigns note ids server-side, so that exact trick is unavailable — the equivalent is an
     * idempotency token the server can recognise.
     *
     * The framework ships `IdempotencyKey.generate()` but its own doc is explicit that the key is
     * "pure in-memory … not persisted to SubmitOutbox / Room", and that forks needing durability
     * across process restarts "must layer their own storage". This IS that storage: the draft's
     * `payloadJson` is already durable, so the key survives process death with the note it
     * belongs to and a replay days later presents the same token as the original attempt.
     *
     * Minted once, at the first submit — NEVER regenerated on retry, which would defeat it.
     *
     * Defaulted because this type is a PERSISTED format: a draft written by a build before this
     * field existed deserializes from JSON that lacks it, and a required field would throw
     * `MissingFieldException` on read — turning every pre-upgrade draft into an unreadable row
     * and losing exactly the notes the outbox exists to protect. An empty key means "no
     * de-duplication available for this legacy draft", which is the pre-upgrade behaviour and
     * strictly better than dropping it.
     */
    val idempotencyKey: String = "",
)

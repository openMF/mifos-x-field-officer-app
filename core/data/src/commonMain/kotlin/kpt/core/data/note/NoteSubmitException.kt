/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.note

/**
 * Why a note submission failed, as a THROWN signal.
 *
 * `SubmitHandler` and `OfflineSubmitSyncer` are built on throw-to-fail: a block that returns is a
 * success and its draft is marked SUBMITTED. This fork's repositories return `MutationResult`
 * instead, so the two conventions meet in `NoteCommandRepositoryImpl.submitDraft` — and the
 * distinction `MutationResult` carries must survive the crossing, or every failure looks alike.
 *
 * The distinction that matters is [Offline] vs [Rejected]:
 *
 *  - **Offline** is NOT a failure of the note. The draft stays PENDING and the syncer retries it on
 *    the next reconnect. The officer is told it is saved and queued.
 *  - **Rejected** means the server refused it — a validation error, a deleted client, an expired
 *    session. Retrying on reconnect would fail identically forever, so the draft goes FAILED and
 *    surfaces in Settings → Sync & Drafts for the human to retry or discard.
 *
 * Collapsing them would either spin a doomed draft forever or discard a note that only needed a
 * signal.
 */
sealed class NoteSubmitException(message: String, cause: Throwable? = null) :
    Exception(message, cause) {

    /** No usable connection. The draft stays PENDING and replays on reconnect. */
    class Offline(reason: String) : NoteSubmitException("note submit blocked: $reason")

    /** The server refused it. Retrying unchanged will not help; a human decides. */
    class Rejected(cause: Throwable?) : NoteSubmitException("note submit rejected", cause)
}

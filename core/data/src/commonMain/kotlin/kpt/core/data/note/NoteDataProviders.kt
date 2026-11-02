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

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kpt.core.base.data.annotation.DataProvider
import kpt.core.base.data.annotation.FromQualifier
import kpt.core.base.data.infra.NetworkMonitor
import kpt.core.base.database.infra.dao.DraftDao
import kpt.core.base.store.infra.impl.RoomSubmitOutbox
import kpt.core.base.store.submit.OfflineSubmitSyncer
import kpt.core.base.store.submit.SubmitOutbox
import kpt.core.datastore.prefs.UserPreferencesRepository
import kpt.core.model.note.NoteDraft

/**
 * Durable outbox for notes — `RoomSubmitOutbox` writes to `framework_submit_drafts`.
 *
 * The qualifier is not optional. Koin indexes `single<T>` by the RAW type, so
 * `SubmitOutbox<NoteDraft>` and any future `SubmitOutbox<X>` collide under `SubmitOutbox::class`
 * and the last registration wins — the wrong serializer then fires at the first
 * `saveByUniqueKey`, as a `ClassCastException` nowhere near the cause. See `OutboxQualifiers`.
 */
@DataProvider(qualifier = NOTE_OUTBOX_QUALIFIER)
fun provideNoteOutbox(dao: DraftDao): SubmitOutbox<NoteDraft> =
    RoomSubmitOutbox(dao = dao, serializer = NoteDraft.serializer(), ownsFormKey = { it == NOTE_FORM_KEY })

/**
 * Eager: watches connectivity from Koin start and replays pending notes once the app is BOTH
 * online and authenticated.
 *
 * `createdAtStart` because the point is to drain drafts the user cannot see. A note written
 * offline on Monday must send on Tuesday morning without anyone opening the notes screen — if this
 * were lazy, nothing would construct it until that screen happened to be visited, which is exactly
 * when it is least needed.
 *
 * ## Why the status flow is gated on AUTH, not just connectivity
 *
 * Measured on device 2026-10-20. With plain `networkMonitor.networkStatus`, the syncer fired at
 * app start — before `AuthHeaderBridge` had seeded the `Authorization` header, which it does
 * asynchronously. The replay went out unauthenticated, Fineract refused it, and
 * `OfflineSubmitSyncer.retryAll` marks FAILED on ANY exception. So a perfectly good note was
 * burned to a terminal FAILED on the first launch after being written, every time.
 *
 * The draft was observed going `PENDING → RETRYING(attempt=1) → FAILED "note submit rejected"`
 * with the device fully online. Raising the retry cap would not have helped: each launch races
 * the same way, so the attempts are spent on a condition that is not the server's fault.
 *
 * Gating here fixes it at the source rather than teaching the failure path to tolerate it: the
 * syncer simply does not see `Available` until a token exists, and `distinctUntilChanged` then
 * fires the sweep on the online+authenticated transition.
 *
 * `submitBlock` THROWS on failure (see `NoteCommandRepository.submitDraft`); the syncer marks a
 * draft SUBMITTED when the block returns, so a silent failure here would delete the note.
 */
@DataProvider(createdAtStart = true)
fun provideNoteSubmitSyncer(
    scope: CoroutineScope,
    @FromQualifier(NOTE_OUTBOX_QUALIFIER) outbox: SubmitOutbox<NoteDraft>,
    networkMonitor: NetworkMonitor,
    userPreferences: UserPreferencesRepository,
    commands: NoteCommandRepository,
): OfflineSubmitSyncer<NoteDraft, Unit> = OfflineSubmitSyncer<NoteDraft, Unit>(
    scope = scope,
    outbox = outbox,
    // Online AND signed in. `NetworkStatus.Unavailable` is used as the "not yet" value so the
    // existing `RetryOnNetworkStatus.OnlineOnly` predicate keeps working unchanged — the syncer
    // needs no new concept, it just never sees Available too early.
    networkStatusFlow = combine(
        networkMonitor.networkStatus,
        userPreferences.userData,
    ) { status, user -> gateOnAuth(status, user.isAuthenticated) },
    submitBlock = { draft -> commands.submitDraft(draft) },
).also { it.start() }

/**
 * The replay gate: a network status the syncer may act on, or `Unavailable` until signed in.
 *
 * A named function rather than a lambda in the `combine` so it can be tested without constructing
 * a syncer, a Koin graph or a fake outbox — the condition it encodes is the whole fix, and it was
 * found on a device rather than reasoned out.
 */
internal fun gateOnAuth(status: NetworkStatus, isAuthenticated: Boolean): NetworkStatus =
    if (isAuthenticated) status else NetworkStatus.Unavailable

/**
 * Koin qualifier for the note outbox.
 *
 * A `const` rather than a `named()` in `OutboxQualifiers` because `@DataProvider(qualifier = …)`
 * needs a compile-time constant; the generated binding and every `@FromQualifier` consumer read
 * this same symbol, so the string exists once.
 */
const val NOTE_OUTBOX_QUALIFIER: String = "outbox.note"

/** The outbox form-key grouping every note draft. One form type, many concurrent drafts. */
const val NOTE_FORM_KEY: String = "note"

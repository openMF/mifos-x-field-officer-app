/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.draft

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kpt.core.base.data.annotation.DataProvider
import kpt.core.base.data.annotation.FromQualifier
import kpt.core.base.data.infra.NetworkMonitor
import kpt.core.base.database.infra.dao.DraftDao
import kpt.core.base.store.infra.impl.RoomSubmitOutbox
import kpt.core.base.store.submit.OfflineSubmitSyncer
import kpt.core.base.store.submit.SubmitOutbox
import kpt.core.base.store.submit.draft.FormDraft
import kpt.core.base.store.submit.draft.FormDraftRepository
import kpt.core.base.store.submit.draft.RoomFormDraftRepository
import kpt.core.data.center.CenterCommandRepository
import kpt.core.data.client.ClientCommandRepository
import kpt.core.data.group.GroupCommandRepository
import kpt.core.data.loan.LoanCommandRepository
import kpt.core.data.note.gateOnAuth
import kpt.core.data.recurringdeposit.RecurringDepositCommandRepository
import kpt.core.data.savings.SavingsCommandRepository
import kpt.core.datastore.prefs.UserPreferencesRepository
import kpt.core.model.draft.CreateForm

/** Qualified for the note outbox's reason: `SubmitOutbox<X>` bindings collide on the raw type. */
const val CREATE_OUTBOX_QUALIFIER: String = "outbox.create"

/**
 * The syncer needs one too, for the same reason: Koin keys `single<OfflineSubmitSyncer<A, B>>` by
 * the RAW class, so an unqualified create syncer and the note syncer are one binding — the note one,
 * registered later, silently overrode this one, which was then never built (device, 2026-10-26: a
 * pending group draft survived every reconnect and relaunch unsent).
 */
const val CREATE_SYNCER_QUALIFIER: String = "syncer.create"

@DataProvider(qualifier = CREATE_OUTBOX_QUALIFIER)
fun provideCreateDraftOutbox(dao: DraftDao): SubmitOutbox<FormDraft> =
    RoomSubmitOutbox(dao = dao, serializer = FormDraft.serializer(), ownsFormKey = { it.startsWith(CreateForm.PREFIX) })

/** The create forms' drafts — what each create form's `FormDraftSlot` and Sync & Drafts read. */
@DataProvider
fun provideCreateDraftRepository(dao: DraftDao): FormDraftRepository =
    RoomFormDraftRepository(dao, CreateForm.entries.map { it.key })

@DataProvider
fun provideCreateDraftSubmitter(
    clients: ClientCommandRepository,
    groups: GroupCommandRepository,
    centers: CenterCommandRepository,
    loans: LoanCommandRepository,
    savings: SavingsCommandRepository,
    recurring: RecurringDepositCommandRepository,
): CreateDraftSubmitter = CreateDraftSubmitter(clients, groups, centers, loans, savings, recurring)

/**
 * Eager, and gated on auth, for the note syncer's reasons (see `provideNoteSubmitSyncer`): a create
 * kept offline must send when the officer is next online and signed in, with no screen open — and
 * never before the auth header exists, or the first launch burns every draft to FAILED.
 */
@DataProvider(createdAtStart = true, qualifier = CREATE_SYNCER_QUALIFIER)
fun provideCreateDraftSyncer(
    scope: CoroutineScope,
    @FromQualifier(CREATE_OUTBOX_QUALIFIER) outbox: SubmitOutbox<FormDraft>,
    networkMonitor: NetworkMonitor,
    userPreferences: UserPreferencesRepository,
    submitter: CreateDraftSubmitter,
): OfflineSubmitSyncer<FormDraft, Unit> = OfflineSubmitSyncer<FormDraft, Unit>(
    scope = scope,
    outbox = outbox,
    networkStatusFlow = combine(networkMonitor.networkStatus, userPreferences.userData) { status, user ->
        gateOnAuth(status, user.isAuthenticated)
    },
    submitBlock = { draft -> submitter.submit(draft) },
).also { it.start() }

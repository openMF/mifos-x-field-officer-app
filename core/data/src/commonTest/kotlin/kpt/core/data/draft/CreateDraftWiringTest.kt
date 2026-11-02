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

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkInfo
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kpt.core.base.store.infra.impl.RoomSubmitOutbox
import kpt.core.base.store.submit.OfflineSubmitSyncer
import kpt.core.base.store.submit.draft.FormDraft
import kpt.core.base.store.submit.draft.FormDraftJson
import kpt.core.base.store.submit.draft.RoomFormDraftRepository
import kpt.core.base.store.submit.draft.formDraftSlot
import kpt.core.data.infra.FakeDraftDao
import kpt.core.model.draft.CreateForm
import kpt.core.model.group.GroupPayload
import kpt.core.model.note.NoteDraft
import kotlin.test.Test
import kotlin.test.assertEquals

/** The fork's wiring of the template drafts: create rows and note rows share one table. */
class CreateDraftWiringTest {

    private val dao = FakeDraftDao()
    private val drafts = RoomFormDraftRepository(dao, CreateForm.entries.map { it.key })

    private fun groupDraft(name: String) = FormDraft(
        form = CreateForm.GROUP.key,
        title = name,
        payload = FormDraftJson.encode(GroupPayload.serializer(), GroupPayload(name = name)),
        state = "{}",
    )

    @Test
    fun theCreateOutboxSeesOnlyCreateRowsNotNotes() = kotlinx.coroutines.test.runTest {
        RoomSubmitOutbox(dao, NoteDraft.serializer()).saveByUniqueKey(
            "note",
            "client:7:new",
            NoteDraft(resourceId = 7, text = "x", resourceType = "clients", idempotencyKey = "k"),
        )
        drafts.save(formDraftSlot(null), groupDraft("FOTest Group"))

        assertEquals(listOf(CreateForm.GROUP.key), provideCreateDraftOutbox(dao).getAllPending().map { it.formKey })
    }

    @Test
    fun aKeptDraftIsReplayedWhenTheConnectionReturns() = runTest(UnconfinedTestDispatcher()) {
        drafts.save(formDraftSlot(null), groupDraft("FOTest Offline Group"))
        val status = MutableStateFlow<NetworkStatus>(NetworkStatus.Unavailable)
        val sent = mutableListOf<FormDraft>()
        OfflineSubmitSyncer<FormDraft, Unit>(
            scope = backgroundScope,
            outbox = provideCreateDraftOutbox(dao),
            networkStatusFlow = status,
            submitBlock = { sent += it },
        ).start()

        status.value = NetworkStatus.Available(NetworkInfo())

        assertEquals(listOf("FOTest Offline Group"), sent.map { it.title })
    }
}

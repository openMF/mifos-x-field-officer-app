/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.testing

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kpt.core.base.store.submit.SubmitOutboxStatus
import kpt.core.base.store.submit.draft.FormDraft
import kpt.core.base.store.submit.draft.FormDraftRepository
import kpt.core.base.store.submit.draft.SavedFormDraft

/** In-memory form drafts: [saved] is what a test asserts on. */
@ExperimentalDataTestingApi
class FakeFormDraftRepository : FormDraftRepository {

    val saved = MutableStateFlow<List<SavedFormDraft>>(emptyList())
    private var nextId = 1L

    override fun observe(form: String, slot: String): Flow<SavedFormDraft?> =
        saved.map { rows -> rows.firstOrNull { it.draft.form == form && it.slot == slot } }

    override fun observeAll(): Flow<List<SavedFormDraft>> = saved

    override suspend fun save(slot: String, draft: FormDraft, failure: String?) {
        saved.value = saved.value.filterNot { it.draft.form == draft.form && it.slot == slot } + SavedFormDraft(
            id = nextId++,
            slot = slot,
            draft = draft,
            status = if (failure == null) SubmitOutboxStatus.PENDING else SubmitOutboxStatus.FAILED,
            updatedAtMs = nextId,
            errorMessage = failure,
        )
    }

    override suspend fun delete(form: String, slot: String) {
        saved.value = saved.value.filterNot { it.draft.form == form && it.slot == slot }
    }
}

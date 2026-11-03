/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.testing

import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.checkerinbox.CheckerInboxCommandRepository
import kpt.core.model.shared.GenericResponse

/**
 * Records every decision; [result] answers per task. An APPLIED decision is removed from [inbox],
 * as the real command clears the row through the store.
 */
@ExperimentalDataTestingApi
class FakeCheckerInboxCommandRepository(
    private val inbox: FakeCheckerInboxRepository? = null,
    private val result: (auditId: Int) -> MutationResult<GenericResponse> = {
        MutationResult.Applied(
            GenericResponse(),
            synced = true,
        )
    },
) : CheckerInboxCommandRepository {

    /** The decisions made, as (command, audit id), in order. */
    val decisions = mutableListOf<Pair<String, Int>>()

    override suspend fun approveCheckerEntry(auditId: Int) = decide("approve", auditId)

    override suspend fun rejectCheckerEntry(auditId: Int) = decide("reject", auditId)

    override suspend fun deleteCheckerEntry(auditId: Int) = decide("delete", auditId)

    private fun decide(command: String, auditId: Int): MutationResult<GenericResponse> {
        decisions += command to auditId
        return result(auditId).also { if (it is MutationResult.Applied) inbox?.remove(auditId) }
    }
}

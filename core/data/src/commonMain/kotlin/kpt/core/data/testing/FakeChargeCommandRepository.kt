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

import kotlinx.coroutines.CompletableDeferred
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.charge.ChargeCommandRepository
import kpt.core.model.objects.charge.ChargeResource
import kpt.core.model.objects.payloads.ChargesPayload

/**
 * Test double for [ChargeCommandRepository] — beside the interface it fakes, same reasoning as
 * [FakeClientCommandRepository]. RECORDS every write and replays one scripted outcome.
 */
@ExperimentalDataTestingApi
class FakeChargeCommandRepository(
    private val gate: CompletableDeferred<Unit>? = null,
    private val result: () -> MutationResult<Unit> = { MutationResult.Applied(Unit, synced = true) },
) : ChargeCommandRepository {

    /** One recorded write — a test asserts the resource it addressed and what it sent. */
    data class Write(
        val resource: ChargeResource,
        val resourceId: Int,
        val chargeId: Int? = null,
        val payload: ChargesPayload? = null,
    )

    val created: MutableList<Write> = mutableListOf()
    val updated: MutableList<Write> = mutableListOf()
    val deleted: MutableList<Write> = mutableListOf()

    override suspend fun createCharge(resource: ChargeResource, resourceId: Int, payload: ChargesPayload) =
        record(created, Write(resource, resourceId, payload = payload))

    override suspend fun deleteCharge(resource: ChargeResource, resourceId: Int, chargeId: Int) =
        record(deleted, Write(resource, resourceId, chargeId))

    override suspend fun updateCharge(
        resource: ChargeResource,
        resourceId: Int,
        chargeId: Int,
        payload: ChargesPayload,
    ) = record(updated, Write(resource, resourceId, chargeId, payload))

    private suspend fun record(into: MutableList<Write>, write: Write): MutationResult<Unit> {
        into += write
        gate?.await()
        return result()
    }
}

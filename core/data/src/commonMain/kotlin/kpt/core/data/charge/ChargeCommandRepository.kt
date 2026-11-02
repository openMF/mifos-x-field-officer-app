/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.charge

import kpt.core.base.store.mutation.MutationResult
import kpt.core.model.objects.charge.ChargeResource
import kpt.core.model.objects.payloads.ChargesPayload

/**
 * Every charge write, for any [ChargeResource], as an explicit outcome.
 *
 * Each returns [MutationResult] rather than `Unit`: a field officer has to be told whether the
 * change reached the server, was refused because the device is offline, or failed — a distinction
 * that cannot be reconstructed above this layer.
 *
 * Once the server accepts one, its outcome is recorded through the charge stores, so the charge list
 * of that resource re-emits without a caller re-reading. Offline CAPTURE is a different operation
 * with its own explicit queue ([kpt.core.data.sync.OfflineQueueRepository]); it is never a silent
 * fallback from here.
 */
interface ChargeCommandRepository {

    /**
     * Levy a charge on a resource.
     *
     * `Unit`, not `HttpResponse`: the endpoint's Ktorfit signature returns a raw response, and a
     * FEATURE cannot name a Ktor type. The gateway already turns a non-2xx into
     * [MutationResult.Failed], which is the only distinction a screen renders.
     */
    suspend fun createCharge(
        resource: ChargeResource,
        resourceId: Int,
        payload: ChargesPayload,
    ): MutationResult<Unit>

    suspend fun deleteCharge(resource: ChargeResource, resourceId: Int, chargeId: Int): MutationResult<Unit>

    /**
     * Edit a levied charge. Only where [ChargeResource.canUpdate] — Fineract has no PUT for a client
     * charge, so for [ChargeResource.CLIENT] the server refuses it and this reports `Failed`.
     */
    suspend fun updateCharge(
        resource: ChargeResource,
        resourceId: Int,
        chargeId: Int,
        payload: ChargesPayload,
    ): MutationResult<Unit>
}

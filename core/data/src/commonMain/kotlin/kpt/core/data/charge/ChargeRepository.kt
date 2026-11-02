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

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.model.charge.Charges
import kpt.core.model.objects.charge.ChargeResource
import kpt.core.model.objects.template.client.ChargeTemplate

/**
 * Every charge READ, for any [ChargeResource] — the one surface `feature/charge` reads through.
 *
 * Replaces `ClientChargeRepository.clientChargesStream` and `LoanRepository.loanChargesStream` /
 * `loanChargeTemplate`, which were the same reads split by owner (and had no savings counterpart).
 */
interface ChargeRepository {

    /**
     * Charges levied on one resource. An owner with no charges is an empty LIST (Content), so a
     * caller maps it to Empty with `emptyIfContent` — and a deleted last charge leaves the screen.
     */
    fun chargesStream(
        resource: ChargeResource,
        resourceId: Int,
        scope: CoroutineScope,
    ): ScreenDataStream<List<Charges>>

    /**
     * Which charges the tenant allows on this resource, and their default amounts. A suspend
     * one-shot: the add-charge form asks once when it opens. `null` on failure.
     */
    suspend fun chargeTemplate(resource: ChargeResource, resourceId: Int): ChargeTemplate?

    /**
     * One charge as the server has it now — fetched through the store, so the table is updated
     * first — falling back to the cached row offline. The edit form prefills from it; `null` when
     * neither is available.
     */
    suspend fun charge(resource: ChargeResource, resourceId: Int, chargeId: Int): Charges?
}

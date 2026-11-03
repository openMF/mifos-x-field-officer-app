/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(kpt.core.base.store.screen.ExperimentalScreenDataStreamTestingApi::class)

package kpt.core.data.testing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.data.charge.ChargeRepository
import kpt.core.model.charge.Charges
import kpt.core.model.objects.charge.ChargeResource
import kpt.core.model.objects.template.client.ChargeTemplate

/**
 * Test double for [ChargeRepository] — beside the interface it fakes, so a new read breaks ONE fake.
 *
 * Hands back a REAL `ScreenDataStream` over [charges] (the sanctioned testing factory), so a test
 * drives the same Loading / Content / Empty transitions production does.
 */
@ExperimentalDataTestingApi
class FakeChargeRepository(
    initial: ScreenState<List<Charges>> = ScreenState.Loading,
    /** Answers the add-charge form's template question; `null` models the read failing. */
    private val template: () -> ChargeTemplate? = { null },
) : ChargeRepository {

    val charges = MutableStateFlow(initial)

    /** Fires once per pull-to-refresh / retry the ViewModel asks for. */
    val refreshes = MutableSharedFlow<Unit>(extraBufferCapacity = 4)

    /** Every owner a stream or template was asked for — the read key is worth asserting. */
    val streamRequests: MutableList<Pair<ChargeResource, Int>> = mutableListOf()

    /** The template reads, in order. */
    val templateRequests: MutableList<Pair<ChargeResource, Int>> = mutableListOf()

    override fun chargesStream(resource: ChargeResource, resourceId: Int, scope: CoroutineScope) =
        screenDataStreamForTesting(charges, refreshTrigger = refreshes)
            .also { streamRequests += resource to resourceId }

    override suspend fun chargeTemplate(resource: ChargeResource, resourceId: Int): ChargeTemplate? {
        templateRequests += resource to resourceId
        return template()
    }

    override suspend fun charge(resource: ChargeResource, resourceId: Int, chargeId: Int): Charges? =
        (charges.value as? ScreenState.Content)?.data?.firstOrNull { it.id == chargeId }
}

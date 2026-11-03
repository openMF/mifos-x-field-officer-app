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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kpt.core.base.store.mutation.MutationResult
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.data.recurringdeposit.RecurringDepositCommandRepository
import kpt.core.data.recurringdeposit.RecurringDepositRepository
import kpt.core.model.recurringdeposit.RecurringDepositAccountTemplate
import kpt.core.model.recurringdeposit.RecurringDepositApplicationInput

/** Test double for [RecurringDepositRepository]. */
@ExperimentalDataTestingApi
class FakeRecurringDepositRepository(
    private val template: MutableStateFlow<ScreenState<RecurringDepositAccountTemplate>>? = null,
) : RecurringDepositRepository {

    /** Every stream's refresh signal. */
    val refreshes: MutableSharedFlow<Unit> = MutableSharedFlow(extraBufferCapacity = 8)

    /** Every client the template was asked for — proves the route argument arrived. */
    val requestedClients: MutableList<Int> = mutableListOf()

    override fun templateStream(
        clientId: Int,
        scope: CoroutineScope,
    ): ScreenDataStream<RecurringDepositAccountTemplate> {
        requestedClients += clientId
        val source = requireNotNull(template) {
            "FakeRecurringDepositRepository: templateStream was read but no state flow was " +
                "supplied for it"
        }
        return screenDataStreamForTesting(source, refreshTrigger = refreshes)
    }
}

/**
 * Test double for [RecurringDepositCommandRepository].
 *
 * [applications] records the INPUT, because the input is what the tests assert: the original's
 * payload builder read a product through an unguarded `get(-1)` and sent penalty fields the officer
 * had unchecked.
 */
@ExperimentalDataTestingApi
class FakeRecurringDepositCommandRepository(
    private val gate: CompletableDeferred<Unit>? = null,
    private val result: () -> MutationResult<Unit> =
        { MutationResult.Applied(Unit, synced = true) },
) : RecurringDepositCommandRepository {

    val applications: MutableList<RecurringDepositApplicationInput> = mutableListOf()

    override suspend fun createRecurringDepositAccount(
        input: RecurringDepositApplicationInput,
    ): MutationResult<Unit> {
        applications += input
        gate?.await()
        return result()
    }
}

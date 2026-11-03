/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.recurringdeposit

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.fieldofficer.infra.OkHttpResponse
import kpt.core.data.infra.testMutationGateway
import kpt.core.data.recurringdeposit.impl.RecurringDepositCommandRepositoryImpl
import kpt.core.model.client.ClientAccounts
import kpt.core.model.objects.payloads.RecurringDepositAccountPayload
import kpt.core.model.recurringdeposit.RecurringDepositAccountTemplate
import kpt.core.model.recurringdeposit.RecurringDepositApplicationInput
import kpt.core.model.savings.SavingsAccount
import kpt.core.network.mifos.recurringdeposit.api.RecurringAccountApi
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.StoreBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kpt.core.model.client.ClientAccounts as ClientAccountsModel

/** The recurring-deposit create over a REAL Store5 accounts-list store on an in-memory row. */
class RecurringDepositCommandRepositoryTest {

    private val serverClientAccounts = mutableMapOf(CLIENT to ClientAccounts())
    private var posts = 0

    /** The read cache row of the client's accounts list. */
    private val clientAccounts = MutableStateFlow<Map<Int, ClientAccountsModel>>(emptyMap())

    private val clientAccountsStore = StoreBuilder.from(
        fetcher = Fetcher.of { clientId: Int -> serverClientAccounts.getValue(clientId) },
        sourceOfTruth = SourceOfTruth.of(
            reader = { clientId: Int -> clientAccounts.map { it[clientId] } },
            writer = { clientId: Int, list: ClientAccountsModel -> clientAccounts.value += clientId to list },
        ),
    ).build()

    private val api = object : RecurringAccountApi {
        override suspend fun createRecurringDepositAccount(
            recurringDepositAccountPayload: RecurringDepositAccountPayload?,
        ) =
            OkHttpResponse.also {
                posts++
                val owner = recurringDepositAccountPayload!!.clientId!!
                serverClientAccounts[owner] = ClientAccounts(savingsAccounts = listOf(SavingsAccount(id = 50)))
            }

        override suspend fun getRecurringDepositAccountTemplate(
            clientId: Int,
            productId: Int?,
        ): RecurringDepositAccountTemplate =
            error("reads go through the store")
    }

    private val input = RecurringDepositApplicationInput(
        clientId = CLIENT,
        productId = 1,
        submittedOnDate = "23 October 2026",
        locale = "en",
        dateFormat = "dd MMMM yyyy",
        mandatoryRecommendedDepositAmount = 100.0,
        recurringFrequency = 1,
        recurringFrequencyType = 2,
        depositPeriod = 12,
        depositPeriodFrequencyId = 2,
    )

    @Test
    fun theNewAccountAppearsInTheClientsAccounts() = runTest {
        val commands = RecurringDepositCommandRepositoryImpl(api, testMutationGateway(), clientAccountsStore)

        assertIs<MutationResult.Applied<Unit>>(commands.createRecurringDepositAccount(input))

        assertEquals(listOf(50), clientAccounts.value[CLIENT]?.savingsAccounts?.map { it.id })
    }

    @Test
    fun offlineItIsBlockedAndWritesNothing() = runTest {
        val commands = RecurringDepositCommandRepositoryImpl(
            api,
            testMutationGateway(isOnline = false),
            clientAccountsStore,
        )

        assertIs<MutationResult.Blocked>(commands.createRecurringDepositAccount(input))

        assertEquals(0, posts)
        assertTrue(clientAccounts.value.isEmpty())
    }

    private companion object {
        const val CLIENT = 7
    }
}

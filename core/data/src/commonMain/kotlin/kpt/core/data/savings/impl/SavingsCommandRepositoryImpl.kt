/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.savings.impl

import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.mutation.CommandSpec
import kpt.core.base.store.mutation.MutationGateway
import kpt.core.base.store.mutation.MutationPolicy
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.savings.SavingsCommandRepository
import kpt.core.database.savings.entity.SavingsAccountTransactionRequestEntity
import kpt.core.model.objects.account.loan.SavingsApproval
import kpt.core.model.objects.payloads.SavingsPayload
import kpt.core.network.mifos.savings.api.FixedDepositApi
import kpt.core.network.mifos.savings.api.SavingsAccountApi
import kpt.core.network.mifos.savings.api.ShareAccountApi
import kpt.core.network.mifos.savings.dto.FixedDepositPayload
import kpt.core.network.mifos.savings.dto.ShareAccountPayload

@RepositoryBinding(binds = SavingsCommandRepository::class)
internal class SavingsCommandRepositoryImpl(
    private val fixedDepositApi: FixedDepositApi,
    private val savingsAccountApi: SavingsAccountApi,
    private val shareAccountApi: ShareAccountApi,
    private val gateway: MutationGateway,
) : SavingsCommandRepository {

    override suspend fun createFixedDepositAccount(fixedDepositPayload: FixedDepositPayload) =
        online { fixedDepositApi.createFixedDepositAccount(fixedDepositPayload = fixedDepositPayload) }

    override suspend fun processTransaction(savingsAccountType: String, savingsAccountId: Int, transactionType: String?, savingsAccountTransactionRequest: SavingsAccountTransactionRequestEntity?) =
        online { savingsAccountApi.processTransaction(savingsAccountType = savingsAccountType, savingsAccountId = savingsAccountId, transactionType = transactionType, savingsAccountTransactionRequest = savingsAccountTransactionRequest) }

    override suspend fun activateSavings(savingsAccountId: Int, genericRequest: HashMap<String, String>) =
        online { savingsAccountApi.activateSavings(savingsAccountId = savingsAccountId, genericRequest = genericRequest) }

    override suspend fun approveSavingsApplication(savingsAccountId: Int, savingsApproval: SavingsApproval?) =
        online { savingsAccountApi.approveSavingsApplication(savingsAccountId = savingsAccountId, savingsApproval = savingsApproval) }

    override suspend fun createSavingsAccount(savingsPayload: SavingsPayload?) =
        online { savingsAccountApi.createSavingsAccount(savingsPayload = savingsPayload) }

    override suspend fun createShareAccount(shareAccountPayload: ShareAccountPayload) =
        online { shareAccountApi.createShareAccount(shareAccountPayload = shareAccountPayload) }

    /**
     * Commands run [MutationPolicy.OnlineRequired]: they await the server and write nothing
     * offline, yielding `Blocked(OFFLINE)` rather than a local success the officer would act on.
     *
     * The call is closed over rather than threaded through [CommandSpec.payload]: the payload hook
     * exists for `localApply` / `rollback` / `conflictOf`, none of which apply to a write that never
     * lands locally. It also keeps the 28 endpoints whose body is nullable usable, which
     * `CommandSpec<P : Any>` would otherwise reject.
     */
    private suspend fun <R : Any> online(endpoint: suspend () -> R): MutationResult<R> =
        gateway.command(
            CommandSpec(payload = Unit, endpoint = { endpoint() }),
            policy = MutationPolicy.OnlineRequired,
        )
}

/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.checkerinbox.impl

import kpt.core.base.data.annotation.FromStore
import kpt.core.base.data.annotation.RepositoryBinding
import kpt.core.base.store.mutation.CommandSpec
import kpt.core.base.store.mutation.MutationGateway
import kpt.core.base.store.mutation.MutationPolicy
import kpt.core.base.store.mutation.MutationResult
import kpt.core.data.checkerinbox.CheckerInboxCommandRepository
import kpt.core.model.objects.checkerinboxtask.CheckerTask
import kpt.core.model.shared.GenericResponse
import kpt.core.network.mifos.checkerinbox.api.CheckerInboxApi
import kpt.core.store.config.AppStoreIds
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.MutableStore

/**
 * Checker-inbox decisions, through the single write door ([MutationGateway.command]).
 *
 * Approve, reject and delete each end the task's life in the PENDING queue, which is all the inbox
 * shows — so the local effect is fully known from the request: [CommandSpec.localApply] clears the
 * task through [taskStore], which shares `checker_tasks` with the inbox read store. It used to end
 * at the endpoint with `payload = Unit`, leaving a decided task on screen until the 5-minute TTL
 * re-read — an invitation to approve it twice.
 *
 * `OnlineRequired`, as before (an authorisation decision must reach Fineract to be real), so
 * `localApply` runs only with the server's result.
 */
@RepositoryBinding(binds = CheckerInboxCommandRepository::class)
internal class CheckerInboxCommandRepositoryImpl(
    private val checkerInboxApi: CheckerInboxApi,
    private val gateway: MutationGateway,
    @FromStore(AppStoreIds.CheckerTask) private val taskStore: MutableStore<Int, CheckerTask>,
) : CheckerInboxCommandRepository {

    override suspend fun approveCheckerEntry(auditId: Int) =
        decide(auditId) { checkerInboxApi.approveCheckerEntry(auditId = it) }

    override suspend fun rejectCheckerEntry(auditId: Int) =
        decide(auditId) { checkerInboxApi.rejectCheckerEntry(auditId = it) }

    override suspend fun deleteCheckerEntry(auditId: Int) =
        decide(auditId) { checkerInboxApi.deleteCheckerEntry(auditId = it) }

    @OptIn(ExperimentalStoreApi::class)
    private suspend fun decide(
        auditId: Int,
        endpoint: suspend (Int) -> GenericResponse,
    ): MutationResult<GenericResponse> = gateway.command(
        CommandSpec(
            payload = auditId,
            endpoint = endpoint,
            localApply = { response -> if (response != null) taskStore.clear(auditId) },
        ),
        policy = MutationPolicy.OnlineRequired,
    )
}

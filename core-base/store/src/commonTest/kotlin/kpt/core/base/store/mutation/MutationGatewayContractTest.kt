/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.store.mutation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Contract test for the [MutationGateway] API surface (SP-1/T1). Asserts the policy + result types
 * compile with the intended shape and that `when` over the sealed hierarchies is exhaustive — the
 * property that forces every ViewModel to handle Blocked / Conflicted / Failed explicitly.
 */
class MutationGatewayContractTest {

    @Test
    fun bothPoliciesExist() {
        val policies: List<MutationPolicy> = listOf(MutationPolicy.Optimistic, MutationPolicy.OnlineRequired)
        assertEquals(2, policies.size)
    }

    @Test
    fun mutationResultIsExhaustive() {
        val results: List<MutationResult<Int>> = listOf(
            MutationResult.Applied(value = 7, synced = true),
            MutationResult.Blocked(BlockReason.OFFLINE),
            MutationResult.Conflicted(conflictId = "c1", server = 9),
            MutationResult.Failed(cause = IllegalStateException("x"), rolledBack = true),
        )
        // Exhaustive when — no `else` branch: the sealed contract is the whole point.
        results.forEach { r ->
            val label: String = when (r) {
                is MutationResult.Applied -> "applied"
                is MutationResult.Blocked -> "blocked"
                is MutationResult.Conflicted -> "conflicted"
                is MutationResult.Failed -> "failed"
            }
            assertTrue(label.isNotEmpty())
        }
    }

    @Test
    fun appliedCarriesSyncedFlag() {
        val queued = MutationResult.Applied(value = "todo", synced = false)
        assertFalse(queued.synced)
    }

    @Test
    fun commandSpecExposesPayloadAndKeyExtractor() {
        val spec = CommandSpec<Int, String>(
            payload = 42,
            endpoint = { p -> "ok:$p" },
            resultKeyOf = { it.length },
        )
        assertEquals(42, spec.payload)
        assertEquals(5, spec.resultKeyOf?.invoke("ok:42"))
    }
}

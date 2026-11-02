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
import kpt.core.data.savings.SavingsCommandRepository
import kpt.core.model.objects.account.saving.SavingsAccountTransactionResponse
import kpt.core.model.objects.account.saving.SavingsActivationInput
import kpt.core.model.objects.account.saving.SavingsApplicationInput
import kpt.core.model.objects.account.saving.SavingsApprovalInput
import kpt.core.model.objects.account.saving.SavingsTransactionInput
import kpt.core.model.savings.FixedDepositPayload
import kpt.core.model.savings.ShareAccountPayload
import kpt.core.model.shared.GenericResponse

/**
 * Test double for [SavingsCommandRepository] — the savings-side twin of
 * [FakeLoanCommandRepository].
 *
 * The three writes an officer performs in the field ([approveSavingsApplication],
 * [activateSavings], [processTransaction]) are implemented and RECORD their input, because the
 * input is what the tests assert: each one's date and `dateFormat` are a single decision, and the
 * original got two of the three wrong by sending epoch millis. The create commands THROW until
 * their slice lands — a fake that returned success for all six would let a miswired command pass.
 *
 * [gate], when supplied, is awaited before returning, so a test can hold a write in flight and
 * prove the double-submit guard.
 */
@ExperimentalDataTestingApi
class FakeSavingsCommandRepository(
    private val gate: CompletableDeferred<Unit>? = null,
    private val approvalResult: () -> MutationResult<GenericResponse> =
        { MutationResult.Applied(GenericResponse(), synced = true) },
    private val activationResult: () -> MutationResult<GenericResponse> =
        { MutationResult.Applied(GenericResponse(), synced = true) },
    private val transactionResult: () -> MutationResult<SavingsAccountTransactionResponse> =
        { MutationResult.Applied(SavingsAccountTransactionResponse(resourceId = 4_200), synced = true) },
    private val createResult: () -> MutationResult<Unit> =
        { MutationResult.Applied(Unit, synced = true) },
) : SavingsCommandRepository {

    /** Every approval sent, with its account — a test asserts the PAYLOAD, not merely a send. */
    val approvals: MutableList<Pair<Int, SavingsApprovalInput>> = mutableListOf()
    val activations: MutableList<Pair<Int, SavingsActivationInput>> = mutableListOf()

    /** Every transaction sent: `(type, id, command, input)`. */
    val transactions: MutableList<Triple<String, Int, SavingsTransactionInput>> = mutableListOf()

    /** The `command` query value each transaction carried — `deposit` / `withdrawal`. */
    val transactionCommands: MutableList<String> = mutableListOf()

    override suspend fun approveSavingsApplication(
        savingsAccountId: Int,
        input: SavingsApprovalInput,
    ): MutationResult<GenericResponse> {
        approvals += savingsAccountId to input
        gate?.await()
        return approvalResult()
    }

    override suspend fun activateSavings(
        savingsAccountId: Int,
        input: SavingsActivationInput,
    ): MutationResult<GenericResponse> {
        activations += savingsAccountId to input
        gate?.await()
        return activationResult()
    }

    override suspend fun processTransaction(
        savingsAccountType: String,
        savingsAccountId: Int,
        transactionType: String,
        input: SavingsTransactionInput,
    ): MutationResult<SavingsAccountTransactionResponse> {
        transactions += Triple(savingsAccountType, savingsAccountId, input)
        transactionCommands += transactionType
        gate?.await()
        return transactionResult()
    }

    override suspend fun createFixedDepositAccount(fixedDepositPayload: FixedDepositPayload) =
        unused()

    /** Every savings application sent — a test asserts the PAYLOAD, not merely a send. */
    val applications: MutableList<SavingsApplicationInput> = mutableListOf()

    override suspend fun createSavingsAccount(
        input: SavingsApplicationInput,
    ): MutationResult<Unit> {
        applications += input
        gate?.await()
        return createResult()
    }

    override suspend fun createShareAccount(shareAccountPayload: ShareAccountPayload) = unused()

    private fun unused(): Nothing =
        error("FakeSavingsCommandRepository: this command is not part of the slice under test")
}

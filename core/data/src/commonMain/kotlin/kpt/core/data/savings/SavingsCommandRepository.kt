/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.savings

import io.ktor.client.statement.HttpResponse
import kpt.core.base.store.mutation.MutationResult
import kpt.core.model.objects.account.saving.SavingsAccountTransactionResponse
import kpt.core.model.objects.account.saving.SavingsActivationInput
import kpt.core.model.objects.account.saving.SavingsApplicationInput
import kpt.core.model.objects.account.saving.SavingsApprovalInput
import kpt.core.model.objects.account.saving.SavingsTransactionInput
import kpt.core.model.savings.FixedDepositPayload
import kpt.core.model.savings.ShareAccountPayload
import kpt.core.model.shared.GenericResponse

/**
 * Every savings write, as an explicit outcome.
 *
 * Each returns [MutationResult] rather than `Unit`: a field officer has to be told whether the
 * change reached the server, was refused because the device is offline, or failed — a distinction
 * that cannot be reconstructed above this layer.
 *
 * Online-required commands: once the server accepts one, the implementation records the outcome
 * through the stores the reads observe, so no caller refreshes anything. Offline CAPTURE is a
 * different operation with its own explicit queue ([kpt.core.data.sync.OfflineQueueRepository]); it
 * is never a silent fallback from here.
 */
interface SavingsCommandRepository {

    /** Opens a fixed-deposit account. */
    suspend fun createFixedDepositAccount(fixedDepositPayload: FixedDepositPayload): MutationResult<HttpResponse>

    /**
     * Deposit or withdraw on one savings account.
     *
     * [savingsAccountType] is Fineract's path segment for the deposit kind, so the same numeric id
     * under a different type is a different account.
     *
     * Takes [SavingsTransactionInput] rather than the nullable Room entity it used to: that entity
     * has every field nullable plus an `errorMessage` column, which is storage shape, not request
     * shape. See the input type for why `locale`/`dateFormat` are required.
     */
    suspend fun processTransaction(
        savingsAccountType: String,
        savingsAccountId: Int,
        transactionType: String,
        input: SavingsTransactionInput,
    ): MutationResult<SavingsAccountTransactionResponse>

    /** Activate an approved savings account. Typed, not a `HashMap<String, String>`. */
    suspend fun activateSavings(
        savingsAccountId: Int,
        input: SavingsActivationInput,
    ): MutationResult<GenericResponse>

    /** Approve a submitted savings application. */
    suspend fun approveSavingsApplication(
        savingsAccountId: Int,
        input: SavingsApprovalInput,
    ): MutationResult<GenericResponse>

    /**
     * Submit a new savings application for a client or a group.
     *
     * `MutationResult<Unit>` and a `core/model` input, for the same reasons the loan create has
     * both: a feature module must not depend on `core:network`, and no caller needs the raw
     * response.
     */
    suspend fun createSavingsAccount(input: SavingsApplicationInput): MutationResult<Unit>

    /** Opens a share account. */
    suspend fun createShareAccount(shareAccountPayload: ShareAccountPayload): MutationResult<HttpResponse>
}

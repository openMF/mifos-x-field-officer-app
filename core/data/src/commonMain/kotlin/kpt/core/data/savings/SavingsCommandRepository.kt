/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.savings

import io.ktor.client.statement.HttpResponse
import kpt.core.base.store.mutation.MutationResult
import kpt.core.database.savings.entity.SavingsAccountTransactionRequestEntity
import kpt.core.model.objects.account.loan.SavingsApproval
import kpt.core.model.objects.account.saving.SavingsAccountTransactionResponse
import kpt.core.model.objects.payloads.SavingsPayload
import kpt.core.model.shared.GenericResponse
import kpt.core.network.mifos.savings.dto.FixedDepositPayload
import kpt.core.network.mifos.savings.dto.ShareAccountPayload

/**
 * Every savings write, as an explicit outcome.
 *
 * Each returns [MutationResult] rather than `Unit`: a field officer has to be told whether the
 * change reached the server, was refused because the device is offline, or failed — a distinction
 * that cannot be reconstructed above this layer.
 *
 * These are commands, so they carry no Store and no cache. Offline CAPTURE is a different
 * operation with its own explicit queue ([kpt.core.data.sync.OfflineQueueRepository]); it is never
 * a silent fallback from here.
 */
interface SavingsCommandRepository {

    suspend fun createFixedDepositAccount(fixedDepositPayload: FixedDepositPayload): MutationResult<HttpResponse>

    suspend fun processTransaction(savingsAccountType: String, savingsAccountId: Int, transactionType: String?, savingsAccountTransactionRequest: SavingsAccountTransactionRequestEntity?): MutationResult<SavingsAccountTransactionResponse>

    suspend fun activateSavings(savingsAccountId: Int, genericRequest: HashMap<String, String>): MutationResult<GenericResponse>

    suspend fun approveSavingsApplication(savingsAccountId: Int, savingsApproval: SavingsApproval?): MutationResult<GenericResponse>

    suspend fun createSavingsAccount(savingsPayload: SavingsPayload?): MutationResult<HttpResponse>

    suspend fun createShareAccount(shareAccountPayload: ShareAccountPayload): MutationResult<HttpResponse>
}

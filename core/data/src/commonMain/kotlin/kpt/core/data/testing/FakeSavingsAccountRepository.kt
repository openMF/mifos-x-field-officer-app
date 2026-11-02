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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.base.store.screen.ScreenState
import kpt.core.base.store.screen.screenDataStreamForTesting
import kpt.core.data.savings.SavingsAccountRepository
import kpt.core.model.objects.organisations.ProductSavings
import kpt.core.model.savings.FixedDepositTemplate
import kpt.core.model.savings.SavingProductsTemplate
import kpt.core.model.savings.SavingsAccountTransactionTemplate
import kpt.core.model.savings.SavingsAccountWithAssociations
import kpt.core.model.savings.ShareTemplate

/**
 * Test double for [SavingsAccountRepository] — the savings-side twin of [FakeLoanRepository].
 *
 * [requestedKeys] records the `(type, id)` pair the stream was asked for, which is the point of the
 * fake here: a savings account is addressed by BOTH, and passing the wrong type silently requests a
 * different account rather than failing.
 */
@ExperimentalDataTestingApi
class FakeSavingsAccountRepository(
    private val account: MutableStateFlow<ScreenState<SavingsAccountWithAssociations>>? = null,
    /** Answers the transaction-template question; null models the read failing. */
    private val transactionTemplate: () -> SavingsAccountTransactionTemplate? = { null },
    private val savingsProducts: MutableStateFlow<ScreenState<List<ProductSavings>>>? = null,
    /** Answers the application-template question, per PRODUCT; null models the read failing. */
    private val applicationTemplate: (productId: Int) -> SavingProductsTemplate? = { null },
    private val shareTemplate: MutableStateFlow<ScreenState<ShareTemplate>>? = null,
    private val fixedDepositTemplate: MutableStateFlow<ScreenState<FixedDepositTemplate>>? = null,
) : SavingsAccountRepository {

    val refreshes: MutableSharedFlow<Unit> = MutableSharedFlow(extraBufferCapacity = 8)

    /** Every `(savingsAccountType, savingsAccountId)` the stream was constructed for. */
    val requestedKeys: MutableList<Pair<String, Int>> = mutableListOf()

    override fun savingsAccountStream(
        savingsAccountType: String,
        savingsAccountId: Int,
        scope: CoroutineScope,
    ): ScreenDataStream<SavingsAccountWithAssociations> {
        requestedKeys += savingsAccountType to savingsAccountId
        val source = requireNotNull(account) {
            "FakeSavingsAccountRepository: savingsAccountStream was read but no state flow " +
                "was supplied for it"
        }
        return screenDataStreamForTesting(source, refreshTrigger = refreshes)
    }

    /** Every `(clientId, productId)` the fixed-deposit template was constructed for. */
    val fixedDepositTemplateQueries: MutableList<Pair<Int, Int?>> = mutableListOf()

    override fun fixedDepositTemplateStream(
        clientId: Int,
        productId: Int?,
        scope: CoroutineScope,
    ): ScreenDataStream<FixedDepositTemplate> {
        fixedDepositTemplateQueries += clientId to productId
        val source = requireNotNull(fixedDepositTemplate) {
            "FakeSavingsAccountRepository: fixedDepositTemplateStream was read but no state flow " +
                "was supplied for it"
        }
        return screenDataStreamForTesting(source, refreshTrigger = refreshes)
    }

    /** Every `(clientId, productId)` the share template was constructed for. */
    val shareTemplateQueries: MutableList<Pair<Int, Int?>> = mutableListOf()

    override fun shareTemplateStream(
        clientId: Int,
        productId: Int?,
        scope: CoroutineScope,
    ): ScreenDataStream<ShareTemplate> {
        shareTemplateQueries += clientId to productId
        val source = requireNotNull(shareTemplate) {
            "FakeSavingsAccountRepository: shareTemplateStream was read but no state flow " +
                "was supplied for it"
        }
        return screenDataStreamForTesting(source, refreshTrigger = refreshes)
    }

    /** Every `(type, id, command)` the template was asked for. */
    val templateQueries: MutableList<Triple<String, Int, String>> = mutableListOf()

    override suspend fun transactionTemplate(
        savingsAccountType: String,
        savingsAccountId: Int,
        transactionType: String,
    ): SavingsAccountTransactionTemplate? {
        templateQueries += Triple(savingsAccountType, savingsAccountId, transactionType)
        return transactionTemplate()
    }

    override fun savingsProductsStream(scope: CoroutineScope): ScreenDataStream<List<ProductSavings>> {
        val source = requireNotNull(savingsProducts) {
            "FakeSavingsAccountRepository: savingsProductsStream was read but no state flow " +
                "was supplied for it"
        }
        return screenDataStreamForTesting(source, refreshTrigger = refreshes)
    }

    /**
     * Every `(ownerId, productId, isGroup)` the application template was asked for.
     *
     * `isGroup` is recorded because it selects the ENDPOINT: the original's wizard dropped the
     * route's `isGroupAccount` entirely, so a test that only checked the ids would not notice a
     * group application reading the client template.
     */
    val applicationTemplateQueries: MutableList<Triple<Int, Int, Boolean>> = mutableListOf()

    override suspend fun savingsApplicationTemplate(
        ownerId: Int,
        productId: Int,
        isGroup: Boolean,
    ): SavingProductsTemplate? {
        applicationTemplateQueries += Triple(ownerId, productId, isGroup)
        return applicationTemplate(productId)
    }
}

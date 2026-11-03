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

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.model.objects.organisations.ProductSavings
import kpt.core.model.savings.FixedDepositTemplate
import kpt.core.model.savings.SavingProductsTemplate
import kpt.core.model.savings.SavingsAccountTransactionTemplate
import kpt.core.model.savings.SavingsAccountWithAssociations
import kpt.core.model.savings.ShareTemplate

/** Reads savings accounts, their transactions and templates, from the cache and the server. */
interface SavingsAccountRepository {
    /**
     * One savings account with its transactions.
     *
     * [savingsAccountType] is a Fineract path segment ("savingsaccounts",
     * "recurringdepositaccounts", …), so the same numeric id means different accounts under
     * different types — both are required to address one.
     */
    fun savingsAccountStream(
        savingsAccountType: String,
        savingsAccountId: Int,
        scope: CoroutineScope,
    ): ScreenDataStream<SavingsAccountWithAssociations>

    /**
     * The transaction template for one (account, command) pair — its payment-type options.
     *
     * A one-shot, for the same reason the loan and group-loan templates are: the deposit and
     * withdraw screens each ask once on open, and a [ScreenDataStream] is keyed at CONSTRUCTION.
     * `null` when the read fails, so the form can still be submitted — Fineract accepts a
     * transaction with no payment type, and refusing to let an officer record cash they have
     * already taken because a reference list did not load would be the worse failure.
     */
    suspend fun transactionTemplate(
        savingsAccountType: String,
        savingsAccountId: Int,
        transactionType: String,
    ): SavingsAccountTransactionTemplate?

    /** The tenant's savings products — what the create wizard opens on. */
    fun savingsProductsStream(scope: CoroutineScope): ScreenDataStream<List<ProductSavings>>

    /**
     * The share-account application template for one client, optionally narrowed to one product.
     *
     * `productId = null` is the FIRST read — it returns the product options to choose from. Passing
     * a product id re-reads with that product's price, currency and charge options filled in, which
     * is why the share wizard reads this stream twice.
     *
     * The store behind it (`@StoreProvider(id = "savingsShareProductTemplate")`) already existed
     * and was unexposed; the share wizard is its first reader.
     */
    fun shareTemplateStream(
        clientId: Int,
        productId: Int?,
        scope: CoroutineScope,
    ): ScreenDataStream<ShareTemplate>

    /**
     * The fixed-deposit application template for one client, optionally narrowed to one product.
     *
     * Same two-phase shape as [shareTemplateStream]: `productId = null` supplies the product
     * options, and re-reading with a product fills in its interest, term and charge defaults. The
     * store behind it already existed and was unexposed; this wizard is its first reader.
     */
    fun fixedDepositTemplateStream(
        clientId: Int,
        productId: Int?,
        scope: CoroutineScope,
    ): ScreenDataStream<FixedDepositTemplate>

    /**
     * The savings-application template for one (owner, product) pair.
     *
     * [isGroup] selects Fineract's GROUP endpoint over the client one. Both exist and both are
     * cached; the original's wizard read neither per-owner — its route carried `groupId` and
     * `isGroupAccount` and the ViewModel dropped both, so the group path could not work at all.
     *
     * A one-shot for the same reason the loan templates are: the wizard re-asks whenever the
     * officer changes product, and a [ScreenDataStream] is keyed at construction.
     */
    suspend fun savingsApplicationTemplate(
        ownerId: Int,
        productId: Int,
        isGroup: Boolean,
    ): SavingProductsTemplate?
}

/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.savings.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import io.ktor.client.statement.HttpResponse
import kpt.core.base.network.annotation.ApiBinding
import kpt.core.database.savings.entity.SavingProductsTemplate
import kpt.core.database.savings.entity.SavingsAccountTransactionRequestEntity
import kpt.core.database.savings.entity.SavingsAccountTransactionTemplateEntity
import kpt.core.database.savings.entity.SavingsAccountWithAssociationsEntity
import kpt.core.model.objects.account.loan.SavingsApproval
import kpt.core.model.objects.account.saving.SavingsAccountTransactionResponse
import kpt.core.model.objects.organisations.ProductSavings
import kpt.core.model.objects.payloads.SavingsPayload
import kpt.core.model.shared.GenericResponse

/**
 * @author fomenkoo
 */
@ApiBinding("mifos")
interface SavingsAccountApi {
    /**
     * This Service Retrieve a savings application/account. From the REST API :
     * https://demo.openmf.org/fineract-provider/api/v1/savingsaccounts/{savingsAccountIs}
     * ?associations={all or transactions or charges}
     *
     * @param savingsAccountType SavingsAccount Type of SavingsAccount
     * @param savingsAccountId   SavingsAccounts Id
     * @param association        {all or transactions or charges}
     * 'all': Gets data related to all associations e.g.
     * ?associations=all.
     * 'transactions': Gets data related to transactions on the account
     * e.g.
     * ?associations=transactions
     * 'charges':Savings Account charges data.
     * @return SavingsAccountWithAssociations
     */
    @GET("{savingsAccountType}/{savingsAccountId}")
    suspend fun getSavingsAccountWithAssociations(
        @Path("savingsAccountType") savingsAccountType: String,
        @Path("savingsAccountId") savingsAccountId: Int,
        @Query("associations") association: String?,
    ): SavingsAccountWithAssociationsEntity

    /**
     * This Method for Retrieving Savings Account Transaction Template from REST API
     * https://demo.openmf.org/fineract-provider/api/v1/{savingsAccountType}/{savingsAccountId}
     * /transactions/template.
     *
     * @param savingsAccountType SavingsAccount Type Example : 'savingsaccounts'
     * @param savingsAccountId   SavingsAccount Id
     * @param transactionType    Transaction Type Example : 'Deposit', 'Withdrawal'
     * @return SavingsAccountTransactionTemplate
     */
    @GET("{savingsAccountType}/{savingsAccountId}/transactions/template")
    suspend fun getSavingsAccountTransactionTemplate(
        @Path("savingsAccountType") savingsAccountType: String,
        @Path("savingsAccountId") savingsAccountId: Int,
        @Query("command") transactionType: String?,
    ): SavingsAccountTransactionTemplateEntity

    /**
     * This Service making POST Request to the REST API :
     * https://demo.openmf.org/fineract-provider/api/v1/{savingsAccountType}/
     * {savingsAccountId}/transactions?command={transactionType}
     *
     * @param savingsAccountType               SavingsAccount Type Example : 'savingsaccounts'
     * @param savingsAccountId                 SavingsAccount Id
     * @param transactionType                  Transaction Type Example : 'Deposit', 'Withdrawal'
     * @param savingsAccountTransactionRequest SavingsAccountTransactionRequest
     * @return SavingsAccountTransactionResponse
     */
    @POST("{savingsAccountType}/{savingsAccountId}/transactions")
    suspend fun processTransaction(
        @Path("savingsAccountType") savingsAccountType: String,
        @Path("savingsAccountId") savingsAccountId: Int,
        @Query("command") transactionType: String?,
        @Body savingsAccountTransactionRequest: SavingsAccountTransactionRequestEntity?,
    ): SavingsAccountTransactionResponse

    @POST("savingsaccounts/{savingsAccountId}/?command=activate")
    suspend fun activateSavings(
        @Path("savingsAccountId") savingsAccountId: Int,
        @Body genericRequest: HashMap<String, String>,
    ): GenericResponse

    @POST("savingsaccounts/{savingsAccountId}?command=approve")
    suspend fun approveSavingsApplication(
        @Path("savingsAccountId") savingsAccountId: Int,
        @Body savingsApproval: SavingsApproval?,
    ): GenericResponse

    @GET("savingsproducts")
    suspend fun allSavingsAccounts(): List<ProductSavings>

    @POST("savingsaccounts")
    suspend fun createSavingsAccount(@Body savingsPayload: SavingsPayload?): HttpResponse

    @GET("savingsproducts/template")
    suspend fun savingsAccountTemplate(): SavingProductsTemplate

    @GET("savingsaccounts/template")
    suspend fun getClientSavingsAccountTemplateByProduct(
        @Query("clientId") clientId: Int,
        @Query("productId") productId: Int,
    ): SavingProductsTemplate

    @GET("savingsaccounts/template")
    suspend fun getGroupSavingsAccountTemplateByProduct(
        @Query("groupId") groupId: Int,
        @Query("productId") productId: Int,
    ): SavingProductsTemplate
}

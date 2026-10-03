/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.report.api

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import de.jensklingenberg.ktorfit.http.QueryMap
import kpt.core.base.network.annotation.ApiBinding
import kpt.core.model.objects.groups.CenterInfo
import kpt.core.model.objects.runreport.FullParameterListResponse
import kpt.core.model.objects.runreport.client.ClientReportTypeItem

/**
 * Created by Rajan Maurya on 05/02/17.
 */
@ApiBinding("mifos")
interface RunReportsApi {
    /**
     * Endpoint to fetch Categories of the reports
     * @param category Client/Savings etc.
     * @param genericResultSet genericResultSet - true/false
     * @param parameterType parameterType - true/false
     * @return List of ClientReportTypeItem
     */
    @GET("runreports/reportCategoryList")
    suspend fun getReportCategories(
        @Query("R_reportCategory") category: String?,
        @Query("genericResultSet") genericResultSet: Boolean,
        @Query("parameterType") parameterType: Boolean,
    ): List<ClientReportTypeItem>

    /**
     * Endpoint to fetch FullParameter list after fetching the categories.
     * @param reportName Report's Name for whom the list has to be retrieved
     * @param parameterType parameterType - true/false
     * @return FullParameterListResponse
     */
    @GET("runreports/FullParameterList")
    suspend fun getReportFullParameterList(
        @Query("R_reportListing") reportName: String,
        @Query("parameterType") parameterType: Boolean,
    ): FullParameterListResponse

    /**
     * Endpoint to fetch the details for a particular parameter.
     * @param parameterName Parameter Name
     * @param parameterType parameterType - true/false
     * @return
     */
    @GET("runreports/{path}")
    suspend fun getReportParameterDetails(
        @Path("path") parameterName: String,
        @Query("parameterType") parameterType: Boolean,
    ): FullParameterListResponse

    @GET("runreports/{path}")
    suspend fun getReportOffice(
        @Path("path") parameterName: String,
        @Query("R_officeId") office: Int,
        @Query("parameterType") parameterType: Boolean,
    ): FullParameterListResponse

    @GET("runreports/{path}")
    suspend fun getReportProduct(
        @Path("path") parameterName: String,
        @Query("R_currencyId") currency: String,
        @Query("parameterType") parameterType: Boolean,
    ): FullParameterListResponse

    /**
     * Endpoint to retrieve final Report based on the parameters.
     * @param reportName Report's Name
     * @param options Map of the queries with their corresponding value.
     * @return
     */
    @GET("runreports/{path}")
    suspend fun getRunReportWithQuery(
        @Path("path") reportName: String,
        @QueryMap options: Map<String, String>,
    ): FullParameterListResponse

    @GET("runreports/GroupSummaryCounts")
    suspend fun getCenterSummaryInfo(
        @Query("R_groupId") centerId: Int,
        @Query("genericResultSet") genericResultSet: Boolean,
    ): List<CenterInfo>

    /**
     * Fetches the savings account transaction receipt from the server.
     *
     * This endpoint generates a receipt for the given transaction and returns it
     * in the requested output format (default: PDF).
     *
     * @param transactionId The unique identifier of the savings account transaction.
     * @param dateFormat The date format to be applied in the receipt (default: "dd MMMM yyyy").
     * @param outputType The type of output to generate for the receipt (default: "PDF").
     *
     * @return A [Flow] emitting the raw bytes of the receipt file (e.g., PDF).
     */
    @GET("runreports/Savings Transaction Receipt")
    suspend fun getSavingsAccountTransactionReceipt(
        @Query("R_transactionId") transactionId: Int,
        @Query("dateFormat") dateFormat: String = "dd MMMM yyyy",
        @Query("output-type") outputType: String = "PDF",
        @Query("locale") locale: String = "en",
    ): ByteArray
}

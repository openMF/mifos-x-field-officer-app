/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.document.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Path
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.statement.HttpResponse
import kpt.core.base.network.annotation.ApiBinding
import kpt.core.model.objects.noncoreobjects.Document
import kpt.core.model.shared.GenericResponse

@ApiBinding("mifos")
interface DocumentApi {
    @GET("{entityType}/{entityId}/documents")
    suspend fun getDocuments(
        @Path("entityType") entityType: String,
        @Path("entityId") entityId: Int,
    ): List<Document>

    /**
     * @param entityType              - Type for which document is being uploaded (Client, Loan
     * or Savings etc)
     * @param entityId                - Id of Entity
     * @param nameOfDocument          - Document Name
     * @param description             - Mandatory - Document Description
     * @param typedFile               - Mandatory
     */
    @POST("{entityType}/{entityId}/documents")
    suspend fun createDocument(
        @Path("entityType") entityType: String,
        @Path("entityId") entityId: Int,
        @Body request: MultiPartFormDataContent,
    ): GenericResponse

    /**
     * This Service is for downloading the Document with EntityType and EntityId and Document Id
     * Rest End Point :
     * https://demo.openmf.org/fineract-provider/api/v1/{entityType}/{entityId}/documents/
     * {documentId}/attachment
     *
     * @param entityType    - Type for which document is being uploaded (Client, Loan
     * or Savings etc)
     * @param entityId      - Id of Entity
     * @param documentId    - Document Id
     * @return ResponseBody
     */

    @Headers("Accept: text/plain, application/json, */*")
    @GET("{entityType}/{entityId}/documents/{documentId}/attachment")
    suspend fun downloadDocument(
        @Path("entityType") entityType: String,
        @Path("entityId") entityId: Int,
        @Path("documentId") documentId: Int,
    ): HttpResponse

    /**
     * This Service is for Deleting the Document with EntityType and EntityId and Document Id.
     * Rest End Point :
     * https://demo.openmf.org/fineract-provider/api/v1/{entityType}/{entityId}/documents/
     * {documentId}
     *
     * @param entityType    - Type for which document is being uploaded (Client, Loan
     * or Savings etc)
     * @param entityId      - Id of Entity
     * @param documentId    - Document Id
     * @return
     */
    @DELETE("{entityType}/{entityId}/documents/{documentId}")
    suspend fun removeDocument(
        @Path("entityType") entityType: String,
        @Path("entityId") entityId: Int,
        @Path("documentId") documentId: Int,
    ): GenericResponse

    /**
     * This Service for Updating the Document with EntityType and EntityId and Document Id.
     * Rest End Point :
     * PUT
     * https://demo.openmf.org/fineract-provider/api/v1/{entityType}/{entityId}/documents/
     * {documentId}
     *
     * @param entityType              - Type for which document is being uploaded (Client, Loan
     * or Savings etc)
     * @param entityId                - Id of Entity
     * @param documentId              - Id of document
     * @param nameOfDocument          - Document Name
     * @param description             - Mandatory - Document Description
     * @param typedFile               - Mandatory
     */
    @PUT("{entityType}/{entityId}/documents/{documentId}")
    suspend fun updateDocument(
        @Path("entityType") entityType: String,
        @Path("entityId") entityId: Int,
        @Path("documentId") documentId: Int,
        @Body request: MultiPartFormDataContent,
    ): GenericResponse
}

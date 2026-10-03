/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.checkerinbox.dto

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * GetMakerCheckerResponse
 *
 * @param actionName
 * @param checkedOnDate
 * @param checker
 * @param clientId
 * @param clientName
 * @param commandAsJson
 * @param entityName
 * @param groupLevelName
 * @param groupName
 * @param id
 * @param loanAccountNo
 * @param loanId
 * @param madeOnDate
 * @param maker
 * @param officeName
 * @param processingResult
 * @param resourceId
 * @param savingsAccountNo
 * @param subresourceId
 * @param url
 */

@Serializable
data class GetMakerCheckerResponse(

    val actionName: String? = null,

    @Contextual
    val checkedOnDate: LocalDateTime? = null,

    val checker: String? = null,

    val clientId: Long? = null,

    val clientName: String? = null,

    val commandAsJson: String? = null,

    val entityName: String? = null,

    val groupLevelName: String? = null,

    val groupName: String? = null,

    val id: Long? = null,

    val loanAccountNo: String? = null,

    val loanId: Int? = null,

    @Contextual
    val madeOnDate: LocalDateTime? = null,

    val maker: String? = null,

    val officeName: String? = null,

    val processingResult: String? = null,

    val resourceId: Long? = null,

    val savingsAccountNo: String? = null,

    val subresourceId: Long? = null,

    val url: String? = null,

)

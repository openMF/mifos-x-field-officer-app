/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos.client.dto

import kotlinx.serialization.Serializable
import kpt.core.model.client.StaffOption
import kpt.core.model.objects.account.share.ShareAccounts
import kpt.core.network.mifos.savings.dto.SavingAccountOption

/**
 *
 *
 * @param accountNo
 * @param active
 * @param displayName
 * @param emailAddress
 * @param fullname
 * @param id
 * @param officeId
 * @param officeName
 * @param status
 */

@Serializable
data class GetClientsPageItemsResponse(

    val accountNo: String? = null,

    val active: Boolean? = null,

    val displayName: String? = null,

    val emailAddress: String? = null,

    /** The full name. */
    val fullName: String? = null,

    val id: Long? = null,

    val officeId: Long? = null,

    val officeName: String? = null,

    /** The server id of the group. */
    val groupId: Int? = null,

    /** The name of the group. */
    val groupName: String? = null,

    val status: GetClientStatus? = null,

    /** The id an external system uses for this record, if one was set. */
    val externalId: String? = null,

    /** The client's legal form (person or entity). */
    val legalForm: GetClientStatus? = null,

    /** The date of birth. */
    val dateOfBirth: List<Int?> = emptyList(),

    /** The staff members that may be assigned. */
    val staffOptions: List<StaffOption> = emptyList(),

    /** The client's savings accounts. */
    val savingAccountOptions: List<SavingAccountOption> = emptyList(),

    /** The share accounts. */
    val shareAccounts: List<ShareAccounts> = emptyList(),

    /** The client's gender. */
    val gender: GetGenderOptions? = null,

    /** The client's type. */
    val clientType: GetClientTypeOptions? = null,

    /** The client's classification. */
    val clientClassification: GetClientClassificationOptions? = null,

)

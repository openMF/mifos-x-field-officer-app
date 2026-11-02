/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.client

import kotlinx.serialization.Serializable
import kpt.core.model.group.Group
import kpt.core.model.shared.Timeline

/**
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `ClientEntity`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class Client(
    val id: Int = 0,
    val groupId: Int? = 0,
    val groupName: String? = null,
    val accountNo: String? = null,
    val clientId: Int? = null,
    val status: ClientStatus? = null,
    val sync: Boolean = false,
    val active: Boolean = false,
    val clientDate: ClientDate? = null,
    val activationDate: List<Int?> = emptyList(),
    val dateOfBirth: List<Int?> = emptyList(),
    val groups: List<Group>? = emptyList(),
    val mobileNo: String? = null,
    val firstname: String? = null,
    val middlename: String? = null,
    val lastname: String? = null,
    val displayName: String? = null,
    val officeId: Int = 0,
    val officeName: String? = null,
    val staffId: Int = 0,
    val staffName: String? = null,
    val timeline: Timeline? = null,
    val fullname: String? = null,
    val savingsAccountId: Long? = null,
    val imageId: Int = 0,
    val imagePresent: Boolean = false,
    val externalId: String? = null,
    val emailAddress: String? = null,
    val legalForm: ClientStatus? = null,
    val gender: ClientGender? = null,
    val clientType: ClientType? = null,
    val clientClassification: ClientClassification? = null,
)

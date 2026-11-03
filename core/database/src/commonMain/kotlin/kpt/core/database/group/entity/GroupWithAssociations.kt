/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.group.entity

import kotlinx.serialization.Serializable
import kpt.core.database.client.entity.ClientEntity
import kpt.core.database.client.entity.ClientStatusEntity
import kpt.core.model.shared.Timeline

/**
 * Created by ishankhanna on 29/06/14.
 */
@Serializable
data class GroupWithAssociations(
    /** The server id of this record. */
    val id: Int? = null,

    /** The account number Fineract assigned. */
    val accountNo: String? = null,

    /** The display name. */
    val name: String? = null,

    /** The record's Fineract status. */
    val status: ClientStatusEntity? = null,

    /** True when the record is active. */
    val active: Boolean? = null,

    /** The date the record was activated. */
    val activationDate: List<Int?> = emptyList(),

    /** The server id of the office. */
    val officeId: Int? = null,

    /** The name of the office. */
    val officeName: String? = null,

    /** The server id of the staff member (loan officer). */
    val staffId: Int? = null,

    /** The staff member's display name. */
    val staffName: String? = null,

    /** The office hierarchy path (e.g. `.1.2.`). */
    val hierarchy: String? = null,

    /** The group's level in the group hierarchy. */
    val groupLevel: Int? = null,

    /** The clients in the group. */
    val clientMembers: List<ClientEntity> = emptyList(),

    /** The dates and users of the record's lifecycle events (submitted, approved, activated, …). */
    val timeline: Timeline? = null,
)

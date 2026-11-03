/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.group

import kotlinx.serialization.Serializable
import kpt.core.model.client.ClientStatus
import kpt.core.model.shared.Timeline

/**
 * GENERATED twin — what every layer above `core/store` sees of `core/database`'s `GroupEntity`.
 * Same field names as the row; the `core/store/mapping/` mappers convert between them.
 */
@Serializable
data class Group(
    /** The server id of this record. */
    val id: Int? = null,
    /** The account number Fineract assigned. */
    val accountNo: String? = null,
    /** True when this record is queued to be synced to the server. */
    val sync: Boolean = false,
    /** The display name. */
    val name: String? = null,
    /** The record's Fineract status. */
    val status: ClientStatus? = null,
    /** True when the record is active. */
    val active: Boolean? = null,
    /** The group's dates. */
    val groupDate: GroupDate? = null,
    /** The date the record was activated. */
    val activationDate: List<Int> = emptyList(),
    /** The server id of the office. */
    val officeId: Int? = null,
    /** The name of the office. */
    val officeName: String? = null,
    /** The server id of the centre. */
    val centerId: Int? = 0,
    /** The name of the centre. */
    val centerName: String? = null,
    /** The server id of the staff member (loan officer). */
    val staffId: Int? = null,
    /** The staff member's display name. */
    val staffName: String? = null,
    /** The office hierarchy path (e.g. `.1.2.`). */
    val hierarchy: String? = null,
    /** The group's level in the group hierarchy. */
    val groupLevel: Int = 0,
    /** The dates and users of the record's lifecycle events (submitted, approved, activated, …). */
    val timeline: Timeline? = null,
    /** The id an external system uses for this record, if one was set. */
    val externalId: String? = null,
)

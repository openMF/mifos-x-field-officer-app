/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.template.loan

import kotlinx.serialization.Serializable

/**
 * Created by mayankjindal on 02/10/16.
 */
@Serializable
data class Group(
    /** The server id of this record. */
    var id: Int? = null,

    /** The account number Fineract assigned. */
    var accountNo: Int? = null,

    /** The display name. */
    var name: String? = null,

    /** The id an external system uses for this record, if one was set. */
    var externalId: Int? = null,

    /** The record's Fineract status. */
    var status: Status? = null,

    /** True when the record is active. */
    var active: Boolean? = null,

    /** The date the record was activated. */
    var activationDate: List<Int>? = null,

    /** The server id of the office. */
    var officeId: Int? = null,

    /** The name of the office. */
    var officeName: String? = null,

    /** The office hierarchy path (e.g. `.1.2.`). */
    var hierarchy: String? = null,

    /** The group's level in the group hierarchy. */
    var groupLevel: Int? = null,

    /** The dates and users of the record's lifecycle events (submitted, approved, activated, …). */
    var timeline: GroupTimeline? = null,
)
